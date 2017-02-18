package pl.asie.charset.misc.shelf.modcompat.mcmultipart;

import mcmultipart.api.addon.IMCMPAddon;
import mcmultipart.api.addon.IWrappedBlock;
import mcmultipart.api.capability.MCMPCapabilities;
import mcmultipart.api.multipart.IMultipartRegistry;
import mcmultipart.api.multipart.IMultipartTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.capability.CapabilityProviderFactory;
import pl.asie.charset.lib.modcompat.mcmultipart.CharsetMCMPAddon;
import pl.asie.charset.misc.shelf.CharsetMiscShelf;
import pl.asie.charset.misc.shelf.TileShelf;

@CharsetMCMPAddon("misc.shelf")
public class MCMPAddonMiscShelf implements IMCMPAddon {
    private static final ResourceLocation KEY = new ResourceLocation("charset:pipeMultipart");
    private CapabilityProviderFactory<IMultipartTile> factory;

    public MCMPAddonMiscShelf() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void registerParts(IMultipartRegistry registry) {
        registry.registerPartWrapper(CharsetMiscShelf.shelfBlock, new MultipartShelf());
        IWrappedBlock pipeBlockWrapper = registry.registerStackWrapper(CharsetMiscShelf.shelfItem, (stack) -> true, CharsetMiscShelf.shelfBlock);
        factory = new CapabilityProviderFactory<>(MCMPCapabilities.MULTIPART_TILE);
    }

    @SubscribeEvent
    public void onAttachTile(AttachCapabilitiesEvent<TileEntity> event) {
        if (event.getObject() instanceof TileShelf) {
            final IMultipartTile multipartTile = new MultipartTileShelf((TileShelf) event.getObject());
            event.addCapability(KEY, factory.create(multipartTile));
        }
    }
}
