package pl.asie.charset.pipes;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.IFluidHandler;

import pl.asie.charset.lib.IConnectable;
import pl.asie.charset.lib.TileBase;
import pl.asie.charset.pipes.api.IShifter;

public class TilePipe extends TileBase implements IConnectable {
	private int ImmibisMicroblocks_TransformableTileEntityMarker;

	protected int[] shifterDistance = new int[6];
	private final Set<PipeItem> itemSet = new HashSet<PipeItem>();

	private boolean neighborBlockChanged;

	public TilePipe() {
	}

	private boolean internalConnects(ForgeDirection side) {
		if (!ImmibisMicroblocks_isSideOpen(side.ordinal())) {
			return false;
		}

		TileEntity tile = getNeighbourTile(side);

		if (tile instanceof IInventory) {
			if (tile instanceof ISidedInventory) {
				int[] slots = ((ISidedInventory) tile).getAccessibleSlotsFromSide(side.getOpposite().ordinal());
				if (slots == null || slots.length == 0) {
					return false;
				}
			}

			return true;
		}

		if (tile instanceof IFluidHandler || tile instanceof TilePipe) {
			return true;
		}

		if (tile instanceof IShifter && ((IShifter) tile).getMode() == IShifter.Mode.Extract && ((IShifter) tile).getDirection() == side.getOpposite()) {
			return true;
		}

		return false;
	}

	@Override
	public boolean connects(ForgeDirection side) {
		if (internalConnects(side)) {
			TileEntity tile = getNeighbourTile(side);
			if (tile instanceof TilePipe && !((TilePipe) tile).internalConnects(side.getOpposite())) {
				return false;
			}

			return true;
		}

		return false;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		NBTTagList list = nbt.getTagList("items", 10);
		itemSet.clear();

		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound compound = list.getCompoundTagAt(i);
			PipeItem pipeItem = new PipeItem(this, compound);
			if (pipeItem.isValid()) {
				itemSet.add(pipeItem);
			}
		}

		shifterDistance = nbt.getIntArray("shifterDist");
		if (shifterDistance == null || shifterDistance.length != 6) {
			shifterDistance = new int[6];
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		NBTTagList list = new NBTTagList();

		for (PipeItem i : itemSet) {
			NBTTagCompound cpd = new NBTTagCompound();
			i.writeToNBT(cpd);
			list.appendTag(cpd);
		}

		nbt.setTag("items", list);
		nbt.setIntArray("shifterDist", shifterDistance);
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if (worldObj.isRemote) {
			itemSet.clear();
		}
	}

	@Override
	public void initialize() {
		updateShifters();
		scheduleRenderUpdate();
	}

	@Override
	public void updateEntity() {
		if (neighborBlockChanged) {
			updateShifters();
			scheduleRenderUpdate();
			neighborBlockChanged = false;
		}

		synchronized (this) {
			Iterator<PipeItem> itemIterator = itemSet.iterator();
			while (itemIterator.hasNext()) {
				PipeItem p = itemIterator.next();
				if (!p.move()) {
					itemIterator.remove();
				}
			}
		}
	}

	protected int getShifterStrength(ForgeDirection direction) {
		return direction == ForgeDirection.UNKNOWN ? 0 : shifterDistance[direction.ordinal()];
	}

	private void updateShifterSide(ForgeDirection dir) {
		int i = dir.ordinal();
		int oldDistance = shifterDistance[i];

		if (shifterDistance[i] == 1 && getNearestShifterInternal(ForgeDirection.getOrientation(i)) != null) {
			return;
		}

		int x = xCoord;
		int y = yCoord;
		int z = zCoord;
		int dist = 0;
		TileEntity tile;

		while ((tile = worldObj.getTileEntity(x, y, z)) instanceof TilePipe) {
			x -= dir.offsetX;
			y -= dir.offsetY;
			z -= dir.offsetZ;
			dist++;

			if (!((TilePipe) tile).connects(dir.getOpposite())) {
				tile = worldObj.getTileEntity(x, y, z);
				break;
			}
		}

		if (tile instanceof IShifter && isMatchingShifter((IShifter) tile, dir, dist)) {
			shifterDistance[i] = dist;
		} else {
			shifterDistance[i] = 0;
		}

		if (oldDistance != shifterDistance[i]) {
			TileEntity notifyTile = getNeighbourTile(dir);
			if (notifyTile instanceof TilePipe) {
				((TilePipe) notifyTile).updateShifterSide(dir);
			}
		}
	}

	private void updateShifters() {
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			updateShifterSide(dir);
		}
	}

	private boolean isMatchingShifter(IShifter p, ForgeDirection dir, int dist) {
		return p.getDirection() == dir && dist <= p.getShiftDistance();
	}

	private IShifter getNearestShifterInternal(ForgeDirection dir) {
		TileEntity tile;

		switch (shifterDistance[dir.ordinal()]) {
			case 0:
				return null;
			case 1:
				tile = getNeighbourTile(dir.getOpposite());
				break;
			default:
				tile = worldObj.getTileEntity(
						xCoord - dir.offsetX * shifterDistance[dir.ordinal()],
						yCoord - dir.offsetY * shifterDistance[dir.ordinal()],
						zCoord - dir.offsetZ * shifterDistance[dir.ordinal()]
				);
		}

		if (tile instanceof IShifter && isMatchingShifter((IShifter) tile, dir, Integer.MAX_VALUE)) {
			return (IShifter) tile;
		} else {
			return null;
		}
	}

	protected IShifter getNearestShifter(ForgeDirection dir) {
		if (dir == ForgeDirection.UNKNOWN) {
			return null;
		} else if (shifterDistance[dir.ordinal()] == 0) {
			return null;
		} else {
			IShifter p = getNearestShifterInternal(dir);
			if (p == null) {
				updateShifterSide(dir);
				return getNearestShifterInternal(dir);
			} else {
				return p;
			}
		}
	}

	protected void addItemClientSide(PipeItem item) {
		if (!worldObj.isRemote) {
			return;
		}

		synchronized (this) {
			Iterator<PipeItem> itemIterator = itemSet.iterator();
			while (itemIterator.hasNext()) {
				PipeItem p = itemIterator.next();
				if (p.id == item.id) {
					itemIterator.remove();
					break;
				}
			}

			itemSet.add(item);
		}
	}

	protected void removeItemClientSide(PipeItem item) {
		if (worldObj.isRemote) {
			itemSet.remove(item);
		}
	}

	protected boolean injectItemInternal(PipeItem item, ForgeDirection dir, boolean simulate) {
		if (item.isValid()) {
			int stuckItems = 0;

			for (PipeItem p : itemSet) {
				if (p.isStuck()) {
					stuckItems++;

					if (stuckItems >= 1) {
						return false;
					}
				}
			}

			if (!simulate) {
				itemSet.add(item);
				item.reset(this, dir);

				if (!worldObj.isRemote) {
					ModCharsetPipes.packet.sendToAllAround(new PacketItemUpdate(this, item, true), this, ModCharsetPipes.PIPE_TESR_DISTANCE);
				}
			}
			return true;
		} else {
			return false;
		}
	}

	public boolean injectItem(ItemStack stack, ForgeDirection direction, boolean simulate) {
		if (worldObj.isRemote || !connects(direction)) {
			return false;
		}

		PipeItem item = new PipeItem(this, stack, direction);

		if (injectItemInternal(item, direction, simulate)) {
			return true;
		} else {
			return false;
		}
	}

	protected void scheduleRenderUpdate() {
		worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
	}

	public void onNeighborBlockChange() {
		neighborBlockChanged = true;
	}

	// IMMIBIS' MICROBLOCKS COMPAT

	public boolean ImmibisMicroblocks_isSideOpen(int side) {
		return true;
	}

	public void ImmibisMicroblocks_onMicroblocksChanged() {

	}

	public Collection<PipeItem> getPipeItems() {
		return Collections.unmodifiableSet(itemSet);
	}
}
