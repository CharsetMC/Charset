package pl.asie.charset.module.tools.building;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import pl.asie.charset.lib.item.ISubItemProvider;
import pl.asie.charset.lib.item.ItemBase;
import pl.asie.charset.lib.item.SubItemProviderCache;
import pl.asie.charset.lib.item.SubItemProviderRecipes;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.module.storage.barrels.CharsetStorageBarrels;

import javax.annotation.Nullable;
import java.util.Collection;

public class ItemCharsetTool extends ItemBase {
    public ItemCharsetTool() {
        super();
        setMaxStackSize(1);
    }

    public ItemMaterial getMaterial(ItemStack stack, MaterialSlot slot) {
        if (stack.hasTagCompound()) {
            NBTTagCompound cpd = stack.getTagCompound();
            if (cpd.hasKey(slot.nbtKey)) {
                return ItemMaterialRegistry.INSTANCE.getMaterial(cpd, slot.nbtKey);
            }
        }

        return getDefaultMaterial(slot);
    }

    protected ItemMaterial getDefaultMaterial(MaterialSlot slot) {
        if (slot == MaterialSlot.HANDLE) {
            return ItemMaterialRegistry.INSTANCE.getOrCreateMaterial(new ItemStack(Items.STICK));
        } else {
            Collection<ItemMaterial> materials = ItemMaterialRegistry.INSTANCE.getMaterialsByTypes("ingot", "iron");
            if (materials.size() > 0) {
                return materials.iterator().next();
            } else {
                materials = ItemMaterialRegistry.INSTANCE.getMaterialsByTypes("ingot");
                if (materials.size() > 0) {
                    return materials.iterator().next();
                } else {
                    throw new RuntimeException("No default material found! D:");
                }
            }
        }
    }

    public enum MaterialSlot {
        HANDLE,
        HEAD;

        public final String nbtKey;

        MaterialSlot() {
            this.nbtKey = "m" + name().substring(0, 1).toUpperCase() + name().substring(1).toLowerCase();
        }
    }
}
