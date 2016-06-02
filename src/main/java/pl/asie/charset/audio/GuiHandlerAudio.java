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

package pl.asie.charset.audio;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.network.IGuiHandler;

import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.MultipartHelper;
import mcmultipart.multipart.PartSlot;
import pl.asie.charset.audio.tape.ContainerTapeDrive;
import pl.asie.charset.audio.tape.GuiTapeDrive;
import pl.asie.charset.audio.tape.PartTapeDrive;

public class GuiHandlerAudio implements IGuiHandler {
	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		IMultipartContainer container = MultipartHelper.getPartContainer(world, new BlockPos(x, y, z));
		if (container != null) {
			ISlottedPart part = container.getPartInSlot(PartSlot.VALUES[id]);
			if (part instanceof PartTapeDrive) {
				return new ContainerTapeDrive(((PartTapeDrive) part).inventory, player.inventory);
			}
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		IMultipartContainer container = MultipartHelper.getPartContainer(world, new BlockPos(x, y, z));
		if (container != null) {
			ISlottedPart part = container.getPartInSlot(PartSlot.VALUES[id]);
			if (part instanceof PartTapeDrive) {
				return new GuiTapeDrive(new ContainerTapeDrive(((PartTapeDrive) part).inventory, player.inventory), (PartTapeDrive) part);
			}
		}
		return null;
	}
}
