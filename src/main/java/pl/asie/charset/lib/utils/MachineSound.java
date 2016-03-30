package pl.asie.charset.lib.utils;

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
