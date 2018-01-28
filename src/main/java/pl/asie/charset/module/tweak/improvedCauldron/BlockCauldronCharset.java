package pl.asie.charset.module.tweak.improvedCauldron;

import net.minecraft.block.BlockCauldron;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import pl.asie.charset.lib.utils.FluidUtils;
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
		if (isEmptyOrWater(worldIn, pos)) {
			if (super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ)) {
				return true;
			}
		}
		if (hand != EnumHand.MAIN_HAND)
			return false;

		TileEntity tankEntity = worldIn.getTileEntity(pos);
		if (tankEntity instanceof TileCauldronCharset) {
			return FluidUtils.handleTank((IFluidHandler) tankEntity, ((TileCauldronCharset) tankEntity).getContents(), worldIn, pos, playerIn, hand);
		} else {
			return false;
		}
	}

	@Override
	public void fillWithRain(World worldIn, BlockPos pos) {
		if (isEmptyOrWater(worldIn, pos)) {
			super.fillWithRain(worldIn, pos);
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
