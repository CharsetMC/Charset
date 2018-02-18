package pl.asie.charset.module.crafting.compression;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.model.animation.FastTESR;
import net.minecraftforge.common.DimensionManager;
import pl.asie.charset.lib.Properties;

public class TileCompressionCrafterRenderer extends FastTESR<TileCompressionCrafter> {
	protected static BlockModelRenderer renderer;

	@Override
	public void renderTileEntityFast(TileCompressionCrafter te, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder buffer) {
		if (renderer == null) {
			renderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
		}

		BlockPos pos = te.getPos();
		IBlockState state = getWorld().getBlockState(pos);

		if (state.getBlock() instanceof BlockCompressionCrafter) {
			EnumFacing facing = state.getValue(Properties.FACING);
			float extension = 0f;
			if (te.shape != null) {
				extension = Math.max(0, te.shape.getRenderProgress());
			}

			double tx = x - pos.getX() + (facing.getFrontOffsetX() * extension);
			double ty = y - pos.getY() + (facing.getFrontOffsetY() * extension);
			double tz = z - pos.getZ() + (facing.getFrontOffsetZ() * extension);

			long r = MathHelper.getPositionRandom(pos);

			tx += ((r & 0x00F) - 7.5f) / 2048f;
			ty += (((r >> 4) & 0x00F) - 7.5f) / 2048f;
			tz += (((r >> 8) & 0x00F) - 7.5f) / 2048f;

			buffer.setTranslation(tx, ty, tz);
			renderer.renderModel(getWorld(), ProxyClient.rodModels[facing.ordinal()], state, pos, buffer, false);
		}
	}
}
