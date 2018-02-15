package pl.asie.charset.lib.modcompat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.util.ResourceLocation;
import pl.asie.charset.lib.CharsetIMC;
import pl.asie.charset.lib.utils.ThreeState;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.charset.Registry")
@ModOnly("charset")
public class Registry {
	@ZenMethod
	public static boolean allow(String key, IItemStack stack) {
		return allow(key, CraftTweakerMC.getItemStack(stack).getItem().getRegistryName().toString());
	}

	@ZenMethod
	public static boolean forbid(String key, IItemStack stack) {
		return forbid(key, CraftTweakerMC.getItemStack(stack).getItem().getRegistryName().toString());
	}

	@ZenMethod
	public static boolean allow(String key, String location) {
		if (!location.contains(":")) {
			return false;
		}
		CraftTweakerAPI.apply(new IMCAction(key, new ResourceLocation(location), "Allowing") {
			@Override
			public void apply() {
				CharsetIMC.INSTANCE.add(ThreeState.YES, this.key, this.location);
			}
		});
		return true;
	}

	@ZenMethod
	public static boolean forbid(String key, String location) {
		if (!location.contains(":")) {
			return false;
		}
		CraftTweakerAPI.apply(new IMCAction(key, new ResourceLocation(location), "Forbidding") {
			@Override
			public void apply() {
				CharsetIMC.INSTANCE.add(ThreeState.NO, this.key, this.location);
			}
		});
		return true;
	}

	public static abstract class IMCAction implements IAction {
		protected final String key;
		protected final ResourceLocation location;
		private final String descriptor;

		public IMCAction(String key, ResourceLocation location, String descriptor) {
			this.key = key;
			this.location = location;
			this.descriptor = descriptor;
		}

		@Override
		public String describe() {
			return descriptor + " Charset functionality " + key + " for block " + location;
		}
	}
}
