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

package pl.asie.charset.module.experiments.projector;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import pl.asie.charset.lib.block.BlockBase;
import pl.asie.charset.lib.utils.Orientation;
import pl.asie.charset.lib.utils.Quaternion;
import pl.asie.charset.lib.utils.SpaceUtils;
import pl.asie.charset.lib.utils.UnlistedPropertyGeneric;
import pl.asie.charset.module.storage.barrels.TileEntityDayBarrel;

import javax.annotation.Nullable;

public class BlockProjector extends BlockBase implements ITileEntityProvider {
	public static final IUnlistedProperty<ProjectorCacheInfo> INFO = new UnlistedPropertyGeneric<>("info", ProjectorCacheInfo.class);
	private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0, 0, 0, 1, 0.75, 1);
	private static final AxisAlignedBB BOUNDING_BOX_ROTATE = new AxisAlignedBB(-0.25, -0.5, -0.5, 0.5, 0.5, 0.5);

	@Override
	protected BlockStateContainer createBlockState() {
		return new ExtendedBlockState(this, new IProperty[]{}, new IUnlistedProperty[]{INFO});
	}

	public BlockProjector() {
		super(Material.CIRCUITS);
		setHardness(2.5F);
		setHarvestLevel("pickaxe", 0);
		setSoundType(SoundType.METAL);
		setUnlocalizedName("charset.projector");
		setFullCube(false);
		setOpaqueCube(false);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		AxisAlignedBB base = BOUNDING_BOX;

		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileProjector) {
			Quaternion quaternion = Quaternion.fromOrientation(((TileProjector) tile).getOrientation());
			base = quaternion.applyRotation(BOUNDING_BOX_ROTATE).offset(0.5, 0.5, 0.5);
		}

		return base;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (world == null || world.isRemote) {
			return true;
		}

		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileProjector) {
			return ((TileProjector) tile).activate(player, side, hand);
		}

		return false;
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileProjector projector = (TileProjector) world.getTileEntity(pos);
		IExtendedBlockState extendedBS = (IExtendedBlockState) super.getExtendedState(state, world, pos);
		if (projector != null) {
			return extendedBS.withProperty(INFO, ProjectorCacheInfo.from(projector));
		} else {
			return extendedBS;
		}
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileProjector();
	}
}
