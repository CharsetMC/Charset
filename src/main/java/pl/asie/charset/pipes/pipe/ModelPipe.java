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

package pl.asie.charset.pipes.pipe;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import pl.asie.charset.lib.material.ColorLookupHandler;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.render.model.ModelPipeShaped;
import pl.asie.charset.lib.utils.ColorUtils;
import pl.asie.charset.lib.utils.RenderUtils;
import pl.asie.charset.pipes.CharsetPipes;

import javax.annotation.Nullable;

public class ModelPipe extends ModelPipeShaped<TilePipe> {
    public static final ResourceLocation PIPE_TEXTURE_LOC = new ResourceLocation("charset", "blocks/pipe");
    public static final ResourceLocation PIPE_OVERLAY_TEXTURE_LOC = new ResourceLocation("charset", "blocks/pipe_overlay");
    public static final Colorizer colorizer = new Colorizer();
    public static TextureAtlasSprite[] sprites0, sprites1;

    public ModelPipe() {
        super(TilePipe.PROPERTY, CharsetPipes.blockPipe);
    }

    @Override
    public float getThickness(BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT ? 7.995f : 7.990f;
    }

    @Override
    public int getOutsideColor(EnumFacing facing, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT ? 0 : 1;
    }

    @Override
    public int getInsideColor(EnumFacing facing, BlockRenderLayer layer) {
        int color = getOutsideColor(facing, layer);
        return facing.getAxis() == EnumFacing.Axis.Y ? (color | 0x100) : color;
    }

    @Override
    public boolean isOpaque() {
        return false;
    }

    @Override
    public boolean shouldRender(TilePipe target, BlockRenderLayer layer) {
        if (layer == BlockRenderLayer.TRANSLUCENT) {
            return target.getColor() != null;
        }
        return true;
    }

    @Override
    public TextureAtlasSprite getTexture(EnumFacing side, BlockRenderLayer layer, int connectionMatrix) {
        TextureAtlasSprite[] sprites = layer == BlockRenderLayer.TRANSLUCENT ? sprites1 : sprites0;
        return sprites != null ? sprites[connectionMatrix] : null;
    }

    public static int getMaterialTint(ItemMaterial material) {
        // FIXME: Hacky workaround for andesite
        if (material != null) {
            if (material.getStack().getItem() == Item.getItemFromBlock(Blocks.STONE)) {
                if (material.getStack().getMetadata() == 5) {
                    return 0x7e8e93;
                }
            }
        }
        return material == null ? -1 : ColorLookupHandler.INSTANCE.getColor(material.getStack(), RenderUtils.AveragingMode.FULL);
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return sprites0 != null ? sprites0[15] : null;
    }

    public static class Colorizer implements IBlockColor, IItemColor {
        @Override
        public int colorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
            int value = -1;
            if (state instanceof IExtendedBlockState) {
                TilePipe pipe = (((IExtendedBlockState) state).getValue(TilePipe.PROPERTY));
                if (pipe != null) {
                    if ((tintIndex & 0xFF) == 1) {
                        EnumDyeColor color = pipe.getColor();
                        if (color != null) {
                            value = ColorUtils.getIntColor(color);
                        }
                    } else {
                        value = getMaterialTint(pipe.getMaterial());
                    }
                }
            }
            return modValue(value, tintIndex);
        }

        @Override
        public int getColorFromItemstack(ItemStack stack, int tintIndex) {
            int value;
            if ((tintIndex & 0xFF) == 1) {
                int color = stack.hasTagCompound() ? stack.getTagCompound().getByte("color") : 0;
                value = color > 0 ? ColorUtils.getIntColor(EnumDyeColor.byMetadata(color - 1)) : -1;
            } else {
                value = getMaterialTint(ItemMaterialRegistry.INSTANCE.getMaterial(stack.getTagCompound(), "material"));
            }
            return modValue(value, tintIndex);
        }

        private int modValue(int value, int tintIndex) {
            if ((tintIndex & 0x100) != 0) {
                return (value & 0xFF000000) | ((value >> 1) & 0x007F7F7F);
            } else {
                return value;
            }
        }
    }
}
