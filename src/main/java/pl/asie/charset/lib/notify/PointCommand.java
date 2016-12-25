/*
 * Copyright (c) 2016 neptunepink
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import pl.asie.charset.lib.ModCharsetLib;

import java.io.IOException;
import java.util.List;

public class PointCommand extends CommandBase {

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
                ModCharsetLib.packet.sendToServer(PacketPoint.atCoord(mop.getBlockPos(), msg));
                break;
            case ENTITY:
                ModCharsetLib.packet.sendToServer(PacketPoint.atEntity(mop.entityHit, msg));
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
        Vec3d vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0,
                vec31.zCoord * d0);
        pointedEntity = null;
        Vec3d vec33 = null;
        float f1 = 1.0F;
        List list = player.world.getEntitiesWithinAABBExcludingEntity(
                player,
                player.getEntityBoundingBox().addCoord(vec31.xCoord * d0,
                        vec31.yCoord * d0, vec31.zCoord * d0).expand(
                        (double) f1, (double) f1, (double) f1));
        double d2 = d1;

        for (int i = 0; i < list.size(); ++i) {
            Entity entity = (Entity) list.get(i);

            if (entity.canBeCollidedWith()) {
                float f2 = entity.getCollisionBorderSize();
                AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().expand(
                        (double) f2, (double) f2, (double) f2);
                RayTraceResult RayTraceResult = axisalignedbb
                        .calculateIntercept(vec3, vec32);

                if (axisalignedbb.isVecInside(vec3)) {
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
