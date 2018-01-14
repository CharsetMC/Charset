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

package pl.asie.charset.lib.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ParticleBlockDustCharset extends ParticleDiggingCharset {
    public ParticleBlockDustCharset(World worldIn, double xIn, double yIn, double zIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, IBlockState state, BlockPos pos, TextureAtlasSprite sprite, int particleTintIndex) {
        super(worldIn, xIn, yIn, zIn, xSpeedIn, ySpeedIn, zSpeedIn, state, pos, sprite, particleTintIndex);
        this.motionX = xSpeedIn;
        this.motionY = ySpeedIn;
        this.motionZ = zSpeedIn;
    }
}
