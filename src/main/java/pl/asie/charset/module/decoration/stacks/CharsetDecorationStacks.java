package pl.asie.charset.module.decoration.stacks;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.module.power.mechanical.render.ModelGearbox;

@CharsetModule(
		name = "decoration.stacks",
		description = "Place things! In the world! And they stack!",
		profile = ModuleProfile.INDEV
)
public class CharsetDecorationStacks {
	public BlockStacks blockStacks;

	@Mod.EventHandler
	public void onPreInit(FMLPreInitializationEvent event) {
		blockStacks = new BlockStacks();
	}

	@SubscribeEvent
	public void registerBlock(RegistryEvent.Register<Block> event) {
		RegistryUtils.register(event.getRegistry(), blockStacks, "stacks_decorative");
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		RegistryUtils.register(TileEntityStacks.class, "stacks_decorative");
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onModelBake(ModelBakeEvent event) {
		event.getModelRegistry().putObject(new ModelResourceLocation("charset:stacks_decorative", "normal"), new RenderTileEntityStacks());
	}

	@SubscribeEvent
	public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		ItemStack stack = event.getItemStack();
		if (!stack.isEmpty()) {
			ItemStack stackOffered = stack.copy();
			stackOffered.setCount(1);
			if (!TileEntityStacks.canAcceptStackType(stackOffered)) {
				return;
			}

			IBlockState state = event.getWorld().getBlockState(event.getPos());
			BlockPos pos = event.getPos();

			if (!(state.getBlock() instanceof BlockStacks) && !state.getBlock().isReplaceable(event.getWorld(), event.getPos()) && event.getFace() != null) {
				pos = pos.offset(event.getFace());
			}

			while (true) {
				state = event.getWorld().getBlockState(pos);
				if (state.getBlock() instanceof BlockStacks) {
					TileEntity tile = event.getWorld().getTileEntity(pos);
					if (tile instanceof TileEntityStacks) {
						if (((TileEntityStacks) tile).stacks.size() >= 64) {
							pos = pos.up();
						} else {
							break;
						}
					}
				} else {
					break;
				}
			}

			if (state.getBlock().isReplaceable(event.getWorld(), pos)) {
				state = blockStacks.getDefaultState();
				event.getWorld().setBlockState(pos, state);
			}

			if (state.getBlock() instanceof BlockStacks) {
				TileEntity tile = event.getWorld().getTileEntity(pos);
				if (tile instanceof TileEntityStacks) {
					if (((TileEntityStacks) tile).offerStack(stackOffered)) {
						if (!event.getEntityPlayer().isCreative()) {
							stack.shrink(stackOffered.getCount());
						}
						event.setCanceled(true);
						event.setCancellationResult(EnumActionResult.SUCCESS);
					}
				}
			}
		}
	}
}
