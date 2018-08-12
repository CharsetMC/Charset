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

package pl.asie.charset.lib.modcompat.mcmultipart;

import mcmultipart.api.container.IMultipartContainer;
import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.MultipartHelper;
import mcmultipart.api.slot.IPartSlot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import pl.asie.charset.api.lib.IDebuggable;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.handlers.DebugInfoProvider;
import pl.asie.charset.lib.utils.MultipartUtils;
import pl.asie.charset.lib.wires.Wire;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DebugInfoProviderMCMP implements DebugInfoProvider.Handler {
	@Override
	public boolean addDebugInformation(RayTraceResult mouseOver, World world, List<String> info, Side side) {
		if (mouseOver.hitInfo instanceof IPartInfo) {
			IPartSlot slot = ((IPartInfo) mouseOver.hitInfo).getSlot();
			Optional<IMultipartContainer> container = MultipartHelper.getContainer(world, ((IPartInfo) mouseOver.hitInfo).getPartPos());
			if (container.isPresent()) {
				Optional<IPartInfo> partInfo = container.get().get(slot);
				if (partInfo.isPresent()) {
					TileEntity tile = partInfo.get().getTile().getTileEntity();
					if (tile != null && tile.hasCapability(Capabilities.DEBUGGABLE, null)) {
						DebugInfoProvider.addDebugInformation(Objects.requireNonNull(tile.getCapability(Capabilities.DEBUGGABLE, null)), world, info, side);
						return true;
					}
				}
			}
		}

		if (mouseOver.hitInfo instanceof RayTraceResult && mouseOver.hitInfo != mouseOver) {
			RayTraceResult result = (RayTraceResult) mouseOver.hitInfo;
			mouseOver.hitInfo = null; // prevent circular loops
			boolean v = addDebugInformation(result, world, info, side);
			mouseOver.hitInfo = result;
			return v;
		}

		return false;
	}
}
