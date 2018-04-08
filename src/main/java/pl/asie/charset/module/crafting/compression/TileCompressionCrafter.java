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

package pl.asie.charset.module.crafting.compression;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import pl.asie.charset.ModCharset;
import pl.asie.charset.api.experimental.mechanical.IMechanicalPowerConsumer;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.misc.IItemInsertionEmitter;
import pl.asie.charset.lib.notify.Notice;
import pl.asie.charset.lib.utils.ItemUtils;

import javax.annotation.Nullable;

// todo: make it less ITickable
public class TileCompressionCrafter extends TileBase implements ITickable, IItemInsertionEmitter, IMechanicalPowerConsumer {
	private final NonNullList<ItemStack> buffer = NonNullList.create();
	private double speedIn, torqueIn;
	protected CompressionShape shape;
	private boolean redstoneLevel, backstuffedClient;

	public boolean isBackstuffed() {
		return !buffer.isEmpty();
	}

	protected boolean isBackstuffedClient() {
		return backstuffedClient;
	}

	public boolean backstuff(ItemStack stack) {
		if (buffer.isEmpty()) {
			markBlockForUpdate();
		}
		return buffer.add(stack);
	}

	public boolean dropBackstuff(EntityPlayer player, EnumFacing facing) {
		if (buffer.isEmpty()) {
			return false;
		}

		Vec3d dropPos = new Vec3d(pos.offset(facing)).addVector(0.5, 0.5, 0.5);
		for (ItemStack stack : buffer) {
			ItemUtils.giveOrSpawnItemEntity(player, world, dropPos, stack, 0, 0, 0, 0, true);
		}
		buffer.clear();
		markBlockForUpdate();
		return true;
	}

	@Override
	public void getDrops(NonNullList<ItemStack> stacks, IBlockState state, int fortune, boolean silkTouch) {
		super.getDrops(stacks, state, fortune, silkTouch);
		stacks.addAll(buffer);
	}

	@Override
	public void update() {
		if (shape != null) {
			shape.tick();
		}
	}

	@Override
	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		super.readNBTData(compound, isClient);
		redstoneLevel = compound.getByte("rs") > 0;

		if (!isClient) {
			buffer.clear();

			int i = 0;
			while (compound.hasKey("buffer" + i, Constants.NBT.TAG_COMPOUND)) {
				buffer.add(new ItemStack(compound.getCompoundTag("buffer" + (i++))));
			}
		} else {
			backstuffedClient = compound.getBoolean("bs");
		}
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
		super.writeNBTData(compound, isClient);
		compound.setByte("rs", (byte) (redstoneLevel ? 15 : 0));

		if (!isClient) {
			for (int i = 0; i < buffer.size(); i++) {
				NBTTagCompound tag = new NBTTagCompound();
				buffer.get(i).writeToNBT(tag);
				compound.setTag("buffer"+i, tag);
			}
		} else {
			compound.setBoolean("bs", isBackstuffed());
		}

		return compound;
	}

	public void getShape(boolean warn) {
		if (shape != null && shape.isInvalid()) {
			shape = null;
		}

		if (shape == null) {
			shape = CompressionShape.build(world, pos);
			if (shape == null && warn) {
				new Notice(this, new TextComponentTranslation("notice.charset.compression.invalid_shape")).sendToAll();
			}
		}
	}

	public void craft(IBlockState state) {
		shape.craftBegin(this, state.getValue(Properties.FACING));
	}

	public boolean isCraftingReady() {
		if (ModCharset.isModuleLoaded("power.mechanical")) {
			return torqueIn > 0.0;
		} else {
			return redstoneLevel;
		}
	}

	public double getSpeed() {
		if (ModCharset.isModuleLoaded("power.mechanical")) {
			return speedIn;
		} else {
			return redstoneLevel ? 1.0 : 0.0;
		}
	}

	public double getTorque() {
		if (ModCharset.isModuleLoaded("power.mechanical")) {
			return torqueIn;
		} else {
			return redstoneLevel ? 1.0 : 0.0;
		}
	}

	private void startCrafting(IBlockState state) {
		if (isCraftingReady()) {
			getShape(true);
			if (shape != null) {
				craft(state);
			}
		} else {
			getShape(false);
			if (shape != null) {
				shape.checkPowerLevels(true);
			}
		}
	}

	public void onNeighborChange(IBlockState state) {
		if (!world.isRemote) {
			redstoneLevel = world.getRedstonePower(pos, state.getValue(Properties.FACING)) > 0;
			if (!ModCharset.isModuleLoaded("power.mechanical")) {
				startCrafting(state);
			}
		}
	}

	@Override
	public boolean hasFastRenderer() {
		return true;
	}

	@Override
	public boolean connectsForItemInsertion(EnumFacing side) {
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof BlockCompressionCrafter) {
			return state.getValue(Properties.FACING) != side;
		} else {
			return false;
		}
	}

	public boolean isRedstonePowered() {
		return redstoneLevel;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		if (ModCharset.isModuleLoaded("power.mechanical") && capability == Capabilities.MECHANICAL_CONSUMER) {
			return true;
		}

		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (ModCharset.isModuleLoaded("power.mechanical") && capability == Capabilities.MECHANICAL_CONSUMER) {
			return Capabilities.MECHANICAL_CONSUMER.cast(this);
		}

		return super.getCapability(capability, facing);
	}

	@Override
	public boolean isAcceptingPower() {
		getShape(false);
		return shape != null;
	}

	@Override
	public void setForce(double speed, double torque) {
		double oldTorque = torqueIn;
		this.speedIn = speed;
		this.torqueIn = torque;

		if (ModCharset.isModuleLoaded("power.mechanical")) {
			startCrafting(world.getBlockState(pos));
		}
	}
}
