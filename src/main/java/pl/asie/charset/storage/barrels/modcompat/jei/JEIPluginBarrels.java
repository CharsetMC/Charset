package pl.asie.charset.storage.barrels.modcompat.jei;

import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import net.minecraft.item.ItemStack;
import pl.asie.charset.lib.modcompat.jei.CharsetJEIPlugin;
import pl.asie.charset.storage.barrels.CharsetStorageBarrels;
import pl.asie.charset.storage.barrels.TileEntityDayBarrel;

import javax.annotation.Nullable;

@CharsetJEIPlugin("storage.barrels")
public class JEIPluginBarrels implements IModPlugin {
    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
        subtypeRegistry.registerSubtypeInterpreter(CharsetStorageBarrels.barrelItem, new ISubtypeRegistry.ISubtypeInterpreter() {
            @Nullable
            @Override
            public String getSubtypeInfo(ItemStack itemStack) {
                TileEntityDayBarrel barrel = new TileEntityDayBarrel();
                barrel.loadFromStack(itemStack);
                return barrel.type.name() + ";" + barrel.woodLog.toString() + ";" + barrel.woodSlab.toString();
            }
        });
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
