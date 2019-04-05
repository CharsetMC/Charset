/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.charset.module.power.mechanical.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.animation.FastTESR;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLSync;
import org.lwjgl.util.vector.Vector3f;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.render.CharsetFaceBakery;
import pl.asie.charset.lib.render.model.ModelTransformer;
import pl.asie.charset.lib.render.model.SimpleBakedModel;
import pl.asie.charset.lib.utils.Orientation;
import pl.asie.charset.lib.utils.ProxiedBlockAccess;
import pl.asie.charset.lib.utils.Quaternion;
import pl.asie.charset.lib.utils.RenderUtils;
import pl.asie.charset.module.power.mechanical.BlockGearbox;
import pl.asie.charset.module.power.mechanical.CharsetPowerMechanical;
import pl.asie.charset.module.power.mechanical.TileAxle;
import pl.asie.charset.module.power.mechanical.TileGearbox;
import pl.asie.charset.module.tweak.carry.CarryHandler;

import javax.annotation.Nullable;

// TODO: Rewrite as FastTESR
public class TileGearboxRenderer extends TileEntitySpecialRenderer<TileGearbox> {
	public static final TileGearboxRenderer INSTANCE = new TileGearboxRenderer();

	private TileGearboxRenderer() {

	}

	public void drawGear(TileGearbox te, Orientation o, ItemStack stack, float xOffset, float zOffset, float scale, float rotation, int color, float yOffset) {
		if (stack.isEmpty()) {
			return;
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate(0.5f, 0.5f, 0.5f);
		Quaternion.fromOrientation(o).glRotate();

		GlStateManager.translate(zOffset / 16f, 0, -xOffset / 16f);
		GlStateManager.scale(scale, 1f, scale);
		GlStateManager.rotate(rotation, 0, 1, 0);
		GlStateManager.translate(-0.5f, -0.49f - yOffset, -0.5f);

		// draw 2D gears to save performance
		BakedQuad quad = CharsetFaceBakery.INSTANCE.makeBakedQuad(
				new Vector3f(0, 16, 0),
				new Vector3f(16, 16, 16),
				color,
				RenderUtils.getItemSprite(stack),
				EnumFacing.UP,
				ModelRotation.X0_Y0,
				true
		);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
		LightUtil.renderQuadColor(buffer, quad, -1);

		tessellator.draw();

		GlStateManager.popMatrix();
	}

	@Override
	public void render(TileGearbox te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if (te == null || te.getWorld() == null) {
			return;
		}

		IBlockState state = te.getWorld().getBlockState(te.getPos());
		if (!(state.getBlock() instanceof BlockGearbox)) {
			return;
		}

		Orientation o = state.getValue(BlockGearbox.ORIENTATION);
		if (!state.shouldSideBeRendered(te.getWorld(), te.getPos(), o.facing)) {
			return;
		}

		bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		Minecraft.getMinecraft().getTextureMapBlocks().setBlurMipmap(false, false);

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);

		double rotD = te.ROTATION.getRotation(partialTicks) * TileAxle.SPEED_MULTIPLIER;

		float rotFI = (float) (rotD % 90) * 4;
		float rotFO = (float) (rotD % 90) * 4;

		if (te.isRedstonePowered()) {
			drawGear(te, o, te.getInventoryStack(2), 2.5f, 3, 0.5f, -rotFI, 0xFFFFFFFF, 0f);
			drawGear(te, o, te.getInventoryStack(1),-3, 0, 0.5f, rotFO + 37.5f, 0xFFD0D0D0, 0.005f);
			drawGear(te, o, te.getInventoryStack(0),2.5f, -3, 0.5f, 0, 0xFF7F7F7F, 0.01f);
		} else {
			drawGear(te, o, te.getInventoryStack(0),2.5f, -3, 0.5f, -rotFI, 0xFFFFFFFF, 0f);
			drawGear(te, o, te.getInventoryStack(1),-3, 0, 0.5f, rotFO + 37.5f, 0xFFD0D0D0, 0.005f);
			drawGear(te, o, te.getInventoryStack(2),2.5f, 3, 0.5f, 0, 0xFF7F7F7F, 0.01f);
		}

		GlStateManager.popMatrix();

		Minecraft.getMinecraft().getTextureMapBlocks().restoreLastBlurMipmap();
	}
}
