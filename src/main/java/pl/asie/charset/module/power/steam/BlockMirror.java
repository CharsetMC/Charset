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

package pl.asie.charset.module.power.steam;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.block.BlockBase;
import pl.asie.charset.lib.item.ISubItemProvider;
import pl.asie.charset.lib.item.SubItemProviderCache;
import pl.asie.charset.lib.item.SubItemProviderRecipes;
import pl.asie.charset.lib.item.SubItemSetHelper;
import pl.asie.charset.module.power.mechanical.CharsetPowerMechanical;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class BlockMirror extends BlockBase implements ITileEntityProvider {
	public static final int ROTATIONS = 32;
	public static final PropertyInteger ROT_PROP = PropertyInteger.create("rotation", 0, ROTATIONS); // max = no rotation

	public BlockMirror() {
		super(Material.IRON);
		setHardness(0.5F); // TODO
		setOpaqueCube(false);
		setFullCube(false);
		setTickRandomly(true);
		setTranslationKey("charset.solar_mirror");
		setDefaultState(getDefaultState().withProperty(ROT_PROP, ROTATIONS));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	protected ISubItemProvider createSubItemProvider() {
		return new SubItemProviderCache(new SubItemProviderRecipes(() -> CharsetPowerSteam.itemMirror) {
			@Override
			protected int compareSets(List<ItemStack> first, List<ItemStack> second) {
				return SubItemSetHelper.wrapLists(first, second, SubItemSetHelper.extractMaterial("material", SubItemSetHelper::sortByItem));
			}
		});
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileMirror) {
			Optional<BlockPos> targetPos = ((TileMirror) tile).getMirrorTargetPos();
			if (targetPos.isPresent()) {
				return state.withProperty(ROT_PROP,
						(int) ((MathHelper.atan2(
								targetPos.get().getX() - pos.getX(),
								targetPos.get().getZ() - pos.getZ()
						) + Math.PI) * ROTATIONS / Math.PI / 2) % ROTATIONS
				);
			}
		}

		return state;
	}

	@Override
	public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random) {
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof TileMirror) {
			((TileMirror) tile).findTarget();
		}
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, ROT_PROP);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState();
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileMirror();
	}

	@Override
	public int getParticleTintIndex() {
		return 0;
	}
}
