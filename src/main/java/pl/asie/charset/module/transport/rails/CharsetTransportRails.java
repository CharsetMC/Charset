package pl.asie.charset.module.transport.rails;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.utils.RegistryUtils;

@CharsetModule(
        name = "transport.rails",
        description = "A small assortment of useful rails"
)
public class CharsetTransportRails {
    public static BlockRailCharset blockRailCross;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        blockRailCross = new BlockRailCharset();
        RegistryUtils.register(blockRailCross, new ItemBlock(blockRailCross),"rail_charset");
        RegistryUtils.registerModel(blockRailCross, 0, "charset:rail_charset");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        GameRegistry.addShapedRecipe(new ItemStack(CharsetTransportRails.blockRailCross, 2), " r ", "r r", " r ", 'r', new ItemStack(Blocks.RAIL));
    }
}
