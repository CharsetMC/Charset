package pl.asie.charset.lib.render;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

 public final class ModelTransformer {
     private ModelTransformer() {

     }

     public static IBakedModel transform(IBakedModel model, IBlockState state, long rand, IVertexTransformer transformer) {
         List<BakedQuad>[] quads = new List[7];

         for (int i = 0; i < quads.length; i++) {
             quads[i] = new ArrayList<BakedQuad>();
             for (BakedQuad quad : model.getQuads(state, (i == 6 ? null : EnumFacing.getFront(i)), rand)) {
                 quads[i].add(transform(quad, transformer));
             }
         }

         return new TransformedModel(model, quads);
     }

     private static BakedQuad transform(BakedQuad quad, IVertexTransformer transformer) {
         VertexFormat format = quad.getFormat();
         UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
         LightUtil.putBakedQuad(new VertexTransformerWrapper(builder, quad, transformer), quad);
         return builder.build();
     }

     private static final class VertexTransformerWrapper implements IVertexConsumer {
         private final IVertexConsumer parent;
         private final BakedQuad parentQuad;
         private final VertexFormat format;
         private final IVertexTransformer transformer;

         public VertexTransformerWrapper(IVertexConsumer parent, BakedQuad parentQuad, IVertexTransformer transformer) {
             this.parent = parent;
             this.parentQuad = parentQuad;
             this.format = parent.getVertexFormat();
             this.transformer = transformer;
         }

         @Override
         public VertexFormat getVertexFormat() {
             return format;
         }

         @Override
         public void setQuadTint(int tint) {
             parent.setQuadTint(tint);
         }

         @Override
         public void setQuadOrientation(EnumFacing orientation) {
             parent.setQuadOrientation(orientation);
         }

         @Override
         public void setApplyDiffuseLighting(boolean diffuse) {
             parent.setApplyDiffuseLighting(diffuse);
         }

         @Override
         public void put(int elementId, float... data) {
             VertexFormatElement element = format.getElement(elementId);
             parent.put(elementId, transformer.transform(parentQuad, element, data));
         }
     }

     private static final class TransformedModel implements IBakedModel {
         private final IBakedModel parent;
         private final List<BakedQuad>[] quads;

         public TransformedModel(IBakedModel parent, List<BakedQuad>[] quads) {
             this.parent = parent;
             this.quads = quads;
         }

         @Override
         public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
             return quads[side == null ? 6 : side.ordinal()];
         }

         @Override
         public boolean isAmbientOcclusion() {
             return parent.isAmbientOcclusion();
         }

         @Override
         public boolean isGui3d() {
             return parent.isGui3d();
         }

         @Override
         public boolean isBuiltInRenderer() {
             return parent.isBuiltInRenderer();
         }

         @Override
         public TextureAtlasSprite getParticleTexture() {
             return parent.getParticleTexture();
         }

         @Override
         public ItemCameraTransforms getItemCameraTransforms() {
             return parent.getItemCameraTransforms();
         }

         @Override
         public ItemOverrideList getOverrides() {
             return parent.getOverrides();
         }
     }

     public interface IVertexTransformer {
         float[] transform(BakedQuad quad, VertexFormatElement element, float... data);
     }
}