package pl.asie.charset.module.tools.building;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.material.ColorLookupHandler;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.utils.RenderUtils;

@SideOnly(Side.CLIENT)
public class ToolItemColor implements IItemColor {
    public static final ToolItemColor INSTANCE = new ToolItemColor();

    private ToolItemColor() {

    }

    @Override
    public int getColorFromItemstack(ItemStack stack, int tintIndex) {
        ItemCharsetTool.MaterialSlot slot = tintIndex == 1 ? ItemCharsetTool.MaterialSlot.HEAD : (tintIndex == 0 ? ItemCharsetTool.MaterialSlot.HANDLE : null);
        if (slot != null && stack.getItem() instanceof ItemCharsetTool) {
            ItemMaterial material = ((ItemCharsetTool) stack.getItem()).getMaterial(slot);
            return ColorLookupHandler.INSTANCE.getColor(material.getStack(), RenderUtils.AveragingMode.FULL);
        }
        return -1;
    }
}
