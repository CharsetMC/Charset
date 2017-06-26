package pl.asie.charset.lib.utils;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import pl.asie.charset.ModCharset;

import java.util.Map;

public final class RegistryUtils {
	private static final TIntObjectMap<String> takenEntityIds = new TIntObjectHashMap<>();

	private RegistryUtils() {

	}

	public static void registerModel(Item item, int meta, String name) {
		UtilProxyCommon.proxy.registerItemModel(item, meta, name);
	}

	public static void register(Class<? extends TileEntity> tileEntity, String name) {
		GameRegistry.registerTileEntity(tileEntity, new ResourceLocation("charset", name).toString());
	}

	public static void register(Class<? extends Entity> entity, String name, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates) {
		int autoAssignedId = 1;
		while (takenEntityIds.containsKey(autoAssignedId)) autoAssignedId++;

		Property prop = ModCharset.configIds.get("entity", name, autoAssignedId);
		int id = prop.getInt(0);
		if (id == 0 || takenEntityIds.containsKey(id)) {
			id = autoAssignedId;
			prop.set(id);
		}

		ResourceLocation nameLoc = new ResourceLocation("charset", name);
		EntityRegistry.registerModEntity(nameLoc, entity, nameLoc.toString(), id, ModCharset.instance, trackingRange, updateFrequency, sendsVelocityUpdates);
		takenEntityIds.put(id, name);
	}

	public static <T extends IForgeRegistryEntry<T>> void register(IForgeRegistry<T> registry, T object, String name) {
		register(registry, object, name, ModCharset.CREATIVE_TAB);
	}

	public static <T extends IForgeRegistryEntry<T>> void register(IForgeRegistry<T> registry, T object, String name, CreativeTabs tab) {
		object.setRegistryName(new ResourceLocation(ModCharset.MODID, name));
		registry.register(object);
		UtilProxyCommon.proxy.setCreativeTabIfNotPresent(object, tab);
	}

	public static void loadConfigIds(Configuration configIds) {
		ConfigCategory cat = configIds.getCategory("entity");
		for (Map.Entry<String, Property> e : cat.entrySet()) {
			if (e.getValue().isIntValue()) {
				takenEntityIds.put(e.getValue().getInt(), e.getKey());
			}
		}
	}
}
