package pl.asie.charset.pipes;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import pl.asie.charset.api.pipes.IShifter;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.TileBase;
import pl.asie.charset.lib.inventory.InventorySlot;
import pl.asie.charset.lib.inventory.InventorySlotIterator;
import pl.asie.charset.lib.utils.RedstoneUtils;

public class TileShifter extends TileBase implements IShifter, ITickable {
	private ItemStack[] filters = new ItemStack[6];
	private int redstoneLevel;
	private int ticker = ModCharsetPipes.RANDOM.nextInt(256);

	public EnumFacing getDirection() {
		return EnumFacing.getFront(getBlockMetadata());
	}

	@Override
	public Mode getMode() {
		EnumFacing direction = getDirection();
		TileEntity input = getNeighbourTile(direction.getOpposite());

		return input instanceof IInventory ? Mode.Extract : Mode.Shift;
	}

	public ItemStack[] getFilters() {
		return filters;
	}

	public void setFilter(int side, ItemStack stack) {
		filters[side] = stack;
		worldObj.markBlockForUpdate(pos);
	}

	public int getRedstoneLevel() {
		return redstoneLevel;
	}

	@Override
	public void initialize() {
		if (!worldObj.isRemote) {
			updateRedstoneLevel();
		}
	}

	public int getShiftDistance() {
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean isShifting() {
		return getRedstoneLevel() > 0;
	}

	@Override
	public boolean hasFilter() {
		for (ItemStack s : filters) {
			if (s != null) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean matches(ItemStack source) {
		int filterCount = 0;
		boolean matches = false;
		for (int i = 0; i < 6; i++) {
			if (filters[i] != null) {
				filterCount++;
				if (ItemUtils.equals(source, filters[i], false, filters[i].getHasSubtypes(), false)) {
					matches = true;
					break;
				}
			}
		}

		return filterCount == 0 || matches;
	}

	@Override
	public void update() {
		super.update();

		if (worldObj.isRemote) {
			return;
		}

		ticker++;

		if (ticker % 16 == 0 && redstoneLevel > 0) {
			EnumFacing direction = getDirection();

			TileEntity input = getNeighbourTile(direction.getOpposite());
			PartPipe output = PipeUtils.getPipe(getWorld(), getPos().offset(direction), direction.getOpposite());
			if (input instanceof IInventory && output instanceof PartPipe) {
				InventorySlotIterator iterator = new InventorySlotIterator((IInventory) input, direction);
				while (iterator.hasNext()) {
					InventorySlot slot = iterator.next();
					if (slot != null) {
						ItemStack source = slot.get();
						if (source != null && matches(source)) {
							ItemStack stack = slot.remove(getRedstoneLevel() >= 8 ? source.stackSize : 1, true);
							if (stack != null) {
								if (output.injectItem(stack, direction.getOpposite(), true) == stack.stackSize) {
									stack = slot.remove(getRedstoneLevel() >= 8 ? source.stackSize : 1, false);
									if (stack != null) {
										output.injectItem(stack, direction.getOpposite(), false);
									}

									return;
								}
							}
						}
					}
				}
			}
		}
	}

	// TODO: Replace with custom packet for redstone level-only sync
	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound tag = new NBTTagCompound();
		writeToNBT(tag);
		return new S35PacketUpdateTileEntity(pos, 2, tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
		worldObj.markBlockRangeForRenderUpdate(pos, pos);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		redstoneLevel = nbt.getByte("rs");
		NBTTagList filterList = nbt.getTagList("filters", 10);
		for (int i = 0; i < Math.min(filterList.tagCount(), filters.length); i++) {
			NBTTagCompound cpd = filterList.getCompoundTagAt(i);
			filters[i] = ItemStack.loadItemStackFromNBT(cpd);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setByte("rs", (byte) redstoneLevel);
		NBTTagList filterList = new NBTTagList();
		for (int i = 0; i < filters.length; i++) {
			NBTTagCompound fnbt = new NBTTagCompound();
			if (filters[i] != null) {
				filters[i].writeToNBT(fnbt);
			}
			filterList.appendTag(fnbt);
		}
		nbt.setTag("filters", filterList);
	}

	public void updateRedstoneLevel() {
		int oldRedstoneLevel = redstoneLevel;

		redstoneLevel = 0;
		for (EnumFacing d : EnumFacing.VALUES) {
			redstoneLevel = Math.max(redstoneLevel, RedstoneUtils.getRedstonePowerWithWire(worldObj, pos.offset(d), d));
		}

		if (oldRedstoneLevel != redstoneLevel) {
			worldObj.markBlockForUpdate(pos);
			worldObj.notifyBlockOfStateChange(pos, getBlockType());
		}

        EnumFacing direction = getDirection();
        PartPipe output = PipeUtils.getPipe(getWorld(), getPos().offset(direction), direction.getOpposite());
        if ((getMode() == Mode.Extract && !output.connects(direction.getOpposite()))
                || (getMode() == Mode.Shift && output.connects(direction.getOpposite()))) {
            worldObj.notifyBlockOfStateChange(pos.offset(getDirection()), getBlockType());
        }
	}
}
