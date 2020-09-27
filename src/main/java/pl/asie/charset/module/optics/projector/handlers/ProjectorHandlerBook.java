/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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

package pl.asie.charset.module.optics.projector.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWrittenBook;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.utils.Orientation;
import pl.asie.charset.lib.utils.Quaternion;
import pl.asie.charset.module.optics.projector.IProjector;
import pl.asie.charset.module.optics.projector.IProjectorHandler;
import pl.asie.charset.module.optics.projector.IProjectorSurface;
import pl.asie.charset.module.optics.projector.ProjectorHelper;

import java.util.List;

public class ProjectorHandlerBook implements IProjectorHandler<ItemStack> {
	@Override
	public boolean matches(ItemStack target) {
		return target.getItem() instanceof ItemWrittenBook;
	}

	@Override
	public int getPageCount(ItemStack target) {
		if (target.hasTagCompound()) {
			NBTTagList pages = target.getTagCompound().getTagList("pages", Constants.NBT.TAG_STRING);
			if (pages.tagCount() >= 1) {
				return pages.tagCount();
			}
		}

		return 1;
	}

	@Override
	public float getAspectRatio(ItemStack target) {
		return 146f/180f;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void render(ItemStack stack, IProjector projector, IProjectorSurface surface) {
		// oh boy! text!
		if (stack.hasTagCompound()) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(((surface.getCornerStart().x + surface.getCornerEnd().x) / 2) + surface.getScreenFacing().getXOffset() * 0.001f,
					((surface.getCornerStart().y + surface.getCornerEnd().y) / 2) + surface.getScreenFacing().getYOffset() * 0.001f,
					((surface.getCornerStart().z + surface.getCornerEnd().z) / 2) + surface.getScreenFacing().getZOffset() * 0.001f);

			Orientation orientation = ProjectorHelper.INSTANCE.getOrientation(surface);

			Quaternion.fromOrientation(orientation).glRotate();
			GlStateManager.rotate(270.0f, 0, 0, 1);
			GlStateManager.rotate(270.0f, 0, 1, 0);
			GlStateManager.translate(0, 0, -ProjectorHelper.OFFSET);

			float scaleVal = 2f * surface.getWidth() / 146f;
			GlStateManager.scale(scaleVal, scaleVal, scaleVal);
			FontRenderer renderer = Minecraft.getMinecraft().fontRenderer;

			NBTTagList pages = stack.getTagCompound().getTagList("pages", Constants.NBT.TAG_STRING);
			if (pages.tagCount() > projector.getPage()) {
				String pageCount = I18n.format("book.pageIndicator", projector.getPage() + 1, pages.tagCount());
				renderer.drawString(pageCount,-73 + 129 - renderer.getStringWidth(pageCount), -90 + 15, 0xFF000000);

				String page = ((NBTTagString) pages.get(projector.getPage())).getString();
				ITextComponent fullComponent = ITextComponent.Serializer.jsonToComponent(page);
				if (fullComponent != null) {
					List<ITextComponent> components = GuiUtilRenderComponents.splitText(fullComponent, 116, renderer, true, true);
					for (int i = 0; i < components.size(); i++) {
						renderer.drawString(components.get(i).getUnformattedText(), -73 + 16, -90 + 30 + i * renderer.FONT_HEIGHT, 0xFF000000);
					}
				}
			}
			GlStateManager.popMatrix();
			surface.restoreGLColor();
		}

		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("textures/gui/book.png"));
		ProjectorHelper.INSTANCE.renderTexture(surface, 20, 20+146, 1, 1+180);
	}
}
