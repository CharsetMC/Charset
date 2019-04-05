/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.module.audio.storage;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.block.BlockBase;

import javax.annotation.Nullable;

public class BlockRecordPlayer extends BlockBase implements ITileEntityProvider {
	public static final AxisAlignedBB BOX = new AxisAlignedBB(0, 0, 0, 1, 0.625f, 1);

	public BlockRecordPlayer() {
		super(Material.ROCK);
		setHardness(2.5F);
		setHarvestLevel("pickaxe", 0);
		setSoundType(SoundType.METAL);
		setTranslationKey("charset.record_player");
		setFullCube(false);
		setOpaqueCube(false);
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(Properties.FACING4, placer.getHorizontalFacing());
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return BOX;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, Properties.FACING4);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(Properties.FACING4, EnumFacing.byIndex((meta & 3) + 2));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(Properties.FACING4).ordinal() - 2;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (world != null) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileRecordPlayer) {
				return ((TileRecordPlayer) tile).activate(player, side, hand, new Vec3d(hitX, hitY, hitZ));
			}
		}

		return false;
	}

	@Override
	public void onFallenUpon(World world, BlockPos pos, Entity entityIn, float fallDistance) {
		if (world != null && !world.isRemote) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileRecordPlayer) {
				((TileRecordPlayer) tile).reactToFall(entityIn, fallDistance);
			}
		}

		super.onFallenUpon(world, pos, entityIn, fallDistance);
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileRecordPlayer();
	}
}
