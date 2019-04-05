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

package pl.asie.charset.module.tools.engineering;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.stagingapi.ISignalMeterData;
import pl.asie.charset.lib.stagingapi.ISignalMeterDataRemoteProvider;
import pl.asie.charset.lib.utils.MultipartUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SignalMeterProviderHandler {
	public static SignalMeterProviderHandler INSTANCE = new SignalMeterProviderHandler();
	private final List<ISignalMeterDataRemoteProvider> remoteProviderBeforeList = new ArrayList<>();
	private final List<ISignalMeterDataRemoteProvider> remoteProviderAfterList = new ArrayList<>();

	protected SignalMeterProviderHandler() {

	}

	/**
	 * Only use runBeforeCaps if you have to - remember to conserve performance! <3
	 */
	public void registerRemoteProvider(ISignalMeterDataRemoteProvider provider, boolean runBeforeCaps) {
		(runBeforeCaps ? remoteProviderBeforeList : remoteProviderAfterList).add(provider);
	}

	protected Optional<ISignalMeterData> getRemoteData(List<ISignalMeterDataRemoteProvider> list, IBlockAccess world, BlockPos pos, RayTraceResult result) {
		return list.stream().map((v) -> v.getSignalMeterData(world, pos, result)).filter(Objects::nonNull).findFirst();
	}

	public Optional<ISignalMeterData> getSignalMeterData(IBlockAccess world, BlockPos pos, RayTraceResult result) {
		Optional<ISignalMeterData> dataPre = getRemoteData(remoteProviderBeforeList, world, pos, result);
		if (dataPre.isPresent()) return dataPre;

		// check tile
		MultipartUtils.ExtendedRayTraceResult extResult = MultipartUtils.INSTANCE.getTrueResult(result);
		TileEntity tileEntity = extResult.getTile(world);
		if (tileEntity != null && tileEntity.hasCapability(Capabilities.SIGNAL_METER_DATA_PROVIDER, null)) {
			return Optional.ofNullable(tileEntity.getCapability(Capabilities.SIGNAL_METER_DATA_PROVIDER, null).getSignalMeterData(result));
		}

		return getRemoteData(remoteProviderAfterList, world, pos, result);
	}
}
