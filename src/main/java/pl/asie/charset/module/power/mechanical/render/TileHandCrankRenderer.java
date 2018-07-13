package pl.asie.charset.module.power.mechanical.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.model.TRSRTransformation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Quaternion;
import pl.asie.charset.lib.utils.Orientation;
import pl.asie.charset.module.power.mechanical.BlockHandCrank;
import pl.asie.charset.module.power.mechanical.CharsetPowerMechanical;
import pl.asie.charset.module.power.mechanical.TileAxle;
import pl.asie.charset.module.power.mechanical.TileHandCrank;

public class TileHandCrankRenderer extends TileEntitySpecialRenderer<TileHandCrank> {
	public static final TileHandCrankRenderer INSTANCE = new TileHandCrankRenderer();
	protected static BlockModelRenderer renderer;
	public IModel crankModel;

	@Override
	public void render(TileHandCrank te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if (renderer == null) {
			renderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
		}
		bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		if (te == null || te.getWorld() == null) {
			return;
		}

		BlockPos pos = te.getPos();
		EnumFacing facing = te.getFacing();

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.translate(0.5f, 0.5f, 0.5f);
		GlStateManager.rotate((float) (te.getRotation() + (te.getForce() * partialTicks)) * TileAxle.SPEED_MULTIPLIER, 0, 0, 1);
		if (facing.getAxis() != EnumFacing.Axis.Y) {
			GlStateManager.rotate(-facing.getHorizontalAngle(), 0, 1, 0);
			GlStateManager.rotate(90f, 1, 0, 0);
		} else if (facing == EnumFacing.DOWN) {
			GlStateManager.rotate(180f, 1, 0, 0);
		}
		GlStateManager.translate(-0.5f, -0.5f, -0.5f);
		GlStateManager.translate(-pos.getX(), -pos.getY(), -pos.getZ());

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		IBakedModel bakedModel = crankModel.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
		renderer.renderModelFlat(getWorld(), bakedModel, CharsetPowerMechanical.blockHandCrank.getDefaultState(), te.getPos(), buffer, false, 0L);
		tessellator.draw();

		GlStateManager.popMatrix();
	}
}