/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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
    public int colorMultiplier(ItemStack stack, int tintIndex) {
        ItemCharsetTool.MaterialSlot slot = tintIndex == 1 ? ItemCharsetTool.MaterialSlot.HEAD : (tintIndex == 0 ? ItemCharsetTool.MaterialSlot.HANDLE : null);
        if (slot != null && stack.getItem() instanceof ItemCharsetTool) {
            ItemMaterial material = ((ItemCharsetTool) stack.getItem()).getMaterial(stack, slot);
            // TODO: Handle material disappearance
            if (material != null) {
                ItemMaterial renderMaterial = material.getRelated("block");
                if (renderMaterial == null) {
                    renderMaterial = material.getRelated("log");
                    if (renderMaterial == null) {
                        renderMaterial = material;
                    }
                }
                return ColorLookupHandler.INSTANCE.getColor(renderMaterial.getStack(), RenderUtils.AveragingMode.FULL);
            }
        }
        return -1;
    }
}
