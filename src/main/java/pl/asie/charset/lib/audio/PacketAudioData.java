package pl.asie.charset.lib.audio;

import io.netty.buffer.ByteBuf;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.INetHandler;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import pl.asie.charset.api.audio.AudioPacket;
import pl.asie.charset.api.audio.IAudioSink;
import pl.asie.charset.api.audio.IAudioSinkEntity;
import pl.asie.charset.lib.audio.manager.AudioStreamManagerClient;
import pl.asie.charset.lib.audio.manager.AudioStreamOpenAL;
import pl.asie.charset.lib.audio.manager.IAudioStream;
import pl.asie.charset.lib.network.Packet;
import pl.asie.charset.lib.utils.DFPWM;

import java.util.HashSet;
import java.util.Set;

public class PacketAudioData extends Packet {
	private class AudioSinkInternal implements IAudioSink {
		private final World world;
		private final Vec3d pos;
		private final float hd, v;

		public AudioSinkInternal(World world, Vec3d pos, float hd, float v) {
			this.world = world;
			this.pos = pos;
			this.hd = hd;
			this.v = v;
		}

		@Override
		public World getWorld() {
			return world;
		}

		@Override
		public Vec3d getPos() {
			return pos;
		}

		@Override
		public float getHearingDistance() {
			return hd;
		}

		@Override
		public float getVolume() {
			return v;
		}

		@Override
		public boolean receive(AudioPacket packet) {
			return false;
		}
	}

	public enum Codec {
		DFPWM;

		public static final Codec[] VALUES = values();
	}

	private static final DFPWM dfpwm = new DFPWM();
	private Codec codec;
	private Set<IAudioSink> sinkSet;
	private byte[] packet;
	private int id, time;

	public PacketAudioData() {
		super();

		this.sinkSet = new HashSet<>();
	}

	protected PacketAudioData(int id, Codec codec, Set<IAudioSink> sinkSet, byte[] packet, int time) {
		super();

		this.id = id;
		this.codec = codec;
		this.sinkSet = sinkSet;
		this.time = time;
		this.packet = packet;
	}

	@Override
	public void writeData(ByteBuf buf) {
		buf.writeInt(id);
		buf.writeByte(codec.ordinal());

		buf.writeMedium(sinkSet.size());
		for (IAudioSink sink : sinkSet) {
			buf.writeFloat(sink.getVolume());
			buf.writeFloat(sink.getHearingDistance());
			if (sink instanceof IAudioSinkEntity) {
				buf.writeInt(Integer.MIN_VALUE);
				buf.writeInt(sink.getWorld().provider.getDimension());
				buf.writeInt(((IAudioSinkEntity) sink).getEntity().getEntityId());
			} else {
				buf.writeInt(sink.getWorld().provider.getDimension());
				buf.writeFloat((float) sink.getPos().xCoord);
				buf.writeFloat((float) sink.getPos().yCoord);
				buf.writeFloat((float) sink.getPos().zCoord);
			}
		}

		buf.writeMedium(time);

		buf.writeMedium(packet.length);
		buf.writeBytes(packet);
	}

	@Override
	public void readData(INetHandler handler, ByteBuf buf) {
		id = buf.readInt();
		codec = Codec.VALUES[buf.readByte()];

		int sinkSize = buf.readUnsignedMedium();
		for (int i = 0; i < sinkSize; i++) {
			float v = buf.readFloat();
			float hd = buf.readFloat();
			int w = buf.readInt();
			World world = Minecraft.getMinecraft().theWorld;

			if (w == Integer.MIN_VALUE) {
				w = buf.readInt();
				int entityId = buf.readInt();
				if (world.provider.getDimension() == w) {
					Entity entity = world.getEntityByID(entityId);
					if (entity != null) {
						sinkSet.add(new AudioSinkInternal(entity.getEntityWorld(), new Vec3d(entity.posX, entity.posY, entity.posZ), hd, v));
					}
				}
			} else {
				float x = buf.readFloat();
				float y = buf.readFloat();
				float z = buf.readFloat();
				if (world.provider.getDimension() == w) {
					sinkSet.add(new AudioSinkInternal(world, new Vec3d(x, y, z), hd, v));
				}
			}
		}

		time = buf.readUnsignedMedium();

		int packetLen = buf.readMedium();
		packet = new byte[packetLen];
		buf.readBytes(packet);

		if (time > 0) {
			float timeSec = (float) time / 20.0f;
			IAudioStream stream = AudioStreamManagerClient.INSTANCE.get(id);
			if (stream == null) {
				stream = new AudioStreamOpenAL(false, false, 8);
				AudioStreamManagerClient.INSTANCE.put(id, stream);
			}

			byte[] out = new byte[packet.length * 8];
			dfpwm.decompress(out, packet, 0, 0, packet.length);
			for (int i = 0; i < out.length; i++) {
				out[i] = (byte) (out[i] ^ 0x80);
			}

			stream.setSampleRate(Math.round(out.length / timeSec));

			/* if (part instanceof PartTapeDrive) {
				ItemStack tape = ((PartTapeDrive) part).invWrapper.getStackInSlot(0);
				if (tape != null && ModCharsetAudio.tapeItem.getMaterial(tape) == ItemTape.Material.SOUL_SAND) {
					int j = 0;
					byte[] rout = out;
					out = new byte[rout.length];
					int rspeed = part.getWorld().rand.nextInt(20) + 5;
					int rand = 3;
					for (int i = 0; i < out.length; i++) {
						if ((i % 4) == 0) {
							rand = part.getWorld().rand.nextInt(rspeed);
						}
						if (j >= out.length) {
							j = out.length - 1;
						}
						out[i] = rout[j];
						if (rand == 0) {
							j += 2;
						} if (rand != 1 && rand != 2) {
							j++;
						}
					}
				}
			} */

			stream.push(out);
			for (IAudioSink sink : sinkSet) {
				stream.play((float) sink.getPos().xCoord, (float) sink.getPos().yCoord, (float) sink.getPos().zCoord,
						sink.getHearingDistance(), sink.getVolume());
			}
		}
	}
}
