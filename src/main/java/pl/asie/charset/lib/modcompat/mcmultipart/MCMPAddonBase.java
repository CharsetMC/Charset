package pl.asie.charset.lib.modcompat.mcmultipart;

import mcmultipart.api.addon.IMCMPAddon;
import mcmultipart.api.addon.IWrappedBlock;
import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.multipart.IMultipartRegistry;
import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.ref.MCMPCapabilities;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.capability.CapabilityProviderFactory;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class MCMPAddonBase implements IMCMPAddon {
    private static final ResourceLocation KEY = new ResourceLocation("charset:multipart");
    protected final Block block;
    protected final Item item;
    protected final Supplier<IMultipart> multipartSupplier;
    protected final Function<TileEntity, IMultipartTile> multipartTileSupplier;
    protected final Predicate<TileEntity> tileEntityPredicate;
    private CapabilityProviderFactory<IMultipartTile> factory;

    public MCMPAddonBase(Block block, Item item, Supplier<IMultipart> multipartSupplier, Predicate<TileEntity> tileEntityPredicate) {
        this(block, item, multipartSupplier, IMultipartTile::wrap, tileEntityPredicate);
    }

    public MCMPAddonBase(Block block, Item item, Supplier<IMultipart> multipartSupplier, Function<TileEntity, IMultipartTile> multipartTileSupplier, Predicate<TileEntity> tileEntityPredicate) {
        this.block = block;
        this.item = item;
        this.multipartSupplier = multipartSupplier;
        this.multipartTileSupplier = multipartTileSupplier;
        this.tileEntityPredicate = tileEntityPredicate;

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public final void registerParts(IMultipartRegistry registry) {
        registry.registerPartWrapper(block, multipartSupplier.get());
        registerStackWrapper(registry);
        factory = new CapabilityProviderFactory<>(MCMPCapabilities.MULTIPART_TILE);
    }

    protected IWrappedBlock registerStackWrapper(IMultipartRegistry registry) {
        return registry.registerStackWrapper(item, (stack) -> true, block);
    }

    @SubscribeEvent
    public final void onAttachTile(AttachCapabilitiesEvent<TileEntity> event) {
        if (tileEntityPredicate.test(event.getObject())) {
            final IMultipartTile multipartTile = multipartTileSupplier.apply(event.getObject());
            event.addCapability(KEY, factory.create(multipartTile));
        }
    }
}
