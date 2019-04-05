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

package pl.asie.charset.module.transport.dyeableMinecarts;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelMinecart;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.ResourceLocation;
import pl.asie.charset.lib.utils.ColorUtils;

public class ModelMinecartWrapped extends ModelMinecart {
	public static final ResourceLocation DYEABLE_MINECART = new ResourceLocation("charset_generated:textures/entity/minecart.png");
	public static final ResourceLocation MINECART = new ResourceLocation("entity/minecart");
	private final ModelBase parent;

	public ModelMinecartWrapped(ModelBase parent) {
		super();
		this.parent = parent;

		if (parent instanceof ModelMinecart) {
			this.sideModels = ((ModelMinecart) parent).sideModels;
		}
	}

	public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale) {
		if (entityIn instanceof EntityMinecart) {
			EntityMinecart minecart = (EntityMinecart) entityIn;
			MinecartDyeable dyeable = MinecartDyeable.get(minecart);
			if (dyeable != null && dyeable.getColor() != null) {
				Minecraft.getMinecraft().renderEngine.bindTexture(DYEABLE_MINECART);
				float[] color = dyeable.getColor().getColorComponentValues();

				GlStateManager.color(color[0], color[1], color[2]);
			}
		}

		parent.render(entityIn, p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale);

		GlStateManager.color(1.0f, 1.0f, 1.0f);
	}
}
