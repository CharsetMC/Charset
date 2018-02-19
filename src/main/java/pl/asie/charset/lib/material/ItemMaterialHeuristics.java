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

package pl.asie.charset.lib.material;

import com.google.common.base.Joiner;
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

    public static ItemStack getCraftingResultQuickly(boolean noShapeless, int nonEmptyStacks, World world, int width, int height, ItemStack... stacks) {
        InventoryCrafting crafting = RecipeUtils.getCraftingInventory(width, height, stacks);
        IRecipe recipe = findMatchingRecipeQuickly(noShapeless, nonEmptyStacks, crafting, world);

        if (recipe != null) {
            return recipe.getCraftingResult(crafting);
        }

        return ItemStack.EMPTY;
    }

    private static List<List<IRecipe>> recipeLists = new ArrayList<>();

    public static void initRecipeLists() {
        if (recipeLists.isEmpty()) {
            // 0-8: shapeless 1-9 ingredients
            // 9-17: shaped 1-9 w/h
            // 18-26: other 1-9 canFit
            // 27: weird
            for (int i = 0; i < 28; i++) {
                recipeLists.add(new ArrayList<>());
            }

            for (IRecipe irecipe : ForgeRegistries.RECIPES) {
                if ((irecipe instanceof ShapelessRecipes || irecipe instanceof ShapelessOreRecipe) && !irecipe.isDynamic()) {
                    recipeLists.get(irecipe.getIngredients().size() - 1).add(irecipe);
                } else if (irecipe instanceof IShapedRecipe && !irecipe.isDynamic()) {
                    int wh = 9 + (((IShapedRecipe) irecipe).getRecipeWidth() - 1) * 3 + ((IShapedRecipe) irecipe).getRecipeHeight() - 1;
                    recipeLists.get(wh).add(irecipe);
                } else {
                    // if it can fit in 2x2, it can also fit in 3x2, 2x3 and 3x3
                    int width = 3;
                    int height = 3;
                    while (irecipe.canFit(width - 1, height) && width > 0) {
                        width--;
                    }
                    while (irecipe.canFit(width, height - 1) && height > 0) {
                        height--;
                    }
                    if (width == 0 || height == 0) {
                        recipeLists.get(27).add(irecipe);
                    } else {
                        recipeLists.get(18 + (width - 1) * 3 + (height - 1)).add(irecipe);
                    }
                }
            }

/*            for (int i = 0; i < 9; i++) {
                System.out.println("SHAPELESS " + (i+1) + " = " + recipeLists.get(i).size());
            }
            for (int i = 0; i < 9; i++) {
                System.out.println("SHAPED " + ((i%3)+1) + "x" + ((i/3)+1) + " = " + recipeLists.get(i + 9).size());
            }
            for (int i = 0; i < 9; i++) {
                System.out.println("WEIRD " + ((i%3)+1) + "x" + ((i/3)+1) + " = " + recipeLists.get(i + 18).size());
            }
            System.out.println("REALLY WEIRD = " + recipeLists.get(27).size()); */
        }
    }

    public static IRecipe findMatchingRecipeQuickly(boolean noShapeless, int nonEmptyStacks, InventoryCrafting craftMatrix, World worldIn) {
        initRecipeLists();

        int width = craftMatrix.getWidth();
        int height = craftMatrix.getHeight();

        for (IRecipe irecipe : recipeLists.get(9 + (width-1)*3 + (height-1))) {
            if (irecipe.matches(craftMatrix, worldIn)) {
                return irecipe;
            }
        }

        if (!noShapeless) {
            for (IRecipe irecipe : recipeLists.get(nonEmptyStacks - 1)) {
                if (irecipe.matches(craftMatrix, worldIn)) {
                    return irecipe;
                }
            }
        }

        for (int rw = width; rw <= 3; rw++) {
            for (int rh = height; rh <= 3; rh++) {
                for (IRecipe irecipe : recipeLists.get(18 + (rw-1)*3 + (rh-1))) {
                    if (irecipe.matches(craftMatrix, worldIn)) {
                        return irecipe;
                    }
                }

            }
        }

        for (IRecipe irecipe : recipeLists.get(27)) {
            if (irecipe.matches(craftMatrix, worldIn)) {
                return irecipe;
            }
        }

        return null;
    }

    private static void findSlab(ItemMaterial base) {
        if (!base.getTypes().contains("block") || base.getRelated("slab") != null)
            return;

        ItemStack slab = getCraftingResultQuickly(true, 3, null, 3, 1,
                base.getStack(), base.getStack(), base.getStack());
        if (isBlock(slab)) {
            addResultingBlock(base, slab, "block", "slab");
        }
    }

    private static void findStair(ItemMaterial base) {
        if (!base.getTypes().contains("block") || base.getRelated("stairs") != null)
            return;

        ItemStack stair = getCraftingResultQuickly(true, 6, null, 3, 3,
                null, null, base.getStack(),
                null, base.getStack(), base.getStack(),
                base.getStack(), base.getStack(), base.getStack());
        if (isBlock(stair)) {
            addResultingBlock(base, stair, "block", "stairs");
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

        ItemStack plank = getCraftingResultQuickly(false, 1,null, 1, 1, log);
        if (isBlock(plank) && ItemUtils.isOreType(plank, "plankWood")) {
            ItemMaterial logMaterial = reg.getOrCreateMaterial(log);
            if (reg.registerTypes(logMaterial, "log", "wood", "block")) {
                plank.setCount(1);
                ItemMaterial plankMaterial = reg.getOrCreateMaterial(plank);
                if (reg.registerTypes(plankMaterial, "plank", "wood", "block")) {
                    reg.registerRelation(logMaterial, plankMaterial, "plank", "log");

                    ItemStack stick = getCraftingResultQuickly(true, 2, null, 1, 2,
                            plank,
                            plank);
                    if (stick.isEmpty()) {
                        stick = new ItemStack(Items.STICK);
                    } else {
                        stick.setCount(1);
                    }

                    ItemMaterial stickMaterial = reg.getOrCreateMaterial(stick);
                    if (reg.registerTypes(stickMaterial, "stick", "wood", "item")) {
                        if (stick.getItem() != Items.STICK) {
                            reg.registerRelation(plankMaterial, stickMaterial, "stick", "plank");
                            reg.registerRelation(logMaterial, stickMaterial, "stick", "log");
                        } else {
                            reg.registerRelation(plankMaterial, stickMaterial, "stick");
                            reg.registerRelation(logMaterial, stickMaterial, "stick");
                        }
                    }
                }
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
                ItemStack block = getCraftingResultQuickly(true, 4, null, 2, 2,
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
                ItemStack nugget = getCraftingResultQuickly(false, 1, null, 1, 1,
                        stack);
                if (!nugget.isEmpty() && containsOreDict(nugget, "nugget" + suffixU)) {
                    ItemMaterial nuggetMat = reg.getOrCreateMaterial(nugget);
                    reg.registerTypes(nuggetMat, "nugget", suffix, "item");
                    reg.registerRelation(ingotMat, nuggetMat, "nugget", prefix);
                }
            }

            // Try crafting a block
            ItemStack block = getCraftingResultQuickly(false, 9, null, 3, 3,
                    stack, stack, stack,
                    stack, stack, stack,
                    stack, stack, stack);
            if (!block.isEmpty() && containsOreDict(block, "block" + suffixU)) {
                ItemMaterial blockMat = reg.getOrCreateMaterial(block);
                reg.registerTypes(blockMat, suffix, "block");
                reg.registerRelation(ingotMat, blockMat, "block", prefix);
            }
        }
    }

    private static void initOreMaterial(String oreName) {
        String prefix = "ore";
        String suffixU = oreName.substring(prefix.length());
        String suffix = suffixU.substring(0, 1).toLowerCase() + suffixU.substring(1);

        // Create ore materials for each ore
        supplyExpandedStacks(OreDictionary.getOres(oreName), (stack -> {
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
            ItemStack block = getCraftingResultQuickly(true, 4, null, 2, 2,
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
        recipeLists.clear();

   //     long time = System.currentTimeMillis();
        if (initPhase >= (modded ? 2 : 1))
            return;

        ProgressManager.ProgressBar bar = ProgressManager.push("Material scanning", 6);

        reg = ItemMaterialRegistry.INSTANCE;
        initPhase = (modded ? 2 : 1);

        bar.step("Wood");
        // Pre-initialize impl woods
        if (!modded) {
            for (int i = 0; i < 6; i++) {
                ItemMaterial log = reg.getOrCreateMaterial(new ItemStack(i >= 4 ? Blocks.LOG2 : Blocks.LOG, 1, i % 4));
                ItemMaterial plank = reg.getOrCreateMaterial(new ItemStack(Blocks.PLANKS, 1, i));
                ItemMaterial stick = reg.getOrCreateMaterial(new ItemStack(Items.STICK));
                reg.registerTypes(log, "log", "block", "wood");
                reg.registerTypes(plank, "plank", "block", "wood");
                reg.registerTypes(stick, "stick", "item", "wood");
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

        for (ItemMaterial material : reg.getMaterialsByType("block")) {
            findSlab(material);
            findStair(material);
        }

        ProgressManager.pop(bar);

/*        time = System.currentTimeMillis() - time;
        System.out.println("slow search: " + time); */

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
