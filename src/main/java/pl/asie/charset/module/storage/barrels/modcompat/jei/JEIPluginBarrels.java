package pl.asie.charset.module.storage.barrels.modcompat.jei;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import net.minecraft.item.ItemStack;
import pl.asie.charset.lib.modcompat.jei.CharsetJEIPlugin;
import pl.asie.charset.module.storage.barrels.CharsetStorageBarrels;
import pl.asie.charset.module.storage.barrels.TileEntityDayBarrel;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

@CharsetJEIPlugin("storage.barrels")
public class JEIPluginBarrels implements IModPlugin {
    private static final Joiner JOINER = Joiner.on(';');
    private static final ISubtypeRegistry.ISubtypeInterpreter interpreter = new ISubtypeRegistry.ISubtypeInterpreter() {
        @Nullable
        @Override
        public String getSubtypeInfo(ItemStack itemStack) {
            TileEntityDayBarrel barrel = new TileEntityDayBarrel();
            barrel.loadFromStack(itemStack);
            List<String> upgradeStringSet = Lists.newArrayList();
            for (TileEntityDayBarrel.Upgrade u : barrel.upgrades) {
                upgradeStringSet.add(u.name());
            }
            Collections.sort(upgradeStringSet);

            return JOINER.join(upgradeStringSet) + ";" + barrel.woodLog.getId() + ";" + barrel.woodSlab.getId();
        }
    };

    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
        subtypeRegistry.registerSubtypeInterpreter(CharsetStorageBarrels.barrelItem, interpreter);
        subtypeRegistry.registerSubtypeInterpreter(CharsetStorageBarrels.barrelCartItem, interpreter);
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
