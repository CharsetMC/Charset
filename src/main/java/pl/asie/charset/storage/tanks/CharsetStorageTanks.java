package pl.asie.charset.storage.tanks;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapedOreRecipe;
import pl.asie.charset.lib.annotation.CharsetModule;
import pl.asie.charset.lib.utils.RegistryUtils;

@CharsetModule(
        name = "storage.tanks",
        description = "Simple BuildCraft-style vertical tanks"
)
public class CharsetStorageTanks {
    public static Block tankBlock;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        tankBlock = new BlockTank();
        RegistryUtils.register(tankBlock, new ItemBlock(tankBlock), "fluidTank");

        RegistryUtils.registerModel(tankBlock, 0, "charset:fluidtank");

        FMLInterModComms.sendMessage("charset", "addCarry", tankBlock.getRegistryName());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        RegistryUtils.register(TileTank.class, "fluidTank");
        GameRegistry.addRecipe(new ShapedOreRecipe(tankBlock,
                "xxx",
                "x x",
                "xxx",
                'x', "blockGlass"
        ));
    }

    @Mod.EventHandler
    @SideOnly(Side.CLIENT)
    public void initClient(FMLInitializationEvent event) {
        ClientRegistry.bindTileEntitySpecialRenderer(TileTank.class, new TileTankRenderer());
    }
}
