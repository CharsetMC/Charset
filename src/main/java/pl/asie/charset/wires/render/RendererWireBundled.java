package pl.asie.charset.wires.render;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class RendererWireBundled extends RendererWireInsulated {
	public RendererWireBundled(String type, int width, int height) {
		super(type, width, height);
	}

	@Override
	protected int getIconArraySize() {
		return 5;
	}

	@Override
	protected TextureAtlasSprite getIcon(boolean isCrossed, boolean lit, boolean isEdge, boolean isCrossroads, EnumFacing side) {
		if (isCrossed) {
			return icons[0];
		} else if (isEdge) {
			return icons[1];
		} else if (isCrossroads && side.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE) {
			return icons[4];
		} else {
			return icons[2 | (side.ordinal() & 1)];
		}
	}

	public void loadTextures(TextureMap map) {
		icons[0] = map.registerSprite(new ResourceLocation("charsetwires:blocks/" + type + "_cross"));
		icons[1] = map.registerSprite(new ResourceLocation("charsetwires:blocks/" + type + "_edge"));
		icons[2] = map.registerSprite(new ResourceLocation("charsetwires:blocks/" + type + "_side_nw"));
		icons[3] = map.registerSprite(new ResourceLocation("charsetwires:blocks/" + type + "_side_se"));
		icons[4] = map.registerSprite(new ResourceLocation("charsetwires:blocks/" + type + "_side_se_wire"));
	}
}
