package pl.asie.charset.pipes.modcompat.mcmultipart;

import com.google.common.base.Predicates;
import mcmultipart.api.addon.IMCMPAddon;
import mcmultipart.api.addon.IWrappedBlock;
import mcmultipart.api.capability.MCMPCapabilities;
import mcmultipart.api.multipart.IMultipartRegistry;
import mcmultipart.api.multipart.IMultipartTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.capability.CapabilityProviderFactory;
import pl.asie.charset.lib.modcompat.mcmultipart.CharsetMCMPAddon;
import pl.asie.charset.pipes.CharsetPipes;
import pl.asie.charset.pipes.pipe.TilePipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@CharsetMCMPAddon("pipes")
public class MCMPAddonPipes implements IMCMPAddon {
    private static final ResourceLocation KEY = new ResourceLocation("charset:pipeMultipart");
    private CapabilityProviderFactory<IMultipartTile> factory;

    public MCMPAddonPipes() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void registerParts(IMultipartRegistry registry) {
        registry.registerPartWrapper(CharsetPipes.blockPipe, new MultipartPipe());
        IWrappedBlock pipeBlockWrapper = registry.registerStackWrapper(CharsetPipes.itemPipe, (stack) -> true, CharsetPipes.blockPipe);
        factory = new CapabilityProviderFactory<>(MCMPCapabilities.MULTIPART_TILE);
    }

    @SubscribeEvent
    public void onAttachTile(AttachCapabilitiesEvent<TileEntity> event) {
        if (event.getObject() instanceof TilePipe) {
            final IMultipartTile multipartTile = new MultipartTilePipe((TilePipe) event.getObject());
            event.addCapability(KEY, factory.create(multipartTile));
        }
    }
}
