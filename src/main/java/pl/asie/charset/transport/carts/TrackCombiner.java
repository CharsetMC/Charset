package pl.asie.charset.transport.carts;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.utils.ItemUtils;

import java.util.HashMap;
import java.util.Map;

public class TrackCombiner {
	private final Map<IBlockState, Map<ItemStack, IBlockState>> transform;
	private final Map<IBlockState, Pair<ItemStack, IBlockState>> transformInv;

	public TrackCombiner() {
		transform = new HashMap<>();
		transformInv = new HashMap<>();
	}

	@SubscribeEvent
	public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		World world = event.getWorld();
		BlockPos pos = event.getPos();

		if (event.getEntityPlayer().isSneaking() && !event.getItemStack().isEmpty()) {
			IBlockState state = world.getBlockState(pos);
			if (transform.containsKey(state)) {
				for (Map.Entry<ItemStack, IBlockState> entry : transform.get(state).entrySet()) {
					if (ItemUtils.canMerge(entry.getKey(), event.getItemStack())) {
						world.setBlockState(pos, entry.getValue());
						event.getItemStack().shrink(1);
						event.setCanceled(true);
						break;
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onBlockHarvest(BlockEvent.HarvestDropsEvent event) {
		if (event.getState() == null) {
			return;
		}

		World world = event.getWorld();
		BlockPos pos = event.getPos();

		// WORKAROUND: Some mods seem to like event.getDrops() being null.
		// This is not what Forge does.
		if (event.getDrops() == null) {
			ModCharset.logger.error("Block " + event.getState().getBlock().getRegistryName() + " provides a null getDrops() list, against Forge's original method behaviour! This is a bug in the mod providing it!");
			return;
		}

		IBlockState state = event.getState();
		if (transformInv.containsKey(state)) {
			event.getDrops().clear();
			IBlockState oldState = world.getBlockState(pos);
			world.setBlockState(pos, state, 4); // This is just for internal use; no block updates or resend
			while (transformInv.containsKey(state)) {
				Pair<ItemStack, IBlockState> pair = transformInv.get(state);
				event.getDrops().add(pair.getLeft().copy());
				state = pair.getRight();
				world.setBlockState(pos, state, 4);
			}
			event.getDrops().addAll(state.getBlock().getDrops(world, pos, state, event.getFortuneLevel()));
			world.setBlockState(pos, oldState, 2);
		}
	}

	@SubscribeEvent
	public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		World world = event.getWorld();
		BlockPos pos = event.getPos();

		if (event.getEntityPlayer().isSneaking()) {
			IBlockState state = world.getBlockState(pos);
			if (transformInv.containsKey(state)) {
				Pair<ItemStack, IBlockState> pair = transformInv.get(state);
				if (!world.isRemote) {
					ItemUtils.spawnItemEntity(world, new Vec3d(pos).addVector(0.5, 0.125, 0.5),
							pair.getLeft().copy(), 0.02f, 0.05f, 0.02f, 1.0f
					);
				}
				world.setBlockState(pos, pair.getRight());
				event.setCanceled(true);
			}
		}
	}

	public void register(IBlockState from, IBlockState to, ItemStack with) {
		if (transformInv.containsKey(to)) {
			throw new RuntimeException("Tried to register two mappings resulting in " + to.toString() + " in TrackCombiner!");
		}

		transformInv.put(to, Pair.of(with, from));
		if (!transform.containsKey(from)) {
			transform.put(from, new HashMap<>());
		}
		transform.get(from).put(with, to);
	}

	public void register(Block from, IBlockState to, ItemStack with) {
		if (transformInv.containsKey(to)) {
			throw new RuntimeException("Tried to register two mappings resulting in " + to.toString() + " in TrackCombiner!");
		}

		transformInv.put(to, Pair.of(with, from.getDefaultState()));
		for (IBlockState fromState : from.getBlockState().getValidStates()) {
			if (!transform.containsKey(from)) {
				transform.put(fromState, new HashMap<>());
			}
			transform.get(fromState).put(with, to);
		}
	}
}
