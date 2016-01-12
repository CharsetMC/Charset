package pl.asie.charset.storage;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.lib.utils.ClientUtils;
import pl.asie.charset.storage.backpack.ItemBackpack;
import pl.asie.charset.storage.backpack.PacketBackpackOpen;
import pl.asie.charset.storage.backpack.TileBackpack;
import pl.asie.charset.storage.backpack.TileBackpackRenderer;

/**
 * Created by asie on 1/10/16.
 */
public class ProxyClient extends ProxyCommon {
    public static final KeyBinding backpackOpen = new KeyBinding("key.charset.backpackOpen", Keyboard.KEY_C, "key.categories.gameplay");
    public static final IBakedModel[] backpackTopModel = new IBakedModel[4];
    public static final IBakedModel[] backpackModel = new IBakedModel[4];

    @Override
    public void init() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileBackpack.class, new TileBackpackRenderer());
        ClientRegistry.registerKeyBinding(backpackOpen);
    }

    protected float interpolateRotation(float par1, float par2, float par3) {
        float f;

        for (f = par2 - par1; f < -180.0F; f += 360.0F) { }

        while (f >= 180.0F) {
            f -= 360.0F;
        }

        return par1 + par3 * f;
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onKey(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;
        if (!mc.inGameHasFocus || player == null) {
            return;
        }

        if (ModCharsetStorage.enableBackpackOpenKey && backpackOpen.isPressed()) {
            ItemStack backpack = ItemBackpack.getBackpack(player);
            if (backpack != null) {
                ModCharsetStorage.packet.sendToServer(new PacketBackpackOpen(player));
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onRenderArmor(RenderPlayerEvent.Post event) {
        ItemStack backpack = event.entityPlayer.getCurrentArmor(2);
        if (backpack != null && backpack.getItem() instanceof ItemBackpack) {
            BlockModelRenderer renderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();

            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);

            GlStateManager.pushMatrix();
            GlStateManager.scale(0.75, 0.75, 0.75);
            GlStateManager.rotate(-interpolateRotation(event.entityPlayer.prevRenderYawOffset, event.entityPlayer.renderYawOffset, event.partialRenderTick), 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(-0.5, 1.125, -0.845);
            if (event.entityPlayer.isSneaking()) {
                GlStateManager.rotate(30.0f, 1.0f, 0.0f, 0.0f);
                GlStateManager.translate(0, 0.1, -0.35);
            }

            int i = backpack.getItem().getColorFromItemStack(backpack, 0);

            if (EntityRenderer.anaglyphEnable) {
                i = TextureUtil.anaglyphColor(i);
            }

            float f = (float)(i >> 16 & 255) / 255.0F;
            float f1 = (float)(i >> 8 & 255) / 255.0F;
            float f2 = (float)(i & 255) / 255.0F;

            renderer.renderModelBrightnessColor(backpackModel[1], 1.0f, f, f1, f2);
            renderer.renderModelBrightnessColor(backpackTopModel[1], 1.0f, f, f1, f2);

            GlStateManager.popMatrix();
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onPostBake(ModelBakeEvent event) {
        IModel backpackModelBase = ClientUtils.getModel(new ResourceLocation("charsetstorage:block/backpack"));
        IModel backpackTopModelBase = ClientUtils.getModel(new ResourceLocation("charsetstorage:block/backpack_top"));
        for (int i = 0; i < 4; i++) {
            backpackModel[i] = backpackModelBase.bake(
                    new TRSRTransformation(EnumFacing.getFront(i + 2)),
                    DefaultVertexFormats.BLOCK, ClientUtils.textureGetter);
            backpackTopModel[i] = backpackTopModelBase.bake(
                    new TRSRTransformation(EnumFacing.getFront(i + 2)),
                    DefaultVertexFormats.BLOCK, ClientUtils.textureGetter);
        }
    }
}
