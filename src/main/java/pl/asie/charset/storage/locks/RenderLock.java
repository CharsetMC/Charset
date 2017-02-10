/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.storage.locks;

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

        if (entity.colors[0] != -1) {
            stack.getTagCompound().setInteger("color0", entity.colors[0]);
        }

        if (entity.colors[1] != -1) {
            stack.getTagCompound().setInteger("color1", entity.colors[1]);
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