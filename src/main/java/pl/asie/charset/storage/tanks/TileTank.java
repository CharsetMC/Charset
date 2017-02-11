package pl.asie.charset.storage.tanks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.FluidHandlerFluidMap;
import pl.asie.charset.lib.blocks.TileBase;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Stack;

public class TileTank extends TileBase implements IFluidHandler, IFluidTankProperties {
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

    protected TileTank bottomTank, aboveTank;
    protected static final int CAPACITY = 16000;
    protected FluidStack fluidStack;

    protected void onStackModified() {
        if (fluidStack.amount == 0)
            fluidStack = null;
        // TODO: Maybe send less often than every *change*?
        markBlockForUpdate();
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
        for (int y = pos.getY() - 1; y >= 0; y--) {
            BlockPos nPos = new BlockPos(pos.getX(), y, pos.getZ());
            TileEntity nTank = world.getTileEntity(nPos);
            if (nTank instanceof TileTank && connects((TileTank) nTank) && ((TileTank) nTank).connects(this)) {
                tank = (TileTank) nTank;
            } else {
                break;
            }
        }
        bottomTank = tank;
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
        FluidStack typeSrc = bottomTank.fluidStack;

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
        FluidStack contents = i.next().fluidStack.copy();
        if (contents != null) {
            while (i.hasNext()) {
                FluidStack c = i.next().fluidStack;
                if (c != null) {
                    contents.amount += c.amount;
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
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY ? CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this) : super.getCapability(capability, facing);
    }
}
