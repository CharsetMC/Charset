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

package pl.asie.charset.module.experiments.projector;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import pl.asie.charset.lib.render.model.ModelFactory;
import pl.asie.charset.lib.render.model.WrappedBakedModel;
import pl.asie.charset.lib.utils.RenderUtils;

public class ProjectorModel extends ModelFactory<ProjectorCacheInfo> {
    public static final ProjectorModel INSTANCE = new ProjectorModel();

    public ProjectorModel() {
        super(BlockProjector.INFO, TextureMap.LOCATION_MISSING_TEXTURE);
        addDefaultBlockTransforms();
    }

    public IModel template;

    @Override
    public IBakedModel bake(ProjectorCacheInfo info, boolean isItem, BlockRenderLayer layer) {
        IModelState state = info.orientation.toTransformation();
        return new WrappedBakedModel(template.bake(state, DefaultVertexFormats.BLOCK, RenderUtils.textureGetter)).addDefaultBlockTransforms();
    }

    @Override
    public ProjectorCacheInfo fromItemStack(ItemStack stack) {
        return ProjectorCacheInfo.from(stack);
    }
}
