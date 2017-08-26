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

package pl.asie.charset.lib.render.model;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.utils.RenderUtils;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public abstract class ModelFactory<T extends IRenderComparable<T>> extends BaseBakedModel implements IStateParticleBakedModel {
    public static final boolean DISABLE_CACHE = ModCharset.INDEV;
    private static final Set<ModelFactory> FACTORIES = new HashSet<>();

    private static class MFItemOverride extends ItemOverrideList {
        public static final MFItemOverride INSTANCE = new MFItemOverride();

        private MFItemOverride() {
            super(ImmutableList.of());
        }

        @Override
        @SuppressWarnings("unchecked")
        public @Nonnull IBakedModel handleItemState(@Nonnull IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
            if (originalModel instanceof ModelFactory) {
                ModelFactory factory = (ModelFactory) originalModel;
                IRenderComparable o = factory.fromItemStack(stack);
                IBakedModel model = factory.getModel(o, null);
                if (model != null) {
                    return model;
                }
            }

            return originalModel;
        }
    }

    private final Cache<ModelKey<T>, IBakedModel> cache;
    private final IUnlistedProperty<T> property;
    private final ResourceLocation particle;

    protected ModelFactory(IUnlistedProperty<T> property, ResourceLocation particle) {
        super();
        FACTORIES.add(this);

        this.particle = particle;
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

    private IBakedModel getModel(T object, BlockRenderLayer layer) {
        if (object == null) {
            return null;
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

    private IBakedModel getModel(IBlockState state) {
        return getModel(state, null);
    }

    private IBakedModel getModel(IBlockState state, BlockRenderLayer layer) {
        if (state instanceof IExtendedBlockState) {
            return getModel(((IExtendedBlockState) state).getValue(property), layer);
        } else {
            return null;
        }
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (state instanceof IExtendedBlockState) {
            IBakedModel model = getModel(state, MinecraftForgeClient.getRenderLayer());
            if (model != null) {
                return model.getQuads(state, side, rand);
            }
        }

        return ImmutableList.of();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return RenderUtils.textureGetter.apply(particle);
    }

    @Override
    public TextureAtlasSprite getParticleTexture(IBlockState state) {
        IBakedModel model = getModel(state);
        return model != null ? model.getParticleTexture() : getParticleTexture();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return MFItemOverride.INSTANCE;
    }
}
