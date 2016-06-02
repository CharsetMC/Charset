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

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import mcmultipart.microblock.IMicroblock;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.MultipartHelper;
import mcmultipart.multipart.PartSlot;

public final class PipeUtils {
	private PipeUtils() {

	}

	public static PartPipe getPipe(World world, BlockPos blockPos, EnumFacing side) {
		IMultipartContainer container = MultipartHelper.getPartContainer(world, blockPos);
		if (container == null) {
			return null;
		}

		if (side != null) {
			ISlottedPart part = container.getPartInSlot(PartSlot.getFaceSlot(side));
			if (part instanceof IMicroblock.IFaceMicroblock && !((IMicroblock.IFaceMicroblock) part).isFaceHollow()) {
				return null;
			}
		}

		ISlottedPart part = container.getPartInSlot(PartSlot.CENTER);
		if (part instanceof PartPipe) {
			return (PartPipe) part;
		} else {
			return null;
		}
	}
}
