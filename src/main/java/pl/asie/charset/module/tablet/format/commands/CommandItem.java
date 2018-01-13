package pl.asie.charset.module.tablet.format.commands;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.charset.module.tablet.format.ITokenizer;
import pl.asie.charset.module.tablet.format.api.ICommand;
import pl.asie.charset.module.tablet.format.api.ITypesetter;
import pl.asie.charset.module.tablet.format.api.TruthError;
import pl.asie.charset.module.tablet.format.words.WordItem;

import java.util.ArrayList;
import java.util.List;

public class CommandItem implements ICommand {
	@Override
	public void call(ITypesetter typesetter, ITokenizer tokenizer) throws TruthError {
		String itemName = tokenizer.getParameter("No item specified");
		List<ItemStack> items = new ArrayList<>();
		Item i = Item.getByNameOrId(itemName);

		String scaleS = tokenizer.getOptionalParameter();
		float scale = 1.0f;
		int stackSize = 1;
		int dmg = 0;

		if (scaleS != null) {
			String stackSizeS = tokenizer.getOptionalParameter();

			if (stackSizeS != null) {
				String dmgS = tokenizer.getOptionalParameter();
				if (dmgS != null) {
					dmg = Integer.parseInt(dmgS);
				}
			}

			scale = Float.parseFloat(scaleS);
		}

		if (i == null) {
			items.addAll(OreDictionary.getOres(itemName));
		} else {
			ItemStack stack = new ItemStack(i, stackSize, dmg);
			items.add(stack);
		}
		typesetter.write(new WordItem(items, scale));
	}
}
