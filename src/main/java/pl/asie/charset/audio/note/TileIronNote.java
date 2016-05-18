package pl.asie.charset.audio.note;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;

import pl.asie.charset.api.wires.IBundledReceiver;
import pl.asie.charset.audio.ModCharsetAudio;
import pl.asie.charset.lib.Capabilities;

public class TileIronNote extends TileEntity {
	private class RedstoneCommunications implements IBundledReceiver {
		private final EnumFacing side;

		RedstoneCommunications(EnumFacing side) {
			this.side = side;
		}

		@Override
		public void onBundledInputChange() {
			onBundledInputChanged(side);
		}
	}

	public static final int MIN_NOTE = 0;
	public static final int MAX_NOTE = 24;

	private static final String[] INSTRUMENTS = {"harp", "bd", "snare", "hat", "bassattack"};
	private final RedstoneCommunications[] COMMS = new RedstoneCommunications[6];
	private byte[] lastInput = new byte[16];

	public TileIronNote() {
		for (int i = 0; i < 6; i++) {
			COMMS[i] = new RedstoneCommunications(EnumFacing.getFront(i));
		}
	}

	private String getInstrument(int id) {
		if (id < 0 || id >= INSTRUMENTS.length) {
			id = 0;
		}

		return INSTRUMENTS[id];
	}

	protected boolean canPlayNote() {
		return worldObj.isAirBlock(pos.up());
	}

	public int getInstrumentID() {
		IBlockState state = worldObj.getBlockState(pos.down());
		Material material = state.getBlock().getMaterial(state);
		if (material == Material.ROCK) return 1;
		else if (material == Material.SAND) return 2;
		else if (material == Material.GLASS) return 3;
		else if (material == Material.WOOD) return 4;
		else return 0;
	}

	protected void playNote(int note, int instrument) {
		if (instrument >= 0 && instrument <= 4) {
			net.minecraftforge.event.world.NoteBlockEvent.Play e = new net.minecraftforge.event.world.NoteBlockEvent.Play(
					worldObj, pos, worldObj.getBlockState(pos), note, instrument);
			if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(e)) {
				return;
			}

			instrument = e.getInstrument().ordinal();
			note = e.getVanillaNoteId();
		}
		if (note < 0) {
			note = 0;
		}
		if (note > 24) {
			note = 24;
		}
		float f = (float) Math.pow(2.0D, (double) (note - 12) / 12.0D);
		worldObj.playSound((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, new SoundEvent(new ResourceLocation("note." + this.getInstrument(instrument))), SoundCategory.BLOCKS, 3.0F, f, true);
		ModCharsetAudio.packet.sendToAllAround(new PacketNoteParticle(this, note), this, 32);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		lastInput = compound.getByteArray("lastInput");
		if (lastInput == null || lastInput.length != 16) {
			lastInput = new byte[16];
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setByteArray("lastInput", lastInput);
		return compound;
	}

	private void onBundledInputChanged(EnumFacing face) {
		if (!canPlayNote()) {
			return;
		}

		BlockPos pos = getPos().offset(face);
		TileEntity tileEntity = worldObj.getTileEntity(pos);

		if (tileEntity != null && tileEntity.hasCapability(Capabilities.BUNDLED_EMITTER, face.getOpposite())) {
			byte[] input = tileEntity.getCapability(Capabilities.BUNDLED_EMITTER, face.getOpposite()).getBundledSignal();

			if (input == null) {
				input = new byte[16];
			}

			for (int i = 0; i < 16; i++) {
				if (lastInput[i] != input[i] && input[i] > 0) {
					playNote(i, getInstrumentID());
				}
			}

			lastInput = input.clone();
		} else {
			lastInput = new byte[16];
		}
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (capability == Capabilities.BUNDLED_RECEIVER) {
			return facing != null;
		} else {
			return super.hasCapability(capability, facing);
		}
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == Capabilities.BUNDLED_RECEIVER && facing != null) {
			return (T) COMMS[facing.ordinal()];
		} else {
			return super.getCapability(capability, facing);
		}
	}
}
