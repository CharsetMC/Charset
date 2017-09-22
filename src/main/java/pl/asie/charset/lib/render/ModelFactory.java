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

package pl.asie.charset.lib.render;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.utils.RenderUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

public abstract class ModelFactory<T extends IRenderComparable<T>> extends BaseBakedModel {
    private static final boolean DISABLE_CACHE = ModCharsetLib.INDEV;
    private static final Set<ModelFactory> FACTORIES = new HashSet<>();

    private static class MFItemOverride extends ItemOverrideList {
        public static final MFItemOverride INSTANCE = new MFItemOverride();

        private MFItemOverride() {
            super(ImmutableList.of());
        }

        @Override
        public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
            if (originalModel instanceof ModelFactory) {
                ModelFactory factory = (ModelFactory) originalModel;
                IRenderComparable o = factory.fromItemStack(stack);
                return factory.getModel(o, null);
            } else {
                return originalModel;
            }
        }
    }

    private final Cache<ModelKey<T>, IBakedModel> cache;
    private final IUnlistedProperty<T> property;

    protected ModelFactory(IUnlistedProperty<T> property, ResourceLocation particle) {
        super(particle);
        this.FACTORIES.add(this);

        this.cache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build();
        this.property = property;
    }

    public static void clearCaches() {
        for (ModelFactory factory : FACTORIES) {
            factory.cache.invalidateAll();
        }
    }

    public IUnlistedProperty<T> getProperty() {
        return property;
    }

    public abstract IBakedModel bake(T object, boolean isItem, BlockRenderLayer layer);
    public abstract T fromItemStack(ItemStack stack);

    public IBakedModel getModel(T object, BlockRenderLayer layer) {
        if (object == null) {
            return this;
        }

        ModelKey<T> key = new ModelKey<>(object, layer);
        if (DISABLE_CACHE) {
            return bake(object, layer == null, layer);
        } else {
            IBakedModel model = cache.getIfPresent(key);
            if (model != null) {
                return model;
            } else {
                model = bake(object, layer == null, layer);
                cache.put(key, model);
                return model;
            }
        }
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (state instanceof IExtendedBlockState) {
            IBakedModel model = getModel(((IExtendedBlockState) state).getValue(property), MinecraftForgeClient.getRenderLayer());
            if (model != null) {
                return model.getQuads(state, side, rand);
            }
        }

        return ImmutableList.of();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        // TODO: If IBlockState-sensitive getParticleTexture ever hits,
        // check for the barrel texture
        return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(Blocks.LOG.getDefaultState());
    }

    @Override
    public ItemOverrideList getOverrides() {
        return MFItemOverride.INSTANCE;
    }
}
