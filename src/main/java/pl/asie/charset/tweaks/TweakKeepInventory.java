package pl.asie.charset.tweaks;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.charset.lib.ModCharsetLib;

public class TweakKeepInventory extends Tweak {
	private static final Predicate<ItemStack> ALWAYS_TRUE = Predicates.alwaysTrue();

	public TweakKeepInventory() {
		super("tweaks", "keepInventoryOnDeath", "Keep inventory on death.", false);
	}

	@Override
	public boolean canTogglePostLoad() {
		return false;
	}

	@Override
	public boolean init() {
		ModCharsetLib.deathHandler.addPredicate(ALWAYS_TRUE);
		return true;
	}
}
