package pl.asie.charset.audio.note;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityNote;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.NoteBlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.api.audio.AudioPacket;
import pl.asie.charset.audio.ModCharsetAudio;
import pl.asie.charset.lib.Capabilities;
import pl.asie.charset.lib.audio.AudioDataSound;
import pl.asie.charset.lib.capability.DefaultAudioSource;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoteBlockManager {
    private static final Map<Predicate<IBlockState>, SoundEvent> INSTRUMENTS = new HashMap<>();
    private static final SoundEvent DEFAULT_INSTRUMENT;
    private static final ResourceLocation NOTE_SOURCE_KEY = new ResourceLocation("charsetaudio:noteSource");

    static {
        DEFAULT_INSTRUMENT = SoundEvents.BLOCK_NOTE_HARP;

        INSTRUMENTS.put(input -> input.getMaterial() == Material.ROCK, SoundEvents.BLOCK_NOTE_BASEDRUM);
        INSTRUMENTS.put(input -> input.getMaterial() == Material.SAND, SoundEvents.BLOCK_NOTE_SNARE);
        INSTRUMENTS.put(input -> input.getMaterial() == Material.GLASS, SoundEvents.BLOCK_NOTE_HAT);
        INSTRUMENTS.put(input -> input.getMaterial() == Material.WOOD, SoundEvents.BLOCK_NOTE_BASS);
    }

    public static SoundEvent getSound(IBlockState state) {
        for (Predicate<IBlockState> predicate : INSTRUMENTS.keySet()) {
            if (predicate.apply(state)) {
                return INSTRUMENTS.get(predicate);
            }
        }

        return DEFAULT_INSTRUMENT;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onNoteEvent(NoteBlockEvent.Play event) {
        event.setCanceled(true);
        World worldIn = event.getWorld();
        BlockPos pos = event.getPos();
        SoundEvent sound = getSound(worldIn.getBlockState(pos.offset(EnumFacing.DOWN)));
        int param = event.getVanillaNoteId();
        float pitch = (float)Math.pow(2.0D, (double)(param - 12) / 12.0D);

        if (!worldIn.isRemote) {
            TileEntity note = worldIn.getTileEntity(pos);
            if (note != null && note.hasCapability(Capabilities.AUDIO_SOURCE, null)) {
                AudioDataSound dataSound = new AudioDataSound(sound.getSoundName().toString(), pitch);
                AudioPacket packet = new AudioPacket(dataSound, 1.0f);

                for (EnumFacing facing : EnumFacing.VALUES) {
                    TileEntity tile = event.getWorld().getTileEntity(pos.offset(facing));
                    if (tile != null && tile.hasCapability(Capabilities.AUDIO_RECEIVER, facing.getOpposite())) {
                        tile.getCapability(Capabilities.AUDIO_RECEIVER, facing.getOpposite()).receive(packet);
                    }
                }

                if (packet.getSinkCount() > 0) {
                    packet.send();
                    return;
                }
            }

            // Did not send sound via speaker - use default implementation
            worldIn.playSound(null, pos, sound, SoundCategory.BLOCKS, 3.0F, pitch);
            ModCharsetAudio.packet.sendToAllAround(new PacketNoteParticle(note, param), note, 32);
        }
    }

    @SubscribeEvent
    public void onAttach(AttachCapabilitiesEvent.TileEntity event) {
        if (event.getTileEntity() instanceof TileEntityNote) {
            event.addCapability(NOTE_SOURCE_KEY, new ICapabilityProvider() {
                private final DefaultAudioSource source = new DefaultAudioSource();

                @Override
                public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
                    return capability == Capabilities.AUDIO_SOURCE;
                }

                @Override
                public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
                    return capability == Capabilities.AUDIO_SOURCE ? Capabilities.AUDIO_SOURCE.cast(source) : null;
                }
            });
        }
    }
}
