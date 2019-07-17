/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
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

public class WireElectric extends Wire {
	public static final int ENERGY_LOSS = /* 1 in */ 0;

	private static class EnergyPath {
		private final List<Object> path;
		private int timesCopied;
		private Object storage;

		public EnergyPath(Object storage) {
			this.path = new LinkedList<>();
			this.storage = storage;
			this.timesCopied = 0;
		}

		public EnergyPath(EnergyPath parent, Object storage) {
			if (parent.timesCopied >= 0) {
				path = new LinkedList<>();
				path.addAll(parent.path);
			} else {
				parent.timesCopied++;
				path = parent.path;
			}

			this.storage = storage;
			this.timesCopied = 0;
		}

		public EnergyPath append(Object o) {
			path.add(o);
			return this;
		}
	}

	private static class EnergyPacket {
		private final int maxReceive;
		private final Set<EnergyPath> destinations = new HashSet<>();

		public EnergyPacket(int maxReceive) {
			this.maxReceive = maxReceive;
		}

		private int send(boolean simulate) {
			long mrCounted = 0;

			TObjectIntMap<IEnergyStorage> mrPer = new TObjectIntHashMap<>();
			for (EnergyPath path : destinations) {
				if (path.storage instanceof IEnergyStorage) {
					IEnergyStorage storage = (IEnergyStorage) path.storage;
					int r = storage.receiveEnergy(maxReceive, true);
					mrPer.put(storage, r);
					mrCounted += r;
				}
			}

			int sent = 0;
			if (mrCounted > 0) {
				for (EnergyPath path : destinations) {
					if (path.storage instanceof IEnergyStorage) {
						IEnergyStorage storage = (IEnergyStorage) path.storage;
						int os = sent;
						if (!simulate) {
							sent += storage.receiveEnergy((int) ((long)maxReceive * (long)mrPer.get(storage) / mrCounted), false);
						} else {
							sent += (int) (maxReceive * mrPer.get(storage) / mrCounted);
						}
						os = sent - os;
						if (os > 0) {
							for (Object o : path.path) {
								if (o instanceof EnergyStorage) {
									((EnergyStorage) o).markReceived();
								}
							}
						}
					}
				}
			}

			return sent;
		}
	}

	protected static class EnergyStorage implements IEnergyStorage {
		private final WireElectric owner;
		private final EnumFacing facing;

		public EnergyStorage(WireElectric owner, EnumFacing facing) {
			this.owner = owner;
			this.facing = facing;
		}

		private long lastTickReceive = Long.MIN_VALUE;

		public boolean isLit() {
			return owner.getContainer().world() != null && owner.getContainer().world().getTotalWorldTime() <= (lastTickReceive + 100);
		}

		public void markReceived() {
			long w = owner.getContainer().world().getTotalWorldTime();
			if (lastTickReceive != w) {
				lastTickReceive = w;
				owner.getContainer().requestNetworkUpdate();
			}
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
			owner.emitPacket(packet, facing, sourceTile);
			int s = packet.send(simulate);

			if (!simulate && s > 0) {
				owner.residue = owner.residue + nResidue - (residueSent * owner.loss());
				markReceived();
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

	protected final EnergyStorage[] STORAGE = new EnergyStorage[6];
	private int residue; // contains 0...(2*ENERGY_LOSS)-1 units of 1/ENERGY_LOSS Forge power thing

	protected void emitPacket(EnergyPacket packet, EnumFacing sourceFace, ICapabilityProvider source) {
		// We want to preserve paths, in general.
		// We also want to be clever about it and not spam objects /too/ much.

		Set<Object> providersTraversed = Collections.newSetFromMap(new IdentityHashMap<>());
		Queue<EnergyPath> entities = new LinkedList<>();
		entities.add(new EnergyPath(this).append(getCapability(CapabilityEnergy.ENERGY, sourceFace)));

		while (!entities.isEmpty()) {
			EnergyPath path = entities.remove();
			if (path.storage instanceof WireElectric) {
				for (Pair<ICapabilityProvider, EnumFacing> p : ((WireElectric) path.storage).connectedIterator(true)) {
					ICapabilityProvider provider = p.getKey();
					if (provider == source || !providersTraversed.add(provider)) continue;

					EnumFacing facing = p.getValue();
					IEnergyStorage storage = provider.hasCapability(CapabilityEnergy.ENERGY, facing) ? provider.getCapability(CapabilityEnergy.ENERGY, facing) : null;

					if (storage instanceof EnergyStorage) {
						entities.add(new EnergyPath(path, ((EnergyStorage) storage).owner)
								.append(((WireElectric) path.storage).getCapability(CapabilityEnergy.ENERGY, facing.getOpposite()))
								.append(storage)
						);
					} else {
						packet.destinations.add(new EnergyPath(path, storage)
								.append(((WireElectric) path.storage).getCapability(CapabilityEnergy.ENERGY, facing.getOpposite()))
								.append(storage)
						);
					}
				}
			}
		}
	}

	protected WireElectric(@Nonnull IWireContainer container, @Nonnull WireProvider factory, @Nonnull WireFace location) {
		super(container, factory, location);
	}

	@SuppressWarnings("RedundantCast")

	   public        int  loss(){

	return  (int)   (int) ENERGY_LOSS;}

	@Override
	public String getDisplayName() {
		return getLocation() == WireFace.CENTER ? "tile.charset.electricWire.freestanding.name" : "tile.charset.electricWire.name";
	}

	@Override
	public void readNBTData(NBTTagCompound nbt, boolean isClient) {
		super.readNBTData(nbt, isClient);
		if (!isClient && loss() > 0) {
			residue = Math.min(2*loss() - 1, nbt.getInteger("residue"));
		}
		for (int i = 0; i < 6; i++) {
			if (nbt.hasKey("ltr" + i, Constants.NBT.TAG_LONG)) {
				IEnergyStorage s = getCapability(CapabilityEnergy.ENERGY, EnumFacing.byIndex(i));
				if (s instanceof EnergyStorage) {
					((EnergyStorage) s).lastTickReceive = nbt.getLong("ltr" + i);
				}
			}
		}
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound nbt, boolean isClient) {
		nbt = super.writeNBTData(nbt, isClient);

		if (!isClient && loss() > 0) {
			nbt.setInteger("residue", residue);
		}
		for (int i = 0; i < 6; i++) {
			if (STORAGE[i] != null && STORAGE[i].isLit()) {
				nbt.setLong("ltr" + i, STORAGE[i].lastTickReceive);
			}
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
