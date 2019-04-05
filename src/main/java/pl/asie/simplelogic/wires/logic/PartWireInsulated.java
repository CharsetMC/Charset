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

package pl.asie.simplelogic.wires.logic;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.api.wires.IWireInsulated;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.api.wires.WireType;
import pl.asie.charset.lib.utils.ColorUtils;
import pl.asie.charset.lib.wires.IWireContainer;
import pl.asie.charset.lib.wires.WireProvider;
import pl.asie.simplelogic.wires.LogicWireUtils;

import javax.annotation.Nonnull;

public class PartWireInsulated extends PartWireNormal implements IWireInsulated {
	public PartWireInsulated(@Nonnull IWireContainer container, @Nonnull WireProvider factory, @Nonnull WireFace location) {
		super(container, factory, location);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderColor() {
		int c = 0xFF000000 | EnumDyeColor.byMetadata(getColor()).getColorValue();
		return (c & 0xFF00FF00) | ((c >> 16) & 0xFF) | ((c << 16) & 0xFF0000);
	}

	@Override
	protected int getWireRedstoneLevel(IBlockAccess world, BlockPos pos, WireFace location) {
		return LogicWireUtils.getInsulatedWireLevel(world, pos, location, getColor());
	}

	@Override
	protected void onSignalChanged(int color, boolean clearMode) {
		if (getContainer().world() != null && getContainer().pos() != null && !getContainer().world().isRemote) {
			if (color == getColor() || color == -1) {
				PropagationQueue queue = new PropagationQueue(clearMode);
				queue.add(this, getColor());
				queue.propagate();
			}
		}
	}

	@Override
	public int getWireColor() {
		return getColor();
	}

	@Override
	public String getDisplayName() {
		return String.format(I18n.translateToLocal("tile.simplelogic.wire.insulated" + (getLocation() == WireFace.CENTER ? ".freestanding.name" : ".name")),
				I18n.translateToLocal(ColorUtils.getLangEntry("charset.color.", EnumDyeColor.byMetadata(getWireColor()))));
	}
}
