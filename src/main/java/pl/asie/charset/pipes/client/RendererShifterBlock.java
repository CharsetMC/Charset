package pl.asie.charset.pipes.client;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class RendererShifterBlock implements ISimpleBlockRenderingHandler {
	private static final int[][] ROTATION_MATRIX = new int[][] {
			{0, 0, 3, 3, 3, 3},
			{0, 0, 0, 0, 0, 0},
			{0, 0, 0, 0, 2, 1},
			{3, 3, 0, 0, 1, 2},
			{1, 2, 2, 1, 0, 0},
			{2, 1, 1, 2, 0, 0}
	};

	private static int renderId;

	public RendererShifterBlock() {
		renderId = RenderingRegistry.getNextAvailableRenderId();
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
		Tessellator tessellator = Tessellator.instance;
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, -1.0F, 0.0F);
		renderer.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(0, metadata));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		renderer.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(1, metadata));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, -1.0F);
		renderer.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(2, metadata));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(3, metadata));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(-1.0F, 0.0F, 0.0F);
		renderer.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(4, metadata));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(1.0F, 0.0F, 0.0F);
		renderer.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(5, metadata));
		tessellator.draw();
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		int metadata = world.getBlockMetadata(x, y, z);
		/* ForgeDirection filterRenderSide = ((BlockShifter) block).getFilterRenderSide(world, x, y, z, metadata);

		int[] rotationMatrix = new int[6];
		for (int i = 0; i < 6; i++) {
			rotationMatrix[i] = ROTATION_MATRIX[metadata][i];
		}
		rotationMatrix[filterRenderSide.ordinal()] = 0; */

		int[] rotationMatrix = ROTATION_MATRIX[metadata];

		renderer.uvRotateBottom = rotationMatrix[0];
		renderer.uvRotateTop = rotationMatrix[1];
		renderer.uvRotateNorth = rotationMatrix[4];
		renderer.uvRotateSouth = rotationMatrix[5];
		renderer.uvRotateWest = rotationMatrix[2];
		renderer.uvRotateEast = rotationMatrix[3];

		renderer.renderStandardBlock(block, x, y, z);

		renderer.uvRotateBottom = 0;
		renderer.uvRotateTop = 0;
		renderer.uvRotateNorth = 0;
		renderer.uvRotateSouth = 0;
		renderer.uvRotateWest = 0;
		renderer.uvRotateEast = 0;

		return true;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}

	@Override
	public int getRenderId() {
		return renderId;
	}

	public static int id() {
		return renderId;
	}
}
