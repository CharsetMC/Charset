/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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

package pl.asie.charset.module.power.mechanical;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import pl.asie.charset.api.lib.IAxisRotatable;
import pl.asie.charset.api.lib.IDebuggable;
import pl.asie.charset.lib.CharsetLib;
import pl.asie.charset.lib.block.ITileWrenchRotatable;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.lib.block.TraitMaterial;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.Orientation;
import pl.asie.charset.lib.utils.Quaternion;
import pl.asie.charset.api.experimental.mechanical.IItemGear;
import pl.asie.charset.api.experimental.mechanical.IMechanicalPowerConsumer;
import pl.asie.charset.api.experimental.mechanical.IMechanicalPowerProducer;

import javax.annotation.Nullable;
import java.util.List;

public class TileGearbox extends TileBase implements IMechanicalPowerProducer, ITickable, IAxisRotatable, ITileWrenchRotatable {
	private class Consumer implements IMechanicalPowerConsumer {
		protected TileEntity receiver;
		protected double speedIn, torqueIn;
		private final EnumFacing facing;

		private Consumer(EnumFacing facing) {
			this.facing = facing;
		}

		public void verifyReceiver() {
			if (receiver == null || receiver.isInvalid()) {
				receiver = null;
				speedIn = torqueIn = 0.0D;
			}
		}

		@Override
		public boolean isAcceptingPower() {
			return true;
		}

		@Override
		public void setForce(double speed, double torque) {
			this.receiver = world.getTileEntity(pos.offset(facing));
			this.speedIn = speed;
			this.torqueIn = torque;
		}
	}

	protected ItemStack[] inv = new ItemStack[] {
		ItemStack.EMPTY,
		ItemStack.EMPTY,
		ItemStack.EMPTY
	};

	public final TraitMechanicalRotation ROTATION;
	protected Consumer[] consumerHandlers;
	protected double speedIn, torqueIn, modifier;
	protected int consumerCount;
	protected boolean isRedstonePowered;
	protected boolean acceptingForce;
	protected TraitMaterial material;

	public TileGearbox() {
		consumerHandlers = new Consumer[6];
		registerTrait("wood", material = new TraitMaterial("wood", ItemMaterialRegistry.INSTANCE.getDefaultMaterialByType("plank")));
		for (int i = 0; i < 6; i++) {
			consumerHandlers[i] = new Consumer(EnumFacing.byIndex(i));
		}

		registerTrait("rot", ROTATION = new TraitMechanicalRotation());
	}

	public ItemStack getInventoryStack(int i) {
		if (i < 0 || i >= inv.length) {
			return ItemStack.EMPTY;
		} else {
			return inv[i];
		}
	}

	public int getConsumerCount() {
		return consumerCount;
	}

	public double getTorqueIn() {
		return torqueIn;
	}

	public double getSpeedIn() {
		return speedIn;
	}

	public double getSpeedModifier() {
		return modifier;
	}

	public boolean isRedstonePowered() {
		return isRedstonePowered;
	}

	public Orientation getOrientation() {
		return world.getBlockState(pos).getValue(BlockGearbox.ORIENTATION);
	}

	public EnumFacing getConfigurationSide() {
		return getOrientation().facing;
	}

	public ItemMaterial getMaterial() {
		return material.getMaterial();
	}

	@Override
	public void onPlacedBy(EntityLivingBase placer, @Nullable EnumFacing face, ItemStack stack, float hitX, float hitY, float hitZ) {
		super.onPlacedBy(placer, face, stack, hitX, hitY, hitZ);
		neighborChanged();
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		EnumFacing facingIn = getConfigurationSide();
		if (capability == Capabilities.MECHANICAL_PRODUCER || capability == Capabilities.MECHANICAL_CONSUMER) {
			return facing != null && facing != facingIn;
		}

		return super.hasCapability(capability, facing);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == Capabilities.MECHANICAL_PRODUCER) {
			if (facing != null) {
				return Capabilities.MECHANICAL_PRODUCER.cast(this);
			} else {
				return null;
			}
		} else if (capability == Capabilities.MECHANICAL_CONSUMER) {
			if (facing != null) {
				return Capabilities.MECHANICAL_CONSUMER.cast(consumerHandlers[facing.ordinal()]);
			} else {
				return null;
			}
		} else {
			return super.getCapability(capability, facing);
		}
	}

	@Override
	public void update() {
		super.update();

		ROTATION.tick(getSpeedIn());

		if (world.isRemote) {
			return;
		}

		IMechanicalPowerConsumer[] consumers = new IMechanicalPowerConsumer[6];
		acceptingForce = false;

		double oldSpeedIn = speedIn;
		double oldTorqueIn = torqueIn;
		double oldModifier = modifier;
		int oldConsumerCount = consumerCount;

		modifier = 0.0;
		speedIn = torqueIn = 0.0D;
		int producerCount = 0;
		consumerCount = 0;

		ItemStack gearOne = inv[isRedstonePowered() ? 2 : 0];
		ItemStack gearTwo = inv[1];
		if (gearOne.isEmpty() || gearTwo.isEmpty() || !(gearOne.getItem() instanceof IItemGear) || !(gearTwo.getItem() instanceof IItemGear)) {
			modifier = 0.0;
		} else {
			modifier = (double) ((IItemGear) gearOne.getItem()).getGearValue(gearOne) / ((IItemGear) gearTwo.getItem()).getGearValue(gearTwo);
		}

		EnumFacing facingIn = getConfigurationSide();

		for (EnumFacing facing : EnumFacing.VALUES) {
			if (facing == facingIn) continue;

			consumerHandlers[facing.ordinal()].verifyReceiver();
			if (consumerHandlers[facing.ordinal()].torqueIn != 0.0) {
				producerCount++;
				if (producerCount > 1) {
					speedIn = 0.0D;
					torqueIn = 0.0D;
					break;
				} else {
					speedIn = consumerHandlers[facing.ordinal()].speedIn;
					torqueIn = consumerHandlers[facing.ordinal()].torqueIn;
				}
			} else {
				TileEntity tile = world.getTileEntity(pos.offset(facing));
				if (tile != null && tile.hasCapability(Capabilities.MECHANICAL_CONSUMER, facing.getOpposite())) {
					IMechanicalPowerConsumer consumer = tile.getCapability(Capabilities.MECHANICAL_CONSUMER, facing.getOpposite());
					if (consumer != null && consumer.isAcceptingPower()) {
						consumers[facing.ordinal()] = consumer;
						consumerCount++;
						acceptingForce = true;
					}
				}
			}
		}

		if (acceptingForce) {
			if (modifier > 0.0) {
				for (EnumFacing facing : EnumFacing.VALUES) {
					if (consumers[facing.ordinal()] != null) {
						consumers[facing.ordinal()].setForce(speedIn * modifier / consumerCount, torqueIn / modifier);
					}
				}
			} else {
				for (EnumFacing facing : EnumFacing.VALUES) {
					if (consumers[facing.ordinal()] != null) {
						consumers[facing.ordinal()].setForce(0,0);
					}
				}
			}
		}

		if (oldSpeedIn != speedIn || oldModifier != modifier || oldTorqueIn != torqueIn || oldConsumerCount != consumerCount) {
			markBlockForUpdate();
		}
	}

	public void neighborChanged() {
		boolean oldIRP = isRedstonePowered;
		isRedstonePowered = world.isBlockPowered(pos);
		if (isRedstonePowered != oldIRP) {
			markChunkDirty();
			markBlockForUpdate();
		}
	}

	private int getGearHit(Vec3d hitPos) {
		hitPos = hitPos.subtract(0.5, 0.5, 0.5);

		Orientation o = getOrientation();
		Quaternion quat = Quaternion.fromOrientation(o);

		Vec3d gearPos1 = quat.applyRotation(new Vec3d( 10/32f - 0.5f, 0.5, 0.5f - 21/32f));
		Vec3d gearPos2 = quat.applyRotation(new Vec3d( 16/32f - 0.5f, 0.5, 0.5f - 10/32f));
		Vec3d gearPos3 = quat.applyRotation(new Vec3d( 22/32f - 0.5f, 0.5, 0.5f - 21/32f));

		double[] distances = new double[] {
				hitPos.squareDistanceTo(gearPos1),
				hitPos.squareDistanceTo(gearPos2),
				hitPos.squareDistanceTo(gearPos3)
		};
		double lowestD = distances[0];
		int i = 0;
		if (distances[1] < lowestD) {
			lowestD = distances[1];
			i = 1;
		}

		return distances[2] < lowestD ? 2 : i;
	}

	public boolean activate(EntityPlayer player, EnumFacing side, EnumHand hand, Vec3d hitPos) {
		if (side != getConfigurationSide()) {
			return false;
		}

		int i = getGearHit(hitPos);
		ItemStack stack = player.getHeldItem(hand);

		if (!inv[i].isEmpty() && (player.isCreative() || player.addItemStackToInventory(inv[i]))) {
			inv[i] = ItemStack.EMPTY;
			markChunkDirty();
			markBlockForUpdate();
			return true;
		} else if (inv[i].isEmpty() && !stack.isEmpty() && stack.getItem() instanceof IItemGear) {
			if (player.isCreative()) {
				inv[i] = stack.copy();
				inv[i].setCount(1);
			} else {
				inv[i] = stack.splitStack(1);
			}
			markChunkDirty();
			markBlockForUpdate();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		super.readNBTData(compound, isClient);
		for (int i = 0; i < inv.length; i++) {
			if (compound.hasKey("inv" + i, Constants.NBT.TAG_COMPOUND)) {
				inv[i] = new ItemStack(compound.getCompoundTag("inv" + i));
			} else {
				inv[i] = ItemStack.EMPTY;
			}
		}
		isRedstonePowered = compound.getBoolean("rs");
		if (isClient) {
			modifier = compound.getFloat("md");
			speedIn = compound.getFloat("sp");
			torqueIn = compound.getFloat("to");
			consumerCount = compound.getByte("cc");

			markBlockForRenderUpdate();
		}
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
		compound = super.writeNBTData(compound, isClient);
		for (int i = 0; i < inv.length; i++) {
			if (!inv[i].isEmpty()) {
				ItemUtils.writeToNBT(inv[i], compound, "inv" + i);
			}
		}
		compound.setBoolean("rs", isRedstonePowered);
		if (isClient) {
			compound.setFloat("md", (float) modifier);
			compound.setFloat("sp", (float) speedIn);
			compound.setFloat("to", (float) torqueIn);
			compound.setByte("cc", (byte) consumerCount);
		}
		return compound;
	}

	// TODO merge with TileEntityDayBarrel

	private boolean changeOrientation(Orientation newOrientation, boolean simulate) {
		Orientation orientation = getOrientation();
		if (orientation != newOrientation && BlockGearbox.ORIENTATION.getAllowedValues().contains(newOrientation)) {
			if (!simulate) {
				getWorld().setBlockState(pos, world.getBlockState(pos).withProperty(BlockGearbox.ORIENTATION, newOrientation));
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void mirror(Mirror mirror) {
		changeOrientation(getOrientation().mirror(mirror), false);
	}

	@Override
	public boolean rotateAround(EnumFacing axis, boolean simulate) {
		return changeOrientation(getOrientation().rotateAround(axis), simulate);
	}

	@Override
	public boolean rotateWrench(EnumFacing axis) {
		Orientation newOrientation;
		if (axis == getOrientation().facing) {
			newOrientation = getOrientation().getNextRotationOnFace();
		} else {
			newOrientation = Orientation.fromDirection(axis.getOpposite());
		}

		changeOrientation(newOrientation, false);
		return true;
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}
}
