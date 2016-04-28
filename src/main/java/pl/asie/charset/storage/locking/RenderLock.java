package pl.asie.charset.storage.locking;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.storage.ModCharsetStorage;

@SideOnly(Side.CLIENT)
public class RenderLock extends Render<EntityLock> {
    private final Minecraft mc = Minecraft.getMinecraft();
    private final ModelResourceLocation model = new ModelResourceLocation("charsetstorage:lock", "inventory");

    public RenderLock(RenderManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public void doRender(EntityLock entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        BlockPos pos = entity.getHangingPosition();
        EnumFacing facing = EnumFacing.fromAngle(entity.rotationYaw);
        double xPos = (double) pos.getX() - entity.posX + x - (facing == null ? 0 : facing.getFrontOffsetX() * 0.46875D);
        double yPos = (double) pos.getY() - entity.posY + y - (facing == null ? 0 : facing.getFrontOffsetY() * 0.46875D);
        double zPos = (double) pos.getZ() - entity.posZ + z - (facing == null ? 0 : facing.getFrontOffsetZ() * 0.46875D);
        GlStateManager.translate(xPos + 0.5D, yPos + 0.5D, zPos + 0.5D);
        GlStateManager.rotate(180.0F - entity.rotationYaw, 0.0F, 1.0F, 0.0F);
        this.renderManager.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5F, 0.5F, 0.5F);

        if (this.renderOutlines) {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(entity));
        }

        ItemStack stack = new ItemStack(ModCharsetStorage.lockItem);
        stack.setTagCompound(new NBTTagCompound());
        int color0 = entity.getDataManager().get(EntityLock.COLOR_0);
        int color1 = entity.getDataManager().get(EntityLock.COLOR_1);
        if (color0 != -1) {
            stack.getTagCompound().setInteger("color0", color0);
        }
        if (color1 != -1) {
            stack.getTagCompound().setInteger("color1", color1);
        }

        mc.getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);

        if (this.renderOutlines) {
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }

        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityLock entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}