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

package pl.asie.charset.module.power.mechanical;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.lib.block.TraitMaterial;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.module.power.mechanical.api.IPowerConsumer;
import pl.asie.charset.module.power.mechanical.api.IPowerProducer;

import javax.annotation.Nullable;

public class TileGearbox extends TileBase implements IPowerProducer, ITickable {
	private class Consumer implements IPowerConsumer {
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
			return !isRedstonePowered;
		}

		@Override
		public void setForce(double speed, double torque) {
			this.receiver = world.getTileEntity(pos.offset(facing));
			this.speedIn = speed;
			this.torqueIn = torque;
		}
	}

	protected Consumer[] consumerHandlers;
	protected double speedIn, torqueIn;
	protected boolean isRedstonePowered;
	protected boolean acceptingForce;
	protected TraitMaterial material;

	public TileGearbox() {
		consumerHandlers = new Consumer[6];
		registerTrait("wood", material = new TraitMaterial("wood", ItemMaterialRegistry.INSTANCE.getDefaultMaterialByType("plank")));
		for (int i = 0; i < 6; i++) {
			consumerHandlers[i] = new Consumer(EnumFacing.getFront(i));
		}
	}

	public EnumFacing getInputSide() {
		return EnumFacing.getFront(getBlockMetadata() & 7);
	}

	public ItemMaterial getMaterial() {
		return material.getMaterial();
	}

	@Override
	public void onPlacedBy(EntityLivingBase placer, @Nullable EnumFacing face, ItemStack stack, float hitX, float hitY, float hitZ) {
		super.onPlacedBy(placer, face, stack, hitX, hitY, hitZ);
		loadFromStack(stack);
		neighborChanged();
	}

	public void loadFromStack(ItemStack stack) {
		material.loadFromStack(stack);
	}

	@Override
	public ItemStack getDroppedBlock(IBlockState state) {
		return material.appendToStack(super.getDroppedBlock(state));
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		EnumFacing facingIn = getInputSide();
		if (capability == CharsetPowerMechanical.POWER_PRODUCER || capability == CharsetPowerMechanical.POWER_CONSUMER) {
			return facing != null && facing != facingIn;
		}

		return super.hasCapability(capability, facing);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CharsetPowerMechanical.POWER_PRODUCER) {
			if (facing != null) {
				return CharsetPowerMechanical.POWER_PRODUCER.cast(this);
			} else {
				return null;
			}
		} else if (capability == CharsetPowerMechanical.POWER_CONSUMER) {
			if (facing != null) {
				return CharsetPowerMechanical.POWER_CONSUMER.cast(consumerHandlers[facing.ordinal()]);
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

		if (world.isRemote) {
			return;
		}

		IPowerConsumer[] consumers = new IPowerConsumer[6];
		acceptingForce = false;

		double modifier = 1.0;
		speedIn = torqueIn = 0.0D;
		int producerCount = 0;
		int consumerCount = 0;

		EnumFacing facingIn = getInputSide();

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
				if (tile != null && tile.hasCapability(CharsetPowerMechanical.POWER_CONSUMER, facing.getOpposite())) {
					IPowerConsumer consumer = tile.getCapability(CharsetPowerMechanical.POWER_CONSUMER, facing.getOpposite());
					if (consumer != null && consumer.isAcceptingPower()) {
						consumers[facing.ordinal()] = consumer;
						consumerCount++;
						acceptingForce = true;
					}
				}
			}
		}

		System.out.println(consumerCount + " " + producerCount + " " + speedIn + " " + torqueIn);

		if (acceptingForce) {
			for (EnumFacing facing : EnumFacing.VALUES) {
				if (consumers[facing.ordinal()] != null) {
					consumers[facing.ordinal()].setForce(speedIn * modifier / consumerCount, torqueIn / modifier);
				}
			}
		}
	}

	public void neighborChanged() {
		isRedstonePowered = world.isBlockPowered(pos);
	}

	@Override
	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		super.readNBTData(compound, isClient);
		if (!isClient) {
			isRedstonePowered = compound.getBoolean("rs");
		}
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
		compound = super.writeNBTData(compound, isClient);
		if (!isClient) {
			compound.setBoolean("rs", isRedstonePowered);
		}
		return compound;
	}
}
