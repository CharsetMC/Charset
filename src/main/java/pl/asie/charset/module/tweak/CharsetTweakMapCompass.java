/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.module.tweak;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.network.Packet;
import pl.asie.charset.lib.network.PacketRegistry;

import javax.annotation.Nullable;

@CharsetModule(
        name = "tweak.mapCompass",
        description = "Holding a map in one hand and a compass in another will make the compass point to the map's center.",
        profile = ModuleProfile.STABLE
)
public class CharsetTweakMapCompass {
    public static class PacketCompassAngle extends Packet {
        private static float currentAngle = -1.0f;
        private float angle;

        public PacketCompassAngle() {

        }

        public PacketCompassAngle(float angle) {
            this.angle = angle;
        }

        @Override
        public void readData(INetHandler handler, PacketBuffer buf) {
            angle = buf.readFloat();
        }

        @Override
        public void apply(INetHandler handler) {
            currentAngle = angle;
        }

        @Override
        public void writeData(PacketBuffer buf) {
            buf.writeFloat(angle);
        }

        @Override
        public boolean isAsynchronous() {
            return true;
        }
    }

    private static final ResourceLocation ANGLE = new ResourceLocation("angle");

    @CharsetModule.PacketRegistry
    private static PacketRegistry packet;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        packet.registerPacket(0x01, PacketCompassAngle.class);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side == Side.SERVER && event.phase == TickEvent.Phase.END) {
            EntityPlayer entityIn = event.player;
            if (entityIn.getEntityWorld().getTotalWorldTime() % 4 != 1) {
                return;
            }

            ItemStack heldMain = entityIn.getHeldItem(EnumHand.MAIN_HAND);
            ItemStack heldOff = entityIn.getHeldItem(EnumHand.OFF_HAND);
            ItemStack heldOther;
            if (!heldMain.isEmpty() && heldMain.getItem() instanceof ItemCompass) {
                heldOther = heldOff;
            } else if (!heldOff.isEmpty() && heldOff.getItem() instanceof ItemCompass) {
                heldOther = heldMain;
            } else {
                heldOther = ItemStack.EMPTY;
            }

            if (!heldOther.isEmpty() && heldOther.getItem() instanceof ItemMap) {
                ItemMap mapItem = (ItemMap) heldOther.getItem();
                MapData mapData = mapItem.getMapData(heldOther, entityIn.world);
                if (mapData != null) {
                    int xPos = mapData.xCenter, zPos = mapData.zCenter;
                    double angle = Math.atan2(zPos - entityIn.posZ, xPos - entityIn.posX) / (Math.PI * 2);
                    double yaw = MathHelper.positiveModulo(entityIn.rotationYaw / 360.0, 1.0);
                    double value = 0.5D - (yaw - 0.25D - angle);
                    packet.sendTo(
                            new PacketCompassAngle(MathHelper.positiveModulo((float) value, 1.0F)),
                            entityIn
                    );
                }
            }
        }
    }

    public static class AngleOverride implements IItemPropertyGetter {
        private final IItemPropertyGetter oldGetter;
        private long lastUpdateTick;
        private double lastRotation;
        private double rotDirection;

        public AngleOverride(IItemPropertyGetter oldGetter) {
            this.oldGetter = oldGetter;
        }

        @SideOnly(Side.CLIENT)
        private float wobble(World worldIn, float value) {
            if (worldIn.getTotalWorldTime() != this.lastUpdateTick) {
                this.lastUpdateTick = worldIn.getTotalWorldTime();
                double difference = value - this.lastRotation;
                difference = MathHelper.positiveModulo(difference + 0.5, 1.0) - 0.5;
                this.rotDirection = (this.rotDirection + difference * 0.1) * 0.8;
                this.lastRotation = MathHelper.positiveModulo(this.lastRotation + this.rotDirection, 1.0);
            }

            return (float) this.lastRotation;
        }

        @Override
        public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
            if (entityIn == Minecraft.getMinecraft().player && PacketCompassAngle.currentAngle >= 0.0f) {
                if (worldIn == null) {
                    worldIn = entityIn.world;
                }
                ItemStack heldMain = entityIn.getHeldItem(EnumHand.MAIN_HAND);
                ItemStack heldOff = entityIn.getHeldItem(EnumHand.OFF_HAND);
                ItemStack heldOther;
                if (stack == heldMain) {
                    heldOther = heldOff;
                } else if (stack == heldOff) {
                    heldOther = heldMain;
                } else {
                    heldOther = ItemStack.EMPTY;
                }

                if (!heldOther.isEmpty() && heldOther.getItem() instanceof ItemMap) {
                    float value = wobble(worldIn, PacketCompassAngle.currentAngle);

                    return MathHelper.positiveModulo(value, 1.0F);
                }
            }

            this.lastRotation = oldGetter.apply(stack, worldIn, entityIn);
            this.rotDirection = 0;
            return (float) this.lastRotation;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    @SideOnly(Side.CLIENT)
    public void onRegisterItems(RegistryEvent.Register<Item> event) {
        for (Item item : event.getRegistry()) {
            if (item instanceof ItemCompass) {
                IItemPropertyGetter oldGetter = item.getPropertyGetter(ANGLE);
                if (oldGetter != null && !(oldGetter instanceof AngleOverride)) {
                    item.addPropertyOverride(ANGLE, new AngleOverride(oldGetter));
                }
            }
        }
    }
}
