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

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
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
         SimpleBakedModel out = new SimpleBakedModel(model);

         for (int i = 0; i <= 6; i++) {
             EnumFacing side = (i == 6 ? null : EnumFacing.getFront(i));
             for (BakedQuad quad : model.getQuads(state, side, rand)) {
                 out.addQuad(side, transform(quad, transformer));
             }
         }

         return out;
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

     public interface IVertexTransformer {
         float[] transform(BakedQuad quad, VertexFormatElement element, float... data);
     }
}