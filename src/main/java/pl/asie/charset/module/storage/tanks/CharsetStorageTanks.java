package pl.asie.charset.module.storage.tanks;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.item.ItemBlockBase;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.RegistryUtils;

@CharsetModule(
        name = "storage.tanks",
        description = "Simple BuildCraft-style vertical tanks",
        profile = ModuleProfile.STABLE
)
public class CharsetStorageTanks {
    public static Block tankBlock;
    public static Item tankItem;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        tankBlock = new BlockTank();
        tankItem = new ItemBlockBase(tankBlock);

        FMLInterModComms.sendMessage("charset", "addLock", new ResourceLocation("charset:fluidTank"));
    }

    @SubscribeEvent
    public void registerModels(ModelRegistryEvent event) {
        RegistryUtils.registerModel(tankItem, 0, "charset:fluidtank");
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        RegistryUtils.register(event.getRegistry(), tankBlock, "fluidTank");
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        RegistryUtils.register(event.getRegistry(), tankItem, "fluidTank");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        RegistryUtils.register(TileTank.class, "fluidTank");
        FMLInterModComms.sendMessage("charset", "addCarry", tankBlock.getRegistryName());
    }

    @Mod.EventHandler
    @SideOnly(Side.CLIENT)
    public void initClient(FMLInitializationEvent event) {
        ClientRegistry.bindTileEntitySpecialRenderer(TileTank.class, new TileTankRenderer());
    }
}
