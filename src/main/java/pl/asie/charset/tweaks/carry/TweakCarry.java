package pl.asie.charset.tweaks.carry;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;
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
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.tweaks.ModCharsetTweaks;
import pl.asie.charset.tweaks.Tweak;
import pl.asie.charset.tweaks.carry.transforms.CarryTransformerEntityMinecart;
import pl.asie.charset.tweaks.carry.transforms.CarryTransformerEntityMinecartDayBarrel;

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
        if (ModCharsetLib.proxy.isClient()) {
            MinecraftForge.EVENT_BUS.register(new TweakCarryRender());
        }
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

    private void syncCarryData(Entity who, EntityPlayer target) {
        if (who instanceof EntityPlayer && who.hasCapability(CAPABILITY, null)) {
            ModCharsetTweaks.packet.sendTo(new PacketCarrySync(who), target);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
        syncCarryData(event.player, event.player);
    }

    @SubscribeEvent
    public void onPlayerRespawnEvent(PlayerEvent.PlayerRespawnEvent event) {
        syncCarryData(event.player, event.player);
    }

    @SubscribeEvent
    public void onPlayerChangedDimensionEvent(PlayerEvent.PlayerChangedDimensionEvent event) {
        syncCarryData(event.player, event.player);
    }

    @SubscribeEvent
    public void onPlayerStartTracking(net.minecraftforge.event.entity.player.PlayerEvent.StartTracking event) {
        syncCarryData(event.getTarget(), event.getEntityPlayer());
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        CarryHandler carryHandler = event.player.getCapability(CAPABILITY, null);
        if (carryHandler != null && carryHandler.isCarrying()) {
            if (event.player.isSprinting()) {
                event.player.setSprinting(false);
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onMouse(MouseEvent event) {
        if (event.isButtonstate() && event.getButton() == 2
                && Minecraft.getMinecraft().gameSettings.keyBindSneak.isKeyDown()) {

            EntityPlayer player = Minecraft.getMinecraft().player;
            CarryHandler carryHandler = player.getCapability(CAPABILITY, null);
            if (!player.isCreative()) {
                event.setCanceled(true);
            }

            if (player.getHeldItem(EnumHand.MAIN_HAND).isEmpty()
                    && player.getHeldItem(EnumHand.OFF_HAND).isEmpty()
                    && !player.isPlayerSleeping()
                    && !player.isRiding()
                    && Minecraft.getMinecraft().currentScreen == null
                    && carryHandler != null) {

                if (player.isCreative()) {
                    event.setCanceled(true);
                }

                RayTraceResult mouseOver = Minecraft.getMinecraft().objectMouseOver;
                if (mouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
                    ModCharsetTweaks.proxy.carryGrabBlock(player, player.getEntityWorld(), mouseOver.getBlockPos());
                } else if (mouseOver.typeOfHit == RayTraceResult.Type.ENTITY) {
                    Entity entity = mouseOver.entityHit;
                    ModCharsetTweaks.proxy.carryGrabEntity(player, player.getEntityWorld(), entity);
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

            carryHandler.place(world, pos, facing);
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

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
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
}
