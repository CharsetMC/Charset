package pl.asie.charset.audio.note;

import com.google.common.base.Predicate;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.NoteBlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.charset.api.audio.AudioPacket;
import pl.asie.charset.api.audio.IAudioSource;
import pl.asie.charset.lib.annotation.CharsetModule;
import pl.asie.charset.lib.audio.types.AudioDataGameSound;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.capability.CapabilityProviderFactory;
import pl.asie.charset.lib.capability.audio.DefaultAudioSource;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.lib.utils.FunctionalUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

@CharsetModule(
        name = "audio.noteblock",
        description = "Noteblock rework. WIP"
)
public class CharsetAudioNoteblock {
    private static final Supplier<CapabilityProviderFactory<IAudioSource>> PROVIDER = FunctionalUtils.lazySupplier(() -> new CapabilityProviderFactory<>(Capabilities.AUDIO_SOURCE));
    private static final List<Pair<Predicate<IBlockState>, SoundEvent>> INSTRUMENTS = new ArrayList<>();
    private static final SoundEvent DEFAULT_INSTRUMENT;
    private static final ResourceLocation NOTE_SOURCE_KEY = new ResourceLocation("charsetaudio:noteSource");

    @CharsetModule.PacketRegistry
    public PacketRegistry packet;

    static {
        DEFAULT_INSTRUMENT = SoundEvents.BLOCK_NOTE_HARP;

        INSTRUMENTS.add(Pair.of(input -> input.getMaterial() == Material.ROCK, SoundEvents.BLOCK_NOTE_BASEDRUM));
        INSTRUMENTS.add(Pair.of(input -> input.getMaterial() == Material.SAND, SoundEvents.BLOCK_NOTE_SNARE));
        INSTRUMENTS.add(Pair.of(input -> input.getMaterial() == Material.GLASS, SoundEvents.BLOCK_NOTE_HAT));
        INSTRUMENTS.add(Pair.of(input -> input.getMaterial() == Material.WOOD, SoundEvents.BLOCK_NOTE_BASS));
    }

    public static SoundEvent getSound(IBlockState state) {
        for (Pair<Predicate<IBlockState>, SoundEvent> predicate : INSTRUMENTS) {
            if (predicate.getKey().apply(state)) {
                return predicate.getValue();
            }
        }

        return DEFAULT_INSTRUMENT;
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        packet.registerPacket(0x01, PacketNoteParticle.class);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onNoteEvent(NoteBlockEvent.Play event) {
        World worldIn = event.getWorld();
        BlockPos pos = event.getPos();
        SoundEvent sound = getSound(worldIn.getBlockState(pos.offset(EnumFacing.DOWN)));
        int param = event.getVanillaNoteId();
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

            // Did not send sound via speaker - use default implementation *for vanilla note blocks*
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
