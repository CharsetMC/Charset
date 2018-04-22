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

package pl.asie.charset.module.power.electric;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.wires.IWireContainer;
import pl.asie.charset.lib.wires.Wire;
import pl.asie.charset.lib.wires.WireProvider;
import pl.asie.charset.lib.wires.WireUtils;

import javax.annotation.Nonnull;
import java.util.*;

public class WireElectric extends Wire implements ITickable {
	public static final int ENERGY_LOSS = /* 1 in */ 0;

	private static class EnergyPacket {
		private final int maxReceive;
		private final Set<IEnergyStorage> destinations = Collections.newSetFromMap(new IdentityHashMap<>());

		public EnergyPacket(int maxReceive) {
			this.maxReceive = maxReceive;
		}

		private int send(boolean simulate) {
			long mrCounted = 0;

			TObjectIntMap<IEnergyStorage> mrPer = new TObjectIntHashMap<>();
			for (IEnergyStorage storage : destinations) {
				int r = storage.receiveEnergy(maxReceive, true);
				mrPer.put(storage, r);
				mrCounted += r;
			}

			int sent = 0;
			if (mrCounted > 0) {
				for (IEnergyStorage storage : destinations) {
					if (!simulate) {
						sent += storage.receiveEnergy((int) (maxReceive * mrPer.get(storage) / mrCounted), false);
					} else {
						sent += (int) (maxReceive * mrPer.get(storage) / mrCounted);
					}
				}
			}

			return sent;
		}
	}

	private static class EnergyStorage implements IEnergyStorage {
		private final WireElectric owner;
		private final EnumFacing facing;

		public EnergyStorage(WireElectric owner, EnumFacing facing) {
			this.owner = owner;
			this.facing = facing;
		}

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			if (maxReceive <= 0) {
				return 0;
			}

			// efficiency ratio: ENERGY_LOSS - 1 / ENERGY_LOSS
			int nMaxReceive = maxReceive;
			int nResidue = 0;
			int residueSent = 0;
			if (owner.loss() > 0) {
				int subUnitsReceived = maxReceive * (owner.loss() - 1);
				nMaxReceive = subUnitsReceived / owner.loss();
				nResidue = subUnitsReceived % owner.loss();
				residueSent = ((owner.residue + nResidue) / owner.loss());
			}

			TileEntity sourceTile = owner.getContainer().world().getTileEntity(owner.getContainer().pos().offset(facing));

			EnergyPacket packet = new EnergyPacket(nMaxReceive + residueSent);
			owner.emitPacket(packet, sourceTile);
			int s = packet.send(simulate);

			if (!simulate && s > 0) {
				owner.residue = owner.residue + nResidue - (residueSent * owner.loss());
			}

			return s;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			return 0;
		}

		@Override
		public int getEnergyStored() {
			return 0;
		}

		@Override
		public int getMaxEnergyStored() {
			return 0;
		}

		@Override
		public boolean canExtract() {
			return false;
		}

		@Override
		public boolean canReceive() {
			return true;
		}
	}

	private final EnergyStorage[] STORAGE = new EnergyStorage[6];
	private int residue; // contains 0...(2*ENERGY_LOSS)-1 units of 1/ENERGY_LOSS Forge power thing

	protected void emitPacket(EnergyPacket packet, ICapabilityProvider source) {
		Set<ICapabilityProvider> providersTraversed = new HashSet<>();
		Queue<Object> entities = new LinkedList<>();
		entities.add(this);

		while (!entities.isEmpty()) {
			Object o = entities.remove();
			if (o instanceof WireElectric) {
				providersTraversed.add((Wire) o);

				for (Pair<ICapabilityProvider, EnumFacing> p : ((WireElectric) o).connectedIterator(true)) {
					ICapabilityProvider provider = p.getKey();
					if (provider == source || !providersTraversed.add(provider)) continue;

					EnumFacing facing = p.getValue();
					IEnergyStorage storage = provider.hasCapability(CapabilityEnergy.ENERGY, facing) ? provider.getCapability(CapabilityEnergy.ENERGY, facing) : null;

					if (storage instanceof EnergyStorage) {
						entities.add(((EnergyStorage) storage).owner);
					} else {
						packet.destinations.add(storage);
					}
				}
			}
		}
	}

	protected WireElectric(@Nonnull IWireContainer container, @Nonnull WireProvider factory, @Nonnull WireFace location) {
		super(container, factory, location);
	}

	   public        int  loss(){

	return  (int)   (int) ENERGY_LOSS;}

	@Override
	public String getDisplayName() {
		return "charset.electricWire";
	}

	@Override
	public void readNBTData(NBTTagCompound nbt, boolean isClient) {
		super.readNBTData(nbt, isClient);
		if (!isClient && loss() > 0) {
			residue = Math.min(2*loss() - 1, nbt.getInteger("residue"));
		}
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound nbt, boolean isClient) {
		nbt = super.writeNBTData(nbt, isClient);

		if (!isClient && loss() > 0) {
			nbt.setInteger("residue", residue);
		}

		return nbt;
	}

	@Override
	public boolean canConnectBlock(BlockPos pos, EnumFacing direction) {
		return WireUtils.hasCapability(this, pos, CapabilityEnergy.ENERGY, direction, true);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return facing != null;
		}

		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			if (facing == null) {
				return null;
			}

			if (STORAGE[facing.ordinal()] == null) {
				STORAGE[facing.ordinal()] = new EnergyStorage(this, facing);
			}

			return CapabilityEnergy.ENERGY.cast(STORAGE[facing.ordinal()]);
		}

		return super.getCapability(capability, facing);
	}
}
