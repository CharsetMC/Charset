package pl.asie.charset.module.storage.barrels;

import com.google.gson.JsonObject;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.function.BooleanSupplier;

public class ConditionBarrelUpgradeEnabledFactory implements IConditionFactory {
    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json) {
        String upgrade = JsonUtils.getString(json, "upgrade");
        TileEntityDayBarrel.Upgrade upgradeEnum = TileEntityDayBarrel.Upgrade.valueOf(upgrade);
        return () -> CharsetStorageBarrels.isEnabled(upgradeEnum);
    }
}
