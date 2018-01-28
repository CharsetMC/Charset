package pl.asie.charset.module.tweak.improvedCauldron;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.BlockSponge;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import pl.asie.charset.lib.capability.CapabilityHelper;
import pl.asie.charset.lib.notify.Notice;
import pl.asie.charset.lib.utils.ColorUtils;
import pl.asie.charset.lib.utils.FluidUtils;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.RecipeUtils;
import pl.asie.charset.module.storage.tanks.TileTank;

import javax.annotation.Nullable;

public class BlockCauldronCharset extends BlockCauldron implements ITileEntityProvider {
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

				if (stack != null && stack.amount > 0) {
					if (stack.getFluid() == FluidRegistry.LAVA) {
						if (!entityIn.isBurning() && stack.amount >= 100 && !entityIn.isImmuneToFire() && entityIn.getEntityBoundingBox().minY <= height) {
							entityIn.setFire(stack.amount / 100);
						}
					} else if (stack.getFluid() == FluidRegistry.WATER) {
						if (entityIn.isBurning() && stack.amount >= 250 && entityIn.getEntityBoundingBox().minY <= height) {
							entityIn.extinguish();
							((TileCauldronCharset) tile).drain(250, true);
						}
					}
				}
			}
		}
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		TileEntity tankEntity = worldIn.getTileEntity(pos);
		ItemStack heldItem = playerIn.getHeldItem(hand);
		EnumDyeColor color = ColorUtils.getDyeColor(heldItem);

		if (color != null) {
			if (tankEntity instanceof TileCauldronCharset) {
				FluidStack stack = ((TileCauldronCharset) tankEntity).getContents();
				if (stack != null) {
					if (stack.getFluid() == FluidRegistry.WATER || stack.getFluid() == CharsetTweakImprovedCauldron.dyedWater) {
						if (!worldIn.isRemote) {
							FluidStack newStack = CharsetTweakImprovedCauldron.dyedWater.appendDye(stack, color);
							if (newStack == null) {
								new Notice(tankEntity, new TextComponentTranslation("notice.charset.cauldron.no_dye")).sendTo(playerIn);
							} else {
								((TileCauldronCharset) tankEntity).setContents(newStack);
								if (!playerIn.isCreative()) {
									heldItem.shrink(1);
								}
							}
						}

						return true;
					}
				}
			}
		}

		if (isEmptyOrWater(worldIn, pos)) {
			if (super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ)) {
				return true;
			}
		}

		if (hand != EnumHand.MAIN_HAND)
			return false;

		if (tankEntity instanceof TileCauldronCharset) {
			if (FluidUtils.handleTank((IFluidHandler) tankEntity, ((TileCauldronCharset) tankEntity).getContents(), worldIn, pos, playerIn, hand)) {
				return true;
			}

			FluidStack stack = ((TileCauldronCharset) tankEntity).getContents();
			if (stack != null) {
				if (stack.getFluid() == CharsetTweakImprovedCauldron.dyedWater) {
					if (heldItem.getItem() instanceof ItemBlock && Block.getBlockFromItem(heldItem.getItem()) instanceof BlockSponge) {
						if (!worldIn.isRemote) {
							((TileCauldronCharset) tankEntity).setContents(new FluidStack(FluidRegistry.WATER, stack.amount));
						}
						return true;
					}

					if (stack.tag != null
						&& stack.amount >= 250
						&& stack.tag.hasKey("dyes", Constants.NBT.TAG_LIST)
						&& !heldItem.isEmpty()) {
						if (!worldIn.isRemote) {
							NBTTagList dyes = (NBTTagList) stack.tag.getTag("dyes");
							ItemStack[] stacks = new ItemStack[9];
							stacks[0] = heldItem.copy();
							stacks[0].setCount(1);
							for (int i = 0; i < 8; i++) {
								if (i < dyes.tagCount()) {
									stacks[i + 1] = new ItemStack(Items.DYE, 1, 15 - ((NBTPrimitive) dyes.get(i)).getByte());
								} else {
									stacks[i + 1] = ItemStack.EMPTY;
								}
							}

							ItemStack result = RecipeUtils.getCraftingResult(worldIn, 3, 3, stacks);
							if (!result.isEmpty() && !ItemUtils.canMerge(stacks[0], result)) {
								boolean success = false;

								if (heldItem.getCount() == 1) {
									playerIn.setHeldItem(hand, result);
									success = true;
								} else {
									if (playerIn.inventory.addItemStackToInventory(result)) {
										heldItem.shrink(1);
										success = true;
									}
								}

								if (success) {
									((TileCauldronCharset) tankEntity).drain(250, true);
								}
							}
						}
					}

					return true;
				}
			}
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
				if (stack == null || stack.getFluid() == FluidRegistry.WATER || stack.getFluid() == CharsetTweakImprovedCauldron.dyedWater) {
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
