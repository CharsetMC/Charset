package pl.asie.charset.pipes.client;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.EnumFacing;

import mcmultipart.client.multipart.MultipartSpecialRenderer;
import pl.asie.charset.pipes.PartPipe;
import pl.asie.charset.pipes.PipeItem;

public class SpecialRendererPipe extends MultipartSpecialRenderer<PartPipe> {
	private static final Random PREDICTIVE_ITEM_RANDOM = new Random();
	private static final float ITEM_RANDOM_OFFSET = 0.01F;

	private static final RenderEntityItem RENDER_ITEM = new RenderEntityItem(Minecraft.getMinecraft().getRenderManager(), Minecraft.getMinecraft().getRenderItem()) {
		@Override
		public boolean shouldBob() {
			return false;
		}

		@Override
		public boolean shouldSpreadItems() {
			return false;
		}
	};

    @Override
    public void renderMultipartAt(PartPipe part, double x, double y, double z, float partialTicks, int destroyStage) {
        if (part == null) {
            return;
        }

        synchronized (part.getPipeItems()) {
            for (PipeItem item : part.getPipeItems()) {
                EntityItem itemEntity = new EntityItem(part.getWorld(), part.getPos().getX(), part.getPos().getY(), part.getPos().getZ(), item.getStack());
                itemEntity.hoverStart = 0;

                EnumFacing id = item.getDirection();
                double ix, iy, iz;

                if (id == null) {
                    ix = 0.5;
                    iy = 0.5;
                    iz = 0.5;
                } else if (item.isStuck() || (!item.hasReachedCenter() && item.getProgress() == 0.5F)) {
                    ix = item.getX();
                    iy = item.getY();
                    iz = item.getZ();
                } else {
                    ix = item.getX() + ((float) id.getFrontOffsetX() * PipeItem.SPEED / PipeItem.MAX_PROGRESS * partialTicks);
                    iy = item.getY() + ((float) id.getFrontOffsetY() * PipeItem.SPEED / PipeItem.MAX_PROGRESS * partialTicks);
                    iz = item.getZ() + ((float) id.getFrontOffsetZ() * PipeItem.SPEED / PipeItem.MAX_PROGRESS * partialTicks);
                }

                if (id != null) {
                    PREDICTIVE_ITEM_RANDOM.setSeed(item.id);

                    switch (id.ordinal() >> 1) {
                        case 0:
                        case 1:
                            ix += PREDICTIVE_ITEM_RANDOM.nextFloat() * ITEM_RANDOM_OFFSET;
                            break;
                        case 2:
                            iz += PREDICTIVE_ITEM_RANDOM.nextFloat() * ITEM_RANDOM_OFFSET;
                            break;
                    }
                }

                GlStateManager.pushMatrix();
                GlStateManager.translate(x + ix, y + iy - 0.25, z + iz);

                RENDER_ITEM.doRender(itemEntity, 0, 0, 0, 0.0f, 0.0f);

                GlStateManager.popMatrix();
            }
        }
    }
}
