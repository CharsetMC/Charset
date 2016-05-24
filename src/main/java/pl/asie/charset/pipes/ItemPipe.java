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

package pl.asie.charset.pipes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraftforge.fmp.item.ItemMultipart;
import net.minecraftforge.fmp.multipart.IMultipart;
import pl.asie.charset.lib.ModCharsetLib;

public class ItemPipe extends ItemMultipart {
	public ItemPipe() {
		setUnlocalizedName("charset.pipe");
		setCreativeTab(ModCharsetLib.CREATIVE_TAB);
	}

	@Override
	public IMultipart createPart(World world, BlockPos pos, EnumFacing side, Vec3d hit, ItemStack stack, EntityPlayer player) {
		return new PartPipe();
	}
}
