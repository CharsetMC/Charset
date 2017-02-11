package pl.asie.charset.lib.utils;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;
import pl.asie.charset.ModCharset;

public final class RegistryUtils {
	private static final TIntSet takenEntityIds = new TIntHashSet();

	private RegistryUtils() {

	}

	public static void registerModel(Item item, int meta, String name) {
		UtilProxyCommon.proxy.registerItemModel(item, meta, name);
	}

	public static void registerModel(Block block, int meta, String name) {
		registerModel(Item.getItemFromBlock(block), meta, name);
	}

	public static void register(Class<? extends TileEntity> tileEntity, String name) {
		GameRegistry.registerTileEntity(tileEntity, new ResourceLocation("charset", name).toString());
	}

	public static void register(Class<? extends Entity> entity, String name, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates) {
		int autoAssignedId = 1;
		while (takenEntityIds.contains(autoAssignedId)) autoAssignedId++;

		Property prop = ModCharset.configIds.get("entity", name, autoAssignedId);
		int id = prop.getInt(0);
		if (id == 0 || takenEntityIds.contains(id)) {
			id = autoAssignedId;
			prop.set(id);
		}

		ResourceLocation nameLoc = new ResourceLocation("charset", name);
		EntityRegistry.registerModEntity(nameLoc, entity, nameLoc.toString(), id, ModCharset.instance, trackingRange, updateFrequency, sendsVelocityUpdates);
		takenEntityIds.add(id);
	}

	public static void register(IForgeRegistryEntry entry, String name) {
		register(entry, name, ModCharset.CREATIVE_TAB);
	}

	public static void register(IForgeRegistryEntry entry, String name, CreativeTabs tab) {
		entry.setRegistryName(new ResourceLocation(ModCharset.MODID, name));
		GameRegistry.register(entry);

		if (entry instanceof Block) {
			Block block = (Block) entry;
			if (block.getCreativeTabToDisplayOn() == null) {
				block.setCreativeTab(tab);
			}
		} else if (entry instanceof Item) {
			Item item = (Item) entry;
			if (item.getCreativeTab() == null) {
				item.setCreativeTab(tab);
			}
		}
	}

	public static void register(Block block, Item item, String name) {
		register(block, name);
		register(item, name);
	}
}
