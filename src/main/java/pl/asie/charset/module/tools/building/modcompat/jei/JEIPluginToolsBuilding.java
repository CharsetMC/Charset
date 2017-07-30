package pl.asie.charset.module.tools.building.modcompat.jei;

import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import pl.asie.charset.lib.modcompat.jei.CharsetJEIPlugin;
import pl.asie.charset.module.tools.building.CharsetToolsBuilding;
import pl.asie.charset.module.tools.building.CharsetToolsWrench;

@CharsetJEIPlugin("tools.building")
public class JEIPluginToolsBuilding implements IModPlugin {
    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
        // TODO
        subtypeRegistry.useNbtForSubtypes(CharsetToolsBuilding.chisel, CharsetToolsWrench.wrench);
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registry) {

    }

    @Override
    public void register(IModRegistry registry) {

    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {

    }
}
