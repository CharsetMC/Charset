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
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import java.util.*;
import java.util.concurrent.TimeUnit;

public abstract class ModelFactory<T extends IRenderComparable<T>> extends BaseBakedModel {
    private static final boolean DISABLE_CACHE = false;
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
                return factory.getModel(o);
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

    public abstract IBakedModel bake(T object);
    public abstract T fromItemStack(ItemStack stack);

    public IBakedModel getModel(T object) {
        if (object == null) {
            return null;
        }

        ModelKey<T> key = new ModelKey<>(object);
        if (DISABLE_CACHE) {
            return bake(object);
        } else {
            IBakedModel model = cache.getIfPresent(key);
            if (model != null) {
                return model;
            } else {
                model = bake(object);
                cache.put(key, model);
                return model;
            }
        }
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (state instanceof IExtendedBlockState) {
            IBakedModel model = getModel(((IExtendedBlockState) state).getValue(property));
            if (model != null) {
                return model.getQuads(state, side, rand);
            }
        }

        return ImmutableList.of();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return MFItemOverride.INSTANCE;
    }
}
