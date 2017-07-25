package pl.asie.charset.module.audio.note;

import com.google.common.base.Predicate;
import net.minecraft.block.BlockNote;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityNote;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.NoteBlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.charset.ModCharset;
import pl.asie.charset.api.audio.AudioPacket;
import pl.asie.charset.api.audio.IAudioSource;
import pl.asie.charset.lib.audio.types.AudioDataGameSound;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.capability.CapabilityProviderFactory;
import pl.asie.charset.lib.capability.audio.DefaultAudioSource;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.lib.utils.FunctionalUtils;
import pl.asie.charset.lib.utils.MethodHandleHelper;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@CharsetModule(
        name = "audio.noteblock",
        description = "Noteblock rework. WIP",
        profile = ModuleProfile.TESTING
)
public class CharsetAudioNoteblock {
    private static final MethodHandle GET_INSTRUMENT = MethodHandleHelper.findMethod(BlockNote.class, "getInstrument", "func_185576_e", int.class);
    private static final Supplier<CapabilityProviderFactory<IAudioSource>> PROVIDER = FunctionalUtils.lazySupplier(() -> new CapabilityProviderFactory<>(Capabilities.AUDIO_SOURCE));
    private static final ResourceLocation NOTE_SOURCE_KEY = new ResourceLocation("charsetaudio:noteSource");

    @CharsetModule.PacketRegistry
    public PacketRegistry packet;

    public static SoundEvent getSound(World world, BlockPos pos, int id) {
        if (GET_INSTRUMENT == null) {
            ModCharset.logger.error("BlockNote.getInstrument not found! This is bad!");
        }

        try {
            return (GET_INSTRUMENT != null ? (SoundEvent) GET_INSTRUMENT.invokeExact((BlockNote) Blocks.NOTEBLOCK, (int) id) : SoundEvents.BLOCK_NOTE_HARP);
        } catch (Throwable e) {
            e.printStackTrace();
            return SoundEvents.BLOCK_NOTE_HARP;
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        packet.registerPacket(0x01, PacketNoteParticle.class);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onNoteEvent(NoteBlockEvent.Play event) {
        World worldIn = event.getWorld();
        BlockPos pos = event.getPos();
        int param = event.getVanillaNoteId();
        SoundEvent sound = getSound(event.getWorld(), event.getPos(), event.getInstrument().ordinal());
        float pitch = (float)Math.pow(2.0D, (double)(param - 12) / 12.0D);

        if (!worldIn.isRemote) {
            TileEntity note = worldIn.getTileEntity(pos);
            if (note != null && note.hasCapability(Capabilities.AUDIO_SOURCE, null)) {
                AudioDataGameSound dataSound = new AudioDataGameSound(SoundEvent.REGISTRY.getNameForObject(sound).toString(), pitch);
                AudioPacket packet = new AudioPacket(dataSound, 1.0f);

                for (EnumFacing facing : EnumFacing.VALUES) {
                    TileEntity tile = event.getWorld().getTileEntity(pos.offset(facing));
                    if (tile != null && tile.hasCapability(Capabilities.AUDIO_RECEIVER, facing.getOpposite())) {
                        tile.getCapability(Capabilities.AUDIO_RECEIVER, facing.getOpposite()).receive(packet);
                    }
                }

                if (packet.getSinkCount() > 0) {
                    event.setCanceled(true);
                    packet.send();
                    return;
                }
            }

            // Did not send sound via speaker - use default implementation *for impl note blocks*
            if (event.getState().getBlock() == Blocks.NOTEBLOCK) {
                event.setCanceled(true);

                worldIn.playSound(null, pos, sound, SoundCategory.BLOCKS, 3.0F, pitch);
                packet.sendToAllAround(new PacketNoteParticle(note, param), note, 32);
            }
        }
    }

    @SubscribeEvent
    public void onAttach(AttachCapabilitiesEvent<TileEntity> event) {
        if (event.getObject() instanceof TileEntityNote) {
            event.addCapability(NOTE_SOURCE_KEY, PROVIDER.get().create(new DefaultAudioSource()));
        }
    }
}
