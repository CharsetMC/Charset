package pl.asie.charset.tweaks.minecart;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.ResourceLocation;

/**
 * Created by asie on 12/5/15.
 */
public class ModelMinecartWrapped extends ModelBase {
	private final ModelBase parent;

	public ModelMinecartWrapped(ModelBase parent) {
		this.parent = parent;
	}

	public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale) {
		MinecartProperties properties;

		if (entityIn instanceof EntityMinecart) {
			EntityMinecart minecart = (EntityMinecart) entityIn;
			if (minecart.getExtendedProperties(MinecartProperties.NAME) instanceof MinecartProperties) {
				properties = (MinecartProperties) minecart.getExtendedProperties(MinecartProperties.NAME);
				if (properties.getColor() >= 0) {
					Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("charsettweaks:textures/entity/minecart.png"));

					float r = (float) ((properties.getColor() >> 16) & 255) / 255.0f;
					float g = (float) ((properties.getColor() >> 8) & 255) / 255.0f;
					float b = (float) (properties.getColor() & 255) / 255.0f;
					GlStateManager.color(r, g, b);
				}
			}
		}

		parent.render(entityIn, p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale);

		GlStateManager.color(1.0f, 1.0f, 1.0f);
	}
}
