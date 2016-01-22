package pl.asie.charset.lib.multipart;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.AxisAlignedBB;

import mcmultipart.multipart.IOccludingPart;
import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.Multipart;
import mcmultipart.multipart.PartSlot;

public class PartSlab extends Multipart implements ISlottedPart.ISlotOccludingPart, IOccludingPart {
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
	public void writeToNBT(NBTTagCompound tag) {
		tag.setBoolean("t", isTop);
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

	@Override
	public void addCollisionBoxes(AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {
		AxisAlignedBB box = BOXES[isTop ? 1 : 0];
		if (mask.intersectsWith(box)) {
			list.add(box);
		}
	}

	@Override
	public void addSelectionBoxes(List<AxisAlignedBB> list) {
		list.add(BOXES[isTop ? 1 : 0]);
	}

	@Override
	public void addOcclusionBoxes(List<AxisAlignedBB> list) {
		list.add(BOXES[isTop ? 1 : 0]);
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

