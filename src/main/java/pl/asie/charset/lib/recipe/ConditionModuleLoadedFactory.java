package pl.asie.charset.lib.recipe;

import com.google.gson.JsonObject;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;
import pl.asie.charset.ModCharset;

import java.util.function.BooleanSupplier;

public class ConditionModuleLoadedFactory implements IConditionFactory {
    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json) {
        String module = JsonUtils.getString(json, "module");
        return () -> ModCharset.isModuleLoaded(module);
    }
}
