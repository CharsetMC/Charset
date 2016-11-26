package pl.asie.charset.storage.crate;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import pl.asie.charset.lib.blocks.TileBase;
import pl.asie.charset.lib.utils.DirectionUtils;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.storage.ModCharsetStorage;

import java.util.EnumSet;
import java.util.Set;

public class TileEntityCrate extends TileBase {
    public static final int CRATE_SIZE = 18;
    public CrateShapeCache cache;
    ItemStackHandler handler = new ItemStackHandler(CRATE_SIZE);
    private ItemStack plank;
    private int clientCM;

    void setCache(CrateShapeCache cache) {
        this.cache = cache;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            initCache();
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(cache.getHandler());
        }

        return null;
    }

    private void initCache() {
        if (cache == null || !cache.isValid()) {
            // We only pick up caches lower than our own - they're closer to the master.
            for (EnumFacing facing : DirectionUtils.NEGATIVES) {
                TileEntity tile = getNeighbourTile(facing);
                if (tile instanceof TileEntityCrate) {
                    TileEntityCrate other = (TileEntityCrate) tile;
                    if (other.cache != null) {
                        if (other.cache.add(this)) {
                            return;
                        }
                    }
                }
            }

            cache = new CrateShapeCache(this);
        }
    }

    private int getConnectionMatrix() {
        initCache();
        cache.checkDirty();

        int connectionMatrix = 0;
        Set<EnumFacing> facingSet = EnumSet.noneOf(EnumFacing.class);

        if (getWorld() != null) {
            for (EnumFacing facing : EnumFacing.VALUES) {
                if (cache.contains(getPos().offset(facing))) {
                    connectionMatrix |= (1 << facing.ordinal());
                    facingSet.add(facing);
                }
            }
        }
        for (int z = 0; z < 2; z++) {
            for (int y = 0; y < 2; y++) {
                for (int x = 0; x < 2; x++) {
                    EnumFacing fx = x == 0 ? EnumFacing.WEST : EnumFacing.EAST;
                    EnumFacing fy = y == 0 ? EnumFacing.DOWN : EnumFacing.UP;
                    EnumFacing fz = z == 0 ? EnumFacing.NORTH : EnumFacing.SOUTH;
                    boolean match = false;

                    if (facingSet.contains(fx) && facingSet.contains(fz)) {
                        if (cache.contains(getPos().offset(fx).offset(fz))) {
                            continue;
                        }
                        match = true;
                    }

                    if (facingSet.contains(fy) && facingSet.contains(fz)) {
                        if (cache.contains(getPos().offset(fy).offset(fz))) {
                            continue;
                        }
                        match = true;
                    }

                    if (facingSet.contains(fx) && facingSet.contains(fy)) {
                        if (cache.contains(getPos().offset(fx).offset(fy))) {
                            continue;
                        }
                        match = true;
                    }

                    if (match) {
                        connectionMatrix |= (1 << (6 + (x * 4) + (y * 2) + z));
                    }
                }
            }
        }

        return connectionMatrix;
    }

    @Override
    public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
        loadFromStack(stack);
        initCache();

        for (EnumFacing facing : EnumFacing.VALUES) {
            TileEntity tile = getNeighbourTile(facing);
            if (tile instanceof TileEntityCrate) {
                TileEntityCrate other = (TileEntityCrate) tile;
                if (other.cache != cache) {
                    CrateShapeCache oldCache = other.cache;
                    if (cache.add(other)) {
                        oldCache.invalidate();
                        cache.addNeighborsRecursively(other);
                    }
                }
            }
        }
    }

    @Override
    public void readNBTData(NBTTagCompound compound, boolean isClient) {
        plank = new ItemStack(compound.getCompoundTag("plank"));

        if (plank.isEmpty()) {
            plank = new ItemStack(Blocks.PLANKS);
        }

        if (isClient && compound.hasKey("c")) {
            int oldCM = clientCM;
            clientCM = compound.getInteger("c");

            if (oldCM != clientCM) {
                markBlockForRenderUpdate();
            }
        } else if (!isClient && compound.hasKey("inv")) {
            handler.deserializeNBT(compound.getCompoundTag("inv"));
        }
    }

    @Override
    public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
        ItemUtils.writeToNBT(plank, compound, "plank");
        if (isClient) {
            compound.setInteger("c", getConnectionMatrix());
        } else {
            compound.setTag("inv", handler.serializeNBT());
        }
        return compound;
    }

    public static ItemStack create(ItemStack plank) {
        TileEntityCrate crate = new TileEntityCrate();
        crate.plank = plank;
        return crate.getDroppedBlock();
    }

    @Override
    public void dropContents() {

    }

    @Override
    public ItemStack getPickedBlock() {
        return getDroppedBlock();
    }

    public ItemStack getDroppedBlock() {
        ItemStack stack = new ItemStack(ModCharsetStorage.crateBlock);
        ItemUtils.writeToNBT(plank, ItemUtils.getTagCompound(stack, true), "plank");
        return stack;
    }

    public void loadFromStack(ItemStack stack) {
        NBTTagCompound compound = stack.getTagCompound();
        readNBTData(compound != null ? stack.getTagCompound() : new NBTTagCompound(), true);
    }

    public CrateCacheInfo getCacheInfo() {
        return new CrateCacheInfo(plank, clientCM);
    }

    public ItemStack getMaterial() {
        return plank;
    }

    public void neighborChanged(Block block) {
        if (getWorld() != null && !getWorld().isRemote) {
            if (cache == null || !cache.isValid()) {
                markDirty();
                markBlockForUpdate();
            } else {
                cache.checkDirty();
            }
        }
    }

    public int getFlamability() {
        try {
            return ItemUtils.getBlockState(plank).getBlock().getFlammability(getWorld(), getPos(), null);
        } catch (Exception e) {
            return 20;
        }
    }
}
