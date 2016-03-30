package pl.asie.charset.audio.note;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.INetHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumParticleTypes;

import pl.asie.charset.lib.network.PacketTile;

/**
 * Created by asie on 1/9/16.
 */
public class PacketNoteParticle extends PacketTile {
	protected int note;

	public PacketNoteParticle() {
		super();
	}

	public PacketNoteParticle(TileEntity tile, int note) {
		this.tile = tile;
		this.note = note;
	}

	@Override
	public void readData(INetHandler handler, ByteBuf buf) {
		super.readData(handler, buf);
		note = buf.readByte();
		if (tile != null) {
			BlockPos pos = tile.getPos();
			double noteX = (((note - TileIronNote.MIN_NOTE) % 6) + 0.5D) / 5.0D;
			double noteZ = (((note - TileIronNote.MIN_NOTE) / 6) + 0.5D) / 5.0D;
			tile.getWorld().spawnParticle(EnumParticleTypes.NOTE, (double) pos.getX() + noteX, (double) pos.getY() + 1.2D, (double) pos.getZ() + noteZ,
					(double) (note - TileIronNote.MIN_NOTE) / (double) (TileIronNote.MAX_NOTE - TileIronNote.MIN_NOTE), 0.0D, 0.0D, new int[0]);
		}
	}

	@Override
	public void writeData(ByteBuf buf) {
		super.writeData(buf);
		buf.writeByte(note);
	}
}
