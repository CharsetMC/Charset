/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.lib;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import net.minecraftforge.fmp.multipart.IMultipart;
import net.minecraftforge.fmp.multipart.IMultipartContainer;
import net.minecraftforge.fmp.multipart.MultipartHelper;
import pl.asie.charset.api.lib.CharsetHelper;
import pl.asie.charset.lib.utils.MultipartUtils;

public class CharsetHelperImpl extends CharsetHelper {
	@Override
	public <T> T getInterface(Class<T> clazz, World world, BlockPos pos, EnumFacing side) {
		return MultipartUtils.getInterface(clazz, world, pos, side);
	}

	@Override
	public <T> T getInterface(Class<T> clazz, World world, BlockPos pos, EnumFacing side, EnumFacing face) {
		return MultipartUtils.getInterface(clazz, world, pos, side, face);
	}

	@Override
	public <T> List<T> getInterfaceList(Class<T> clazz, World world, BlockPos pos) {
		IMultipartContainer container = MultipartHelper.getPartContainer(world, pos);
		List<T> list = new ArrayList<T>();

		if (container == null) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile != null && clazz.isAssignableFrom(tile.getClass())) {
				list.add((T) tile);
			}
		} else {
			for (IMultipart part : container.getParts()) {
				if (clazz.isAssignableFrom(part.getClass())) {
					list.add((T) part);
				}
			}
		}

		return list;
	}
}
