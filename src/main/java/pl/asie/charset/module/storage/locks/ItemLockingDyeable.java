package pl.asie.charset.module.storage.locks;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.item.IDyeableItem;
import pl.asie.charset.lib.item.ItemBase;
import pl.asie.charset.lib.utils.ItemUtils;

import java.util.List;

public class ItemLockingDyeable extends ItemBase implements IDyeableItem {
    @Override
    public int getColor(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey("color") ? stack.getTagCompound().getInteger("color") : -1;
    }

    @Override
    public void setColor(ItemStack stack, int color) {
        ItemUtils.getTagCompound(stack, true).setInteger("color", color);
    }

    @SideOnly(Side.CLIENT)
    public static class Color implements IItemColor {
        @Override
        public int getColorFromItemstack(ItemStack stack, int tintIndex) {
            if (tintIndex > 0 && stack.hasTagCompound() && stack.getTagCompound().hasKey("color")) {
                return stack.getTagCompound().getInteger("color");
            }

            return CharsetStorageLocks.DEFAULT_LOCKING_COLOR;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        if (hasColor(stack)) {
            tooltip.add(LockEventHandler.getColorDyed(getColor(stack)));
        }
    }
}
