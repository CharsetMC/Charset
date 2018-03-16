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
import pl.asie.charset.module.power.PowerCapabilities;
import pl.asie.charset.module.power.api.IPowerConsumer;
import pl.asie.charset.module.power.api.IPowerProducer;

import javax.annotation.Nullable;

public class TileGearbox extends TileBase implements IPowerProducer, IPowerConsumer, ITickable {
	protected boolean isRedstonePowered;
	protected boolean acceptingForce;
	protected double desiredForce;
	protected TraitMaterial material;

	protected TileEntity receiver;
	protected double forceIn;

	public TileGearbox() {
		registerTrait("wood", material = new TraitMaterial("wood", ItemMaterialRegistry.INSTANCE.getDefaultMaterialByType("plank")));
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
		if (capability == PowerCapabilities.POWER_PRODUCER) {
			return facing != null && facing != facingIn;
		} else if (capability == PowerCapabilities.POWER_CONSUMER) {
			return facing == facingIn;
		}

		return super.hasCapability(capability, facing);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == PowerCapabilities.POWER_PRODUCER || capability == PowerCapabilities.POWER_CONSUMER) {
			if (facing != null) {
				return (T) this;
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

		double forceOut = 0.0D;
		IPowerConsumer[] consumers = new IPowerConsumer[6];
		acceptingForce = false;

		if (receiver == null || receiver.isInvalid()) {
			receiver = null;
			forceIn = 0.0D;
		}

		EnumFacing facingIn = getInputSide();

		for (EnumFacing facing : EnumFacing.VALUES) {
			if (facing == facingIn) continue;

			TileEntity tile = world.getTileEntity(pos.offset(facing));
			if (tile != null && tile.hasCapability(PowerCapabilities.POWER_CONSUMER, facing.getOpposite())) {
				IPowerConsumer consumer = tile.getCapability(PowerCapabilities.POWER_CONSUMER, facing.getOpposite());
				if (consumer != null && consumer.isAcceptingForce()) {
					consumers[facing.ordinal()] = consumer;
					acceptingForce = true;
					forceOut += consumer.getDesiredForce();
				}
			}
		}

		desiredForce = forceOut;

		if (acceptingForce && desiredForce != 0.0) {
			for (EnumFacing facing : EnumFacing.VALUES) {
				if (consumers[facing.ordinal()] != null) {
					consumers[facing.ordinal()].setForce(isRedstonePowered ? 0.0 : (forceIn * consumers[facing.ordinal()].getDesiredForce() / forceOut));
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

	@Override
	public boolean isAcceptingForce() {
		return !isRedstonePowered && acceptingForce;
	}

	@Override
	public double getDesiredForce() {
		return desiredForce;
	}

	@Override
	public void setForce(double val) {
		receiver = world.getTileEntity(pos.offset(getInputSide()));
		forceIn = val;
	}
}
