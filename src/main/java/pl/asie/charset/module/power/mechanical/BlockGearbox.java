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

package pl.asie.charset.module.power.mechanical;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.api.experimental.mechanical.IMechanicalPowerConsumer;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.block.BlockBase;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.capability.CapabilityHelper;
import pl.asie.charset.lib.item.ISubItemProvider;
import pl.asie.charset.lib.item.SubItemProviderCache;
import pl.asie.charset.lib.item.SubItemProviderRecipes;
import pl.asie.charset.lib.item.SubItemSetHelper;
import pl.asie.charset.lib.utils.Orientation;
import pl.asie.charset.lib.utils.UnlistedPropertyGeneric;
import pl.asie.charset.module.power.mechanical.render.GearboxCacheInfo;

import javax.annotation.Nullable;
import java.util.List;

public class BlockGearbox extends BlockBase implements ITileEntityProvider {
	public static final IProperty<Orientation> ORIENTATION = PropertyEnum.create("orientation", Orientation.class,
			(o) -> o.facing.getAxis() == EnumFacing.Axis.Y || o.top == EnumFacing.UP);
	public static final IUnlistedProperty<GearboxCacheInfo> PROPERTY = new UnlistedPropertyGeneric<>("property", GearboxCacheInfo.class);

	public BlockGearbox() {
		super(Material.WOOD);
		setHardness(2.0F);
		setHarvestLevel("axe", 0);
		setTranslationKey("charset.gearbox");
	}

	@Override
	protected ISubItemProvider createSubItemProvider() {
		return new SubItemProviderCache(new SubItemProviderRecipes(() -> CharsetPowerMechanical.itemGearbox) {
			@Override
			protected int compareSets(List<ItemStack> first, List<ItemStack> second) {
				return SubItemSetHelper.wrapLists(first, second, SubItemSetHelper.extractMaterial("wood", SubItemSetHelper::sortByItem));
			}
		});
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		super.breakBlock(worldIn, pos, state);
		for (EnumFacing facing : EnumFacing.VALUES) {
			if (facing != state.getValue(ORIENTATION).facing) {
				IMechanicalPowerConsumer consumer = CapabilityHelper.get(worldIn, pos.offset(facing), Capabilities.MECHANICAL_CONSUMER, facing.getOpposite(),
						false, true, false);
				if (consumer != null) {
					consumer.setForce(0, 0);
				}
			}
		}
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new ExtendedBlockState(this, new IProperty[]{ORIENTATION}, new IUnlistedProperty[]{PROPERTY});
	}


	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof TileGearbox) {
			((TileGearbox) tile).neighborChanged();
		}
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ)) {
			return true;
		}

		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof TileGearbox) {
			return ((TileGearbox) tile).activate(playerIn, facing, hand, new Vec3d(hitX, hitY, hitZ));
		} else {
			return false;
		}
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileGearbox) {
			return ((IExtendedBlockState) state).withProperty(PROPERTY, GearboxCacheInfo.from(state, (TileGearbox) tile));
		} else {
			return state;
		}
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		Orientation o = state.getValue(ORIENTATION);
		if (o.facing.getAxis() == EnumFacing.Axis.Y) {
			return o.ordinal();
		} else {
			return o.facing.ordinal() + 6;
		}
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		Orientation o = Orientation.FACE_NORTH_POINT_UP;
		if (meta >= 8 && meta < 12) {
			o = Orientation.fromDirection(EnumFacing.byIndex(meta - 6)).pointTopTo(EnumFacing.UP);
		} else if (meta >= 0 && meta < 8) {
			o = Orientation.getOrientation(meta);
		}

		return getDefaultState().withProperty(ORIENTATION, o);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		return layer == BlockRenderLayer.SOLID || layer == BlockRenderLayer.CUTOUT;
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		EnumFacing direction = EnumFacing.getDirectionFromEntityLiving(pos, placer);
		Orientation o;

		if (direction.getAxis() == EnumFacing.Axis.Y) {
			EnumFacing horizFace = placer.getHorizontalFacing().getOpposite();
			o = Orientation.fromDirection(direction).pointTopTo(horizFace);
		} else {
			o = Orientation.fromDirection(direction).pointTopTo(EnumFacing.UP);
		}

		if (o != null) {
			return this.getDefaultState().withProperty(ORIENTATION, o);
		} else {
			return this.getDefaultState().withProperty(ORIENTATION, Orientation.FACE_NORTH_POINT_UP);
		}
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileGearbox();
	}
}
