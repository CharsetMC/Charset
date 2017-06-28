package pl.asie.charset.module.storage.locks;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.item.IDyeableItem;
import pl.asie.charset.lib.item.ItemBase;
import pl.asie.charset.lib.utils.ItemUtils;

public class ItemLockingDyeable extends ItemBase implements IDyeableItem {
    @Override
    public int getColor(ItemStack stack) {
        return hasColor(stack) ? stack.getTagCompound().getInteger("color1") : -1;
    }

    @Override
    public boolean hasColor(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey("color1");
    }

    @Override
    public void setColor(ItemStack stack, int color) {
        ItemUtils.getTagCompound(stack, true).setInteger("color1", color);
    }

    @SideOnly(Side.CLIENT)
    public static class Color implements IItemColor {
        @Override
        public int getColorFromItemstack(ItemStack stack, int tintIndex) {
            if (stack.hasTagCompound()) {
                for (int i = tintIndex; i >= 0; i--) {
                    String key = "color" + i;
                    if (stack.getTagCompound().hasKey(key)) {
                        return stack.getTagCompound().getInteger(key);
                    }
                }
            }

            return CharsetStorageLocks.DEFAULT_LOCKING_COLOR;
        }
    }
}
