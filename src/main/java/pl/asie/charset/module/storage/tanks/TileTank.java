/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.charset.module.storage.tanks;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
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
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.ModCharset;
import pl.asie.charset.api.lib.ICacheable;
import pl.asie.charset.api.lib.IDebuggable;
import pl.asie.charset.api.lib.IMovable;
import pl.asie.charset.api.lib.IMultiblockStructure;
import pl.asie.charset.lib.CharsetLib;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.scheduler.Scheduler;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public class TileTank extends TileBase implements IFluidHandler, IFluidTankProperties, IMovable, IDebuggable, IMultiblockStructure, ICacheable {
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

    public static boolean checkPlacementConflict(TileEntity a, TileEntity b, int variant) {
        if (a instanceof TileTank && b instanceof TileTank
                && ((TileTank) a).getBottomTank().fluidStack != null
                && ((TileTank) b).getBottomTank().fluidStack != null
                && ((TileTank) a).variant == variant
                && ((TileTank) a).variant == ((TileTank) b).variant) {
            return !((TileTank) a).getBottomTank().fluidStack.isFluidEqual(((TileTank) b).getBottomTank().fluidStack);
        } else {
            return false;
        }
    }

    protected TileTank bottomTank, aboveTank;
    protected static final int CAPACITY = 16000;
    protected FluidStack fluidStack;
    private int variant;

    public int getVariant() {
        return variant;
    }

    public boolean setVariant(int variant) {
        if (variant >= 0 && variant < 17 && this.variant != variant) {
            TileEntity tUp = world.getTileEntity(pos.up());
            TileEntity tDown = world.getTileEntity(pos.down());
            if (!(checkPlacementConflict(this, tUp, variant) || checkPlacementConflict(this, tDown, variant) || checkPlacementConflict(tUp, tDown, variant))) {
                this.variant = variant;
                markBlockForUpdate();
                world.notifyNeighborsRespectDebug(pos, CharsetStorageTanks.tankBlock, false);
                return true;
            }
        }

        return false;
    }

    @Override
    public ItemStack getDroppedBlock(IBlockState state) {
        return new ItemStack(state.getBlock(), 1, variant);
    }

    @Override
    public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
        variant = stack.getItemDamage() % 17;
    }

    protected void onTankStructureChanged() {
        updateAboveTank();
        BlockPos tankPos = getPos();
        TileEntity tankEntity = this;

        while (tankEntity instanceof TileTank) {
            ((TileTank) tankEntity).findBottomTank();
            tankPos = tankPos.up();
            tankEntity =  getWorld().getTileEntity(tankPos);
        }

        onStackModified();
        BlockPos below = getPos().down();
        TileEntity belowEntity = world.getTileEntity(below);
        if (belowEntity instanceof TileTank) {
            ((TileTank) belowEntity).getBottomTank().onStackModified();
        }
    }

    protected void onStackModified() {
        if (fluidStack != null) {
            if (fluidStack.amount < 0)
                ModCharset.logger.warn("Tank at " + getPos() + " had negative FluidStack amount " + fluidStack.amount + "! This is a bug!");

            if (fluidStack.amount <= 0)
                fluidStack = null;
        }

        Iterator<TileTank> tankIterator = getAllTanks();
        while (tankIterator.hasNext()) {
            tankIterator.next().updateComparators();
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
    public void validate() {
        super.validate();
        Scheduler.INSTANCE.in(getWorld(), 1, this::updateComparators);
    }

    @Override
    public void readNBTData(NBTTagCompound compound, boolean isClient) {
        variant = compound.getByte("variant");

        if (compound.hasKey("fluid", Constants.NBT.TAG_COMPOUND)) {
            fluidStack = FluidStack.loadFluidStackFromNBT(compound.getCompoundTag("fluid"));
        } else {
            fluidStack = null;
        }
    }

    @Override
    public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
        compound.setByte("variant", (byte) variant);
        if (fluidStack != null) {
            NBTTagCompound fluidCpd = new NBTTagCompound();
            fluidStack.writeToNBT(fluidCpd);
            compound.setTag("fluid", fluidCpd);
        }
        return compound;
    }

    protected boolean connects(TileTank tank) {
        return tank.getVariant() == getVariant();
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
        if (getWorld() == null)
            return this;

        if (bottomTank == null || bottomTank.isInvalid()) {
            findBottomTank();
        }
        return bottomTank;
    }

    public TileTank getAboveTank() {
        if (getWorld() == null)
            return null;

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
        if (getWorld() == null)
            return fluidStack;

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
        if (getWorld() == null)
            return CAPACITY;

        int c = 0;
        Iterator<TileTank> i = getAllTanks();
        while (i.hasNext()) {
            c += CAPACITY;
            i.next();
        }
        return c;
    }

    @Override
    public int getComparatorValue() {
        if (getWorld() == null)
            return 0;

        if (getBottomTank() != this)
            return getBottomTank().getComparatorValue();

        FluidStack contents = getContents();
        if (contents == null || contents.amount <= 0)
            return 0;

        return Math.max(1, contents.amount * 15 / getCapacity());
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
        if (stack == null) {
            return false;
        }
        getBottomTank();
        return bottomTank.fluidStack == null || bottomTank.fluidStack.isFluidEqual(stack);
    }

    @Override
    public boolean canDrainFluidType(FluidStack stack) {
        if (stack == null) {
            return false;
        }
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
        if (side == Side.SERVER && world != null) {
            stringList.add("Bottom: " + getBottomTank().getPos());
        }

        stringList.add("Global: " + dbgFluidToString(getContents()) + TextFormatting.RESET + "/" + getCapacity());
        stringList.add("Local: " + dbgFluidToString(fluidStack) + TextFormatting.RESET + "/" + CAPACITY);
    }

    @Override
    public boolean canMoveFrom() {
        return true;
    }

    @Override
    public boolean canMoveTo(World world, BlockPos pos) {
        TileEntity tUp = world.getTileEntity(pos.up());
        TileEntity tDown = world.getTileEntity(pos.down());
        return !(checkPlacementConflict(this, tUp, variant) || checkPlacementConflict(this, tDown, variant) || checkPlacementConflict(tUp, tDown, variant));
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

    @Override
    public boolean isCacheValid() {
        return !isInvalid();
    }

    @Override
    public net.minecraft.util.math.AxisAlignedBB getRenderBoundingBox() {
        TileTank bottomTank = getBottomTank();
        if (bottomTank != this) {
            return new AxisAlignedBB(getPos());
        } else {
            BlockPos bottomPos = getBottomTank().getPos();
            int height = getCapacity() / TileTank.CAPACITY;

            return new AxisAlignedBB(
                    bottomPos.getX(),
                    bottomPos.getY(),
                    bottomPos.getZ(),
                    bottomPos.getX() + 1,
                    bottomPos.getY() + height + 1,
                    bottomPos.getZ() + 1
            );
        }
    }
}
