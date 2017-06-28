package pl.asie.charset.module.storage.tanks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import pl.asie.charset.ModCharset;
import pl.asie.charset.api.lib.IDebuggable;
import pl.asie.charset.api.lib.IMovable;
import pl.asie.charset.api.lib.IMultiblockStructure;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.lib.capability.Capabilities;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public class TileTank extends TileBase implements IFluidHandler, IFluidTankProperties, IMovable, IDebuggable, IMultiblockStructure {
    public static class TankIterator implements Iterator<TileTank> {
        private TileTank currTank;

        public TankIterator(TileTank tank) {
            currTank = tank;
        }

        @Override
        public boolean hasNext() {
            return currTank != null;
        }

        @Override
        public TileTank next() {
            TileTank tank = currTank;
            currTank = currTank.getAboveTank();
            return tank;
        }
    }

    private static class BlockPosIterator implements Iterator<BlockPos> {
        private TileTank currTank;

        public BlockPosIterator(TileTank tank) {
            currTank = tank;
        }

        @Override
        public boolean hasNext() {
            return currTank != null;
        }

        @Override
        public BlockPos next() {
            TileTank tank = currTank;
            currTank = currTank.getAboveTank();
            return tank.getPos();
        }
    }

    public static boolean checkPlacementConflict(TileEntity a, TileEntity b) {
        if (a instanceof TileTank && b instanceof TileTank
                && ((TileTank) a).fluidStack != null
                && ((TileTank) b).fluidStack != null) {
            return !((TileTank) a).fluidStack.isFluidEqual(((TileTank) b).fluidStack);
        } else {
            return false;
        }
    }

    protected TileTank bottomTank, aboveTank;
    protected static final int CAPACITY = 16000;
    protected FluidStack fluidStack;

    protected void onStackModified() {
        if (fluidStack != null) {
            if (fluidStack.amount < 0)
                ModCharset.logger.warn("Tank at " + getPos() + " had negative FluidStack amount " + fluidStack.amount + "! This is a bug!");

            if (fluidStack.amount <= 0)
                fluidStack = null;
        }
        // TODO: Maybe send less often than every *change*?
        markBlockForUpdate();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        world.notifyNeighborsRespectDebug(getPos(), CharsetStorageTanks.tankBlock, false);
    }

    @Override
    public void readNBTData(NBTTagCompound compound, boolean isClient) {
        if (compound.hasKey("fluid", Constants.NBT.TAG_COMPOUND)) {
            fluidStack = FluidStack.loadFluidStackFromNBT(compound.getCompoundTag("fluid"));
        } else {
            fluidStack = null;
        }
    }

    @Override
    public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
        if (fluidStack != null) {
            NBTTagCompound fluidCpd = new NBTTagCompound();
            fluidStack.writeToNBT(fluidCpd);
            compound.setTag("fluid", fluidCpd);
        }
        return compound;
    }

    protected boolean connects(TileTank tank) {
        return true;
    }

    protected void updateAboveTank() {
        TileEntity nTank = world.getTileEntity(pos.up());
        if (nTank instanceof TileTank && connects((TileTank) nTank) && ((TileTank) nTank).connects(this)) {
            aboveTank = (TileTank) nTank;
        } else {
            aboveTank = null;
        }
    }

    protected void findBottomTank() {
        TileTank tank = this;
        Stack<TileTank> drainTanks = new Stack<TileTank>();
        for (int y = pos.getY() - 1; y >= 0; y--) {
            BlockPos nPos = new BlockPos(pos.getX(), y, pos.getZ());
            TileEntity nTank = world.getTileEntity(nPos);
            if (nTank instanceof TileTank && connects((TileTank) nTank) && ((TileTank) nTank).connects(this)) {
                tank = (TileTank) nTank;
                drainTanks.add(tank);
            } else {
                break;
            }
        }
        bottomTank = tank;

        // Shift the liquid down in case there's new tanks below
        boolean fluidStackChanged = false;
        while (!drainTanks.empty() && fluidStack != null && fluidStack.amount > 0) {
            tank = drainTanks.pop();

            if (tank.fluidStack == null) {
                tank.fluidStack = fluidStack;
                fluidStack = null;
                fluidStackChanged = true;
            } else {
                int toAdd = Math.min(TileTank.CAPACITY - tank.fluidStack.amount, fluidStack.amount);
                tank.fluidStack.amount += toAdd;
                fluidStack.amount -= toAdd;
                if (fluidStack.amount == 0) fluidStack = null;
                fluidStackChanged = true;
            }

            tank.onStackModified();
        }
        if (fluidStackChanged) onStackModified();
    }

    public Iterator<TileTank> getAllTanks() {
        return new TankIterator(getBottomTank());
    }

    public TileTank getBottomTank() {
        if (bottomTank == null || bottomTank.isInvalid()) {
            findBottomTank();
        }
        return bottomTank;
    }

    public TileTank getAboveTank() {
        if (aboveTank == null || aboveTank.isInvalid()) {
            updateAboveTank();
        }
        return aboveTank;
    }

    @Override
    public boolean hasFastRenderer() {
        return true;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return new IFluidTankProperties[]{this};
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (canFillFluidType(resource)) {
            int toFill = resource.amount;
            Iterator<TileTank> i = getAllTanks();
            while (i.hasNext() && toFill > 0) {
                TileTank tank = i.next();
                int canFill = Math.min(toFill, CAPACITY - (tank.fluidStack == null ? 0 : tank.fluidStack.amount));
                if (doFill) {
                    if (tank.fluidStack == null) {
                        tank.fluidStack = new FluidStack(resource, canFill);
                    } else {
                        tank.fluidStack.amount += canFill;
                    }
                    tank.onStackModified();
                }
                toFill -= canFill;
            }
            return resource.amount - toFill;
        } else {
            return 0;
        }
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        if (canDrainFluidType(resource)) {
            return drain(resource.amount, doDrain);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        int toDrain = maxDrain;
        FluidStack typeSrc = getBottomTank().fluidStack;
        if (typeSrc == null) {
            return null;
        }

        Stack<TileTank> drainTanks = new Stack<TileTank>();
        Iterator<TileTank> i = getAllTanks();
        while (i.hasNext()) {
            TileTank tank = i.next();
            if (tank.fluidStack == null)
                break;
            drainTanks.add(tank);
        }

        TileTank tank;
        while (!drainTanks.empty() && toDrain > 0) {
            tank = drainTanks.pop();
            int canDrain = Math.min(toDrain, tank.fluidStack.amount);
            if (doDrain) {
                tank.fluidStack.amount -= canDrain;
                tank.onStackModified();
            }
            toDrain -= canDrain;
        }
        return new FluidStack(typeSrc, maxDrain - toDrain);
    }

    @Nullable
    @Override
    public FluidStack getContents() {
        Iterator<TileTank> i = getAllTanks();
        FluidStack contents = i.next().fluidStack;
        if (contents != null) {
            contents = contents.copy();
            while (i.hasNext()) {
                FluidStack c = i.next().fluidStack;
                if (c != null) {
                    contents.amount += c.amount;
                } else {
                    break;
                }
            }
        }
        return contents;
    }

    @Override
    public int getCapacity() {
        int c = 0;
        Iterator<TileTank> i = getAllTanks();
        while (i.hasNext()) {
            c += CAPACITY;
            i.next();
        }
        return c;
    }

    @Override
    public boolean canFill() {
        return getBottomTank().fluidStack == null;
    }

    @Override
    public boolean canDrain() {
        return getBottomTank().fluidStack != null;
    }

    @Override
    public boolean canFillFluidType(FluidStack stack) {
        getBottomTank();
        return bottomTank.fluidStack == null || bottomTank.fluidStack.isFluidEqual(stack);
    }

    @Override
    public boolean canDrainFluidType(FluidStack stack) {
        getBottomTank();
        return bottomTank.fluidStack != null && bottomTank.fluidStack.isFluidEqual(stack);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return true;
        } else if (capability == Capabilities.DEBUGGABLE) {
            return true;
        } else if (capability == Capabilities.MOVABLE) {
            return true;
        } else if (capability == Capabilities.MULTIBLOCK_STRUCTURE) {
            return true;
        } else {
            return super.hasCapability(capability, facing);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || capability == Capabilities.DEBUGGABLE
                || capability == Capabilities.MOVABLE || capability == Capabilities.MULTIBLOCK_STRUCTURE) {
            return (T) this;
        } else {
            return super.getCapability(capability, facing);
        }
    }

    private String dbgFluidToString(FluidStack stack) {
        return stack == null ? TextFormatting.ITALIC + "<empty>" : stack.getLocalizedName() + " x " + stack.amount;
    }

    @Override
    public void addDebugInformation(List<String> stringList, Side side) {
        stringList.add("Global: " + dbgFluidToString(getContents()) + TextFormatting.RESET + "/" + getCapacity());
        stringList.add("Local: " + dbgFluidToString(fluidStack) + TextFormatting.RESET + "/" + CAPACITY);
    }

    @Override
    public boolean canMoveFrom() {
        return true;
    }

    @Override
    public boolean canMoveTo(World world, BlockPos pos) {
        return !(checkPlacementConflict(this, world.getTileEntity(pos.up())) || checkPlacementConflict(this, world.getTileEntity(pos.down())));
    }

    @Override
    public Iterator<BlockPos> iterator() {
        return new BlockPosIterator(getBottomTank());
    }

    @Override
    public boolean contains(BlockPos pos) {
        if (pos.getX() == getPos().getX() && pos.getZ() == getPos().getZ() && pos.getY() >= getBottomTank().getPos().getY()) {
            TileTank tank = this;
            if (pos.getY() <= tank.getPos().getY()) {
                return true;
            }

            while ((tank = tank.getAboveTank()) != null) {
                if (pos.getY() <= tank.getPos().getY()) {
                    return true;
                }
            }
        }

        return false;
    }
}
