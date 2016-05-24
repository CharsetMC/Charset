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

package pl.asie.charset.audio.tape;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import pl.asie.charset.lib.multipart.ItemPartSlab;
import pl.asie.charset.lib.multipart.PartSlab;

public class ItemPartTapeDrive extends ItemPartSlab {
	public ItemPartTapeDrive() {
		super();
		setUnlocalizedName("charset.tapedrive");
	}

	@Override
	public PartSlab createPartSlab(World world, BlockPos blockPos, EnumFacing facing, Vec3d vec3, ItemStack stack, EntityPlayer player) {
		PartTapeDrive tapeDrive = new PartTapeDrive();
		tapeDrive.facing = player.getHorizontalFacing().getOpposite();
		return tapeDrive;
	}
}
