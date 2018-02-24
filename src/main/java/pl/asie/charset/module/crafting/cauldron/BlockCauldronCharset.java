/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
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

package pl.asie.charset.module.crafting.cauldron;

import net.minecraft.block.BlockCauldron;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import pl.asie.charset.lib.notify.Notice;
import pl.asie.charset.lib.utils.FluidUtils;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.module.crafting.cauldron.api.CauldronContents;
import pl.asie.charset.module.crafting.cauldron.api.ICauldronRecipe;
import pl.asie.charset.module.crafting.cauldron.api.ICauldron;

import javax.annotation.Nullable;
import java.util.Optional;

public class BlockCauldronCharset extends BlockCauldron implements ITileEntityProvider {
	protected static final AxisAlignedBB AABB_INSIDE = new AxisAlignedBB(0.125D, 0.3125D, 0.125D, 0.875D, 1.0D, 0.875D);

	private boolean isEmptyOrWater(IBlockAccess access, BlockPos pos) {
		TileEntity tile = access.getTileEntity(pos);
		if (tile instanceof TileCauldronCharset) {
			return ((TileCauldronCharset) tile).isEmptyOrWater();
		} else {
			return true;
		}
	}

	protected void onVanillaMethodCalled(IBlockAccess access, BlockPos pos, IBlockState state) {
		TileEntity tile = access.getTileEntity(pos);
		if (tile instanceof TileCauldronCharset) {
			((TileCauldronCharset) tile).rebuildFromVanillaLevel(state);
		}
	}

	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		if (!worldIn.isRemote) {
			TileEntity tile = worldIn.getTileEntity(pos);
			if (tile instanceof TileCauldronCharset) {
				FluidStack stack = ((TileCauldronCharset) tile).getContents();
				float height = (float) pos.getY() + ((TileCauldronCharset) tile).getFluidHeight() / 16.0F;

				if (stack != null && stack.amount > 0 && entityIn.getEntityBoundingBox().minY <= height) {
					if (stack.getFluid() == FluidRegistry.LAVA) {
						if (!entityIn.isBurning() && stack.amount >= 100 && !entityIn.isImmuneToFire()) {
							entityIn.setFire(stack.amount / 100);
							return;
						}
					} else if (stack.getFluid() == FluidRegistry.WATER) {
						if (entityIn.isBurning() && stack.amount >= 250) {
							entityIn.extinguish();
							((TileCauldronCharset) tile).drain(250, true);
							return;
						}
					}

					if (entityIn instanceof EntityItem) {
						EntityItem entityItem = (EntityItem) entityIn;
						ItemStack heldItem = entityItem.getItem();
						if (!heldItem.isEmpty()) {
							ItemStack heldItemOne = heldItem.copy();
							heldItemOne.setCount(1);
							Optional<CauldronContents> contentsNew = Optional.empty();

							Optional<ItemStack> fluidResult = FluidUtils.handleTank((IFluidHandler) tile, stack, worldIn, pos, heldItemOne, false, true, false);
							if (fluidResult.isPresent()) {
								contentsNew = Optional.of(new CauldronContents(((TileCauldronCharset) tile).getContents(), fluidResult.get()));
							}

							if (!contentsNew.isPresent()) {
								contentsNew = CharsetCraftingCauldron.craft((ICauldron) tile, new CauldronContents(CauldronContents.Source.ENTITY, stack, heldItemOne));
							}

							if (contentsNew.isPresent()) {
								CauldronContents cc = contentsNew.get();
								if (cc.hasResponse()) {
									new Notice(tile, cc.getResponse()).sendToAll();
								} else {
									if (cc.getHeldItem().isEmpty()) {
										heldItem.shrink(1);
									} else if (cc.getHeldItem().getCount() == 1 && ItemUtils.canMerge(cc.getHeldItem(), heldItem)) {
										// pass
									} else {
										heldItem.shrink(1);
										ItemUtils.spawnItemEntity(
												worldIn,
												entityItem.getPositionVector(),
												cc.getHeldItem(),
												0, 0, 0, 0
										);
									}

									((TileCauldronCharset) tile).setContents(cc.getFluidStack());
								}
							}
						}
					}
				}
			}
		}
	}

	private void notice(World worldIn, TileEntity tankEntity, EntityPlayer playerIn) {
		if (!worldIn.isRemote) {
			if (tankEntity instanceof TileCauldronCharset) {
				FluidStack stack = ((TileCauldronCharset) tankEntity).getContents();
				if (stack == null) {
					new Notice(tankEntity, new TextComponentTranslation("notice.charset.cauldron.empty")).sendTo(playerIn);
				} else {
					new Notice(tankEntity, new TextComponentTranslation("notice.charset.cauldron.fluid",
							new TextComponentString(Integer.toString(stack.amount)),
							new TextComponentTranslation(FluidUtils.getCorrectUnlocalizedName(stack))
					)).sendTo(playerIn);
				}
			}
		}
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		TileEntity tankEntity = worldIn.getTileEntity(pos);
		ItemStack heldItem = playerIn.getHeldItem(hand);

		if (heldItem.isEmpty()) {
			notice(worldIn, tankEntity, playerIn);
			return true;
		}

		if (isEmptyOrWater(worldIn, pos)) {
			if (super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ)) {
				return true;
			}
		}

		if (hand != EnumHand.MAIN_HAND) {
			return false;
		}

		if (tankEntity instanceof TileCauldronCharset) {
			if (FluidUtils.handleTank((IFluidHandler) tankEntity, ((TileCauldronCharset) tankEntity).getContents(), worldIn, pos, playerIn, hand)) {
				return true;
			}

			if (!heldItem.isEmpty()) {
				FluidStack stack = ((TileCauldronCharset) tankEntity).getContents();
				ItemStack heldItemOne = heldItem.copy();
				heldItemOne.setCount(1);
				Optional<CauldronContents> contentsNew = CharsetCraftingCauldron.craft((ICauldron) tankEntity, new CauldronContents(CauldronContents.Source.HAND, stack, heldItemOne));

				if (contentsNew.isPresent()) {
					if (!worldIn.isRemote) {
						boolean success = false;
						CauldronContents cc = contentsNew.get();
						if (cc.hasResponse()) {
							new Notice(tankEntity, cc.getResponse()).sendTo(playerIn);
						} else {
							if (cc.getHeldItem().isEmpty()) {
								if (!playerIn.isCreative()) {
									heldItem.shrink(1);
								}
								success = true;
							} else if (cc.getHeldItem().getCount() == 1 && ItemUtils.canMerge(cc.getHeldItem(), heldItem)) {
								success = true;
							} else if (heldItem.getCount() > 1) {
								if (playerIn.inventory.addItemStackToInventory(cc.getHeldItem())) {
									heldItem.shrink(1);
									success = true;
								}
							} else if (heldItem.getCount() == 1) {
								playerIn.setHeldItem(hand, cc.getHeldItem());
								success = true;
							}

							if (success) {
								((TileCauldronCharset) tankEntity).setContents(cc.getFluidStack());
							}
						}
					}

					return true;
				}
			}
		}

		if (!playerIn.isSneaking()) {
			notice(worldIn, tankEntity, playerIn);
			return true;
		}

		return false;
	}

	@Override
	public void fillWithRain(World worldIn, BlockPos pos) {
		float f = worldIn.getBiome(pos).getTemperature(pos);

		if (worldIn.getBiomeProvider().getTemperatureAtHeight(f, pos.getY()) >= 0.15F) {
			TileEntity tile = worldIn.getTileEntity(pos);
			if (tile instanceof TileCauldronCharset) {
				FluidStack stack = ((TileCauldronCharset) tile).getContents();
				if (stack == null || stack.getFluid() == FluidRegistry.WATER || stack.getFluid() == CharsetCraftingCauldron.dyedWater) {
					if (stack == null || stack.amount < 667) {
						((TileCauldronCharset) tile).fill(new FluidStack(FluidRegistry.WATER, Math.min(40, 667 - (stack == null ? 0 : stack.amount))), true);
					}
				}
			}
		}
	}

	@Override
	public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof TileCauldronCharset) {
			return ((TileCauldronCharset) tile).getComparatorValue();
		} else {
			return blockState.getValue(LEVEL);
		}
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		// Fluid information is now stored in the tile entity.
		return 0;
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileCauldronCharset();
	}
}
