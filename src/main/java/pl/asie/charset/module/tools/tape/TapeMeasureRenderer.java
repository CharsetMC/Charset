package pl.asie.charset.module.tools.tape;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TapeMeasureRenderer implements IItemColor {
	public static final TapeMeasureRenderer INSTANCE = new TapeMeasureRenderer();
	private TextureAtlasSprite tape;

	private TapeMeasureRenderer() {

	}

	@SubscribeEvent
	public void onTextureStitch(TextureStitchEvent.Pre event) {
		tape = event.getMap().registerSprite(new ResourceLocation("charset:misc/tape_measure_tape"));
	}

	@SubscribeEvent
	public void onItemColor(ColorHandlerEvent.Item event) {
		event.getItemColors().registerItemColorHandler(this, CharsetToolsTapeMeasure.tapeMeasure);
	}

	@Override
	public int colorMultiplier(ItemStack stack, int tintIndex) {
		if (tintIndex != 1) {
			return -1;
		}

		// TODO
		return 0xFFF1EC5D;
	}
}
