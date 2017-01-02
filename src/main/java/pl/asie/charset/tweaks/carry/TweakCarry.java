package pl.asie.charset.tweaks.carry;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import pl.asie.charset.tweaks.ModCharsetTweaks;
import pl.asie.charset.tweaks.Tweak;

import java.util.Random;

public class TweakCarry extends Tweak {
    public static final ResourceLocation CAP_IDENTIFIER = new ResourceLocation("charsettweaks:carry");

    @CapabilityInject(CarryHandler.class)
    public static Capability<CarryHandler> CAPABILITY;

    public TweakCarry() {
        super("tweaks", "blockCarrying", "Allow players to carry blocks by shift-pickblock.", true);
    }

    @Override
    public boolean init() {
        CarryTransformerRegistry.INSTANCE.registerEntityTransformer(new CarryTransformerEntityMinecart());
        if (Loader.isModLoaded("charsetstorage")) {
            CarryTransformerRegistry.INSTANCE.registerEntityTransformer(new CarryTransformerEntityMinecartDayBarrel());
        }

        MinecraftForge.EVENT_BUS.register(this);
        CapabilityManager.INSTANCE.register(CarryHandler.class, CarryHandler.STORAGE, CarryHandler.class);
        return true;
    }

    @SubscribeEvent
    public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            event.addCapability(CAP_IDENTIFIER, new CarryHandler.Provider((EntityPlayer) event.getObject()));
        }
    }

    private void cancelIfCarrying(Event event, EntityPlayer player) {
        CarryHandler carryHandler = player.getCapability(CAPABILITY, null);
        if (carryHandler != null && carryHandler.isCarrying()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onMouse(MouseEvent event) {
        if (event.isButtonstate() && event.getButton() == 2
                && GuiScreen.isShiftKeyDown()) {

            EntityPlayer player = Minecraft.getMinecraft().player;
            CarryHandler carryHandler = player.getCapability(CAPABILITY, null);

            if (player.getHeldItem(EnumHand.MAIN_HAND).isEmpty()
                    && player.getHeldItem(EnumHand.OFF_HAND).isEmpty()
                    && Minecraft.getMinecraft().currentScreen == null
                    && carryHandler != null) {

                RayTraceResult mouseOver = Minecraft.getMinecraft().objectMouseOver;
                if (mouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
                    ModCharsetTweaks.proxy.carryGrabBlock(player, player.getEntityWorld(), mouseOver.getBlockPos());
                    event.setCanceled(true);
                } else if (mouseOver.typeOfHit == RayTraceResult.Type.ENTITY) {
                    Entity entity = mouseOver.entityHit;
                    ModCharsetTweaks.proxy.carryGrabEntity(player, player.getEntityWorld(), entity);
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();

        CarryHandler carryHandler = player.getCapability(CAPABILITY, null);
        if (carryHandler != null && carryHandler.isCarrying()) {
            event.setCanceled(true);

            World world = event.getWorld();
            BlockPos pos = event.getPos();
            EnumFacing facing = event.getFace();
            IBlockState state = world.getBlockState(pos);

            if (!state.getBlock().isReplaceable(world, pos)) {
                pos = pos.offset(facing);
            }

            if (!world.isRemote) {
                carryHandler.place(world, pos, facing);
            } else {
                carryHandler.empty();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        EntityPlayer player = event.getEntityPlayer();

        CarryHandler carryHandler = player.getCapability(CAPABILITY, null);
        if (carryHandler != null && carryHandler.isCarrying()) {
            event.setCanceled(true);
            Entity entity = event.getTarget();

            for (ICarryTransformer<Entity> transformer : CarryTransformerRegistry.INSTANCE.getEntityTransformers()) {
                IBlockState state = carryHandler.getBlockState();
                TileEntity tile = carryHandler.getTileEntity();

                if (transformer.insert(entity, state, tile, true)) {
                    carryHandler.empty();
                    transformer.insert(entity, state, tile, false);
                    return;
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        CarryHandler carryHandler = event.player.getCapability(CAPABILITY, null);
        if (carryHandler != null && carryHandler.isCarrying() && event.player.isSprinting()) {
            event.player.setSprinting(false);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        cancelIfCarrying(event, event.getEntityPlayer());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        cancelIfCarrying(event, event.getEntityPlayer());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        cancelIfCarrying(event, event.getEntityPlayer());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        cancelIfCarrying(event, event.getEntityPlayer());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        cancelIfCarrying(event, event.getEntityPlayer());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    @SideOnly(Side.CLIENT)
    public void onRenderHand(RenderHandEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        float partialTicks = event.getPartialTicks();

        CarryHandler carryHandler = player.getCapability(CAPABILITY, null);
        if (carryHandler != null && carryHandler.isCarrying()) {
            event.setCanceled(true);
            Minecraft.getMinecraft().entityRenderer.enableLightmap();

            GlStateManager.pushMatrix();
            float rotX = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;
            float rotY = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partialTicks;

            GlStateManager.pushMatrix();
            GlStateManager.rotate(rotX, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(rotY, 0.0F, 1.0F, 0.0F);
            RenderHelper.enableStandardItemLighting();
            GlStateManager.popMatrix();

            //OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 15, 15);
            Random random = new Random(System.currentTimeMillis());
            //GlStateManager.translate(random.nextFloat()*10-5,random.nextFloat()*10-5,random.nextFloat()*10-5);
            GlStateManager.translate(-0.5, -1.25, -1.5);
            GlStateManager.enableRescaleNormal();

            try {
                Tessellator tessellator = Tessellator.getInstance();
                VertexBuffer buffer = tessellator.getBuffer();
                //Minecraft.getMinecraft().getRenderItem().renderItem(
                  //      new ItemStack(Blocks.STONE), player, ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND, false);

                buffer.setTranslation(0, -64, 0);
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);

                IBlockState renderState = carryHandler.getBlockState().getActualState(carryHandler.getBlockAccess(), CarryHandler.ACCESS_POS);
                IBlockState renderStateExt = carryHandler.getBlockState().getBlock().getExtendedState(renderState, carryHandler.getBlockAccess(), CarryHandler.ACCESS_POS);

                BlockRendererDispatcher brd = Minecraft.getMinecraft().getBlockRendererDispatcher();
                IBakedModel model = brd.getModelForState(renderState);
                if (carryHandler.getBlockState().getRenderType() == EnumBlockRenderType.MODEL) {
                    brd.getBlockModelRenderer().renderModelFlat(carryHandler.getBlockAccess(),
                            model, renderStateExt,
                            CarryHandler.ACCESS_POS, buffer, false, 0L
                    );
                }

                tessellator.draw();
                buffer.setTranslation(0, 0, 0);

                TileEntity tile = carryHandler.getBlockAccess().getTileEntity(CarryHandler.ACCESS_POS);
                if (tile != null) {
                    try {
                        TileEntityRendererDispatcher.instance.renderTileEntityAt(tile, 0, 0, 0, partialTicks);
                    } catch (Exception e) {

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            GlStateManager.disableRescaleNormal();
            GlStateManager.popMatrix();
            Minecraft.getMinecraft().entityRenderer.disableLightmap();
        }
    }
}
