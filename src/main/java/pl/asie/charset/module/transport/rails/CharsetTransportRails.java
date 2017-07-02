package pl.asie.charset.module.transport.rails;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.item.ItemBlockBase;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.RegistryUtils;

@CharsetModule(
        name = "transport.rails",
        description = "A small assortment of useful rails",
        profile = ModuleProfile.STABLE
)
public class CharsetTransportRails {
    public static BlockRailCharset blockRailCross;
    public static ItemBlock itemRailCross;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        blockRailCross = new BlockRailCharset();
        itemRailCross = new ItemBlockBase(blockRailCross);
    }

    @SubscribeEvent
    public void registerModels(ModelRegistryEvent event) {
        RegistryUtils.registerModel(itemRailCross, 0, "charset:rail_charset");
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        RegistryUtils.register(event.getRegistry(), blockRailCross, "rail_charset");
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        RegistryUtils.register(event.getRegistry(), itemRailCross, "rail_charset");
    }
}
