package pl.asie.charset.module.tools.building.chisel;

import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.charset.lib.render.model.BaseBakedModel;
import pl.asie.charset.module.tools.building.CharsetToolsBuilding;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ChiselBakedModel extends BaseBakedModel {
    private BakedQuad buildQuad(EnumFacing side, TextureAtlasSprite sprite,
                                float x, float y, float w, float h, float z, float colorMultiplier) {
        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(DefaultVertexFormats.ITEM);
        builder.setQuadOrientation(side);
        builder.setTexture(sprite);
        if (side.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
            buildVertex(builder, side, x / 16.0f, y / 16.0f, z / 16.0f, sprite, colorMultiplier);
            buildVertex(builder, side, (x+w) / 16.0f, y / 16.0f, z / 16.0f, sprite, colorMultiplier);
            buildVertex(builder, side, (x+w) / 16.0f, (y+h) / 16.0f, z / 16.0f, sprite, colorMultiplier);
            buildVertex(builder, side, x / 16.0f, (y+h) / 16.0f, z / 16.0f, sprite, colorMultiplier);
        } else {
            buildVertex(builder, side, x / 16.0f, y / 16.0f, z / 16.0f, sprite, colorMultiplier);
            buildVertex(builder, side, x / 16.0f, (y+h) / 16.0f, z / 16.0f, sprite, colorMultiplier);
            buildVertex(builder, side, (x+w) / 16.0f, (y+h) / 16.0f, z / 16.0f, sprite, colorMultiplier);
            buildVertex(builder, side, (x+w) / 16.0f, y / 16.0f, z / 16.0f, sprite, colorMultiplier);
        }
        return builder.build();
    }

    private void buildVertex(UnpackedBakedQuad.Builder builder, EnumFacing side, float x, float y, float z, TextureAtlasSprite sprite, float colorMultiplier) {
        VertexFormat format = builder.getVertexFormat();
        for(int e = 0; e < format.getElementCount(); e++) {
            VertexFormatElement element = format.getElement(e);
            switch (element.getUsage()) {
                case POSITION:
                    builder.put(e, x, (1-y), z, 1);
                    break;
                case COLOR:
                    builder.put(e, colorMultiplier, colorMultiplier, colorMultiplier, 1);
                    break;
                case UV:
                    builder.put(e, sprite.getInterpolatedU(x*16), sprite.getInterpolatedV(y*16), 0, 1);
                    break;
                case NORMAL:
                    builder.put(e, side.getFrontOffsetX(), side.getFrontOffsetY(), side.getFrontOffsetZ(), 0);
                    break;
                default:
                    builder.put(e);
                    break;
            }
        }
    }

    private final IBakedModel parent;
    private final ItemOverrideList list;
    private final int mask;

    public ChiselBakedModel(IBakedModel parent) {
        this.parent = parent;
        this.mask = 0x1FF;
        this.list = new ItemOverrideList(Collections.emptyList()) {
            @Override
            public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
                return new ChiselBakedModel(parent, stack);
            }
        };
    }

    public ChiselBakedModel(IBakedModel parent, ItemStack stack) {
        this.parent = parent;
        this.mask = CharsetToolsBuilding.chisel.getBlockMask(stack);
        this.list = ItemOverrideList.NONE;
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        Pair<? extends IBakedModel, Matrix4f> parentPair = parent.handlePerspective(cameraTransformType);
        return ImmutablePair.of(this, parentPair.getRight());
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        List<BakedQuad> quads = parent.getQuads(state, side, rand);
        if (mask != 0x1FF && side == null) {
            List<BakedQuad> quads2 = Lists.newArrayList(quads);
            TextureAtlasSprite sprite = parent.getParticleTexture();
            int blockMask = mask;
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 3; x++) {
                    if ((blockMask & 1) == 0) {
                        quads2.add(buildQuad(EnumFacing.NORTH, sprite, 4+x, 9+y, 1, 1, 7.49f, 0.6f));
                        quads2.add(buildQuad(EnumFacing.SOUTH, sprite, 4+x, 9+y, 1, 1, 8.51f, 0.6f));
                    }
                    blockMask >>= 1;
                }
            }

            return quads2;
        }
        return quads;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return parent.getParticleTexture();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return list;
    }
}
