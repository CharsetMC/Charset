package pl.asie.charset.lib.recipe;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.*;
import java.util.Map;

public class OutputSupplier {
    private static class Stack implements IOutputSupplier {
        private final ItemStack stack;

        Stack(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public ItemStack getCraftingResult(RecipeCharset recipe, IngredientMatcher matcher, InventoryCrafting inv) {
            return stack;
        }

        @Override
        public ItemStack getDefaultOutput() {
            return stack;
        }
    }

    private static final Gson GSON = new Gson();
    private static final Map<String, IOutputSupplierFactory> outputSuppliers = Maps.newHashMap();
    private static boolean initialized;

    private static void loadFactories(ModContainer container) {
        File file = container.getSource();
        try {
            BufferedReader reader;
            if (file.exists()) {
                if (file.isDirectory()) {
                    File f = new File(file, "assets/" + container.getModId() + "/recipes/_factories.json");
                    if (f.exists()) {
                        reader = Files.newBufferedReader(f.toPath());
                    } else {
                        return;
                    }
                } else {
                    FileSystem fileSystem = FileSystems.newFileSystem(file.toPath(), null);
                    Path p = fileSystem.getPath("assets/" + container.getModId() + "/recipes/_factories.json");
                    reader = Files.newBufferedReader(p, Charsets.UTF_8);
                }
            } else {
                return;
            }
            JsonObject json = JsonUtils.fromJson(GSON, reader, JsonObject.class);
            if (json != null && json.has("charset:output_suppliers")) {
                JsonObject object = JsonUtils.getJsonObject(json, "charset:output_suppliers");
                for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                    String key = new ResourceLocation(container.getModId(), entry.getKey()).toString();
                    String value = entry.getValue().getAsString();
                    Object o = Class.forName(value).newInstance();
                    if (o instanceof IOutputSupplierFactory) {
                        outputSuppliers.put(key, (IOutputSupplierFactory) o);
                    } else if (o instanceof IOutputSupplier) {
                        outputSuppliers.put(key, (a, b) -> (IOutputSupplier) o);
                    } else {
                        throw new Exception("Invalid OutputSupplier object type: " + (o != null ? o.getClass().getName() : "null"));
                    }
                }
            }
        } catch (NoSuchFileException | FileSystemNotFoundException e) {
            // Don't worry~
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initialize() {
        if (initialized) return;

        Loader.instance().getActiveModList().forEach(OutputSupplier::loadFactories);

        initialized = true;
    }

    public static IOutputSupplier createOutputSupplier(JsonContext context, JsonObject json) {
        initialize();
        if (json.has("supplier")) {
            return outputSuppliers.get(JsonUtils.getString(json, "supplier")).parse(context, (JsonObject) json);
        } else {
            return new Stack(CraftingHelper.getItemStack(json, context));
        }
    }

    public static IOutputSupplier createStackOutputSupplier(ItemStack stack) {
        return new Stack(stack);
    }
}
