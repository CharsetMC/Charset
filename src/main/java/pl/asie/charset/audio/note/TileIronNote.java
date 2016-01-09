package pl.asie.charset.audio.note;

import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;

import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.MultipartHelper;
import pl.asie.charset.api.wires.IBundledEmitter;
import pl.asie.charset.api.wires.IBundledUpdatable;
import pl.asie.charset.api.wires.IConnectable;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.api.wires.WireType;

public class TileIronNote extends TileEntity implements IBundledUpdatable, IConnectable {
    private static final String[] INSTRUMENTS = {"harp", "bd", "snare", "hat", "bassattack"};
    private byte[] lastInput = new byte[16];

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
        Material material = worldObj.getBlockState(pos.down()).getBlock().getMaterial();
        if (material == Material.rock) return 1;
        else if (material == Material.sand) return 2;
        else if (material == Material.glass) return 3;
        else if (material == Material.wood) return 4;
        else return 0;
    }

    protected void playNote(int note, int instrument) {
        if (instrument >= 0 && instrument <= 4) {
            net.minecraftforge.event.world.NoteBlockEvent.Play e = new net.minecraftforge.event.world.NoteBlockEvent.Play(
                    worldObj, pos, worldObj.getBlockState(pos), note, instrument);
            if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(e)) {
                return;
            }

            instrument = e.instrument.ordinal();
            note = e.getVanillaNoteId();
        }
        float f = (float)Math.pow(2.0D, (double)(note - 12) / 12.0D);
        worldObj.playSoundEffect((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, "note." + this.getInstrument(instrument), 3.0F, f);
        worldObj.spawnParticle(EnumParticleTypes.NOTE, (double)pos.getX() + 0.5D, (double)pos.getY() + 1.2D, (double)pos.getZ() + 0.5D, (double)note / 24.0D, 0.0D, 0.0D, new int[0]);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        lastInput = compound.getByteArray("lastInput");
        if (lastInput == null || lastInput.length != 16) {
            lastInput = new byte[16];
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        compound.setByteArray("lastInput", lastInput);
    }

    @Override
    public void onBundledInputChanged(EnumFacing face) {
        byte[] input;
        BlockPos pos = getPos().offset(face);
        TileEntity tileEntity = worldObj.getTileEntity(pos);

        if (tileEntity instanceof IBundledEmitter) {
            input = ((IBundledEmitter) tileEntity).getBundledSignal(null, face.getOpposite());
        } else {
            input = new byte[16];
            IMultipartContainer container = MultipartHelper.getPartContainer(worldObj, pos);

            for (IMultipart part : container.getParts()) {
                if (part instanceof IBundledEmitter) {
                    IBundledEmitter emitter = (IBundledEmitter) part;
                    byte[] data = emitter.getBundledSignal(null, face.getOpposite());
                    if (data != null) {
                        for (int i = 0; i < 16; i++) {
                            if (data[i] > input[i]) {
                                input[i] = data[i];
                            }
                        }
                    }
                }
            }
        }

        if (canPlayNote()) {
            if (input != null) {
                for (int i = 0; i < 16; i++) {
                    if (lastInput[i] < input[i]) {
                        playNote(getInstrumentID(), i + 4);
                    }
                }
            }
        }

        lastInput = input;
    }

    @Override
    public boolean canConnect(WireType type, WireFace face, EnumFacing direction) {
        return type == WireType.BUNDLED;
    }
}
