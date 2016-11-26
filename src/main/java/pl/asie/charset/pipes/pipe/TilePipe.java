package pl.asie.charset.pipes.pipe;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import pl.asie.charset.api.pipes.IPipe;
import pl.asie.charset.api.pipes.IShifter;
import pl.asie.charset.lib.blocks.TileBase;
import pl.asie.charset.lib.misc.IConnectable;
import pl.asie.charset.lib.utils.GenericExtendedProperty;
import pl.asie.charset.lib.utils.RotationUtils;
import pl.asie.charset.pipes.ModCharsetPipes;
import pl.asie.charset.pipes.PipeUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TilePipe extends TileBase implements IConnectable, IPipe, ITickable {
    public static final GenericExtendedProperty<TilePipe> PROPERTY = new GenericExtendedProperty<TilePipe>("part", TilePipe.class);

    final PipeFluidContainer fluid = new PipeFluidContainer(this);
    boolean renderFast = false;

    protected int[] shifterDistance = new int[6];

    private final Set<PipeItem> itemSet = new HashSet<PipeItem>();
    private byte connectionCache = 0;
    private boolean neighborBlockChanged;
    private boolean requestUpdate;

    public TileEntity getNeighbourTile(EnumFacing side) {
        return side != null ? getWorld().getTileEntity(getPos().offset(side)) : null;
    }

    private boolean internalConnects(EnumFacing side) {
        /* ISlottedPart part = getContainer().getPartInSlot(PartSlot.getFaceSlot(side));
        if (part instanceof IMicroblock.IFaceMicroblock) {
            if (!((IMicroblock.IFaceMicroblock) part).isFaceHollow()) {
                return false;
            }
        }

        if (!OcclusionHelper.occlusionTest(OcclusionHelper.boxes(BOXES[side.ordinal()]), p -> p == this, getContainer())) {
            return false;
        } */

        if (PipeUtils.getPipe(getWorld(), getPos().offset(side), side.getOpposite()) != null) {
            return true;
        }

        TileEntity tile = getNeighbourTile(side);

        if (tile != null) {
            if (tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite())) {
                return true;
            }

            if (tile instanceof ISidedInventory || tile instanceof IInventory) {
                return true;
            }

            if (tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite())) {
                return true;
            }

            if (tile.hasCapability(ModCharsetPipes.CAP_SHIFTER, side.getOpposite())) {
                return tile.getCapability(ModCharsetPipes.CAP_SHIFTER, side.getOpposite()).getMode() == IShifter.Mode.Extract;
            }
        }

        return false;
    }

    private void updateConnections(EnumFacing side) {
        if (side != null) {
            connectionCache &= ~(1 << side.ordinal());

            if (internalConnects(side)) {
                TilePipe pipe = PipeUtils.getPipe(getWorld(), getPos().offset(side), side.getOpposite());
                if (pipe != null && !pipe.internalConnects(side.getOpposite())) {
                    return;
                }

                connectionCache |= 1 << side.ordinal();
            }
        } else {
            for (EnumFacing facing : EnumFacing.VALUES) {
                updateConnections(facing);
            }
        }
    }

    @Override
    public boolean connects(EnumFacing side) {
        return (connectionCache & (1 << side.ordinal())) != 0;
    }

    @Override
    public void readNBTData(NBTTagCompound nbt, boolean isClient) {
        if (!isClient) {
            readItems(nbt);

            NBTTagCompound tag = nbt.getCompoundTag("fluid");
            fluid.readFromNBT(tag);
        }

        connectionCache = nbt.getByte("cc");

        if (!isClient) {
            shifterDistance = nbt.getIntArray("shifterDist");
            if (shifterDistance == null || shifterDistance.length != 6) {
                shifterDistance = new int[6];
            }
        } else {
            requestUpdate = true;
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
    public NBTTagCompound writeNBTData(NBTTagCompound nbt, boolean isClient) {
        if (!isClient) {
            writeItems(nbt);

            NBTTagCompound tag = new NBTTagCompound();
            fluid.writeToNBT(tag);
            if (tag.getSize() > 0) {
                nbt.setTag("fluid", tag);
            }
        }

        nbt.setByte("cc", connectionCache);
        if (!isClient && shifterDistance != null) {
            nbt.setIntArray("shifterDist", shifterDistance);
        }
        return nbt;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (getWorld() != null && getWorld().isRemote) {
            synchronized (itemSet) {
                itemSet.clear();
            }
        }
    }

    @Override
    public void validate() {
        super.validate();
        neighborBlockChanged = true;
        scheduleRenderUpdate();
    }

    @Override
    public void update() {
        super.update();

        if (requestUpdate) {
            markBlockForRenderUpdate();
            ModCharsetPipes.packet.sendToServer(new PacketPipeSyncRequest(this));
        }

        if (neighborBlockChanged) {
            updateNeighborInfo(true);
            neighborBlockChanged = false;
        }

        fluid.update();

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

        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos(getPos());
        EnumFacing dirO = dir.getOpposite();
        int dist = 0;
        TilePipe pipe;
        TileEntity tile;

        while ((pipe = PipeUtils.getPipe(getWorld(), p, dirO)) != null) {
            p.move(dirO);
            dist++;

            if (!pipe.connects(dirO)) {
                break;
            }
        }

        tile = getWorld().getTileEntity(p);

        if (tile != null && tile.hasCapability(ModCharsetPipes.CAP_SHIFTER, dir)
                && isMatchingShifter(tile.getCapability(ModCharsetPipes.CAP_SHIFTER, dir), dir, dist)) {
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

    private void updateNeighborInfo(boolean sendPacket) {
        if (!getWorld().isRemote) {
            byte oc = connectionCache;

            for (EnumFacing dir : EnumFacing.VALUES) {
                updateConnections(dir);
                updateShifterSide(dir);
            }

            if (sendPacket && connectionCache != oc) {
                markBlockForUpdate();
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

        IShifter shifter;
        if (tile != null && tile.hasCapability(ModCharsetPipes.CAP_SHIFTER, dir) && isMatchingShifter(
                shifter = tile.getCapability(ModCharsetPipes.CAP_SHIFTER, dir), dir, Integer.MAX_VALUE)) {
            return shifter;
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
        if (getWorld() == null || !getWorld().isRemote) {
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

            if (renderFast && ModCharsetPipes.proxy.stopsRenderFast(getWorld(), item.stack)) {
                renderFast = false;
            }
            itemSet.add(item);
        }
    }

    protected void removeItemClientSide(PipeItem item) {
        if (getWorld() != null && getWorld().isRemote) {
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

                    if (renderFast && ModCharsetPipes.proxy.stopsRenderFast(getWorld(), item.stack)) {
                        renderFast = false;
                    }
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
        if (getWorld() == null || getWorld().isRemote || !connects(direction)) {
            return 0;
        }

        PipeItem item = new PipeItem(this, stack, direction);

        if (injectItemInternal(item, direction, simulate)) {
            return stack.getCount();
        } else {
            return 0;
        }
    }

    protected void scheduleRenderUpdate() {
        if (getWorld() != null) {
            getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
        }
    }

    public void onNeighborBlockChange(Block block , BlockPos neighborPos) {
        neighborBlockChanged = true;
    }

    public Collection<PipeItem> getPipeItems() {
        return itemSet;
    }

    PipeItem getItemByID(int id) {
        synchronized (itemSet) {
            for (PipeItem p : itemSet) {
                if (p.id == id) {
                    return p;
                }
            }
        }

        return null;
    }

    protected void onSyncRequest() {
        // TODO: HACK! HACK! HACK! HACK! HACK! HACK! HACK! HACK!
        synchronized (itemSet) {
            for (PipeItem p : itemSet) {
                p.sendPacket(true);
            }
        }

        fluid.sendPacket(true);
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
                    if (p.getDirection() == null) {
                        continue;
                    }

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
    public boolean hasFastRenderer() {
        return itemSet.size() == 0 || renderFast;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluid.tanks[facing.ordinal()]);
        }

        return null;
    }
}