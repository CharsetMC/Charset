package pl.asie.charset.lib;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.wires.CharsetLibWires;
import pl.asie.charset.lib.wires.ItemWire;
import pl.asie.charset.lib.wires.WireProvider;

public abstract class CreativeTabCharset extends CreativeTabs {
	public CreativeTabCharset(String label) {
		super(label);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void displayAllRelevantItems(NonNullList<ItemStack> list) {
		boolean addedWires = false;

		for (Item item : Item.REGISTRY) {
			if (item instanceof ItemWire) {
				if (!addedWires) {
					CharsetLibWires.getOrderedWireProviders().map(WireProvider::getItemWire)
							.filter((i) -> Item.REGISTRY.containsKey(i.getRegistryName()))
							.forEachOrdered((i) -> i.getSubItems(this, list));

					addedWires = true;
				}
			} else {
				item.getSubItems(this, list);
			}
		}
	}
}
