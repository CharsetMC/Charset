package pl.asie.charset.tweaks.carry;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.utils.AttributeUtils;

import java.util.UUID;

public class TweakCarryEventHandler {
    private static final AttributeModifier MODIFIER_CARRY = AttributeUtils.newModifierSingleton("charsettweaks:carry", -0.25D, AttributeUtils.Operation.ADD_MULTIPLIED);

    private void cancelIfCarrying(Event event, EntityPlayer player) {
        CarryHandler carryHandler = player.getCapability(TweakCarry.CAPABILITY, null);
        if (carryHandler != null && carryHandler.isCarrying()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onLivingFall(LivingFallEvent event) {
        CarryHandler carryHandler = event.getEntityLiving().getCapability(TweakCarry.CAPABILITY, null);
        if (carryHandler != null && carryHandler.isCarrying() && event.getDistance() >= 4.0f) {
            // TODO: add distance-based scaling
            TweakCarry.dropCarriedBlock(event.getEntityLiving(), false);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onLivingHurt(LivingHurtEvent event) {
        CarryHandler carryHandler = event.getEntityLiving().getCapability(TweakCarry.CAPABILITY, null);
        if (carryHandler != null && carryHandler.isCarrying() && event.getSource() != DamageSource.FALL) {
            TweakCarry.dropCarriedBlock(event.getEntityLiving(), false);
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        TweakCarry.dropCarriedBlock(event.getEntityLiving(), true);
    }

    @SubscribeEvent
    public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            event.addCapability(TweakCarry.CAP_IDENTIFIER, CarryHandler.PROVIDER.create(new CarryHandler().setPlayer((EntityPlayer) event.getObject())));
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
        TweakCarry.syncCarryWithClient(event.player, event.player);
    }

    @SubscribeEvent
    public void onPlayerRespawnEvent(PlayerEvent.PlayerRespawnEvent event) {
        TweakCarry.syncCarryWithClient(event.player, event.player);
    }

    @SubscribeEvent
    public void onPlayerChangedDimensionEvent(PlayerEvent.PlayerChangedDimensionEvent event) {
        TweakCarry.syncCarryWithClient(event.player, event.player);
    }

    @SubscribeEvent
    public void onPlayerStartTracking(net.minecraftforge.event.entity.player.PlayerEvent.StartTracking event) {
        TweakCarry.syncCarryWithClient(event.getTarget(), event.getEntityPlayer());
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        CarryHandler carryHandler = event.player.getCapability(TweakCarry.CAPABILITY, null);
        IAttributeInstance movementSpeed = event.player.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (carryHandler != null && carryHandler.isCarrying()) {
            if (event.player.isSprinting()) {
                event.player.setSprinting(false);
            }

            if (!event.player.isPotionActive(MobEffects.STRENGTH) && !movementSpeed.hasModifier(MODIFIER_CARRY)) {
               movementSpeed.applyModifier(MODIFIER_CARRY);
            }
        } else {
            if (movementSpeed.hasModifier(MODIFIER_CARRY)) {
                movementSpeed.removeModifier(MODIFIER_CARRY);
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onMouse(MouseEvent event) {
        if (event.isButtonstate() && event.getButton() == 2
                && Minecraft.getMinecraft().gameSettings.keyBindSneak.isKeyDown()) {

            EntityPlayer player = Minecraft.getMinecraft().player;
            CarryHandler carryHandler = player.getCapability(TweakCarry.CAPABILITY, null);
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
                    TweakCarry.grabBlock(player, player.getEntityWorld(), mouseOver.getBlockPos());
                } else if (mouseOver.typeOfHit == RayTraceResult.Type.ENTITY) {
                    Entity entity = mouseOver.entityHit;
                    TweakCarry.grabEntity(player, player.getEntityWorld(), entity);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();

        CarryHandler carryHandler = player.getCapability(TweakCarry.CAPABILITY, null);
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

        CarryHandler carryHandler = player.getCapability(TweakCarry.CAPABILITY, null);
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
