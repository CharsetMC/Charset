package pl.asie.charset.module.misc.shelf;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import pl.asie.charset.lib.Properties;

/**
 * Created by asie on 2/13/17.
 */
public class TileShelfRenderer extends TileEntitySpecialRenderer<TileShelf> {
    private final RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

    @Override
    public void render(TileShelf tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        IBlockState state = getWorld().getBlockState(tile.getPos());
        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        for (int i = 0; i < 4; i++) {
            ItemStack stack = tile.handler.getStackInSlot(14 + i);
            if (!stack.isEmpty()) {
                Vec3d offset = new Vec3d(
                        ((i & 1) != 0) ? 11.5F / 16F : 4.5F / 16F,
                        ((i & 2) != 0) ? 13 / 16F : 5 / 16F,
                        (state.getValue(BlockShelf.BACK)) ? 4F / 16F : 12F / 16F);

                GlStateManager.pushMatrix();
                IBakedModel model = renderItem.getItemModelWithOverrides(stack, tile.getWorld(), null);
                GlStateManager.translate(x, y, z);
                GlStateManager.rotate(state.getValue(Properties.FACING4).getHorizontalAngle(), 0, 1, 0);
                GlStateManager.translate(offset.x, offset.y, offset.z);
                GlStateManager.scale(0.25F, 0.25F, 0.25F);

                //
                //GlStateManager.translate(offset.x, offset.y,  offset.z);
                //GlStateManager.scale(0.25F, 0.25F, 0.25F);]

                renderItem.renderItem(stack, model);
                GlStateManager.popMatrix();
            }
        }
    }
}
