/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.lib.material;

import com.google.common.base.Joiner;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import org.apache.commons.lang3.ArrayUtils;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.CharsetLib;
import pl.asie.charset.lib.recipe.RecipeCharset;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.RecipeUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class ItemMaterialHeuristics {
    private static int initPhase = 0;
    private static ItemMaterialRegistry reg;

    private ItemMaterialHeuristics() {

    }

    public static boolean isFullyInitialized() {
        return initPhase >= 2;
    }

    private static boolean isBlock(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof ItemBlock || Block.getBlockFromItem(stack.getItem()) != Blocks.AIR);
    }

    private static void addResultingBlock(ItemMaterial base, ItemStack result, String source, String target) {
        if (!result.isEmpty()) {
            result.setCount(1);
            ItemMaterial slabMaterial = reg.getOrCreateMaterial(result);
            if (reg.registerType(slabMaterial, target)) {
                reg.registerRelation(base, slabMaterial, target, source);
                for (String s : base.getTypes()) {
                    if (!s.equals(source)) {
                        reg.registerType(slabMaterial, s);
                    }
                }
            }
        }
    }

    private static void findSlab(ItemMaterial base) {
        if (!base.getTypes().contains("block") || base.getRelated("slab") != null)
            return;

        ItemStack slab = FastRecipeLookup.getCraftingResultQuickly(true, 3, null, 3, 1,
                base.getStack(), base.getStack(), base.getStack());
        if (isBlock(slab)) {
            addResultingBlock(base, slab, "block", "slab");
        }
    }

    private static void findStair(ItemMaterial base) {
        if (!base.getTypes().contains("block") || base.getRelated("stairs") != null)
            return;

        ItemStack stair = FastRecipeLookup.getCraftingResultQuickly(true, 6, null, 3, 3,
                base.getStack(), null, null,
                base.getStack(), base.getStack(), null,
                base.getStack(), base.getStack(), base.getStack());
        if (isBlock(stair)) {
            addResultingBlock(base, stair, "block", "stairs");
        }
    }

    private static void initLoglessPlankMaterial(ItemStack plank) {
        // Log Material is null, as logWood iteration should have caught those before.
        initPlankMaterial(plank, null);
    }

    private static void initPlankMaterial(ItemStack plank, @Nullable ItemMaterial logMaterial) {
        if (!isBlock(plank)) {
            return;
        }

        ItemMaterial material = reg.getMaterialIfPresent(plank);
        if (material != null && material.getTypes().contains("plank")) {
            return;
        }

        if (plank.getCount() > 1) {
            plank = plank.copy();
            plank.setCount(1);
        }
        ItemMaterial plankMaterial = reg.getOrCreateMaterial(plank);
        if (reg.registerTypes(plankMaterial, "plank", "wood", "block")) {
            if (logMaterial != null) {
                reg.registerRelation(logMaterial, plankMaterial, "plank", "log");
            }

            ItemStack stick = FastRecipeLookup.getCraftingResultQuickly(true, 2, null, 1, 2,
                    plank,
                    plank);
            if (!stick.isEmpty() && !ItemUtils.isOreType(stick, "stickWood")) {
                stick = ItemStack.EMPTY;
            }

            if (stick.isEmpty()) {
                stick = new ItemStack(Items.STICK);
            } else {
                stick.setCount(1);
            }

            ItemMaterial stickMaterial = reg.getOrCreateMaterial(stick);
            if (reg.registerTypes(stickMaterial, "stick", "wood", "item")) {
                if (stick.getItem() != Items.STICK) {
                    reg.registerRelation(plankMaterial, stickMaterial, "stick", "plank");
                    if (logMaterial != null) {
                        reg.registerRelation(logMaterial, stickMaterial, "stick", "log");
                    }
                } else {
                    reg.registerRelation(plankMaterial, stickMaterial, "stick");
                    if (logMaterial != null) {
                        reg.registerRelation(logMaterial, stickMaterial, "stick");
                    }
                }
            }
        }
    }
    private static void initLogMaterial(ItemStack log) {
        if (!isBlock(log))
            return;

        // Check if already registered
        ItemMaterial material = reg.getMaterialIfPresent(log);
        if (material != null && material.getTypes().contains("log")) {
            return;
        }

        // We look for the plank first to ensure only valid logs
        // get registered.

        ItemStack plank = FastRecipeLookup.getCraftingResultQuickly(false, 1,null, 1, 1, log);

        if (isBlock(plank) && ItemUtils.isOreType(plank, "plankWood")) {
            ItemMaterial logMaterial = reg.getOrCreateMaterial(log);
            if (reg.registerTypes(logMaterial, "log", "wood", "block")) {
                initPlankMaterial(plank, logMaterial);
            }
        }

    }

    private static boolean containsOreDict(ItemStack stack, String entry) {
        return ArrayUtils.contains(OreDictionary.getOreIDs(stack), OreDictionary.getOreID(entry));
    }

    private static int indexOfUpper(String s, int from) {
        for (int i = from; i < s.length(); i++) {
            if (Character.isUpperCase(s.codePointAt(i))) {
                return i;
            }
        }

        return -1;
    }

    // TODO: Tie ores to ingots
    private static void initIngotLikeMaterial(String oreName, ItemStack stack) {
        int splitPoint = indexOfUpper(oreName, 0);
        if (splitPoint < 0) return;

        String prefix = oreName.substring(0, splitPoint);
        String suffixU = oreName.substring(splitPoint);
        String suffix = suffixU.substring(0, 1).toLowerCase() + suffixU.substring(1);
        ItemMaterial ingotMat = reg.getOrCreateMaterial(stack);

        if (prefix.equals("ingot") && suffix.startsWith("brick")) {
            if (reg.registerTypes(ingotMat, "brick", "item")) {
                int splitPoint2 = indexOfUpper(suffix, 1);
                if (splitPoint2 >= 0) {
                    String suffixU2 = suffix.substring(splitPoint2);
                    String suffix2 = suffixU2.substring(0, 1).toLowerCase() + suffixU2.substring(1);
                    reg.registerType(ingotMat, suffix2);
                }

                // Try crafting a block
                ItemStack block = FastRecipeLookup.getCraftingResultQuickly(true, 4, null, 2, 2,
                        stack, stack,
                        stack, stack);
                if (!block.isEmpty() && block.getItem() instanceof ItemBlock) {
                    ItemMaterial blockMat = reg.getOrCreateMaterial(block);
                    reg.registerTypes(blockMat, suffix, "block");
                    reg.registerRelation(ingotMat, blockMat, "block", prefix);
                }
            }

            return;
        }

        if (reg.registerTypes(ingotMat, prefix, suffix, "item")) {
            // Try crafting a nugget
            if (prefix.equals("ingot")) {
                ItemStack nugget = FastRecipeLookup.getCraftingResultQuickly(false, 1, null, 1, 1,
                        stack);
                if (!nugget.isEmpty() && containsOreDict(nugget, "nugget" + suffixU)) {
                    ItemMaterial nuggetMat = reg.getOrCreateMaterial(nugget);
                    reg.registerTypes(nuggetMat, "nugget", suffix, "item");
                    reg.registerRelation(ingotMat, nuggetMat, "nugget", prefix);
                }
            }

            // Try crafting a block
            ItemStack block = FastRecipeLookup.getCraftingResultQuickly(false, 9, null, 3, 3,
                    stack, stack, stack,
                    stack, stack, stack,
                    stack, stack, stack);
            if (!block.isEmpty()) {
                // Fancy heuristics for non-oredict-reg'd variants (Base Metals obsidian, redstone ingots)
                boolean isBlockVariant = containsOreDict(block, "block" + suffixU);
                if (!isBlockVariant) {
                    isBlockVariant = (block.getItem() instanceof ItemBlock);
                    if (isBlockVariant) {
                        ItemStack reverse = FastRecipeLookup.getCraftingResultQuickly(false, 1, null, 1, 1, block.copy());
                        if (!reverse.isEmpty() && ItemUtils.equals(reverse, stack, false, true, true) && reverse.getCount() == 9) {
                            isBlockVariant = true;
                        }
                    }
                }

                if (isBlockVariant) {
                    ItemMaterial blockMat = reg.getOrCreateMaterial(block);
                    reg.registerTypes(blockMat, suffix, "block");
                    reg.registerRelation(ingotMat, blockMat, "block", prefix);
                }
            }
        }
    }

    private static void initOreMaterial(String oreName) {
        String prefix = "ore";
        String suffixU = oreName.substring(prefix.length());
        String suffix = suffixU.substring(0, 1).toLowerCase() + suffixU.substring(1);

        // Create ore materials for each ore
        supplyExpandedStacks(OreDictionary.getOres(oreName, false), (stack -> {
            if (isBlock(stack)) {
                ItemMaterial oreMaterial = reg.getOrCreateMaterial(stack);
                reg.registerTypes(oreMaterial, prefix, suffix, "block");
            }
        }));
    }

    private static void initStoneMaterial(String oreName, ItemStack stack) {
        if (oreName.endsWith("Polished") || !isBlock(stack)) return;

        String prefix = "stone";
        String suffixU = oreName.substring(prefix.length());
        String suffix = suffixU.length() > 0 ? suffixU.substring(0, 1).toLowerCase() + suffixU.substring(1) : "";

        ItemMaterial stoneMat = reg.getOrCreateMaterial(stack);
        if (reg.registerTypes(stoneMat, "stone", suffix, "block")) {
            // Try crafting a brick
            ItemStack block = FastRecipeLookup.getCraftingResultQuickly(true, 4, null, 2, 2,
                    stack, stack,
                    stack, stack);
            if (!block.isEmpty()) {
                ItemMaterial brickMat = reg.getOrCreateMaterial(block);
                reg.registerTypes(brickMat, "stone", "brick", suffix, "block");
                reg.registerRelation(stoneMat, brickMat, "brick", "parent");
            }
        }
    }

    private static void initCobblestoneMaterial(String oreName, ItemStack stack) {
        ItemMaterial cobbleMat = reg.getOrCreateMaterial(stack);
        reg.registerTypes(cobbleMat, "cobblestone", "block");

        ItemStack stoneStack = FurnaceRecipes.instance().getSmeltingResult(stack);
        ItemMaterial stoneMaterial = reg.getMaterialIfPresent(stoneStack);
        if (stoneMaterial == null) {
            ModCharset.logger.warn("Found OreDict cobblestone which does not give OreDict stone -> " + cobbleMat.toString());
        } else {
            reg.registerRelation(cobbleMat, stoneMaterial, "stone", "cobblestone");
        }
    }

    private static void supplyExpandedStacks(Collection<ItemStack> stacks, Consumer<ItemStack> stackConsumer) {
        for (ItemStack log : stacks) {
            try {
                if (log.getMetadata() == OreDictionary.WILDCARD_VALUE) {
                    NonNullList<ItemStack> stackList = NonNullList.create();
                    log.getItem().getSubItems(CreativeTabs.SEARCH, stackList);
                    for (int i = 0; i < stackList.size(); i++) {
                        stackConsumer.accept(stackList.get(i));
                    }
                } else {
                    stackConsumer.accept(log.copy());
                }
            } catch (Exception e) {

            }
        }
    }

    public static void init(boolean modded) {
        FastRecipeLookup.clearRecipeLists();
        if (modded) {
            FastRecipeLookup.initRecipeLists();
        }

        long time = System.currentTimeMillis();
        if (initPhase >= (modded ? 2 : 1))
            return;

        ProgressManager.ProgressBar bar = ProgressManager.push("Material scanning", 6);

        reg = ItemMaterialRegistry.INSTANCE;
        initPhase = (modded ? 2 : 1);

        bar.step("Wood");
        // Pre-initialize impl woods
        if (!modded) {
            ItemMaterial stick = reg.getOrCreateMaterial(new ItemStack(Items.STICK));
            reg.registerTypes(stick, "stick", "item", "wood");

            for (int i = 0; i < 6; i++) {
                ItemMaterial log = reg.getOrCreateMaterial(new ItemStack(i >= 4 ? Blocks.LOG2 : Blocks.LOG, 1, i % 4));
                ItemMaterial plank = reg.getOrCreateMaterial(new ItemStack(Blocks.PLANKS, 1, i));
                reg.registerTypes(log, "log", "block", "wood");
                reg.registerTypes(plank, "plank", "block", "wood");
                reg.registerRelation(log, plank, "plank", "log");
                if (i == 0) {
                    reg.registerRelation(plank, stick, "stick", "plank");
                    reg.registerRelation(log, stick, "stick", "log");
                } else {
                    reg.registerRelation(plank, stick, "stick");
                    reg.registerRelation(log, stick, "stick");
                }
            }
        } else {
            supplyExpandedStacks(OreDictionary.getOres("logWood", false), ItemMaterialHeuristics::initLogMaterial);
            // supplyExpandedStacks(OreDictionary.getOres("plankWood", false), ItemMaterialHeuristics::initLoglessPlankMaterial);
        }

        bar.step("Ores");

        if (modded) {
            for (String oreName : OreDictionary.getOreNames()) {
                if (oreName.startsWith("ore")) {
                    initOreMaterial(oreName);
                }
            }
        }

        bar.step("Ingots/Dusts/Gems");

        if (modded) {
            for (String oreName : OreDictionary.getOreNames()) {
                if (oreName.startsWith("ingot") || oreName.startsWith("dust") || oreName.startsWith("gem")) {
                    supplyExpandedStacks(OreDictionary.getOres(oreName, false), (s -> ItemMaterialHeuristics.initIngotLikeMaterial(oreName, s)));
                }
            }
        }

        bar.step("Stones");

        if (modded) {
            for (String oreName : OreDictionary.getOreNames()) {
                if (oreName.startsWith("stone")) {
                    supplyExpandedStacks(OreDictionary.getOres(oreName, false), (s -> ItemMaterialHeuristics.initStoneMaterial(oreName, s)));
                }
            }

            for (String oreName : OreDictionary.getOreNames()) {
                if (oreName.startsWith("cobblestone")) {
                    supplyExpandedStacks(OreDictionary.getOres(oreName, false), (s -> ItemMaterialHeuristics.initCobblestoneMaterial(oreName, s)));
                }
            }
        }

        bar.step("Misc");
        if (modded)
            reg.registerTypes(reg.getOrCreateMaterial(new ItemStack(Blocks.BEDROCK)), "block", "bedrock");

        bar.step("Slabs/Stairs");

        if (modded) {
            for (ItemMaterial material : reg.getMaterialsByType("block")) {
                findSlab(material);
                findStair(material);
            }
        }

        ProgressManager.pop(bar);

        time = System.currentTimeMillis() - time;
        ModCharset.logger.info("Charset material heuristics time (phase " + initPhase + "): " + time + "ms");

        if (CharsetLib.enableDebugInfo && initPhase == 2) {
            try {
                File outputFile = new File("charsetItemMaterials.txt");
                PrintWriter writer = new PrintWriter(outputFile);
                Joiner commaJoiner = Joiner.on(",");

                for (ItemMaterial material : reg.getAllMaterials()) {
                    writer.println(material.getId());
                    writer.println("- Types: " + commaJoiner.join(material.getTypes()));
                    for (Map.Entry<String, ItemMaterial> entry : material.getRelations().entrySet()) {
                        writer.println("- Relation: " + entry.getKey() + " -> " + entry.getValue().getId());
                    }
                }

                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
