package pl.asie.charset.audio.note;

import com.google.common.collect.Lists;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.NoteBlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.api.audio.AudioPacket;
import pl.asie.charset.audio.ModCharsetAudio;
import pl.asie.charset.lib.Capabilities;
import pl.asie.charset.lib.audio.AudioDataSound;
import pl.asie.charset.lib.audio.AudioSinkBlock;
import pl.asie.charset.lib.audio.AudioUtils;
import pl.asie.charset.lib.capability.DefaultAudioSource;

import javax.annotation.Nullable;
import java.util.List;

public class NoteEventHandler {
    private static final List<SoundEvent> INSTRUMENTS = Lists.newArrayList(new SoundEvent[] {SoundEvents.BLOCK_NOTE_HARP, SoundEvents.BLOCK_NOTE_BASEDRUM, SoundEvents.BLOCK_NOTE_SNARE, SoundEvents.BLOCK_NOTE_HAT, SoundEvents.BLOCK_NOTE_BASS});
    private static final ResourceLocation NOTE_SOURCE_KEY = new ResourceLocation("charsetaudio:noteSource");

    @SubscribeEvent
    public void onNoteEvent(NoteBlockEvent.Play event) {
        TileEntity note = event.getWorld().getTileEntity(event.getPos());
        if (note != null && note.hasCapability(Capabilities.AUDIO_SOURCE, null) && !event.getWorld().isRemote) {
            BlockPos pos = event.getPos();
            int param = event.getVanillaNoteId();
            float f = (float)Math.pow(2.0D, (double)(param - 12) / 12.0D);
            AudioDataSound dataSound = new AudioDataSound(INSTRUMENTS.get(event.getInstrument().ordinal()).getSoundName().toString(), f);
            AudioPacket packet = new AudioPacket(dataSound, 1.0f);

            for (EnumFacing facing : EnumFacing.VALUES) {
                TileEntity tile = event.getWorld().getTileEntity(pos.offset(facing));
                if (tile != null && tile.hasCapability(Capabilities.AUDIO_RECEIVER, facing.getOpposite())) {
                    tile.getCapability(Capabilities.AUDIO_RECEIVER, facing.getOpposite()).receive(packet);
                }
            }

            if (packet.getSinkCount() > 0) {
                AudioUtils.send(0, packet);
                event.setCanceled(true);
            }
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
