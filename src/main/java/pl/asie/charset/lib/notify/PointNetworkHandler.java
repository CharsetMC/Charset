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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

public enum PointNetworkHandler {
    INSTANCE;
    String channelName = NotifyNetwork.channelName + "|point";
    FMLEventChannel channel;
    
    void initialize() {
        channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(channelName);
        channel.register(this);
    }
    
    @SubscribeEvent
    public void recievePacket(ServerCustomPacketEvent event) {
        try {
            EntityPlayer player = ((NetHandlerPlayServer) event.getHandler()).playerEntity;
            handlePoint(event.getPacket().payload(), player);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    static final byte COORD = 1, ENTITY = 2;
    
    void handlePoint(ByteBuf input, EntityPlayer player) throws IOException {
        Notice notice;
        switch (input.readByte()) {
            default: return;
            case COORD: {
                BlockPos pos = new BlockPos(input.readInt(), input.readInt(), input.readInt());
                String msg = buildMessage(player, input);
                NotificationCoord at = new NotificationCoord(player.world, pos);
                notice = new Notice(at, msg);
                break;
            }
            case ENTITY: {
                int entityId = input.readInt();
                String msg = buildMessage(player, input);
                Entity ent = player.world.getEntityByID(entityId);
                if (ent == null) return;
                notice = new Notice(ent, msg);
                break;
            }
        }
        notice.withStyle(Style.DRAWFAR, Style.VERY_LONG, Style.SCALE_SIZE, Style.EXACTPOSITION);
        double maxDist = 0xFF * 0xFF;
        for (EntityPlayer viewer : player.world.playerEntities) {
            if (player.getDistanceSqToEntity(viewer) > maxDist) continue;
            notice.sendTo(viewer);
        }
    }
    
    private String buildMessage(EntityPlayer player, ByteBuf input) throws IOException {
        String base = "<" + player.getName() + ">";
        String msg = ByteBufUtils.readUTF8String(input);
        if (msg == null || msg.length() == 0) {
            return base;
        }
        return base + "\n" + msg;
    }
    
    @SideOnly(Side.CLIENT)
    void pointAtCoord(NotificationCoord coord, String msg) throws IOException {
        ByteBuf out = Unpooled.buffer();
        out.writeByte(COORD);
        out.writeInt(coord.getPos().getX());
        out.writeInt(coord.getPos().getY());
        out.writeInt(coord.getPos().getZ());
        ByteBufUtils.writeUTF8String(out, msg);
        send(out);
    }
    
    @SideOnly(Side.CLIENT)
    void pointAtEntity(Entity ent, String msg) throws IOException {
        if (ent == null) return;
        ByteBuf out = Unpooled.buffer();
        out.writeByte(ENTITY);
        out.writeInt(ent.getEntityId());
        ByteBufUtils.writeUTF8String(out, msg);
        send(out);
    }
    
    @SideOnly(Side.CLIENT)
    void send(ByteBuf out) {
        channel.sendToServer(new FMLProxyPacket(new PacketBuffer(out), channelName));
    }
}
