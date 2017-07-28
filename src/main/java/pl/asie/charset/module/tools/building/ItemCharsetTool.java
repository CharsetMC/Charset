package pl.asie.charset.module.tools.building;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import pl.asie.charset.lib.item.ItemBase;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;

import java.util.Collection;

public class ItemCharsetTool extends ItemBase {
    public ItemCharsetTool() {
        super();
        setMaxStackSize(1);
    }

    public ItemMaterial getMaterial(MaterialSlot slot) {
        return getDefaultMaterial(slot);
    }

    protected ItemMaterial getDefaultMaterial(MaterialSlot slot) {
        if (slot == MaterialSlot.HANDLE) {
            return ItemMaterialRegistry.INSTANCE.getOrCreateMaterial(new ItemStack(Items.STICK));
        } else {
            Collection<ItemMaterial> materials = ItemMaterialRegistry.INSTANCE.getMaterialsByTypes("block", "iron");
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
        HEAD
    }
}
