package pl.asie.charset.pipes;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.ITickable;

import net.minecraftforge.fluids.IFluidHandler;

import mcmultipart.MCMultiPartMod;
import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.Multipart;
import mcmultipart.multipart.PartSlot;
import mcmultipart.raytrace.PartMOP;
import pl.asie.charset.api.pipes.IPipe;
import pl.asie.charset.api.pipes.IShifter;
import pl.asie.charset.lib.IConnectable;
import pl.asie.charset.lib.refs.Properties;

public class PartPipe extends Multipart implements IConnectable, ISlottedPart, IPipe, ITickable {
	protected int[] shifterDistance = new int[6];
	private final Set<PipeItem> itemSet = new HashSet<PipeItem>();

	private boolean neighborBlockChanged;

	public PartPipe() {
	}

    // Block logic

    @Override
    public float getHardness(PartMOP hit) {
        return 0.3F;
    }

    @Override
    public Material getMaterial() {
        return Material.glass;
    }

    @Override
    public IBlockState getExtendedState(IBlockState state) {
        return state
                .withProperty(Properties.DOWN, connects(EnumFacing.DOWN))
                .withProperty(Properties.UP, connects(EnumFacing.UP))
                .withProperty(Properties.NORTH, connects(EnumFacing.NORTH))
                .withProperty(Properties.SOUTH, connects(EnumFacing.SOUTH))
                .withProperty(Properties.WEST, connects(EnumFacing.WEST))
                .withProperty(Properties.EAST, connects(EnumFacing.EAST));
    }

    @Override
    public BlockState createBlockState() {
        return new BlockState(MCMultiPartMod.multipart,
                Properties.DOWN,
                Properties.UP,
                Properties.NORTH,
                Properties.SOUTH,
                Properties.WEST,
                Properties.EAST);
    }

    private static class BoundingBox {
        private static final AxisAlignedBB[] bounds = new AxisAlignedBB[0x40];

        static {
            for (int mask = 0; mask < 0x40; ++mask) {
                bounds[mask] = AxisAlignedBB.fromBounds(
                        ((mask & (1 << 4)) != 0 ? 0 : 0.25),
                        ((mask & (1 << 0)) != 0 ? 0 : 0.25),
                        ((mask & (1 << 2)) != 0 ? 0 : 0.25),
                        ((mask & (1 << 5)) != 0 ? 1 : 0.75),
                        ((mask & (1 << 1)) != 0 ? 1 : 0.75),
                        ((mask & (1 << 3)) != 0 ? 1 : 0.75)
                );
            }
        }

        private static AxisAlignedBB getBox(int msk) {
            return bounds[msk];
        }
    }

    @Override
    public void addSelectionBoxes(List<AxisAlignedBB> list) {
        list.add(BoundingBox.getBox(neighbors()));
    }

    @Override
    public void addCollisionBoxes(AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {
        AxisAlignedBB box = BoundingBox.getBox(neighbors());
        if (box.intersectsWith(mask)) {
            list.add(box);
        }
    }

    @Override
    public boolean canRenderInLayer(EnumWorldBlockLayer layer) {
        return layer == EnumWorldBlockLayer.CUTOUT;
    }

    private int neighbors() {
        int result = 0;
            for (EnumFacing side : EnumFacing.VALUES) {
            if (connects(side)) {
                result |= 1 << side.ordinal();
            }
        }
        return result;
    }

    @Override
    public String getModelPath() {
        return "charsetpipes:pipe";
    }

    // Tile logic

    public TileEntity getNeighbourTile(EnumFacing side) {
        return side != null ? getWorld().getTileEntity(getPos().offset(side)) : null;
    }

	private boolean internalConnects(EnumFacing side) {
        if (PipeUtils.getPipe(getWorld(), getPos().offset(side), side.getOpposite()) != null) {
            return true;
        }

		TileEntity tile = getNeighbourTile(side);

		if (tile instanceof IInventory) {
			if (tile instanceof ISidedInventory) {
				int[] slots = ((ISidedInventory) tile).getSlotsForFace(side.getOpposite());
				if (slots == null || slots.length == 0) {
					return false;
				}
			}

			return true;
		}

		if (tile instanceof IFluidHandler) {
			return true;
		}

		if (tile instanceof IShifter && ((IShifter) tile).getMode() == IShifter.Mode.Extract && ((IShifter) tile).getDirection() == side.getOpposite()) {
			return true;
		}

		return false;
	}

	@Override
	public boolean connects(EnumFacing side) {
		if (internalConnects(side)) {
			PartPipe pipe = PipeUtils.getPipe(getWorld(), getPos().offset(side), side.getOpposite());
			if (pipe != null && !pipe.internalConnects(side.getOpposite())) {
				return false;
			}

			return true;
		}

		return false;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		readItems(nbt);

		shifterDistance = nbt.getIntArray("shifterDist");
		if (shifterDistance == null || shifterDistance.length != 6) {
			shifterDistance = new int[6];
		}
	}

	private void readItems(NBTTagCompound nbt) {
		NBTTagList list = nbt.getTagList("items", 10);
		synchronized (itemSet) {
			itemSet.clear();

			for (int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound compound = list.getCompoundTagAt(i);
				PipeItem pipeItem = new PipeItem(this, compound);
				if (pipeItem.isValid()) {
					itemSet.add(pipeItem);
				}
			}
		}
	}

	private void writeItems(NBTTagCompound nbt) {
		NBTTagList list = new NBTTagList();

		synchronized (itemSet) {
			for (PipeItem i : itemSet) {
				NBTTagCompound cpd = new NBTTagCompound();
				i.writeToNBT(cpd);
				list.appendTag(cpd);
			}
		}

		nbt.setTag("items", list);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		writeItems(nbt);

		nbt.setIntArray("shifterDist", shifterDistance);
	}

	@Override
	public void onUnloaded() {
		super.onUnloaded();
		if (getWorld().isRemote) {
			synchronized (itemSet) {
				itemSet.clear();
			}
		}
	}

    @Override
    public void onAdded() {
        updateShifters();
        scheduleRenderUpdate();
    }

	@Override
	public void onLoaded() {
		if (getWorld().isRemote) {
			ModCharsetPipes.packet.sendToServer(new PacketPipeSyncRequest(this));
		}

		updateShifters();
		scheduleRenderUpdate();
	}

	@Override
	public void update() {
		if (neighborBlockChanged) {
			updateShifters();
			scheduleRenderUpdate();
			neighborBlockChanged = false;
		}

		synchronized (itemSet) {
			Iterator<PipeItem> itemIterator = itemSet.iterator();
			while (itemIterator.hasNext()) {
				PipeItem p = itemIterator.next();
				if (!p.move()) {
					itemIterator.remove();
				}
			}
		}
	}

	protected int getShifterStrength(EnumFacing direction) {
		return direction == null ? 0 : shifterDistance[direction.ordinal()];
	}

	private void updateShifterSide(EnumFacing dir) {
		int i = dir.ordinal();
		int oldDistance = shifterDistance[i];

		if (shifterDistance[i] == 1 && getNearestShifterInternal(dir) != null) {
			return;
		}

		BlockPos p = getPos();
		int dist = 0;
        PartPipe pipe;
        TileEntity tile;

		while ((pipe = PipeUtils.getPipe(getWorld(), p, dir.getOpposite())) instanceof PartPipe) {
			p = p.offset(dir.getOpposite());
			dist++;

			if (!pipe.connects(dir.getOpposite())) {
				break;
			}
		}

        tile = getWorld().getTileEntity(p);

		if (tile instanceof IShifter && isMatchingShifter((IShifter) tile, dir, dist)) {
			shifterDistance[i] = dist;
		} else {
			shifterDistance[i] = 0;
		}

		if (oldDistance != shifterDistance[i]) {
			pipe = PipeUtils.getPipe(getWorld(), getPos().offset(dir), null);
			if (pipe != null) {
				pipe.updateShifterSide(dir);
			}
		}
	}

	private void updateShifters() {
		if (!getWorld().isRemote) {
			for (EnumFacing dir : EnumFacing.VALUES) {
				updateShifterSide(dir);
			}
		}
	}

	private boolean isMatchingShifter(IShifter p, EnumFacing dir, int dist) {
		return p.getDirection() == dir && dist <= p.getShiftDistance();
	}

	private IShifter getNearestShifterInternal(EnumFacing dir) {
		TileEntity tile;

		switch (shifterDistance[dir.ordinal()]) {
			case 0:
				return null;
			case 1:
				tile = getNeighbourTile(dir.getOpposite());
				break;
			default:
				tile = getWorld().getTileEntity(getPos().offset(dir.getOpposite(), shifterDistance[dir.ordinal()]));
		}

		if (tile instanceof IShifter && isMatchingShifter((IShifter) tile, dir, Integer.MAX_VALUE)) {
			return (IShifter) tile;
		} else {
			return null;
		}
	}

	protected IShifter getNearestShifter(EnumFacing dir) {
		if (dir == null) {
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
		if (!getWorld().isRemote) {
			return;
		}

		synchronized (itemSet) {
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
		if (getWorld().isRemote) {
			synchronized (itemSet) {
				itemSet.remove(item);
			}
		}
	}

	protected boolean injectItemInternal(PipeItem item, EnumFacing dir, boolean simulate) {
		if (item.isValid()) {
			int stuckItems = 0;

			synchronized (itemSet) {
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
				}
			}

			if (!simulate) {
				item.reset(this, dir);
				item.sendPacket(true);
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean canInjectItems(EnumFacing side) {
		return connects(side);
	}

	@Override
	public int injectItem(ItemStack stack, EnumFacing direction, boolean simulate) {
		if (getWorld().isRemote || !connects(direction)) {
			return 0;
		}

		PipeItem item = new PipeItem(this, stack, direction);

		if (injectItemInternal(item, direction, simulate)) {
			return stack.stackSize;
		} else {
			return 0;
		}
	}

	protected void scheduleRenderUpdate() {
		getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
	}

    @Override
	public void onNeighborBlockChange(Block block) {
        System.out.println("!!");
		neighborBlockChanged = true;
	}

	public Collection<PipeItem> getPipeItems() {
		return itemSet;
	}

	protected void onSyncRequest() {
		// TODO: HACK! HACK! HACK! HACK! HACK! HACK! HACK! HACK!
		synchronized (itemSet) {
			for (PipeItem p : itemSet) {
				p.sendPacket(true);
			}
		}
	}

	@Override
	public ItemStack getTravellingStack(EnumFacing side) {
		float targetError = 1000f;
		PipeItem targetItem = null;

		synchronized (itemSet) {
			for (PipeItem p : itemSet) {
				float error;

				if (side == null) {
					error = Math.abs(p.getProgress() - 0.5f);

					if (error > 0.25f) {
						continue;
					}
				} else {
					if (p.getProgress() <= 0.25f && side == p.getDirection().getOpposite()) {
						error = Math.abs(p.getProgress() - 0.125f);
					} else if (p.getProgress() >= 0.75f && side == p.getDirection()) {
						error = Math.abs(p.getProgress() - 0.875f);
					} else {
						continue;
					}

					if (error > 0.125f) {
						continue;
					}
				}

				if (error < targetError) {
					targetError = error;
					targetItem = p;
				}
			}
		}

		return targetItem != null ? targetItem.getStack() : null;
	}

    @Override
    public EnumSet<PartSlot> getSlotMask() {
        return EnumSet.of(PartSlot.CENTER);
    }
}
