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

package pl.asie.charset.module.storage.barrels;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import pl.asie.charset.lib.material.ColorLookupHandler;
import pl.asie.charset.lib.render.model.ModelColorHandler;
import pl.asie.charset.lib.render.model.ModelFactory;
import pl.asie.charset.lib.render.model.WrappedBakedModel;
import pl.asie.charset.lib.utils.RenderUtils;

import java.lang.reflect.Field;

public class BarrelModel extends ModelFactory<BarrelCacheInfo> {
    public static final BarrelModel INSTANCE = new BarrelModel();

    public final ModelColorHandler<BarrelCacheInfo> colorizer = new ModelColorHandler<BarrelCacheInfo>(this) {
        @Override
        public int colorMultiplier(BarrelCacheInfo info, int tintIndex) {
            if (!info.isMetal && !info.upgrades.contains(TileEntityDayBarrel.Upgrade.HOPPING)) {
                return ColorLookupHandler.INSTANCE.getColor(info.logStack, RenderUtils.AveragingMode.V_EDGES_ONLY);
            }
            return -1;
        }
    };

    public BarrelModel() {
        super(BlockBarrel.BARREL_INFO, TextureMap.LOCATION_MISSING_TEXTURE);
        addDefaultBlockTransforms();
    }

    public TextureAtlasSprite font = null, front = null, front_silky = null, front_silky_sticky = null, front_sticky = null;
    public TextureAtlasSprite side = null, side_hopping = null, side_sticky = null, top = null, top_hopping = null, top_metal = null;

    public IModel template;

    public void onTextureLoad(TextureMap map) {
        try {
            for (Field f : BarrelModel.class.getFields()) {
                if (f.getType() == TextureAtlasSprite.class) {
                    f.set(this, map.registerSprite(new ResourceLocation("charset:blocks/barrel/" + f.getName())));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBakedModel bake(BarrelCacheInfo info, boolean isItem, BlockRenderLayer layer) {
        TextureAtlasSprite log = info.log;
        TextureAtlasSprite plank = info.plank;
        TextureAtlasSprite top = info.isMetal ? this.top_metal : this.top;
        TextureAtlasSprite front = this.front;
        TextureAtlasSprite side = this.side;

        if (info.upgrades.contains(TileEntityDayBarrel.Upgrade.STICKY)) {
            side = this.side_sticky;
            front = info.upgrades.contains(TileEntityDayBarrel.Upgrade.SILKY) ? this.front_silky_sticky : this.front_sticky;
        } else if (info.upgrades.contains(TileEntityDayBarrel.Upgrade.SILKY)) {
            front = this.front_silky;
        }

        ImmutableMap.Builder<String, String> textures = new ImmutableMap.Builder<>();
        if (isItem || layer == BlockRenderLayer.SOLID) {
            textures.put("log", log.getIconName());
            textures.put("plank", plank.getIconName());
        } else {
            textures.put("#log", ""); textures.put("log", "");
            textures.put("#plank", ""); textures.put("plank", "");
        }

        if (isItem || layer == BlockRenderLayer.TRANSLUCENT) {
            if (info.upgrades.contains(TileEntityDayBarrel.Upgrade.HOPPING)) {
                top = this.top_hopping;
                textures.put("hopping", side_hopping.getIconName());
            } else {
                textures.put("#hopping", ""); textures.put("hopping", "");
            }

            textures.put("top", top.getIconName());
            textures.put("front", front.getIconName());
            textures.put("side", side.getIconName());
        } else {
            textures.put("#top", ""); textures.put("top", "");
            textures.put("#front", ""); textures.put("front", "");
            textures.put("#side", ""); textures.put("side", "");
            textures.put("#hopping", ""); textures.put("hopping", "");
        }

        IModelState state = info.orientation.toTransformation();
        IModel retexture = template.retexture(textures.build());
        return new WrappedBakedModel(retexture.bake(state, DefaultVertexFormats.BLOCK, RenderUtils.textureGetter), log).addDefaultBlockTransforms();
    }

    @Override
    public BarrelCacheInfo fromItemStack(ItemStack stack) {
        return BarrelCacheInfo.from(stack);
    }
}
