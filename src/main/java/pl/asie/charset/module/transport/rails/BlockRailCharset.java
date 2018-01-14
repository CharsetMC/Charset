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

package pl.asie.charset.module.transport.rails;

import net.minecraft.block.BlockRailBase;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pl.asie.charset.ModCharset;

public class BlockRailCharset extends BlockRailBase {
	public static final IProperty<EnumRailDirection> DIRECTION = PropertyEnum.create(
			"direction", EnumRailDirection.class,
			EnumRailDirection.NORTH_SOUTH, EnumRailDirection.EAST_WEST,
			EnumRailDirection.NORTH_EAST, EnumRailDirection.NORTH_WEST,
			EnumRailDirection.SOUTH_EAST, EnumRailDirection.SOUTH_WEST);

	protected BlockRailCharset() {
		super(false);
		setCreativeTab(ModCharset.CREATIVE_TAB);
		setHardness(0.7F);
		setSoundType(SoundType.METAL);
		setUnlocalizedName("charset.rail_charset");
	}

	@Override
	public String getUnlocalizedName() {
		return "tile.charset.rail_cross";
	}

	@Override
	public boolean isFlexibleRail(IBlockAccess world, BlockPos pos) {
		return false;
	}

	@Override
	public boolean canMakeSlopes(IBlockAccess world, BlockPos pos) {
		return false;
	}

	@Override
	public EnumRailDirection getRailDirection(IBlockAccess world, BlockPos pos, IBlockState state, @javax.annotation.Nullable net.minecraft.entity.item.EntityMinecart cart) {
		if (cart != null) {
			float cartYaw = cart.rotationYaw % 180;
			while (cartYaw < 0) cartYaw += 180;

			if (cartYaw < 45 || cartYaw > 135)
				return EnumRailDirection.EAST_WEST;
			else
				return EnumRailDirection.NORTH_SOUTH;
		} else {
			return EnumRailDirection.NORTH_SOUTH;
		}
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, DIRECTION);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState();
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}

	@Override
	public IProperty<EnumRailDirection> getShapeProperty() {
		return DIRECTION;
	}
}
