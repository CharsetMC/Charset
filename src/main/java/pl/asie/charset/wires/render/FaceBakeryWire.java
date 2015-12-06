package pl.asie.charset.wires.render;

import org.lwjgl.util.vector.Vector3f;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;

public class FaceBakeryWire extends FaceBakery {
	public int uvScale = 1;
	public int uvOffset = 0;

	private TextureAtlasSprite sprite;

	public BakedQuad makeBakedQuad(Vector3f min, Vector3f max, int tintIndex, float[] uv,
									TextureAtlasSprite icon, EnumFacing facing, ModelRotation rot, boolean uvLocked) {
		sprite = icon;

		if (!uvLocked && uvScale > 1) {
			float ox = (uvOffset % uvScale) * (16.0f / uvScale);
			float oy = (uvOffset / uvScale) * (16.0f / uvScale);
			uv[0] = uv[0] / (float) uvScale + ox;
			uv[1] = uv[1] / (float) uvScale + oy;
			uv[2] = uv[2] / (float) uvScale + ox;
			uv[3] = uv[3] / (float) uvScale + oy;
		}

		return makeBakedQuad(
				min, max,
				new BlockPartFace(null, tintIndex, "", new BlockFaceUV(uv, 0)),
				icon, facing, rot, null, uvLocked, true
		);
	}

	private void func_178401_a(int p_178401_1_, int[] p_178401_2_, EnumFacing facing, BlockFaceUV p_178401_4_, TextureAtlasSprite p_178401_5_)
	{
		int i = 7 * p_178401_1_;
		float f = Float.intBitsToFloat(p_178401_2_[i]);
		float f1 = Float.intBitsToFloat(p_178401_2_[i + 1]);
		float f2 = Float.intBitsToFloat(p_178401_2_[i + 2]);

		if (f < -0.1F || f >= 1.1F)
		{
			f -= (float) MathHelper.floor_float(f);
		}

		if (f1 < -0.1F || f1 >= 1.1F)
		{
			f1 -= (float)MathHelper.floor_float(f1);
		}

		if (f2 < -0.1F || f2 >= 1.1F)
		{
			f2 -= (float)MathHelper.floor_float(f2);
		}

		float f3 = 0.0F;
		float f4 = 0.0F;

		switch (facing)
		{
			case DOWN:
				f3 = f * 16.0F;
				f4 = (1.0F - f2) * 16.0F;
				break;
			case UP:
				f3 = f * 16.0F;
				f4 = f2 * 16.0F;
				break;
			case NORTH:
				f3 = (1.0F - f) * 16.0F;
				f4 = (1.0F - f1) * 16.0F;
				break;
			case SOUTH:
				f3 = f * 16.0F;
				f4 = (1.0F - f1) * 16.0F;
				break;
			case WEST:
				f3 = f2 * 16.0F;
				f4 = (1.0F - f1) * 16.0F;
				break;
			case EAST:
				f3 = (1.0F - f2) * 16.0F;
				f4 = (1.0F - f1) * 16.0F;
		}

		if (uvScale > 1) {
			float ox = (uvOffset % uvScale) * (16.0f / uvScale);
			float oy = (uvOffset / uvScale) * (16.0f / uvScale);
			f3 = f3 / (float) uvScale + ox;
			f4 = f4 / (float) uvScale + oy;
		}

		int j = p_178401_4_.func_178345_c(p_178401_1_) * 7;
		p_178401_2_[j + 4] = Float.floatToRawIntBits(p_178401_5_.getInterpolatedU((double) f3));
		p_178401_2_[j + 4 + 1] = Float.floatToRawIntBits(p_178401_5_.getInterpolatedV((double) f4));
	}

	@Override
	public void func_178409_a(int[] faceData, EnumFacing facing, BlockFaceUV faceUV, TextureAtlasSprite p_178409_4_) {
		for (int i = 0; i < 4; ++i)
		{
			this.func_178401_a(i, faceData, facing, faceUV, p_178409_4_);
		}
	}
}
