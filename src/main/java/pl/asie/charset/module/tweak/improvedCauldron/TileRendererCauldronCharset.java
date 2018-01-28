package pl.asie.charset.module.tweak.improvedCauldron;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.animation.FastTESR;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.util.vector.Vector3f;
import pl.asie.charset.lib.render.model.ModelTransformer;
import pl.asie.charset.lib.render.model.SimpleBakedModel;
import pl.asie.charset.lib.utils.ProxiedBlockAccess;
import pl.asie.charset.lib.utils.RenderUtils;
import pl.asie.charset.module.storage.tanks.TileTank;
import pl.asie.charset.module.storage.tanks.TileTankRenderer;

public class TileRendererCauldronCharset extends FastTESR<TileCauldronCharset> {
	private static final BlockModelRenderer renderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();

	private static final class FluidColorTransformer implements ModelTransformer.IVertexTransformer {
		private final int color;

		public FluidColorTransformer(int color) {
			this.color = color;
		}

		@Override
		public float[] transform(BakedQuad quad, VertexFormatElement element, float... data) {
			if (element.getUsage() == VertexFormatElement.EnumUsage.COLOR) {
				for (int i = 0; i < Math.min(data.length, 4); i++) {
					data[i] = data[i] * ((color >> ((i < 3 ? (2 - i) : i) * 8)) & 0xFF) / 255.0f;
				}
			}
			return data;
		}
	}

	private static final class TankBlockAccess extends ProxiedBlockAccess {
		private final int fluidLight;

		public TankBlockAccess(IBlockAccess access, int fluidLight) {
			super(access);
			this.fluidLight = fluidLight;
		}

		@Override
		public int getCombinedLight(BlockPos pos, int lightValue) {
			return access.getCombinedLight(pos, Math.max(fluidLight, lightValue));
		}
	}

	private static IBlockState getFluidState(FluidStack stack, IBlockState default_state) {
		return stack.getFluid().getBlock() != null ? stack.getFluid().getBlock().getDefaultState() : default_state;
	}

	@Override
	public void renderTileEntityFast(TileCauldronCharset te, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder buffer) {
		BlockPos pos = te.getPos();
		FluidStack contents = te.getContents();
		if (contents == null) {
			return;
		}

		buffer.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());

		IBlockState state = getFluidState(contents, Blocks.AIR.getDefaultState());
		IBakedModel model;

		TextureMap map = Minecraft.getMinecraft().getTextureMapBlocks();
		TextureAtlasSprite sprite = map.getTextureExtry(contents.getFluid().getStill().toString());
		if (sprite == null) {
			sprite = map.getTextureExtry(TextureMap.LOCATION_MISSING_TEXTURE.toString());
			if (sprite == null) return;
		}

		float height = te.getFluidHeight();
		SimpleBakedModel smodel = new SimpleBakedModel();

		int color = contents.getFluid().getColor(contents);

		Vector3f from = new Vector3f(2, height, 2);
		Vector3f to = new Vector3f(14, height, 14);

		smodel.addQuad(null, RenderUtils.createQuad(from, to, EnumFacing.UP, sprite, -1));

		if (color == -1) {
			model = smodel;
		} else {
			model = ModelTransformer.transform(smodel, state, 0L, new FluidColorTransformer(color));
		}

		int light = contents.getFluid().getLuminosity();
		IBlockAccess tankAccess = new TankBlockAccess(te.getWorld(), light);

		renderer.renderModel(tankAccess, model, state, pos, buffer, false, 0L);

		buffer.setTranslation(0, 0, 0);
	}
}
