package pl.asie.charset.module.misc.scaffold.modcompat.jei;

import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import net.minecraft.item.ItemStack;
import pl.asie.charset.lib.modcompat.jei.CharsetJEIPlugin;
import pl.asie.charset.module.misc.scaffold.CharsetMiscScaffold;
import pl.asie.charset.module.misc.scaffold.TileScaffold;

import javax.annotation.Nullable;

@CharsetJEIPlugin("misc.scaffold")
public class JEIPluginScaffold implements IModPlugin {
    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
        subtypeRegistry.registerSubtypeInterpreter(CharsetMiscScaffold.scaffoldItem, new ISubtypeRegistry.ISubtypeInterpreter() {
            @Nullable
            @Override
            public String getSubtypeInfo(ItemStack itemStack) {
                return TileScaffold.getPlankFromNBT(itemStack.getTagCompound()).getId();
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
