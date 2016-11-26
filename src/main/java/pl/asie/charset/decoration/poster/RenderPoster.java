/*
 * Copyright (c) 2016 neptunepink
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
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

package pl.asie.charset.decoration.poster;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class RenderPoster extends Render<EntityPoster> {

    public RenderPoster(RenderManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public void doRender(EntityPoster poster, double x, double y, double z, float yaw, float partial) {
        final Minecraft mc = Minecraft.getMinecraft();
        final RayTraceResult mop = mc.objectMouseOver;
        boolean selected = mop != null && mop.entityHit == poster;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        if (selected && !mc.gameSettings.hideGUI) {
            GlStateManager.pushMatrix();
            // They ordinarily don't move, so no need to bother w/ interpolation
            GlStateManager.translate(-poster.posX, -poster.posY, -poster.posZ + 1 / 16.0);
            GlStateManager.disableTexture2D();
            RenderGlobal.drawSelectionBoundingBox(poster.getEntityBoundingBox(), 1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.enableTexture2D();
            GlStateManager.popMatrix();
        }
        poster.rot.glRotate();
        GlStateManager.scale(poster.scale, poster.scale, poster.scale);
        try {
            renderItem(poster.inv);
        } catch (Throwable t) {
            t.printStackTrace();
            poster.inv = new ItemStack(Blocks.FIRE); // Hopefully fire doesn't also somehow error out.
        }
        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityPoster entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }

    static EntityLiving dummy_entity = new EntityEnderman(null);

    public void renderItem(ItemStack is) {
        // ... copied from ServoMotor...
        // but we can't merge them because, again, item rendering code is some mad BS
        // (Well, the dummy classes could get merged.)
        // Copied from RenderBiped.renderEquippedItems

        // Pre-emptively undo transformations that the item renderer does so
        // that we don't get a stupid angle. Minecraft render code is terrible.
        GlStateManager.translate(0, 0, -0.5F/16F);

        // TODO

        GlStateManager.rotate(180, 0, 1, 0);
        // Why is this necessary? Changing the TransformType below does nothing...

        Minecraft.getMinecraft().getItemRenderer().renderItem(dummy_entity, is, ItemCameraTransforms.TransformType.NONE);
    }
}
