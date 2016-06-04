package pl.asie.charset.storage.crate;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CrateShapeCache {
    public class Handler implements IItemHandler {
        private final List<Integer> slotLookup = new ArrayList<>(getSlots());

        public Handler() {
            for (int i = 0; i < getSlots(); i++) {
                slotLookup.add(i);
            }

            Collections.shuffle(slotLookup);
        }

        @Override
        public int getSlots() {
            return crates.size() * TileEntityCrate.CRATE_SIZE;
        }

        @Override
        public ItemStack getStackInSlot(int slotPre) {
            int slotPost = slotLookup.get(slotPre);
            TileEntityCrate crate = crates.get(slotPost / TileEntityCrate.CRATE_SIZE);
            int slot = slotPost % TileEntityCrate.CRATE_SIZE;
            return crate.handler.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slotPre, ItemStack stack, boolean simulate) {
            int slotPost = slotLookup.get(slotPre);
            TileEntityCrate crate = crates.get(slotPost / TileEntityCrate.CRATE_SIZE);
            int slot = slotPost % TileEntityCrate.CRATE_SIZE;
            return crate.handler.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slotPre, int amount, boolean simulate) {
            int slotPost = slotLookup.get(slotPre);
            TileEntityCrate crate = crates.get(slotPost / TileEntityCrate.CRATE_SIZE);
            int slot = slotPost % TileEntityCrate.CRATE_SIZE;
            return crate.handler.extractItem(slot, amount, simulate);
        }
    }

    private List<TileEntityCrate> crates = new ArrayList<>();
    private Set<BlockPos> positions = new HashSet<>();
    private BlockPos posMin, posMax;
    private TileEntityCrate master;
    private boolean isValid = true;
    private boolean dirty = false;

    public CrateShapeCache() {

    }

    public CrateShapeCache(TileEntityCrate crate) {
        this();
        addRecursively(crate);
    }

    public IItemHandler getHandler() {
        return new Handler();
    }

    public void checkDirty() {
        if (dirty) {
            for (TileEntityCrate crate : crates) {
                crate.markBlockForUpdate();
            }

            dirty = false;
        }
    }

    public boolean contains(TileEntity o) {
        return crates.contains(o);
    }

    public boolean contains(BlockPos o) {
        return positions.contains(o);
    }

    public void invalidate() {
        isValid = false;
    }

    public boolean isValid() {
        if (isValid) {
            for (TileEntityCrate crate : crates) {
                if (crate.isInvalid()) {
                    isValid = false;
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean add(TileEntityCrate crate) {
        BlockPos pos = crate.getPos();
        if (master == null) {
            master = crate;
            posMin = posMax = pos;
        } else {
            if (!ItemStack.areItemStacksEqual(master.getMaterial(), crate.getMaterial())) {
                return false;
            }
        }

        if (positions.add(pos)) {
            crates.add(crate);
            if (pos.getX() <= posMin.getX() && pos.getY() <= posMin.getY() && pos.getZ() <= posMin.getZ()) {
                posMin = pos;
            }
            if (pos.getX() >= posMax.getX() && pos.getY() >= posMax.getY() && pos.getZ() >= posMax.getZ()) {
                posMax = pos;
            }
            crate.setCache(this);
            dirty = true;
            return true;
        } else {
            return false;
        }
    }

    public void addRecursively(TileEntityCrate crate) {
        if (add(crate)) {
            addNeighborsRecursively(crate);
        }
    }

    public void addNeighborsRecursively(TileEntityCrate crate) {
        for (EnumFacing facing : EnumFacing.VALUES) {
            TileEntity tile = crate.getNeighbourTile(facing);
            if (tile instanceof TileEntityCrate) {
                addRecursively((TileEntityCrate) tile);
            }
        }
    }
}
