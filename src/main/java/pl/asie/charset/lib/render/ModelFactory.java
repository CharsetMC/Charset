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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ModelFactory<T extends IRenderComparable<T>> implements IPerspectiveAwareModel {
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
    private final ResourceLocation particle;
    private final Map<ItemCameraTransforms.TransformType, TRSRTransformation> transformMap = new HashMap<ItemCameraTransforms.TransformType, TRSRTransformation>();

    public ModelFactory(IUnlistedProperty<T> property, ResourceLocation particle) {
        this.property = property;
        this.particle = particle;
    }

    public abstract IBakedModel bake(T object);
    public abstract T fromItemStack(ItemStack stack);

    public void addTransformation(ItemCameraTransforms.TransformType type, TRSRTransformation transformation) {
        transformMap.put(type, transformation);
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        return new ImmutablePair<>(this,
                transformMap.containsKey(cameraTransformType) ? transformMap.get(cameraTransformType).getMatrix() : TRSRTransformation.identity().getMatrix());
    }

    public IBakedModel getModel(T object) {
        if (object == null) {
            return null;
        }

        ModelKey<T> key = new ModelKey<>(object);
        if (cache.containsKey(key)) {
            return cache.get(key);
        } else {
            IBakedModel model = bake(object);
            cache.put(key, model);
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
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return ClientUtils.textureGetter.apply(particle);
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return MFItemOverride.INSTANCE;
    }
}
