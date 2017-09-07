/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

package pl.asie.charset.lib.render.model;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

import javax.annotation.Nullable;
import java.util.function.Function;

public final class ModelTransformer {
     private ModelTransformer() {

     }

     public static IBakedModel transform(IBakedModel model, IBlockState state, long rand, IVertexTransformer transformer) {
         return transform(model, state, rand, transformer, null);
     }

     public static IBakedModel transform(IBakedModel model, IBlockState state, long rand, IVertexTransformer transformer, @Nullable Function<BakedQuad, VertexFormat> format) {
         SimpleBakedModel out = new SimpleBakedModel(model);

         for (int i = 0; i <= 6; i++) {
             EnumFacing side = (i == 6 ? null : EnumFacing.getFront(i));
             for (BakedQuad quad : model.getQuads(state, side, rand)) {
                 out.addQuad(side, transform(quad, transformer, format));
             }
         }

         return out;
     }

    public static BakedQuad transform(BakedQuad quad, IVertexTransformer transformer) {
         return transform(quad, transformer, null);
    }

     public static BakedQuad transform(BakedQuad quad, IVertexTransformer transformer, @Nullable Function<BakedQuad, VertexFormat> format2) {
         VertexFormat format = format2 != null ? format2.apply(quad) : quad.getFormat();
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
         public void setTexture(TextureAtlasSprite texture) {
             parent.setTexture(texture);
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

     public class Tinter implements IVertexTransformer {
         private float[] color;

         public Tinter(int color) {
             this.color = new float[4];
             this.color[0] = ((color >> 24) & 0xFF) / 255.0f;
             this.color[1] = ((color >> 16) & 0xFF) / 255.0f;
             this.color[2] = ((color >> 8) & 0xFF) / 255.0f;
             this.color[3] = ((color >> 0) & 0xFF) / 255.0f;
         }

         @Override
         public float[] transform(BakedQuad quad, VertexFormatElement element, float... data) {
             if (element.getUsage() == VertexFormatElement.EnumUsage.COLOR) {
                 return new float[] { data[0]*color[0], data[1]*color[1], data[2]*color[2], data[3]*color[3] };
             } else {
                 return data;
             }
         }
     }
}