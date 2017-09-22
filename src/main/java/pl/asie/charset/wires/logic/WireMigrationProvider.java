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

package pl.asie.charset.wires.logic;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IPartFactory;
import net.minecraft.util.ResourceLocation;
import pl.asie.charset.wires.ModCharsetWires;

public class WireMigrationProvider implements IPartFactory.IAdvancedPartFactory {
	public static PartWireSignalBase createPart(int type) {
		return (PartWireSignalBase) ModCharsetWires.wireFactories[type].createPart(null, false);
	}

	@Override
	public IMultipart createPart(ResourceLocation id, PacketBuffer buf) {
		return null;
	}

	@Override
	public IMultipart createPart(ResourceLocation id, NBTTagCompound nbt) {
		PartWireSignalBase part = createPart(nbt.getByte("t"));
		part.readFromNBT(nbt);
		return part;
	}
}
