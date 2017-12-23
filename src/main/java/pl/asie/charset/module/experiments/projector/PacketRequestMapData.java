package pl.asie.charset.module.experiments.projector;

import gnu.trove.map.TIntLongMap;
import gnu.trove.map.hash.TIntLongHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.fml.common.FMLCommonHandler;
import pl.asie.charset.lib.network.Packet;

public class PacketRequestMapData extends Packet {
	private static final TIntLongMap requestTimes = new TIntLongHashMap();
	private int mapId;

	public PacketRequestMapData() {

	}

	public PacketRequestMapData(ItemStack stack) {
		mapId = stack.getMetadata();
	}

	@Override
	public void readData(INetHandler handler, PacketBuffer buf) {
		mapId = buf.readInt();
	}

	@Override
	public void apply(INetHandler handler) {
		EntityPlayer player = getPlayer(handler);
		if (player instanceof EntityPlayerMP) {
			ItemStack stack = new ItemStack(Items.FILLED_MAP, 1, mapId);
			MapData data = Items.FILLED_MAP.getMapData(stack, player.getEntityWorld());

			// this is probably wrong
			ItemStack oldStack = player.inventory.getStackInSlot(0);
			player.inventory.setInventorySlotContents(0, stack);
			data.updateVisiblePlayers(player, stack);
			player.inventory.setInventorySlotContents(0, oldStack);

			net.minecraft.network.Packet<?> packet = ((ItemMap) Items.FILLED_MAP).createMapDataPacket(
					stack,
					player.getEntityWorld(),
					player
			);

			if (packet != null) {
				((EntityPlayerMP) player).connection.sendPacket(packet);
			}
		}
	}

	@Override
	public void writeData(PacketBuffer buf) {
		buf.writeInt(mapId);
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}

	public static void requestMap(ItemStack stack) {
		long time = System.currentTimeMillis();
		if (requestTimes.containsKey(stack.getMetadata()) && requestTimes.get(stack.getMetadata()) < time) {
			return;
		}

		requestTimes.put(stack.getMetadata(), time + 5000);
		CharsetProjector.packet.sendToServer(new PacketRequestMapData(stack));
	}
}
