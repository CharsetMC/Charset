/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.charset.lib.handlers;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.ArrayUtils;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.CharsetLib;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class ShiftScrollHandler {
	public interface Provider {
		boolean matches(ItemStack stack);
		void addAllMatching(NonNullList<ItemStack> list);
	}

	public static class ItemGroup implements Provider {
		private final Set<Item> items;

		public ItemGroup(Collection c) {
			items = new LinkedHashSet<>();
			for (Object o : c) {
				if (o instanceof Block) {
					items.add(Item.getItemFromBlock((Block) o));
				} else if (o instanceof Item) {
					items.add((Item) o);
				}
			}
		}

		public ItemGroup(Block... blocks) {
			items = new LinkedHashSet<>();
			for (Block b : blocks) {
				items.add(Item.getItemFromBlock(b));
			}
		}

		public ItemGroup(Item... its) {
			items = new LinkedHashSet<>();
			items.addAll(Arrays.asList(its));
		}

		@Override
		public boolean matches(ItemStack stack) {
			return !stack.isEmpty() && items.contains(stack.getItem());
		}

		@Override
		public void addAllMatching(NonNullList<ItemStack> list) {
			for (Item i : items) {
				i.getSubItems(CreativeTabs.SEARCH, list);
			}
		}
	}

	public static class ItemGroupMetadataLimited implements Provider {
		private final Item item;
		private final int minMeta, maxMeta;

		public ItemGroupMetadataLimited(Item i, int minMeta, int maxMeta) {
			this.item = i;
			this.minMeta = minMeta;
			this.maxMeta = maxMeta;
		}

		@Override
		public boolean matches(ItemStack stack) {
			return !stack.isEmpty() && stack.getItem() == item && stack.getItemDamage() >= minMeta && stack.getItemDamage() <= maxMeta;
		}

		@Override
		public void addAllMatching(NonNullList<ItemStack> list) {
			NonNullList<ItemStack> filteredList = NonNullList.create();
			item.getSubItems(CreativeTabs.SEARCH, filteredList);
			for (ItemStack stack : filteredList) {
				if (stack.getItemDamage() >= minMeta && stack.getItemDamage() <= maxMeta) {
					list.add(stack);
				}
			}
		}
	}

	public static class OreDictionaryGroup implements Provider {
		private final String oreName;
		private final transient int oreId;
		private final transient NonNullList<ItemStack> items;

		public OreDictionaryGroup(String oreName) {
			this.oreName = oreName;
			this.oreId = OreDictionary.getOreID(oreName);
			this.items = OreDictionary.getOres(oreName);
		}

		@Override
		public boolean matches(ItemStack stack) {
			return !stack.isEmpty() && ArrayUtils.contains(OreDictionary.getOreIDs(stack), oreId);
		}

		@Override
		public void addAllMatching(NonNullList<ItemStack> list) {
			for (ItemStack stack : items) {
				if (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
					stack.getItem().getSubItems(CreativeTabs.SEARCH, list);
				} else {
					list.add(stack);
				}
			}
		}
	}

	public static ShiftScrollHandler INSTANCE = new ShiftScrollHandler();
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private final List<Provider> providers = new ArrayList<>();

	private ShiftScrollHandler() {

	}

	@SuppressWarnings("unchecked")
	public void loadCustomRules() {
		// First, save the default rules.
		File rulesDir = new File(new File(ModCharset.getConfigDir(), "module"), "lib.shiftScroll");
		if (!rulesDir.isDirectory()) {
			rulesDir.mkdirs();
		}

		File defaultRulesFile = new File(rulesDir, "rules.json.default");
		List<Object> defaultRules = new ArrayList<>();
		for (Provider p : providers) {
			Map<String, Object> providerMap = new HashMap<>();
			if (p instanceof ItemGroup) {
				List<String> itemNames = new ArrayList<>();
				for (Item i : ((ItemGroup) p).items) {
					itemNames.add(i.getRegistryName().toString());
				}
				if (itemNames.size() == 1) {
					providerMap.put("item", itemNames.get(0));
				} else {
					providerMap.put("items", itemNames);
				}
			} else if (p instanceof ItemGroupMetadataLimited) {
				providerMap.put("item", ((ItemGroupMetadataLimited) p).item.getRegistryName().toString());
				providerMap.put("minMeta", ((ItemGroupMetadataLimited) p).minMeta);
				providerMap.put("maxMeta", ((ItemGroupMetadataLimited) p).maxMeta);
			} else if (p instanceof OreDictionaryGroup) {
				providerMap.put("oreName", ((OreDictionaryGroup) p).oreName);
			} else {
				ModCharset.logger.warn("Could not JSONify " + p.getClass().getName() + "!");
				continue;
			}
			defaultRules.add(providerMap);
		}

		try {
			Files.write(defaultRulesFile.toPath(), gson.toJson(defaultRules).getBytes(Charsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<File> filesToCheck = Lists.newArrayList();
		for (File f : rulesDir.listFiles()) {
			if (f.isFile() && f.getName().endsWith(".json")) {
				if (f.getName().equals("rules.json")) {
					providers.clear();
				}
				filesToCheck.add(f);
			}
		}

		for (File f : filesToCheck) {
			try {
				List deserialized = gson.fromJson(Files.newBufferedReader(f.toPath()), List.class);
				for (Object o : deserialized) {
					if (o instanceof Map) {
						try {
							Map m = (Map) o;
							if (m.containsKey("items")) {
								Object oo = m.get("items");

								if (oo instanceof Collection) {
									List<Item> items = new ArrayList<>();
									for (String s : (Collection<String>) oo) {
										Item i = ForgeRegistries.ITEMS.getValue(new ResourceLocation(s));
										if (i != Item.getItemFromBlock(Blocks.AIR)) {
											items.add(i);
										}
									}
									providers.add(new ItemGroup(items));
								}
							} else if (m.containsKey("item")) {
								Item i = ForgeRegistries.ITEMS.getValue(new ResourceLocation((String) m.get("item")));
								if (i != Item.getItemFromBlock(Blocks.AIR)) {
									if (m.containsKey("minMeta")) {
										providers.add(new ItemGroupMetadataLimited(i, (Integer) m.get("minMeta"), (Integer) m.get("maxMeta")));
									} else {
										providers.add(new ItemGroup(i));
									}
								}
							} else if (m.containsKey("oreName")) {
								providers.add(new OreDictionaryGroup((String) m.get("oreName")));
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public @Nullable Provider getMatchingProvider(ItemStack stack) {
		if (stack.isEmpty()) return null;
		for (Provider p : providers) {
			if (p.matches(stack))
				return p;
		}
		return null;
	}

	public void register(Provider provider) {
		providers.add(provider);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onMouse(MouseEvent event) {
		int wheel = event.getDwheel();
		if (wheel != 0 && Minecraft.getMinecraft().gameSettings.keyBindSneak.isKeyDown()) {
			EntityPlayer player = Minecraft.getMinecraft().player;
			ItemStack currStack = player.getHeldItemMainhand();
			if (!currStack.isEmpty() && getMatchingProvider(currStack) != null) {
				event.setCanceled(true);
				CharsetLib.packet.sendToServer(new PacketRequestScroll(player.inventory.currentItem, wheel));
			}
		}
	}
}
