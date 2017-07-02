package pl.asie.charset.module.overhaul;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.charset.lib.item.ItemBlockBase;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.recipe.RecipeReplacement;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.module.storage.tanks.CharsetStorageTanks;

@CharsetModule(
        name = "overhaul.oneDimension",
        description = "Removes the Nether and End, and adapts recipes for Nether/End items to either remove them or add new ways of crafting them.",
        dependencies = {"tweak.remove.netherPortals"},
        profile = ModuleProfile.VERY_UNSTABLE
)
public class CharsetOverhaulOneDimension {
    public static BlockOreQuartz quartzOreBlock;
    public static ItemBlockBase quartzOreItem;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        quartzOreBlock = new BlockOreQuartz();
        quartzOreItem = new ItemBlockBase(quartzOreBlock);
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        RegistryUtils.register(event.getRegistry(), quartzOreBlock, "oreQuartz");
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        Items.QUARTZ.setUnlocalizedName("charset.gemQuartz");
        RegistryUtils.register(event.getRegistry(), quartzOreItem, "oreQuartz");
    }

    @SubscribeEvent
    public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        ItemStack glowstoneStack = new ItemStack(Items.GLOWSTONE_DUST, 1, 0);
        for (ItemStack stack : OreDictionary.getOres("dustRedstone")) {
            FurnaceRecipes.instance().addSmeltingRecipe(stack, glowstoneStack, 0.2F);
        }

        DimensionManager.unregisterDimension(1);
        DimensionManager.unregisterDimension(-1);
    }

    @SubscribeEvent
    public void disableEndStrongholds(InitMapGenEvent event) {
        if (event.getOriginalGen() instanceof MapGenStronghold) {
            event.setNewGen(new MapGenStrongholdNull());
        }
    }
}
