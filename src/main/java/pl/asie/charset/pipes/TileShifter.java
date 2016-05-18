package pl.asie.charset.pipes;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import pl.asie.charset.api.pipes.IShifter;
import pl.asie.charset.lib.TileBase;
import pl.asie.charset.lib.inventory.InventoryUtils;
import pl.asie.charset.lib.refs.Properties;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.RedstoneUtils;

public class TileShifter extends TileBase implements IShifter, ITickable {
	private ItemStack[] filters = new ItemStack[6];
	private int redstoneLevel;
	private int ticker = ModCharsetPipes.RANDOM.nextInt(256);

	public EnumFacing getDirection(IBlockState state) {
		return state.getValue(Properties.FACING);
	}

	public EnumFacing getDirection() {
		if (worldObj != null) {
			return getDirection(worldObj.getBlockState(pos));
		} else {
			return EnumFacing.UP;
		}
	}

	private boolean isInput(TileEntity input, EnumFacing direction) {
		return input != null && (input instanceof IFluidHandler || InventoryUtils.getItemHandler(input, direction) != null);
	}

	@Override
	public Mode getMode() {
		EnumFacing direction = getDirection();
		TileEntity input = getNeighbourTile(direction.getOpposite());

		return isInput(input, direction) ? Mode.Extract : Mode.Shift;
	}

	public ItemStack[] getFilters() {
		return filters;
	}

	public void setFilter(int side, ItemStack stack) {
		filters[side] = stack;
		markBlockForUpdate();
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
	public boolean matches(FluidStack stack) {
		if (stack == null) {
			return false;
		}

		if (!hasFilter()) {
			return true;
		}

		for (ItemStack s : filters) {
			if (s != null) {
				if (FluidContainerRegistry.containsFluid(s, stack)) {
					return true;
				} else if (s.getItem() instanceof IFluidContainerItem) {
					FluidStack filter = ((IFluidContainerItem) s.getItem()).getFluid(s);
					if (filter != null && filter.amount > 0 && filter.isFluidEqual(stack)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	@Override
	public void update() {
		super.update();

		if (worldObj.isRemote) {
			return;
		}

		ticker++;

		if (redstoneLevel > 0) {
			EnumFacing direction = getDirection();

			TileEntity input = getNeighbourTile(direction.getOpposite());
			PartPipe output = PipeUtils.getPipe(getWorld(), getPos().offset(direction), direction.getOpposite());
			if (input != null && output != null) {
				if (input instanceof IFluidHandler) {
					FluidStack stack = ((IFluidHandler) input).drain(direction, PipeFluidContainer.TANK_RATE, false);
					if (stack != null) {
						boolean matches = matches(stack);

						if (matches) {
							int filled = output.fluid.fill(direction.getOpposite(), stack, false);
							if (filled > 0) {
								stack.amount = filled;
								stack = ((IFluidHandler) input).drain(direction, stack, true);
								output.fluid.fill(direction.getOpposite(), stack, true);
							}
						}
					}
				}

				if (ticker % 16 == 0) {
					IItemHandler handler = InventoryUtils.getItemHandler(input, direction);
					if (handler != null) {
						for (int i = 0; i < handler.getSlots(); i++) {
							ItemStack source = handler.getStackInSlot(i);
							if (source != null && matches(source)) {
								int maxSize = /* getRedstoneLevel() >= 8 ? source.stackSize : */ 1;
								ItemStack stack = handler.extractItem(i, maxSize, true);
								if (stack != null) {
									if (output.injectItem(stack, direction.getOpposite(), true) == stack.stackSize) {
										stack = handler.extractItem(i, maxSize, false);
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
	}

	// TODO: Replace with custom packet for redstone level-only sync
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound tag = new NBTTagCompound();
		writeToNBT(tag);
		return new SPacketUpdateTileEntity(pos, 2, tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
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
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
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
		return nbt;
	}

	public void updateRedstoneLevel() {
		int oldRedstoneLevel = redstoneLevel;

		redstoneLevel = 0;
		for (EnumFacing d : EnumFacing.VALUES) {
			redstoneLevel = Math.max(redstoneLevel, RedstoneUtils.getRedstonePowerWithWire(worldObj, pos.offset(d), d));
		}

		if (oldRedstoneLevel != redstoneLevel) {
			markBlockForUpdate();
			worldObj.notifyBlockOfStateChange(pos, getBlockType());
		}

		EnumFacing direction = getDirection();
		PartPipe output = PipeUtils.getPipe(getWorld(), getPos().offset(direction), direction.getOpposite());
		if (output != null) {
			if ((getMode() == Mode.Extract && !output.connects(direction.getOpposite()))
					|| (getMode() == Mode.Shift && output.connects(direction.getOpposite()))) {
				worldObj.notifyBlockOfStateChange(pos.offset(getDirection()), getBlockType());
			}
		}
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return ((facing == null || facing == getDirection()) && capability == ModCharsetPipes.CAP_SHIFTER)
				|| super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if ((facing == null || facing == getDirection()) && capability == ModCharsetPipes.CAP_SHIFTER) {
			return (T) this;
		}
		return super.getCapability(capability, facing);
	}
}
