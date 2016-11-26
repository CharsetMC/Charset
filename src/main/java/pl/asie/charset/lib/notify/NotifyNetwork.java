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
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

public class NotifyNetwork {
    static final String channelName = "fzNotify";
    static FMLEventChannel channel;
    
    static final byte COORD = 0, VEC3 = 1, ENTITY = 2, TILEENTITY = 3, ONSCREEN = 4, REPLACEABLE = 5;
    
    public NotifyNetwork() {
        channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(channelName);
        channel.register(this);
    }
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void recievePacket(ClientCustomPacketEvent event) {
        try {
            handleNotify(event.getPacket().payload(), Minecraft.getMinecraft().player);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SideOnly(Side.CLIENT)
    void handleNotify(ByteBuf input, EntityPlayer me) throws IOException {
        Object target;
        BlockPos pos;
        switch (input.readByte()) {
        case COORD:
            pos = new BlockPos(input.readInt(), input.readInt(), input.readInt());
            target = new NotificationCoord(me.world, pos);
            break;
        case ENTITY:
            int id = input.readInt();
            if (id == me.getEntityId()) {
                target = me; //bebna
            } else {
                target = me.world.getEntityByID(id);
            }
            break;
        case TILEENTITY:
            pos = new BlockPos(input.readInt(), input.readInt(), input.readInt());
            target = me.world.getTileEntity(pos);
            if (target == null) {
                target = new NotificationCoord(me.world, pos);
            }
            break;
        case VEC3:
            target = new Vec3d(input.readDouble(), input.readDouble(), input.readDouble());
            break;
        case ONSCREEN:
            String message = ByteBufUtils.readUTF8String(input);
            String[] formatArgs = readStrings(input);
            NotifyImplementation.proxy.onscreen(message, formatArgs);
            return;
        case REPLACEABLE:
            String str = ByteBufUtils.readUTF8String(input);
            int msgKey = input.readInt();
            ITextComponent msg = ITextComponent.Serializer.jsonToComponent(str);
            NotifyImplementation.proxy.replaceable(msg, msgKey);
            return;
        default: return;
        }
        if (target == null) {
            return;
        }
        
        ItemStack item = ByteBufUtils.readItemStack(input);
        String msg = ByteBufUtils.readUTF8String(input);
        String args[] = readStrings(input);
        NotifyImplementation.recieve(me, target, item, msg, args);
    }
    
    
    static void broadcast(FMLProxyPacket packet, EntityPlayer player, TargetPoint area) {
        if (player == null) {
            NotifyNetwork.channel.sendToAll(packet);
        } else if (player instanceof EntityPlayerMP) {
            NotifyNetwork.channel.sendTo(packet, (EntityPlayerMP) player);
        }
    }
    
    private static void writeStrings(ByteBuf output, String[] args) throws IOException {
        output.writeByte((byte) args.length);
        for (String s : args) {
            if (s == null) s = "null";
            ByteBufUtils.writeUTF8String(output, s);
        }
    }
    
    private static String[] readStrings(ByteBuf input)  throws IOException {
        String[] ret = new String[input.readByte()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = ByteBufUtils.readUTF8String(input);
        }
        return ret;
    }
    
    static FMLProxyPacket notifyPacket(Object where, ItemStack item, String format, String ...args) {
        try {
            ByteBuf output = Unpooled.buffer();

            if (where instanceof NotificationCoord) {
                where = ((NotificationCoord) where).getPos();
            }
            
            if (where instanceof Vec3d) {
                output.writeByte(VEC3);
                Vec3d v = (Vec3d) where;
                output.writeDouble(v.xCoord);
                output.writeDouble(v.yCoord);
                output.writeDouble(v.zCoord);
            } else if (where instanceof BlockPos) {
                output.writeByte(COORD);
                BlockPos pos = (BlockPos) where;
                output.writeInt(pos.getX());
                output.writeInt(pos.getY());
                output.writeInt(pos.getZ());
            } else if (where instanceof Entity) {
                output.writeByte(ENTITY);
                Entity ent = (Entity) where;
                output.writeInt(ent.getEntityId());
            } else if (where instanceof TileEntity) {
                output.writeByte(TILEENTITY);
                TileEntity te = (TileEntity) where;
                output.writeInt(te.getPos().getX());
                output.writeInt(te.getPos().getY());
                output.writeInt(te.getPos().getZ());
            } else {
                return null;
            }

            ByteBufUtils.writeItemStack(output, item);
            ByteBufUtils.writeUTF8String(output, format);
            writeStrings(output, args);
            return generate(output);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    static FMLProxyPacket onscreenPacket(String message, String[] formatArgs) {
        try {
            ByteBuf output = Unpooled.buffer();
            output.writeByte(ONSCREEN);
            ByteBufUtils.writeUTF8String(output, message);
            writeStrings(output, formatArgs);
            return generate(output);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    static FMLProxyPacket replaceableChatPacket(ITextComponent msg, int msgKey) {
        String str = ITextComponent.Serializer.componentToJson(msg);
        ByteBuf output = Unpooled.buffer();
        output.writeByte(REPLACEABLE);
        ByteBufUtils.writeUTF8String(output, str);
        output.writeInt(msgKey);
        return generate(output);
    }
    
    
    
    public static FMLProxyPacket generate(ByteBuf buf) {
        return new FMLProxyPacket(new PacketBuffer(buf), channelName);
    }
}
