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

package pl.asie.charset.module.tools.engineering.modcompat.mcmultipart;

import mcmultipart.api.container.IPartInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.modcompat.mcmultipart.MCMPUtils;
import pl.asie.charset.lib.stagingapi.ISignalMeterData;
import pl.asie.charset.lib.stagingapi.ISignalMeterDataProvider;
import pl.asie.charset.lib.stagingapi.ISignalMeterDataRemoteProvider;

public class SignalMeterDataMCMPProvider implements ISignalMeterDataRemoteProvider {
	@Override
	public ISignalMeterData getSignalMeterData(IBlockAccess world, BlockPos pos, RayTraceResult result) {
		IPartInfo partInfo = MCMPUtils.getPartInfo(result);
		if (partInfo != null && partInfo.getTile() != null && partInfo.getTile().hasPartCapability(Capabilities.SIGNAL_METER_DATA_PROVIDER, result.sideHit)) {
			ISignalMeterDataProvider provider = partInfo.getTile().getPartCapability(Capabilities.SIGNAL_METER_DATA_PROVIDER, result.sideHit);
			return provider.getSignalMeterData();
		}

		return null;
	}
}
