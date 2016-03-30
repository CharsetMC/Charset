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

import net.minecraftforge.client.model.IColoredBakedQuad;

public class FaceBakeryWire extends FaceBakery {
	public int uvScale = 1;
	public int uvOffset = 0;

	private int getFaceShadeColor(int tintIndex, EnumFacing facing) {
		int r = tintIndex >> 16;
		int g = (tintIndex >> 8) & 255;
		int b = tintIndex & 255;
		float f = this.getFaceBrightness(facing);

		r = MathHelper.clamp_int((int) ((float) r * f), 0, 255);
		g = MathHelper.clamp_int((int) ((float) g * f), 0, 255);
		b = MathHelper.clamp_int((int) ((float) b * f), 0, 255);
		return -16777216 | b << 16 | g << 8 | r;
	}

	private float getFaceBrightness(EnumFacing facing) {
		switch (facing) {
			case DOWN:
				return 0.5F;
			case UP:
				return 1.0F;
			case NORTH:
			case SOUTH:
				return 0.8F;
			case WEST:
			case EAST:
				return 0.6F;
			default:
				return 1.0F;
		}
	}

	public BakedQuad makeBakedQuad(Vector3f min, Vector3f max, int tintIndex, float[] uvOrig,
								   TextureAtlasSprite icon, EnumFacing facing, ModelRotation rot, boolean uvLocked) {
		float[] uv = uvOrig;
		if (!uvLocked && uvScale > 1) {
			float ox = (uvOffset % uvScale) * (16.0f / uvScale);
			float oy = (uvOffset / uvScale) * (16.0f / uvScale);
			uv = new float[]{
					uv[0] / (float) uvScale + ox,
					uv[1] / (float) uvScale + oy,
					uv[2] / (float) uvScale + ox,
					uv[3] / (float) uvScale + oy
			};
		}

		BakedQuad quad = makeBakedQuad(
				min, max,
				new BlockPartFace(null, tintIndex, "", new BlockFaceUV(uv, 0)),
				icon, facing, rot, null, uvLocked, true
		);

		if (tintIndex == -1) {
			return quad;
		} else {
			int[] data = quad.getVertexData();
			data[3] = data[10] = data[17] = data[24] = getFaceShadeColor(tintIndex, rot.rotate(facing));
			return new IColoredBakedQuad.ColoredBakedQuad(data, tintIndex, quad.getFace());
		}
	}

	private void func_178401_a(int p_178401_1_, int[] p_178401_2_, EnumFacing facing, BlockFaceUV p_178401_4_, TextureAtlasSprite p_178401_5_) {
		int i = 7 * p_178401_1_;
		float f = Float.intBitsToFloat(p_178401_2_[i]);
		float f1 = Float.intBitsToFloat(p_178401_2_[i + 1]);
		float f2 = Float.intBitsToFloat(p_178401_2_[i + 2]);

		if (f < -0.1F || f >= 1.1F) {
			f -= (float) MathHelper.floor_float(f);
		}

		if (f1 < -0.1F || f1 >= 1.1F) {
			f1 -= (float) MathHelper.floor_float(f1);
		}

		if (f2 < -0.1F || f2 >= 1.1F) {
			f2 -= (float) MathHelper.floor_float(f2);
		}

		float f3 = 0.0F;
		float f4 = 0.0F;

		switch (facing) {
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
	public void lockUv(int[] faceData, EnumFacing facing, BlockFaceUV faceUV, TextureAtlasSprite p_178409_4_) {
		for (int i = 0; i < 4; ++i) {
			this.func_178401_a(i, faceData, facing, faceUV, p_178409_4_);
		}
	}
}
