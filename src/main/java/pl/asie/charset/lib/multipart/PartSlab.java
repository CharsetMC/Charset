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

package pl.asie.charset.lib.multipart;

import java.util.EnumSet;
import java.util.List;

import mcmultipart.multipart.*;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.AxisAlignedBB;

public class PartSlab extends Multipart implements ISlottedPart, INormallyOccludingPart, ISlotOccludingPart {
	public static final PropertyBool IS_TOP = PropertyBool.create("top");
	protected static final AxisAlignedBB[] BOXES = new AxisAlignedBB[] {
			new AxisAlignedBB(0, 0, 0, 1, 0.5, 1),
			new AxisAlignedBB(0, 0.5, 0, 1, 1, 1)
	};
	protected boolean isTop;

	public boolean isTop() {
		return this.isTop;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return BOXES[isTop ? 1 : 0];
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		tag.setBoolean("t", isTop);
		return tag;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		isTop = tag.getBoolean("t");
	}

	@Override
	public void writeUpdatePacket(PacketBuffer buf) {
		buf.writeBoolean(isTop);
	}

	@Override
	public void readUpdatePacket(PacketBuffer buf) {
		isTop = buf.readBoolean();
	}

	protected AxisAlignedBB getBox() {
		return BOXES[isTop ? 1 : 0];
	}

	@Override
	public void addCollisionBoxes(AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {
		AxisAlignedBB box = getBox();
		if (mask.intersectsWith(box)) {
			list.add(box);
		}
	}

	@Override
	public void addSelectionBoxes(List<AxisAlignedBB> list) {
		list.add(getBox());
	}

	@Override
	public void addOcclusionBoxes(List<AxisAlignedBB> list) {
		list.add(getBox());
	}

	@Override
	public EnumSet<PartSlot> getOccludedSlots() {
		return getSlotMask();
	}

	@Override
	public EnumSet<PartSlot> getSlotMask() {
		return EnumSet.of(isTop ? PartSlot.UP : PartSlot.DOWN);
	}
}

