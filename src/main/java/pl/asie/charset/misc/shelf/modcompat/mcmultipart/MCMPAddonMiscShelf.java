package pl.asie.charset.misc.shelf.modcompat.mcmultipart;

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
import pl.asie.charset.lib.modcompat.mcmultipart.CharsetMCMPAddon;
import pl.asie.charset.lib.modcompat.mcmultipart.MCMPAddonBase;
import pl.asie.charset.lib.modcompat.mcmultipart.MultipartTile;
import pl.asie.charset.misc.shelf.CharsetMiscShelf;
import pl.asie.charset.misc.shelf.TileShelf;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@CharsetMCMPAddon("misc.shelf")
public class MCMPAddonMiscShelf extends MCMPAddonBase {
    public MCMPAddonMiscShelf() {
        super(CharsetMiscShelf.shelfBlock, CharsetMiscShelf.shelfItem,
                MultipartShelf::new, (tile) -> new MultipartTileShelf((TileShelf) tile),
                (tile) -> tile instanceof TileShelf);
    }
}
