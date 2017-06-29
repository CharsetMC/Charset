package pl.asie.charset.module.misc.scaffold.modcompat.mcmultipart;

import com.sun.org.apache.xpath.internal.operations.Mult;
import mcmultipart.api.slot.IPartSlot;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.modcompat.mcmultipart.CharsetMCMPAddon;
import pl.asie.charset.lib.modcompat.mcmultipart.MCMPAddonBase;
import pl.asie.charset.module.misc.scaffold.CharsetMiscScaffold;
import pl.asie.charset.module.misc.scaffold.TileScaffold;

@CharsetMCMPAddon("misc.scaffold")
public class MCMPAddonScaffold extends MCMPAddonBase {
    public MCMPAddonScaffold() {
        super(CharsetMiscScaffold.scaffoldBlock, CharsetMiscScaffold.scaffoldItem,
                MultipartScaffold::new, (tile) -> tile instanceof TileScaffold);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterSlots(RegistryEvent.Register<IPartSlot> event) {
        event.getRegistry().register(MultipartScaffold.Slot.INSTANCE);
    }
}