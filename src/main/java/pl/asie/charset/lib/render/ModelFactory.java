package pl.asie.charset.lib.render;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.charset.lib.utils.ClientUtils;

import javax.vecmath.Matrix4f;
import java.util.*;

public abstract class ModelFactory<T extends IRenderComparable<T>> extends CharsetBakedModel {
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

    // TODO: Garbage collection?
    private final Map<ModelKey<T>, IBakedModel> cache = new HashMap<>();

    private final IUnlistedProperty<T> property;

    protected ModelFactory(IUnlistedProperty<T> property, ResourceLocation particle) {
        super(particle);
        this.FACTORIES.add(this);
        this.property = property;
    }

    public static void clearCaches() {
        for (ModelFactory factory : FACTORIES) {
            factory.cache.clear();
        }
    }

    public abstract IBakedModel bake(T object);
    public abstract T fromItemStack(ItemStack stack);

    public IBakedModel getModel(T object) {
        if (object == null) {
            return null;
        }

        ModelKey<T> key = new ModelKey<>(object);
        if (cache.containsKey(key)) {
            return cache.get(key);
        } else {
            IBakedModel model = bake(object);
            if (!DISABLE_CACHE) {
                cache.put(key, model);
            }
            return model;
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
