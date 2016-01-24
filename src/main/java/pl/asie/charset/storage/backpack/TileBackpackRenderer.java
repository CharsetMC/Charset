package pl.asie.charset.storage.backpack;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.BlockPos;

import net.minecraftforge.client.model.animation.FastTESR;

import pl.asie.charset.lib.refs.Properties;
import pl.asie.charset.storage.ProxyClient;

/**
 * Created by asie on 1/10/16.
 */
public class TileBackpackRenderer extends FastTESR<TileBackpack> {
	protected static BlockModelRenderer renderer;

	@Override
	public void renderTileEntityFast(TileBackpack te, double x, double y, double z, float partialTicks, int destroyStage, WorldRenderer worldRenderer) {
		if(renderer == null) {
			renderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
		}

		BlockPos pos = te.getPos();
		IBlockState state = getWorld().getBlockState(pos);

		if (state.getBlock() instanceof BlockBackpack) {
			worldRenderer.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());
			renderer.renderModel(getWorld(), ProxyClient.backpackTopModel[state.getValue(Properties.FACING4).ordinal() - 2], state, pos, worldRenderer, false);
		}
	}
}
