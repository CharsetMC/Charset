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

package pl.asie.charset.storage.barrels;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IRetexturableModel;
import net.minecraftforge.common.model.IModelState;
import pl.asie.charset.lib.material.ColorLookupHandler;
import pl.asie.charset.lib.render.model.ModelColorHandler;
import pl.asie.charset.lib.render.model.ModelFactory;
import pl.asie.charset.lib.render.model.WrappedBakedModel;
import pl.asie.charset.lib.utils.RenderUtils;

import java.util.HashMap;
import java.util.Map;

public class BarrelModel extends ModelFactory<BarrelCacheInfo> {
    public static final BarrelModel INSTANCE = new BarrelModel();

    public final ModelColorHandler<BarrelCacheInfo> colorizer = new ModelColorHandler<BarrelCacheInfo>(this) {
        @Override
        public int colorMultiplier(BarrelCacheInfo info, int tintIndex) {
            if (!info.isMetal && !info.type.isHopping()) {
                return ColorLookupHandler.INSTANCE.getColor(info.logStack, RenderUtils.AveragingMode.V_EDGES_ONLY);
            }
            return -1;
        }
    };

    public BarrelModel() {
        super(BlockBarrel.BARREL_INFO, TextureMap.LOCATION_MISSING_TEXTURE);
        addDefaultBlockTransforms();
    }

    public static class BarrelGroup {
        public TextureAtlasSprite front, top, side, top_metal;

        public BarrelGroup(String type, TextureMap map) {
            front = map.registerSprite(new ResourceLocation("charset:blocks/barrel/" + type + "/front"));
            side = map.registerSprite(new ResourceLocation("charset:blocks/barrel/" + type + "/side"));
            top = map.registerSprite(new ResourceLocation("charset:blocks/barrel/" + type + "/top"));
            top_metal = map.registerSprite(new ResourceLocation("charset:blocks/barrel/" + type + "/top_metal"));
        }

        public static void add(Map<String, BarrelGroup> map, String type, TextureMap tMap) {
            map.put(type, new BarrelGroup(type, tMap));
        }
    }

    public final Map<String, BarrelGroup> TEXTURE_MAP = new HashMap<>();
    public TextureAtlasSprite font;
    public IRetexturableModel template;

    public void onTextureLoad(TextureMap map) {
        TEXTURE_MAP.clear();
        BarrelGroup.add(TEXTURE_MAP, "hopping", map);
        BarrelGroup.add(TEXTURE_MAP, "sticky", map);
        BarrelGroup.add(TEXTURE_MAP, "silky", map);
        BarrelGroup.add(TEXTURE_MAP, "normal", map);
        font = map.registerSprite(new ResourceLocation("charset:blocks/barrel/font"));
    }

    private String getGroupName(TileEntityDayBarrel.Type type) {
        if (type.isHopping()) {
            return "hopping";
        } else if (type == TileEntityDayBarrel.Type.SILKY) {
            return "silky";
        } else if (type == TileEntityDayBarrel.Type.STICKY) {
            return "sticky";
        } else {
            return "normal";
        }
    }

    @Override
    public IBakedModel bake(BarrelCacheInfo info, boolean isItem, BlockRenderLayer layer) {
        TextureAtlasSprite log = info.log;
        TextureAtlasSprite plank = info.plank;
        String groupName = getGroupName(info.type);
        BarrelGroup group = TEXTURE_MAP.get(groupName);
        TextureAtlasSprite top = info.isMetal ? group.top_metal : group.top;
        TextureAtlasSprite front = group.front;
        TextureAtlasSprite side = group.side;
        ImmutableMap.Builder<String, String> textures = new ImmutableMap.Builder<>();
        if (isItem || layer == BlockRenderLayer.SOLID) {
            textures.put("log", log.getIconName());
            textures.put("plank", plank.getIconName());
            textures.put("#top", ""); textures.put("top", "");
            textures.put("#front", ""); textures.put("front", "");
            textures.put("#side", ""); textures.put("side", "");
        }
        if (isItem || layer == BlockRenderLayer.TRANSLUCENT) {
            textures.put("top", top.getIconName());
            textures.put("front", front.getIconName());
            textures.put("side", side.getIconName());
            textures.put("#log", ""); textures.put("log", "");
            textures.put("#plank", ""); textures.put("plank", "");
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
