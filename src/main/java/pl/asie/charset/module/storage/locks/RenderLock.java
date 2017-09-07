/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

package pl.asie.charset.module.storage.locks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderLock extends Render<EntityLock> {
    private final Minecraft mc = Minecraft.getMinecraft();

    public RenderLock(RenderManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public void doRender(EntityLock entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        BlockPos pos = entity.getHangingPosition();
        EnumFacing facing = EnumFacing.fromAngle(entity.rotationYaw);
        double xPos = (double) pos.getX() - entity.posX + x - (facing == null ? 0 : facing.getFrontOffsetX() * 0.46875D);
        double yPos = (double) pos.getY() - entity.posY + y - (facing == null ? 0 : facing.getFrontOffsetY() * 0.46875D);
        double zPos = (double) pos.getZ() - entity.posZ + z - (facing == null ? 0 : facing.getFrontOffsetZ() * 0.46875D);
        GlStateManager.translate(xPos + 0.5D, yPos + 0.5D, zPos + 0.5D);
        GlStateManager.rotate(180.0F - entity.rotationYaw, 0.0F, 1.0F, 0.0F);
        this.renderManager.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        GlStateManager.scale(0.5F, 0.5F, 0.5F);

        if (this.renderOutlines) {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(entity));
        }

        ItemStack stack = new ItemStack(CharsetStorageLocks.lockItem);
        stack.setTagCompound(new NBTTagCompound());

        if (entity.color != -1) {
            stack.getTagCompound().setInteger("color", entity.color);
        }

        mc.getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);

        if (this.renderOutlines) {
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }

        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityLock entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}