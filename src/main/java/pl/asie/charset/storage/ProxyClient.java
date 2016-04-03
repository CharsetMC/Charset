package pl.asie.charset.storage;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.model.TRSRTransformation;
import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.lib.utils.ClientUtils;
import pl.asie.charset.storage.backpack.*;

/**
 * Created by asie on 1/10/16.
 */
public class ProxyClient extends ProxyCommon {
	public static final KeyBinding backpackOpenKey = new KeyBinding("key.charset.backpackOpen", Keyboard.KEY_C, "key.categories.gameplay");
	public static final IBakedModel[] backpackTopModel = new IBakedModel[4];
	public static final IBakedModel[] backpackModel = new IBakedModel[4];

	@Override
	public void init() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileBackpack.class, new RendererBackpack.Tile());
		ClientRegistry.registerKeyBinding(backpackOpenKey);

		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(new BlockBackpack.Color(), ModCharsetStorage.backpackBlock);
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new ItemBackpack.Color(), ModCharsetStorage.backpackBlock);

		MinecraftForge.EVENT_BUS.register(new RendererBackpack.Armor());
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onKey(InputEvent.KeyInputEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.thePlayer;
		if (!mc.inGameHasFocus || player == null) {
			return;
		}

		if (ModCharsetStorage.enableBackpackOpenKey && backpackOpenKey.isPressed()) {
			ItemStack backpack = ItemBackpack.getBackpack(player);
			if (backpack != null) {
				ModCharsetStorage.packet.sendToServer(new PacketBackpackOpen(player));
			}
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
