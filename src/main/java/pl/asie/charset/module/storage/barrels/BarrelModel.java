/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
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

package pl.asie.charset.module.storage.barrels;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
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
            if (!info.isMetal && !info.upgrades.contains(BarrelUpgrade.HOPPING)) {
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

        if (info.upgrades.contains(BarrelUpgrade.STICKY)) {
            side = this.side_sticky;
            front = info.upgrades.contains(BarrelUpgrade.SILKY) ? this.front_silky_sticky : this.front_sticky;
        } else if (info.upgrades.contains(BarrelUpgrade.SILKY)) {
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
            if (info.upgrades.contains(BarrelUpgrade.HOPPING)) {
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
        return new WrappedBakedModel(retexture.bake(state, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter()), log).addDefaultBlockTransforms();
    }

    @Override
    public BarrelCacheInfo fromItemStack(ItemStack stack) {
        return BarrelCacheInfo.from(stack);
    }
}
