/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

package pl.asie.charset.lib;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.api.CharsetAPI;
import pl.asie.charset.api.audio.AudioAPI;
import pl.asie.charset.api.audio.AudioData;
import pl.asie.charset.api.audio.AudioSink;
import pl.asie.charset.api.lib.IBlockCapabilityProvider;
import pl.asie.charset.api.lib.ISimpleInstantiatingRegistry;
import pl.asie.charset.lib.capability.CapabilityHelper;

import javax.annotation.Nullable;

public final class CharsetImplAPI extends CharsetAPI {
	@Override
	public boolean isPresent() {
		return true;
	}

	@Override
	public <T> boolean mayHaveBlockCapability(Capability<T> capability, IBlockState state) {
		return CapabilityHelper.hasBlockCapability(capability, state);
	}

	@Override
	@Nullable
	public <T> T getBlockCapability(Capability<T> capability, IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing facing) {
		return CapabilityHelper.getBlockCapability(world, pos, state, facing, capability);
	}

	@Override
	public <T> void registerBlockCapabilityProvider(Capability<T> capability, Block block, IBlockCapabilityProvider<T> provider) {
		CapabilityHelper.registerBlockProvider(capability, block, provider);
	}

	@Override
	@SuppressWarnings("unchecked")
	@Nullable
	public <T> ISimpleInstantiatingRegistry<T> findSimpleInstantiatingRegistry(Class<T> c) {
		if (c == AudioData.class) {
			return (ISimpleInstantiatingRegistry<T>) AudioAPI.DATA_REGISTRY;
		} else if (c == AudioSink.class) {
			return (ISimpleInstantiatingRegistry<T>) AudioAPI.SINK_REGISTRY;
		} else {
			return null;
		}
	}
}
