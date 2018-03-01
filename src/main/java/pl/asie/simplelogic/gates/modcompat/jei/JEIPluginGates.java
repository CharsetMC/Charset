package pl.asie.simplelogic.gates.modcompat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.ISubtypeRegistry;
import net.minecraftforge.common.util.Constants;
import pl.asie.charset.lib.modcompat.jei.CharsetJEIPlugin;
import pl.asie.simplelogic.gates.SimpleLogicGates;

@CharsetJEIPlugin("simplelogic.gates")
public class JEIPluginGates implements IModPlugin {
	@Override
	public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
		subtypeRegistry.registerSubtypeInterpreter(SimpleLogicGates.itemGate, (stack) -> {
			if (stack.hasTagCompound() && stack.getTagCompound().hasKey("logic", Constants.NBT.TAG_STRING)) {
				return stack.getTagCompound().getString("logic");
			} else {
				return "dummy";
			}
		});
	}
}
