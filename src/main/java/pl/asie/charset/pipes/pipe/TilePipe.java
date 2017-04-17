package pl.asie.charset.pipes.pipe;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Chars;
import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipartTile;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import pl.asie.charset.api.lib.IDebuggable;
import pl.asie.charset.api.lib.IItemInsertionHandler;
import pl.asie.charset.api.pipes.IPipeView;
import pl.asie.charset.api.pipes.IShifter;
import pl.asie.charset.lib.IConnectable;
import pl.asie.charset.lib.blocks.TileBase;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.capability.CapabilityHelper;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.utils.*;
import pl.asie.charset.pipes.CharsetPipes;
import pl.asie.charset.pipes.PipeUtils;

import javax.annotation.Nullable;
import java.util.*;

public class TilePipe extends TileBase implements IConnectable, IPipeView, ITickable, IDebuggable {
    public static final int EXPLOSION_TIME = 20 * 30;
    public static final int EXPLOSION_ITEM_AMOUNT = (int) Math.round(PipeItem.TICKS_PER_BLOCK * 1.25);

    public class InsertionHandler implements IItemInsertionHandler {
        private final EnumFacing facing;

        private InsertionHandler(EnumFacing facing) {
            this.facing = facing;
        }

        @Override
        public ItemStack insertItem(ItemStack stack, boolean simulate) {
            if (getWorld().isRemote || !connects(facing)) {
                return stack;
            }

            PipeItem item = new PipeItem(TilePipe.this, stack, facing);

            if (injectItemInternal(item, facing, simulate)) {
                return ItemStack.EMPTY;
            } else {
                return stack;
            }
        }
    }

    public static final UnlistedPropertyGeneric<TilePipe> PROPERTY = new UnlistedPropertyGeneric<TilePipe>("part", TilePipe.class);

    final PipeFluidContainer fluid = new PipeFluidContainer(this);
    boolean renderFast = false;
    int explosionTimer;

    protected int[] shifterDistance = new int[6];

    private final PipeLogic logic = new PipeLogic(this);
    private final IItemInsertionHandler[] insertionHandlers = new IItemInsertionHandler[6];
    private final Set<PipeItem> itemSet = new HashSet<PipeItem>();
    private ItemMaterial material;
    private byte color = 0;
    private byte connectionCache = 0;
    private int neighborsUpdate = 0;
    private boolean requestUpdate;

    public PipeLogic getLogic() {
        return logic;
    }

    public TilePipe() {
        material = getMaterialFromNBT(null);
        for (EnumFacing facing : EnumFacing.VALUES)
            insertionHandlers[facing.ordinal()] = new InsertionHandler(facing);
    }

    public TileEntity getNeighbourTile(EnumFacing side) {
        return side != null ? getWorld().getTileEntity(getPos().offset(side)) : null;
    }

    private boolean internalConnects(EnumFacing side) {
        if (OcclusionUtils.INSTANCE.intersects(Collections.singletonList(BlockPipe.BOXES[side.ordinal()]), world, pos)) {
            return false;
        }

        TilePipe pipe = PipeUtils.getPipe(getWorld(), getPos().offset(side), side.getOpposite());

        if (pipe != null && pipe.getMaterial().equals(material)) {
            return true;
        }

        TileEntity tile = getNeighbourTile(side);

        if (tile != null) {
            if (CapabilityHelper.get(Capabilities.ITEM_INSERTION_HANDLER, tile, side.getOpposite()) != null) {
                return true;
            }

            if (CapabilityHelper.get(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, tile, side.getOpposite()) != null) {
                return true;
            }

            if (tile.hasCapability(CharsetPipes.CAP_SHIFTER, side.getOpposite())) {
                return tile.getCapability(CharsetPipes.CAP_SHIFTER, side.getOpposite()).getMode() == IShifter.Mode.Extract;
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
        material = getMaterialFromNBT(nbt);
        color = nbt.getByte("color");
        connectionCache = nbt.getByte("cc");

        if (nbt.hasKey("expT")) {
            explosionTimer = nbt.getShort("expT");
        } else {
            explosionTimer = 0;
        }

        logic.deserializeNBT(nbt.getCompoundTag("logic"));

        if (!isClient) {

            readItems(nbt);

            NBTTagCompound tag = nbt.getCompoundTag("fluid");
            fluid.readFromNBT(tag);

            shifterDistance = nbt.getIntArray("shifterDist");
            if (shifterDistance == null || shifterDistance.length != 6) {
                shifterDistance = new int[6];
            }
        } else {
            requestUpdate = true;
        }
    }

    protected ItemMaterial getMaterialFromNBT(NBTTagCompound compound) {
        return ItemMaterialRegistry.INSTANCE.getMaterial(compound, "material", "stone", new ItemStack(Blocks.STONE));
    }

    @Override
    public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
        material = getMaterialFromNBT(stack.getTagCompound());
        color = stack.hasTagCompound() ? stack.getTagCompound().getByte("color") : 0;
        markBlockForUpdate();
    }

    @Override
    public ItemStack getDroppedBlock() {
        return BlockPipe.createStack(material, getColor(), 1);
    }

    public ItemMaterial getMaterial() {
        return material;
    }

    @Nullable
    public EnumDyeColor getColor() {
        return color == 0 ? null : EnumDyeColor.byMetadata(color - 1);
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

            if (shifterDistance != null) {
                nbt.setIntArray("shifterDist", shifterDistance);
            }
        }

        if (explosionTimer > 0) {
            nbt.setShort("expT", (short) explosionTimer);
        }

        nbt.setTag("logic", logic.serializeNBT());

        if (material != null) {
            material.writeToNBT(nbt, "material");
        }
        nbt.setByte("color", color);
        nbt.setByte("cc", connectionCache);
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
        scheduleFullNeighborUpdate();
        scheduleRenderUpdate();
    }

    @Override
    public void update() {
        super.update();

        if (requestUpdate) {
            markBlockForRenderUpdate();
            CharsetPipes.instance.packet.sendToServer(new PacketPipeSyncRequest(this));
            requestUpdate = false;
        }

        if (neighborsUpdate != 0) {
            updateNeighborInfo(true);
            neighborsUpdate = 0;
        }

        fluid.update();

        if (!getWorld().isRemote) {
            if (itemSet.size() >= EXPLOSION_ITEM_AMOUNT) {
                if (explosionTimer == 0) {
                    CharsetPipes.packet.sendToWatching(new PacketPipeSyncExplosionTimer(this, true), this);
                    explosionTimer = CharsetPipes.rand.nextInt(16);
                } else {
                    explosionTimer++;
                    if (explosionTimer >= EXPLOSION_TIME) {
                        Block.spawnAsEntity(world, pos, getDroppedBlock());
                        for (ItemStack stack : getDrops()) {
                            Block.spawnAsEntity(world, pos, stack);
                        }
                        world.setBlockToAir(pos);
                        this.world.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 0.7F);
                        this.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 1.0D, 0.0D, 0.0D, new int[0]);
                    }
                }
            } else {
                if (explosionTimer > 0) {
                    CharsetPipes.packet.sendToWatching(new PacketPipeSyncExplosionTimer(this, false), this);
                }
                explosionTimer = 0;
            }
        } else {
            if (explosionTimer > 0) {
                for (int i = 0; i < Math.ceil(explosionTimer / 100.0); i++) {
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
                            pos.getX() + 0.25D + CharsetPipes.rand.nextDouble() * 0.5D,
                            pos.getY() + 0.25D + CharsetPipes.rand.nextDouble() * 0.5D,
                            pos.getZ() + 0.25D + CharsetPipes.rand.nextDouble() * 0.5D, CharsetPipes.rand.nextDouble() * 0.002D - 0.001D, 0.01D + (explosionTimer / 10000.0) + CharsetPipes.rand.nextDouble() * 0.03D, CharsetPipes.rand.nextDouble() * 0.002D - 0.001D, new int[0]);
                }
            }
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

    private boolean markShifterChange(EnumFacing dir, IShifter shifter, int shifterDist) {
        int i = dir.ordinal();
        int oldDistance = shifterDistance[i];

        if (shifter != null && isMatchingShifter(shifter, dir, shifterDist)) {
            shifterDistance[i] = shifterDist;
        } else {
            shifterDistance[i] = 0;
        }

        if (oldDistance != shifterDistance[i]) {
            logic.updateDirections();
            return true;
        } else {
            return false;
        }
    }

    private void propagateShifterChange(EnumFacing dir, IShifter shifter, int shifterDist) {
        TilePipe pipe = this;
        while (pipe != null && pipe.markShifterChange(dir, shifter, shifterDist)) {
            pipe = PipeUtils.getPipe(getWorld(), pipe.getPos().offset(dir), null);
            shifterDist++;
        }
    }

    private void updateShifterSide(EnumFacing dirBack) {
        BlockPos.MutableBlockPos shifterPos = new BlockPos.MutableBlockPos(getPos());
        EnumFacing dir = dirBack.getOpposite();
        int dist = 0;
        TilePipe pipe;

        while ((pipe = PipeUtils.getPipe(getWorld(), shifterPos, dirBack)) != null) {
            shifterPos.move(dirBack);
            dist++;

            if (!pipe.connects(dirBack)) {
                break;
            }
        }

        TileEntity shifterTile = getWorld().getTileEntity(shifterPos);
        IShifter shifter = CapabilityHelper.get(CharsetPipes.CAP_SHIFTER, shifterTile, dir);
        propagateShifterChange(dir, shifter, dist);
    }

    private void updateNeighborInfo(boolean sendPacket) {
        byte oc = connectionCache;

        for (EnumFacing dir : EnumFacing.VALUES) {
            if ((neighborsUpdate & (1 << dir.ordinal())) != 0) {
                updateConnections(dir);
                if (!getWorld().isRemote) {
                    updateShifterSide(dir);
                }
            }
        }

        if (!getWorld().isRemote) {
            logic.updateDirections();
        }

        if (sendPacket && connectionCache != oc) {
            world.notifyNeighborsRespectDebug(pos, getBlockType(), false);
            markBlockForUpdate();
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
        if (tile != null && tile.hasCapability(CharsetPipes.CAP_SHIFTER, dir) && isMatchingShifter(
                shifter = tile.getCapability(CharsetPipes.CAP_SHIFTER, dir), dir, Integer.MAX_VALUE)) {
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

            if (renderFast && CharsetPipes.proxy.stopsRenderFast(getWorld(), item.stack)) {
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

    protected void updateObservers(ItemStack source) {
        if (!world.isRemote && !source.isEmpty()) {
            ObserverHelper.updateObservingBlocksAt(world, pos, getBlockType());

            // TODO: Think of a better design for Shifters so we can bring this back
            /* ObserverHelper.updateObservingBlocksAt(world, pos, getBlockType(), (observerPos, facing) -> {
                TileEntity tile = world.getTileEntity(observerPos.offset(facing));
                if (tile != null) {
                    IItemHandler handler = CapabilityHelper.get(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, tile, facing.getOpposite());
                    if (handler != null) {
                        boolean hasFilter = false;
                        for (int i = 0; i < handler.getSlots(); i++) {
                            ItemStack stack = handler.getStackInSlot(i);
                            if (!stack.isEmpty()) {
                                hasFilter = true;
                                if (ItemUtils.equals(source, stack, false, stack.getHasSubtypes(), false)) {
                                    return true;
                                }
                            }
                        }

                        return !hasFilter;
                    }
                }

                return true;
            }); */
        }
    }

    // TODO: hack...
    public boolean isLikelyToFailInsertingItem(EnumFacing dir) {
        synchronized (itemSet) {
            for (PipeItem p : itemSet) {
                if (p.isStuck(dir)) {
                    return true;
                }
            }
        }

        return false;
    }

    protected boolean injectItemInternal(PipeItem item, EnumFacing dir, boolean simulate) {
        if (item.isValid()) {
            synchronized (itemSet) {
                if (isLikelyToFailInsertingItem(dir)) {
                    return false;
                }

                if (!simulate) {
                    itemSet.add(item);

                    if (renderFast && CharsetPipes.proxy.stopsRenderFast(getWorld(), item.stack)) {
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

    protected void scheduleRenderUpdate() {
         markBlockForRenderUpdate();
    }

    public void scheduleFullNeighborUpdate() {
        neighborsUpdate |= 0x3F;
    }

    public void onNeighborBlockChange(Block block, BlockPos neighborPos) {
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (this.getPos().offset(facing).equals(neighborPos)) {
                neighborsUpdate |= 1 << facing.ordinal();
                return;
            }
        }

        // fallback
        neighborsUpdate |= 0x3F;
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

    protected void onSyncRequest(EntityPlayer player) {
        synchronized (itemSet) {
            for (PipeItem p : itemSet) {
                CharsetPipes.packet.sendTo(p.getSyncPacket(true), player);
            }
        }

        CharsetPipes.packet.sendTo(fluid.getSyncPacket(true), player);
    }

    @Override
    public Collection<ItemStack> getTravellingStacks() {
        ImmutableList.Builder<ItemStack> builder = new ImmutableList.Builder<>();

        synchronized (itemSet) {
            for (PipeItem p : itemSet) {
                if (p.isValid()) {
                    builder.add(p.getStack());
                }
            }
        }

        return builder.build();
    }

    @Override
    public boolean hasFastRenderer() {
        return itemSet.size() == 0 || renderFast;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == Capabilities.DEBUGGABLE || capability == Capabilities.PIPE_VIEW)
            return true;

        if (facing != null && connects(facing)) {
            return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
                    || capability == Capabilities.ITEM_INSERTION_HANDLER;
        } else {
            return super.hasCapability(capability, facing);
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == Capabilities.DEBUGGABLE)
            return Capabilities.DEBUGGABLE.cast(this);
        if (capability == Capabilities.PIPE_VIEW)
            return Capabilities.PIPE_VIEW.cast(this);

        if (facing != null && connects(facing)) {
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluid.tanks[facing.ordinal()]);
            } else if (capability == Capabilities.ITEM_INSERTION_HANDLER) {
                return Capabilities.ITEM_INSERTION_HANDLER.cast(insertionHandlers[facing.ordinal()]);
            }
        }

        return super.getCapability(capability, facing);
    }

    @Override
    public void addDebugInformation(List<String> stringList, Side side) {
        if (side != Side.SERVER)
            return;

        StringBuilder info = new StringBuilder();
        info.append("i" + getPipeItems().size());

        stringList.add(info.toString());

        int directions = 0;
        int pDirections = 0;
        for (PipeLogic.Direction direction : getLogic().getPressuredDirections()) {
            if (direction != null) {
                pDirections |= 1 << direction.dir.ordinal();
            }
        }
        for (PipeLogic.Direction direction : getLogic().getNonPressuredDirections()) {
            if (direction != null) {
                directions |= 1 << direction.dir.ordinal();
            }
        }

        for (int i = 0; i <= 6; i++) {
            EnumFacing facing = SpaceUtils.getFacing(i);
            StringBuilder sideInfo = new StringBuilder();

            sideInfo.append(facing != null ? facing.name().charAt(0) : 'C');
            sideInfo.append(": ");
            if (i < 6) {
                boolean isPressured = (((pDirections) & (1 << facing.ordinal())) != 0);
                boolean isNotPressured = (((directions) & (1 << facing.ordinal())) != 0);

                sideInfo.append(connects(facing) ? '+' : '-');
                sideInfo.append(isPressured
                        ? (isNotPressured ? '%' : '>')
                        : (isNotPressured ? '+' : '-')
                );
            } else {
                sideInfo.append("--");
            }
            sideInfo.append(" f" + fluid.tanks[i].type.name().charAt(0) + "(" + fluid.tanks[i].amount + ")");
            if (i < 6) {
                sideInfo.append(" s" + shifterDistance[i]);
            }

            stringList.add(sideInfo.toString());
        }
    }

    public List<ItemStack> getDrops() {
        List<ItemStack> list = new ArrayList<>();

        synchronized (itemSet) {
            for (PipeItem item : itemSet) {
                if (!item.getStack().isEmpty()) {
                    list.add(item.getStack());
                }
            }
        }

        return list;
    }
}
