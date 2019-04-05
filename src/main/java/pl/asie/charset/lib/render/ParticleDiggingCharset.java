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

package pl.asie.charset.lib.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ParticleDiggingCharset extends ParticleDigging {
	private final IBlockState sourceState;
	private final int particleTintIndex;

	public ParticleDiggingCharset(World worldIn, double xIn, double yIn, double zIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, IBlockState state, BlockPos pos, TextureAtlasSprite sprite, int particleTintIndex) {
		super(worldIn, xIn, yIn, zIn, xSpeedIn, ySpeedIn, zSpeedIn, state);
		this.sourceState = state;
		this.particleTintIndex = particleTintIndex;
		setBlockPos(pos);
		setParticleTexture(sprite);
	}

	@Override
	protected void multiplyColor(@Nullable BlockPos p_187154_1_) {
		if (particleTintIndex >= 0) {
			int i = Minecraft.getMinecraft().getBlockColors().colorMultiplier(this.sourceState, this.world, p_187154_1_, particleTintIndex);
			if (i != -1) {
				this.particleRed *= (float) (i >> 16 & 255) / 255.0F;
				this.particleGreen *= (float) (i >> 8 & 255) / 255.0F;
				this.particleBlue *= (float) (i & 255) / 255.0F;
			}
		}
	}
}
