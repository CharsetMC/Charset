/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
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

package pl.asie.charset.module.tweak.carry;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
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
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.utils.AttributeUtils;

public class TweakCarryEventHandler {
    private static final AttributeModifier MODIFIER_CARRY = AttributeUtils.newModifierSingleton("charsettweaks:carry", -0.25D, AttributeUtils.Operation.ADD_MULTIPLIED);

    private void cancelIfCarrying(Event event, EntityPlayer player) {
        CarryHandler carryHandler = player.getCapability(CharsetTweakBlockCarrying.CAPABILITY, null);
        if (carryHandler != null && carryHandler.isCarrying()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlayerUpdate(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            CarryHandler carryHandler = event.player.getCapability(CharsetTweakBlockCarrying.CAPABILITY, null);
            if (carryHandler != null && carryHandler.isCarrying()) {
                carryHandler.update();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onLivingFall(LivingFallEvent event) {
        CarryHandler carryHandler = event.getEntityLiving().getCapability(CharsetTweakBlockCarrying.CAPABILITY, null);
        if (carryHandler != null && event.getEntityLiving() instanceof EntityPlayer && carryHandler.isCarrying() && event.getDistance() >= 4.0f) {
            // TODO: add distance-based scaling
            CharsetTweakBlockCarrying.dropCarriedBlock((EntityPlayer) event.getEntityLiving(), false);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onLivingHurt(LivingHurtEvent event) {
        CarryHandler carryHandler = event.getEntityLiving().getCapability(CharsetTweakBlockCarrying.CAPABILITY, null);
        if (carryHandler != null && event.getEntityLiving() instanceof EntityPlayer && carryHandler.isCarrying() && event.getSource() != DamageSource.FALL
                && event.getSource() != DamageSource.CACTUS) {
            /* TODO: Do something about cactus damage etc, to not whitelist it unnecessarily */
            CharsetTweakBlockCarrying.dropCarriedBlock((EntityPlayer) event.getEntityLiving(), false);
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            CharsetTweakBlockCarrying.dropCarriedBlock((EntityPlayer) event.getEntityLiving(), true);
        }
    }

    @SubscribeEvent
    public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            event.addCapability(CharsetTweakBlockCarrying.CAP_IDENTIFIER, CarryHandler.PROVIDER.create(new CarryHandler().setPlayer((EntityPlayer) event.getObject())));
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
        CharsetTweakBlockCarrying.syncCarryWithAllClients(event.player);
    }

    @SubscribeEvent
    public void onPlayerRespawnEvent(PlayerEvent.PlayerRespawnEvent event) {
        CharsetTweakBlockCarrying.syncCarryWithAllClients(event.player);
    }

    @SubscribeEvent
    public void onPlayerChangedDimensionEvent(PlayerEvent.PlayerChangedDimensionEvent event) {
        CharsetTweakBlockCarrying.syncCarryWithAllClients(event.player);
    }

    @SubscribeEvent
    public void onPlayerStartTracking(net.minecraftforge.event.entity.player.PlayerEvent.StartTracking event) {
        CharsetTweakBlockCarrying.syncCarryWithClient(event.getTarget(), event.getEntityPlayer());
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        CarryHandler carryHandler = event.player.getCapability(CharsetTweakBlockCarrying.CAPABILITY, null);
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

    @SideOnly(Side.CLIENT)
    private boolean startCarry(boolean allowCreative) {
        boolean result = false;
        EntityPlayer player = Minecraft.getMinecraft().player;
        CarryHandler carryHandler = player.getCapability(CharsetTweakBlockCarrying.CAPABILITY, null);
        if (!player.isCreative()) {
            result = true;
        } else if (!allowCreative || !CharsetTweakBlockCarrying.enabledCreative) {
            return false;
        }

        if (CharsetTweakBlockCarrying.canPlayerConsiderCarryingBlock(player)
                && carryHandler != null) {

            if (player.isCreative()) {
                result = true;
            }

            RayTraceResult mouseOver = Minecraft.getMinecraft().objectMouseOver;
            if (mouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
                CharsetTweakBlockCarrying.grabBlock(player, player.getEntityWorld(), mouseOver.getBlockPos());
            } else if (mouseOver.typeOfHit == RayTraceResult.Type.ENTITY) {
                Entity entity = mouseOver.entityHit;
                CharsetTweakBlockCarrying.grabEntity(player, player.getEntityWorld(), entity);
            }
        }

        return result;
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onMouse(MouseEvent event) {
        if (event.isButtonstate() && Minecraft.getMinecraft().gameSettings.keyBindPickBlock.isActiveAndMatches(-100 + event.getButton())
                && Minecraft.getMinecraft().gameSettings.keyBindSneak.isKeyDown()) {
            if (startCarry(true)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onKey(InputEvent.KeyInputEvent event) {
        if (Minecraft.getMinecraft().gameSettings.keyBindPickBlock.isKeyDown()
                && Minecraft.getMinecraft().gameSettings.keyBindSneak.isKeyDown()) {
            startCarry(false);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();

        CarryHandler carryHandler = player.getCapability(CharsetTweakBlockCarrying.CAPABILITY, null);
        if (carryHandler != null && carryHandler.isCarrying()) {
            event.setCanceled(true);

            World world = event.getWorld();
            BlockPos pos = event.getPos();
            EnumFacing facing = event.getFace();
            IBlockState state = world.getBlockState(pos);

            if (!state.getBlock().isReplaceable(world, pos)) {
                pos = pos.offset(facing);
            }

            carryHandler.place(world, pos, facing, player);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        EntityPlayer player = event.getEntityPlayer();

        CarryHandler carryHandler = player.getCapability(CharsetTweakBlockCarrying.CAPABILITY, null);
        if (carryHandler != null && carryHandler.isCarrying()) {
            event.setCanceled(true);
            Entity entity = event.getTarget();

            for (ICarryTransformer<Entity> transformer : CarryTransformerRegistry.INSTANCE.getEntityTransformers()) {
                IBlockState state = carryHandler.getState();
                TileEntity tile = carryHandler.getTile();

                if (transformer.insert(entity, state, tile, true)) {
                    carryHandler.empty();
                    transformer.insert(entity, state, tile, false);
                    CharsetTweakBlockCarrying.syncCarryWithAllClients(player);
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
