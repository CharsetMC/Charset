package pl.asie.charset.audio.tape;

import io.netty.buffer.ByteBuf;

import mcmultipart.multipart.IMultipart;
import pl.asie.charset.api.audio.IAudioSource;
import pl.asie.charset.audio.ProxyClient;
import pl.asie.charset.audio.manager.AudioStreamOpenAL;
import pl.asie.charset.audio.manager.IAudioStream;
import pl.asie.charset.lib.network.PacketPart;
import pl.asie.charset.lib.utils.DFPWM;

public class PacketDriveAudio extends PacketPart {
	private static final DFPWM dfpwm = new DFPWM();
	private byte[] packet;

	public PacketDriveAudio() {
		super();
	}

	public PacketDriveAudio(IMultipart part, byte[] packet) {
		super(part);
		this.packet = packet;
	}

	@Override
	public void writeData(ByteBuf buf) {
		super.writeData(buf);

		buf.writeMedium(packet.length);
		buf.writeBytes(packet);
	}

	@Override
	public void readData(ByteBuf buf) {
		super.readData(buf);

		int len = buf.readMedium();
		packet = new byte[len];
		buf.readBytes(packet);

		if (part instanceof IAudioSource) {
			IAudioSource source = (IAudioSource) part;
			IAudioStream stream = ProxyClient.stream.get(source);
			if (stream == null) {
				stream = new AudioStreamOpenAL(false, false, 8);
				stream.setSampleRate(48000);
				stream.setHearing(32.0F, 1.0F);
				ProxyClient.stream.put(source, stream);
			}

			byte[] out = new byte[packet.length * 8];
			dfpwm.decompress(out, packet, 0, 0, packet.length);
			for (int i = 0; i < out.length; i++) {
				out[i] = (byte) (out[i] ^ 0x80);
			}

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
			stream.play(part.getPos().getX(), part.getPos().getY(), part.getPos().getZ());
		}
	}
}
