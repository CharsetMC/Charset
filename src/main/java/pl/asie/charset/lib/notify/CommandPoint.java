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

package pl.asie.charset.lib.notify;

import com.google.common.base.Joiner;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import pl.asie.charset.lib.CharsetLib;

import java.util.List;

public class CommandPoint extends CommandBase {

    @Override
    public String getName() {
        return "point";
    }

    @Override
    public String getUsage(ICommandSender var1) {
        return "/point [optional text]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
    
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return super.checkPermission(server, sender) && sender instanceof EntityPlayer;
    }
    
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        String msg = Joiner.on(" ").join(args);
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        RayTraceResult mop = getMouseOver(player, 64);
        if (mop == null || mop.typeOfHit == RayTraceResult.Type.MISS) {
            sender.sendMessage(new TextComponentTranslation("charset.point.toofar"));
            return;
        }
        switch (mop.typeOfHit) {
            case BLOCK:
                CharsetLib.packet.sendToServer(PacketPoint.atCoord(mop.getBlockPos(), msg));
                break;
            case ENTITY:
                CharsetLib.packet.sendToServer(PacketPoint.atEntity(mop.entityHit, msg));
                break;
            default:
                return;
        }
    }

    private static RayTraceResult getMouseOver(EntityPlayer player, double reachDistance) {
        float par1 = 1;
        Entity pointedEntity;
        double d0 = reachDistance;
        RayTraceResult objectMouseOver = player.rayTrace(d0, par1);
        double d1 = d0;
        Vec3d vec3 = player.getPositionEyes(par1);

        if (objectMouseOver != null) {
            d1 = objectMouseOver.hitVec.distanceTo(vec3);
        }

        Vec3d vec31 = player.getLook(par1);
        Vec3d vec32 = vec3.add(vec31.x * d0, vec31.y * d0,
                vec31.z * d0);
        pointedEntity = null;
        Vec3d vec33 = null;
        float f1 = 1.0F;
        List<Entity> list = player.world.getEntitiesWithinAABBExcludingEntity(
                player,
                player.getEntityBoundingBox().offset(vec31.x * d0,
                        vec31.y * d0, vec31.z * d0).grow(
                        (double) f1, (double) f1, (double) f1));
        double d2 = d1;

        for (int i = 0; i < list.size(); ++i) {
            Entity entity = list.get(i);

            if (entity.canBeCollidedWith()) {
                float f2 = entity.getCollisionBorderSize();
                AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().grow(
                        (double) f2, (double) f2, (double) f2);
                RayTraceResult RayTraceResult = axisalignedbb
                        .calculateIntercept(vec3, vec32);

                if (axisalignedbb.contains(vec3)) {
                    if (0.0D < d2 || d2 == 0.0D) {
                        pointedEntity = entity;
                        vec33 = RayTraceResult == null ? vec3
                                : RayTraceResult.hitVec;
                        d2 = 0.0D;
                    }
                } else if (RayTraceResult != null) {
                    double d3 = vec3.distanceTo(RayTraceResult.hitVec);

                    if (d3 < d2 || d2 == 0.0D) {
                        if (entity == player.getRidingEntity()
                                && !entity.canRiderInteract()) {
                            if (d2 == 0.0D) {
                                pointedEntity = entity;
                                vec33 = RayTraceResult.hitVec;
                            }
                        } else {
                            pointedEntity = entity;
                            vec33 = RayTraceResult.hitVec;
                            d2 = d3;
                        }
                    }
                }
            }
        }

        if (pointedEntity != null && (d2 < d1 || objectMouseOver == null)) {
            objectMouseOver = new RayTraceResult(pointedEntity, vec33);
        }
        return objectMouseOver;
    }
}
