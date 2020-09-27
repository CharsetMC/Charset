/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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

package pl.asie.charset.lib.utils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import pl.asie.charset.lib.capability.CapabilityHelper;
import pl.asie.charset.module.storage.tanks.TileTank;

import javax.annotation.Nullable;
import java.util.Optional;

public final class FluidUtils {
	public static String getCorrectLocalizedName(FluidStack stack) {
	    if (stack.getFluid() == FluidRegistry.WATER) {
	        return I18n.translateToLocal("tile.water.name");
        } else if (stack.getFluid() == FluidRegistry.LAVA) {
	        return I18n.translateToLocal("tile.lava.name");
        } else {
	        return stack.getLocalizedName();
        }
	}

	public interface IFluidHandlerAutomationDetecting extends IFluidHandler {
        @Nullable
        FluidStack drain(int maxDrain, boolean doDrain, boolean isAutomated);
        @Nullable
        FluidStack drain(FluidStack resource, boolean doDrain, boolean isAutomated);
    }

    private FluidUtils() {

    }

    private static FluidStack drain(IFluidHandler handler, int maxDrain, boolean doDrain, boolean isAutomated) {
        if (handler instanceof IFluidHandlerAutomationDetecting) {
            return ((IFluidHandlerAutomationDetecting) handler).drain(maxDrain, doDrain, isAutomated);
        } else {
            return handler.drain(maxDrain, doDrain);
        }
    }

    private static FluidStack drain(IFluidHandler handler, FluidStack maxDrain, boolean doDrain, boolean isAutomated) {
        if (handler instanceof IFluidHandlerAutomationDetecting) {
            return ((IFluidHandlerAutomationDetecting) handler).drain(maxDrain, doDrain, isAutomated);
        } else {
            return handler.drain(maxDrain, doDrain);
        }
    }

    public static Optional<ItemStack> handleTank(IFluidHandler tank, FluidStack fluidContained, World worldIn, BlockPos pos, ItemStack stack, boolean isCreative, boolean drainTank, boolean fillTank) {
        IFluidHandlerItem handler = CapabilityHelper.get(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, stack, null);
        if (handler != null) {
            if (!worldIn.isRemote) {
                boolean changed = false;

                FluidStack fluidExtracted;
                if (fluidContained != null) {
                    FluidStack f = fluidContained.copy();
                    f.amount = Fluid.BUCKET_VOLUME;
                    fluidExtracted = handler.drain(f, false);
                } else {
                    fluidExtracted = handler.drain(Fluid.BUCKET_VOLUME, false);
                }

                if (fluidExtracted == null) {
                    if (drainTank) {
                        // tank -> holder
                        fluidExtracted = drain(tank, Fluid.BUCKET_VOLUME, false, false);
                        if (fluidExtracted != null) {
                            int amount = handler.fill(fluidExtracted, false);
                            if (amount > 0) {
                                fluidExtracted.amount = amount;
                                fluidExtracted = drain(tank, fluidExtracted, true, false);
                                if (fluidExtracted != null) {
                                    handler.fill(fluidExtracted, !isCreative);
                                    changed = true;
                                }
                            }
                        }
                    }
                } else {
                    // holder -> tank
                    if (fillTank) {
                        int amount = tank.fill(fluidExtracted, false);
                        if (amount > 0) {
                            fluidExtracted.amount = amount;
                            fluidExtracted = handler.drain(fluidExtracted, !isCreative);
                            if (fluidExtracted != null) {
                                tank.fill(fluidExtracted, true);
                                changed = true;
                            }
                        }
                    }
                }

                if (changed) {
                    return Optional.of(handler.getContainer());
                }
            }

            return Optional.of(stack);
        }

        return Optional.empty();

    }

    public static boolean handleTank(IFluidHandler tank, FluidStack fluidContained, World worldIn, BlockPos pos, EntityPlayer playerIn, EnumHand hand) {
        return handleTank(tank, fluidContained, worldIn, pos, playerIn, hand, false, false);
    }

    public static boolean handleTank(IFluidHandler tank, FluidStack fluidContained, World worldIn, BlockPos pos, EntityPlayer playerIn, EnumHand hand, boolean iterateAllDrain, boolean iterateAllFill) {
	    // ugly repetition
	    if (iterateAllDrain || iterateAllFill) {
	        boolean changed = false;
	        for (int i = 0; i < playerIn.inventory.mainInventory.size(); i++) {
                if (!iterateAllFill && tank.drain(1, false) == null) {
                    break;
                }

                ItemStack stack = playerIn.inventory.mainInventory.get(i);
                if (!stack.isEmpty()) {
                    if (stack.getCount() == 1) {
                        Optional<ItemStack> result = handleTank(tank, fluidContained, worldIn, pos, stack, playerIn.isCreative(), iterateAllDrain, iterateAllFill);
                        if (result.isPresent()) {
                            ItemStack resultStack = result.get();
                            if (resultStack != stack) {
                                playerIn.inventory.mainInventory.set(i, resultStack);
                            }

                            changed = true;
                        }
                    } else {
                        ItemStack stackOne = stack.copy();
                        stackOne.setCount(1);

                        Optional<ItemStack> result = handleTank(tank, fluidContained, worldIn, pos, stackOne, playerIn.isCreative(), iterateAllDrain, iterateAllFill);
                        if (result.isPresent()) {
                            ItemStack resultStack = result.get();
                            stack.shrink(1);
                            if (!playerIn.inventory.addItemStackToInventory(resultStack)) {
                                ItemUtils.spawnItemEntity(worldIn, playerIn.getPositionVector(), resultStack, 0, 0, 0, 0);
                                // no more!
                                return changed;
                            }

                            changed = true;
                        }
                    }
                }
            }
            return changed;
        } else {
            ItemStack stack = playerIn.getHeldItem(hand);
            if (stack.isEmpty()) {
                return false;
            } else if (stack.getCount() == 1) {
                Optional<ItemStack> result = handleTank(tank, fluidContained, worldIn, pos, stack, playerIn.isCreative(), true, true);
                if (result.isPresent()) {
                    ItemStack resultStack = result.get();
                    if (resultStack != stack) {
                        playerIn.setHeldItem(hand, resultStack);
                    }

                    return true;
                } else {
                    return false;
                }
            } else if (stack.getCount() > 1) {
                ItemStack stackOne = stack.copy();
                stackOne.setCount(1);

                Optional<ItemStack> result = handleTank(tank, fluidContained, worldIn, pos, stackOne, playerIn.isCreative(), true, true);
                if (result.isPresent()) {
                    ItemStack resultStack = result.get();
                    stack.shrink(1);
                    if (!playerIn.inventory.addItemStackToInventory(resultStack)) {
                        ItemUtils.spawnItemEntity(worldIn, playerIn.getPositionVector(), resultStack, 0, 0, 0, 0);
                    }

                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    public static boolean matches(IFluidHandler handler, FluidStack stack) {
        IFluidTankProperties[] properties = handler.getTankProperties();
        if (properties != null) for (IFluidTankProperties property : properties) {
            FluidStack match = property.getContents();
            if (match != null && match.isFluidEqual(stack)) {
                return true;
            }
        }

        return false;
    }

    public static int push(IFluidHandler from, IFluidHandler to, int amt) {
        if (amt > 0) {
            FluidStack drained = from.drain(amt, false);
            if (drained != null && drained.amount > 0) {
                amt = to.fill(drained, true);
                if (amt > 0) {
                    FluidStack toDrain = drained.copy();
                    toDrain.amount = amt;
                    FluidStack drainedReal = from.drain(toDrain, true);
                    return drainedReal != null ? drainedReal.amount : 0;
                }
            }
        }

        return 0;
    }

    public static int push(IFluidHandler from, IFluidHandler to, FluidStack pushed) {
        if (pushed != null && pushed.amount > 0) {
            FluidStack drained = from.drain(pushed, false);
            if (drained != null && drained.amount > 0) {
                int amt = to.fill(drained, true);
                if (amt > 0) {
                    FluidStack toDrain = drained;
                    toDrain.amount = amt;
                    FluidStack drainedReal = from.drain(toDrain, true);
                    return drainedReal != null ? drainedReal.amount : 0;
                }
            }
        }

        return 0;
    }
}
