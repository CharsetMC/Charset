package pl.asie.charset.lib.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraftforge.common.util.ForgeDirection;

import pl.asie.charset.lib.IConnectable;

public abstract class RendererPipeLikeBlock implements ISimpleBlockRenderingHandler {
	private static int renderId;

	public RendererPipeLikeBlock() {
		renderId = RenderingRegistry.getNextAvailableRenderId();
	}

	public abstract float getPipeThickness();
	
	private void renderInventoryIcon(Block block, IIcon icon, RenderBlocks renderer) {
		Tessellator tessellator = Tessellator.instance;
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, -1.0F, 0.0F);
		renderer.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, icon);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		renderer.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, icon);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, -1.0F);
		renderer.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, icon);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, icon);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(-1.0F, 0.0F, 0.0F);
		renderer.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, icon);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(1.0F, 0.0F, 0.0F);
		renderer.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, icon);
		tessellator.draw();
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId,
									 RenderBlocks renderer) {
		FakeBlock fakeBlock = FakeBlock.INSTANCE;
		float offset = getPipeThickness() / 2;

		fakeBlock.setIcon(block.getIcon(0, 0));
		fakeBlock.resetRenderMask();

		renderer.setRenderBounds(offset, 0, offset, 1 - offset, 1, 1 - offset);
		renderInventoryIcon(fakeBlock, fakeBlock.getIcon(0, 0), renderer);
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		int connMask = 0;

		TileEntity tile = world.getTileEntity(x, y, z);

		if (tile != null && tile instanceof IConnectable) {
			IConnectable pipeTile = (IConnectable) tile;
			for (int i = 0; i < 6; i++) {
				if (pipeTile.connects(ForgeDirection.getOrientation(i))) {
					connMask |= (1 << i);
				}
			}
		}

		FakeBlock fakeBlock = FakeBlock.INSTANCE;
		float offset = getPipeThickness() / 2;

		IIcon[] icons = new IIcon[6];

		for (int i = 0; i < 6; i++) {
			icons[i] = block.getIcon(0, 0);
		}

		switch (connMask) {
			case 0x03: // up-down
				icons[2] = icons[3] = icons[4] = icons[5] = block.getIcon(0, 1);
				renderer.uvRotateNorth = 1;
				renderer.uvRotateSouth = 1;
				renderer.uvRotateWest = 1;
				renderer.uvRotateEast = 1;
				break;
			case 0x0C: // north-south
				icons[0] = icons[1] = icons[4] = icons[5] = block.getIcon(0, 1);
				renderer.uvRotateTop = 1;
				renderer.uvRotateBottom = 1;
				break;
			case 0x30: // east-west
				icons[0] = icons[1] = icons[2] = icons[3] = block.getIcon(0, 1);
				break;
		}

		fakeBlock.setIconArray(icons);
		fakeBlock.setRenderMask(connMask ^ 0x3f);
		renderer.setRenderBounds(offset, offset, offset, 1 - offset, 1 - offset, 1 - offset);
		renderTwoWay(renderer, fakeBlock, x, y, z);

		for (int i = 0; i < 6; i++) {
			if ((connMask & (1 << i)) != 0) {
				fakeBlock.setRenderMask(0x3f ^ (1 << (i ^ 1)) ^ (1 << i));
				switch (i) {
					case 0:
						renderer.setRenderBounds(offset, 0, offset, 1 - offset, offset, 1 - offset);
						break;
					case 1:
						renderer.setRenderBounds(offset, 1 - offset, offset, 1 - offset, 1, 1 - offset);
						break;
					case 2:
						renderer.setRenderBounds(offset, offset, 0, 1 - offset, 1 - offset, offset);
						break;
					case 3:
						renderer.setRenderBounds(offset, offset, 1 - offset, 1 - offset, 1 - offset, 1);
						break;
					case 4:
						renderer.setRenderBounds(0, offset, offset, offset, 1 - offset, 1 - offset);
						break;
					case 5:
						renderer.setRenderBounds(1 - offset, offset, offset, 1, 1 - offset, 1 - offset);
						break;
				}
				renderTwoWay(renderer, fakeBlock, x, y, z);
			}
		}

		renderer.uvRotateBottom = 0;
		renderer.uvRotateTop = 0;
		renderer.uvRotateNorth = 0;
		renderer.uvRotateSouth = 0;
		renderer.uvRotateWest = 0;
		renderer.uvRotateEast = 0;

		return true;
	}

	private void renderTwoWay(RenderBlocks renderer, Block block, int x, int y, int z) {
		renderer.renderStandardBlockWithColorMultiplier(block, x, y, z, 1.0F, 1.0F, 1.0F);

		double temp = renderer.renderMaxX;
		renderer.renderMaxX = renderer.renderMinX;
		renderer.renderMinX = temp;

		temp = renderer.renderMaxY;
		renderer.renderMaxY = renderer.renderMinY;
		renderer.renderMinY = temp;

		temp = renderer.renderMaxZ;
		renderer.renderMaxZ = renderer.renderMinZ;
		renderer.renderMinZ = temp;

		int renderMask = FakeBlock.INSTANCE.getRenderMask();
		FakeBlock.INSTANCE.setRenderMask((renderMask & 0x2A) >> 1 | (renderMask & 0x15) << 1);

		renderer.renderStandardBlockWithColorMultiplier(block, x, y, z, 1.0F, 1.0F, 1.0F);
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
