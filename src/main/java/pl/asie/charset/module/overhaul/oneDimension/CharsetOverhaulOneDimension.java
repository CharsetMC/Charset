/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.charset.module.overhaul.oneDimension;

import net.minecraft.block.Block;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.charset.lib.item.ItemBlockBase;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.recipe.RecipeReplacement;
import pl.asie.charset.lib.utils.RegistryUtils;

import java.util.Random;

@CharsetModule(
        name = "overhaul.oneDimension",
        description = "Removes the Nether and End, and adapts recipes for Nether/End items to either remove them or add new ways of crafting them.",
        dependencies = {"tweak.remove.netherPortals"},
        categories = {"overhaul"},
        profile = ModuleProfile.INDEV
)
public class CharsetOverhaulOneDimension {
    public static BlockOreQuartz quartzOreBlock;
    public static ItemBlockBase quartzOreItem;
    private static WorldGenerator quartzOreGenerator;

    public void generateQuartz(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        for (int i = 0; i < 5; i++) {
            int x = (chunkX << 4) + random.nextInt(16);
            int y = 8 + random.nextInt(25);
            int z = (chunkZ << 4) + random.nextInt(16);
            quartzOreGenerator.generate(world, random, new BlockPos(x, y, z));
        }
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        quartzOreBlock = new BlockOreQuartz();
        quartzOreItem = new ItemBlockBase(quartzOreBlock);
        // Nether Quartz: 16 x 14
        // Redstone: 8 x 8 ( but on low level )
        quartzOreGenerator = new WorldGenMinable(quartzOreBlock.getDefaultState(), 12, BlockMatcher.forBlock(Blocks.STONE));
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
    @SideOnly(Side.CLIENT)
    public void registerModels(ModelRegistryEvent event) {
        RegistryUtils.registerModel(quartzOreItem, 0, "charset:oreQuartz");
    }

    @SubscribeEvent
    public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        RecipeReplacement.PRIMARY.add(Item.getItemFromBlock(Blocks.SOUL_SAND), Item.getItemFromBlock(Blocks.SAND));
        RecipeReplacement.PRIMARY.add(Item.getItemFromBlock(Blocks.QUARTZ_ORE), "oreQuartz");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        OreDictionary.registerOre("oreQuartz", quartzOreBlock);
        GameRegistry.registerWorldGenerator(this::generateQuartz, 0);
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
