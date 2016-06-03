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

package pl.asie.charset.storage.barrel;

import com.google.common.collect.ImmutableMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IRetexturableModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.property.IExtendedBlockState;
import pl.asie.charset.lib.material.ColorLookupHandler;
import pl.asie.charset.lib.render.ModelFactory;
import pl.asie.charset.lib.render.WrappedBakedModel;
import pl.asie.charset.lib.utils.RenderUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class BarrelModel extends ModelFactory<BarrelCacheInfo> {
    public static final Colorizer COLORIZER = new Colorizer();

    public BarrelModel() {
        super(BlockBarrel.BARREL_INFO, TextureMap.LOCATION_MISSING_TEXTURE);
        addDefaultBlockTransforms();
    }

    public static class Colorizer implements IBlockColor, IItemColor {
        private int colorMultiplier(BarrelCacheInfo info, int tintIndex) {
            if (!info.isMetal && !info.type.isHopping()) {
                return ColorLookupHandler.INSTANCE.getColor(info.logStack, RenderUtils.AveragingMode.V_EDGES_ONLY);
            }
            return -1;
        }

        @Override
        public int colorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
            if (state instanceof IExtendedBlockState) {
                BarrelCacheInfo info = ((IExtendedBlockState) state).getValue(BlockBarrel.BARREL_INFO);
                if (info != null) {
                    return colorMultiplier(info, tintIndex);
                }
            }
            return -1;
        }

        @Override
        public int getColorFromItemstack(ItemStack stack, int tintIndex) {
            BarrelCacheInfo info = BarrelCacheInfo.from(stack);
            if (info != null) {
                return colorMultiplier(info, tintIndex);
            }
            return -1;
        }
    }

    public static class BarrelGroup {
        public TextureAtlasSprite front, top, side, top_metal;

        public BarrelGroup(String type, TextureMap map) {
            front = map.registerSprite(new ResourceLocation("charsetstorage:blocks/barrel/" + type + "/front"));
            side = map.registerSprite(new ResourceLocation("charsetstorage:blocks/barrel/" + type + "/side"));
            top = map.registerSprite(new ResourceLocation("charsetstorage:blocks/barrel/" + type + "/top"));
            top_metal = map.registerSprite(new ResourceLocation("charsetstorage:blocks/barrel/" + type + "/top_metal"));
            TEXTURE_MAP.put(type, this);
        }
    }

    public static final Map<String, BarrelGroup> TEXTURE_MAP = new HashMap<>();
    public static TextureAtlasSprite font;
    public static IRetexturableModel template;

    public static void onTextureLoad(TextureMap map) {
        TEXTURE_MAP.clear();
        new BarrelGroup("hopping", map);
        new BarrelGroup("sticky", map);
        new BarrelGroup("silky", map);
        new BarrelGroup("normal", map);
        font = map.registerSprite(new ResourceLocation("charsetstorage:blocks/barrel/font"));
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
        HashMap<String, String> textures = new HashMap<String, String>();
        final HashMap<ResourceLocation, TextureAtlasSprite> map = new HashMap<ResourceLocation, TextureAtlasSprite>();
        if (isItem || layer == BlockRenderLayer.SOLID) {
            textures.put("log", log.getIconName());
            textures.put("plank", plank.getIconName());
        }
        if (isItem || layer == BlockRenderLayer.TRANSLUCENT) {
            textures.put("top", top.getIconName());
            textures.put("front", front.getIconName());
            textures.put("side", side.getIconName());
        }
        for (String s : new String[]{"log", "plank", "top", "front", "side"}) {
            if (textures.get(s) == null) {
                textures.put(s, "");
                textures.put("#" + s, "");
            }
        }
        textures.put("particle", log.getIconName());
        map.put(new ResourceLocation(log.getIconName()), log);
        map.put(new ResourceLocation(plank.getIconName()), plank);
        map.put(new ResourceLocation(top.getIconName()), top);
        map.put(new ResourceLocation(front.getIconName()), front);
        map.put(new ResourceLocation(side.getIconName()), side);
        IModelState state = info.orientation.toTransformation();
        ImmutableMap<String, String> textureMap = ImmutableMap.copyOf(textures);
        IModel retexture = template.retexture(textureMap);
        return new WrappedBakedModel(retexture.bake(state, DefaultVertexFormats.BLOCK, RenderUtils.textureGetter)).addDefaultBlockTransforms();
    }

    @Override
    public BarrelCacheInfo fromItemStack(ItemStack stack) {
        return BarrelCacheInfo.from(stack);
    }
}
