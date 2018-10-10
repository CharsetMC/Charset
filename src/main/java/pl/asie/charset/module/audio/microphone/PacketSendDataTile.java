package pl.asie.charset.module.audio.microphone;

import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import pl.asie.charset.api.CharsetAPI;
import pl.asie.charset.api.audio.AudioData;
import pl.asie.charset.lib.network.PacketTile;

import java.util.UUID;

public class PacketSendDataTile extends PacketTile {
	private UUID senderId;
	private AudioData data;

	public PacketSendDataTile() {
		super();
	}

	public PacketSendDataTile(TileEntity tile, UUID senderId, AudioData data) {
		super(tile);
		this.senderId = senderId;
		this.data = data;
	}

	@Override
	public void writeData(PacketBuffer buf) {
		super.writeData(buf);
		buf.writeUniqueId(senderId);
		buf.writeShort(CharsetAPI.INSTANCE.findSimpleInstantiatingRegistry(AudioData.class).getId(data));
		data.writeData(buf);
	}

	@Override
	public void readData(INetHandler handler, PacketBuffer buf) {
		super.readData(handler, buf);
		senderId = buf.readUniqueId();
		data = CharsetAPI.INSTANCE.findSimpleInstantiatingRegistry(AudioData.class).create(buf.readUnsignedShort());
		data.readData(buf);
	}

	@Override
	public void apply(INetHandler handler) {
		super.apply(handler);

		if (tile != null && tile.hasCapability(CharsetAudioMicrophone.WIRELESS_AUDIO_RECEIVER, null)) {
			IWirelessAudioReceiver receiver = tile.getCapability(CharsetAudioMicrophone.WIRELESS_AUDIO_RECEIVER, null);
			//noinspection ConstantConditions
			receiver.receiveWireless(senderId, data);
		}
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}
}
