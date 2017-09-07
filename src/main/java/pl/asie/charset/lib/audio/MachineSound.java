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

package pl.asie.charset.lib.audio;

import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

/**
 * @author SleepyTrousers, Vexatos
 */
public class MachineSound extends PositionedSound implements ITickableSound {

	private boolean donePlaying;

	public MachineSound(ResourceLocation sound, SoundCategory category, float x, float y, float z, float volume, float pitch) {
		this(sound, category, x, y, z, volume, pitch, true);
	}

	public MachineSound(ResourceLocation sound, SoundCategory category, float x, float y, float z, float volume, float pitch, boolean repeat) {
		super(sound, category);
		this.xPosF = x;
		this.yPosF = y;
		this.zPosF = z;
		this.volume = volume;
		this.pitch = pitch;
		this.repeat = repeat;
	}

	@Override
	public void update() {
	}

	public void endPlaying() {
		donePlaying = true;
	}

	public void startPlaying() {
		donePlaying = false;
	}

	@Override
	public boolean isDonePlaying() {
		return donePlaying;
	}

}
