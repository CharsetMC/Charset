package pl.asie.charset.tweaks;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.render.sprite.PixelOperationSprite;
import pl.asie.charset.lib.utils.ColorUtils;

import java.util.Arrays;

public class TweakWoolColors extends Tweak {
	private int colorMultiplier(String prefix, EnumDyeColor color) {
		float[] dOrig = EntitySheep.getDyeRgb(color);
		float[] d = Arrays.copyOf(dOrig, 3);

		if (color == EnumDyeColor.BLUE) {
			d[0] *= 0.925F;
			d[1] *= 0.925F;
			d[2] *= 0.875F;
		} else if (color == EnumDyeColor.ORANGE) {
			d[0] *= 1.075F;
			d[1] *= 1.075F;
		} else if (color == EnumDyeColor.YELLOW) {
			d[0] *= 1.10F;
			d[1] *= 0.95F;
			d[2] *= 0.95F;
		} else if (color == EnumDyeColor.MAGENTA) {
			d[0] *= 1.1F;
			d[1] *= 1.05F;
			d[2] *= 1.1F;
		} else if (color == EnumDyeColor.LIGHT_BLUE) {
			d[0] *= 1.05F;
			d[1] *= 1.05F;
			d[2] *= 1.05F;
		} else if (color == EnumDyeColor.PINK) {
			d[0] *= 1.025F;
			d[1] *= 1.075F;
			d[2] *= 1.025F;
		} else if (color == EnumDyeColor.CYAN) {
			d[0] *= 0.9F;
			d[1] *= 0.95F;
			d[2] *= 1.05F;
		} else if (color == EnumDyeColor.PURPLE) {
			d[0] *= 1F;
			d[1] *= 1.075F;
			d[2] *= 1F;
		} else if (color == EnumDyeColor.BROWN) {
			d[0] *= 1.0F;
			d[1] *= 0.925F;
			d[2] *= 1.0F;
		} else if (color == EnumDyeColor.BLACK) {
			d[0] *= 1.33F;
			d[1] *= 1.33F;
			d[2] *= 1.33F;
		} else if (color == EnumDyeColor.GRAY) {
			d[0] *= 1.125F;
			d[1] *= 1.125F;
			d[2] *= 1.125F;
		}

		if (prefix.contains("hardened_clay")) {
			float lum = d[0] * 0.3F + d[1] * 0.59F + d[2] * 0.11F;
			d[0] += (lum - d[0]) * 0.35F;
			d[1] += (lum - d[1]) * 0.35F;
			d[2] += (lum - d[2]) * 0.35F;
		}

		return    (Math.min(Math.round(d[0] * 255.0F), 255) << 16)
				| (Math.min(Math.round(d[1] * 255.0F), 255) << 8)
				| (Math.min(Math.round(d[2] * 255.0F), 255))
				| 0xFF000000;
	}

	public TweakWoolColors() {
		super("client", "tweakedWoolColors", "Makes wool a bit prettier.", true);

	}

	@Override
	public void enable() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public boolean canTogglePostLoad() {
		return false;
	}

	private void recolorTextures(TextureMap map, String prefix) {
		ResourceLocation source = new ResourceLocation(prefix + "white");
		for (int i = 1; i < 16; i++) { // skip white
			String s = ColorUtils.UNDERSCORE_DYE_SUFFIXES[i];
			ResourceLocation target = new ResourceLocation(prefix + s);
			map.setTextureEntry(new PixelOperationSprite.Multiply(target.toString(), source, colorMultiplier(prefix, EnumDyeColor.byMetadata(i))));
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureStitchPre(TextureStitchEvent.Pre event) {
		recolorTextures(event.getMap(), "minecraft:blocks/wool_colored_");
		recolorTextures(event.getMap(), "minecraft:blocks/glass_");
		recolorTextures(event.getMap(), "minecraft:blocks/glass_pane_top_");
		// recolorTextures(event.getMap(), "minecraft:blocks/hardened_clay_stained_");
	}
}
