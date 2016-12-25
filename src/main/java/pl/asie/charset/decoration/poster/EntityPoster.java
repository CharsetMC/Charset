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

package pl.asie.charset.decoration.poster;

import com.google.common.base.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.decoration.ModCharsetDecoration;
import pl.asie.charset.lib.factorization.Quaternion;
import pl.asie.charset.lib.factorization.SpaceUtil;
import pl.asie.charset.lib.utils.DataSerializersCharset;
import pl.asie.charset.lib.utils.DirectionUtils;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.PlayerUtils;
import pl.asie.charset.storage.barrel.EntityMinecartDayBarrel;

import javax.annotation.Nullable;
import java.io.IOException;

public class EntityPoster extends Entity {
    private static final DataParameter<NBTTagCompound> PARAMETER_TAG = EntityDataManager.createKey(EntityPoster.class, DataSerializersCharset.NBT_TAG_COMPOUND);

    /* private static final DataParameter<Optional<ItemStack>> PARAMETER_INV = EntityDataManager.createKey(EntityPoster.class, DataSerializers.OPTIONAL_ITEM_STACK);
    private static final DataParameter<Quaternion> PARAMETER_ROT = EntityDataManager.createKey(EntityPoster.class, DataSerializersCharset.OUATERNION);
    private static final DataParameter<Float> PARAMETER_SCALE = EntityDataManager.createKey(EntityPoster.class, DataSerializers.FLOAT);
    private static final DataParameter<Quaternion> PARAMETER_BASE_ROT = EntityDataManager.createKey(EntityPoster.class, DataSerializersCharset.OUATERNION);
    private static final DataParameter<Float> PARAMETER_BASE_SCALE = EntityDataManager.createKey(EntityPoster.class, DataSerializers.FLOAT);
    private static final DataParameter<Integer> PARAMETER_SPIN_NORMAL = EntityDataManager.createKey(EntityPoster.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> PARAMETER_SPIN_VERTICAL = EntityDataManager.createKey(EntityPoster.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> PARAMETER_SPIN_TILT = EntityDataManager.createKey(EntityPoster.class, DataSerializers.VARINT);
    private static final DataParameter<Byte> PARAMETER_DELTA_SCALE = EntityDataManager.createKey(EntityPoster.class, DataSerializers.BYTE);
    private static final DataParameter<EnumFacing> PARAMETER_NORM = EntityDataManager.createKey(EntityPoster.class, DataSerializers.FACING);
    private static final DataParameter<EnumFacing> PARAMETER_TOP = EntityDataManager.createKey(EntityPoster.class, DataSerializers.FACING);
    private static final DataParameter<EnumFacing> PARAMETER_TILT = EntityDataManager.createKey(EntityPoster.class, DataSerializers.FACING);
    private static final DataParameter<Boolean> PARAMETER_LOCKED = EntityDataManager.createKey(EntityPoster.class, DataSerializers.BOOLEAN); */

    public ItemStack inv = new ItemStack(ModCharsetDecoration.posterItem);
    public Quaternion rot = new Quaternion();
    public double scale = 1.0;
    public boolean locked = false;

    Quaternion base_rotation = new Quaternion();
    double base_scale = 1.0;
    public short spin_normal = 0, spin_vertical = 0, spin_tilt = 0;
    byte delta_scale = 0;
    EnumFacing norm = EnumFacing.NORTH, top = EnumFacing.UP, tilt = EnumFacing.EAST;

    private boolean initialized = false;

    public EntityPoster(World w) {
        super(w);
        setSize(0.5F, 0.5F);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        if (!compound.hasKey("inv")) {
            inv = new ItemStack(ModCharsetDecoration.posterItem);
        } else {
            inv = new ItemStack(compound.getCompoundTag("inv"));
            if (inv.isEmpty()) inv = new ItemStack(ModCharsetDecoration.posterItem);
        }
        rot = Quaternion.loadFromTag(compound, "rot");
        scale = compound.getDouble("scale");
        base_rotation = Quaternion.loadFromTag(compound, "base_rot");
        base_scale = compound.getDouble("base_scale");
        spin_normal = compound.getShort("spin_normal");
        spin_vertical = compound.getShort("spin_vertical");
        spin_tilt = compound.getShort("spin_tilt");
        delta_scale = compound.getByte("delta_scale");
        locked = compound.getBoolean("locked");
        norm = DirectionUtils.get(compound.getByte("norm"));
        top = DirectionUtils.get(compound.getByte("top"));
        tilt = DirectionUtils.get(compound.getByte("tilt"));

        updateSize();
        if (!world.isRemote) {
            syncData();
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        ItemUtils.writeToNBT(inv, compound, "inv");
        rot.writeToTag(compound, "rot");
        compound.setDouble("scale", scale);
        base_rotation.writeToTag(compound, "base_rot");
        compound.setDouble("base_scale", base_scale);
        compound.setShort("spin_normal", spin_normal);
        compound.setShort("spin_tilt", spin_tilt);
        compound.setShort("spin_vertical", spin_vertical);
        compound.setByte("delta_scale", delta_scale);
        compound.setBoolean("locked", locked);
        compound.setByte("norm", (byte) DirectionUtils.ordinal(norm));
        compound.setByte("top", (byte) DirectionUtils.ordinal(top));
        compound.setByte("tilt", (byte) DirectionUtils.ordinal(tilt));
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (world.isRemote) {
            if (dataManager.isDirty()) {
                readEntityFromNBT(dataManager.get(PARAMETER_TAG));
            }
        }
    }

    void updateSize() {
        Vec3d here = SpaceUtil.fromEntPos(this);
        Vec3d[] parts = new Vec3d[6];
        EnumFacing[] values = EnumFacing.VALUES;
        for (int i = 0; i < values.length; i++) {
            EnumFacing dir = values[i];
            if (dir == norm.getOpposite()) {
                continue;
            }
            float s = (float) scale;
            if (dir == norm) {
                s /= 16;
            } else {
                s /= 2;
            }
            parts[i] = SpaceUtil.scale(SpaceUtil.fromDirection(dir), s).add(here);
        }
        setEntityBoundingBox(SpaceUtil.newBox(parts));
    }

    public void setItem(ItemStack item) {
        if (item.isEmpty()) {
            item = new ItemStack(ModCharsetDecoration.posterItem);
        }
        inv = item;
    }

    public ItemStack getItem() {
        if (inv.getItem() == ModCharsetDecoration.posterItem) return null;
        return inv;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    public boolean isInRangeToRender3d(double p_145770_1_, double p_145770_3_, double p_145770_5_) {
        return super.isInRangeToRender3d(p_145770_1_, p_145770_3_, p_145770_5_);
    }

    public void setBase(double baseScale, Quaternion baseRotation, EnumFacing norm, EnumFacing top, AxisAlignedBB bounds) {
        this.scale = this.base_scale = baseScale;
        this.base_rotation = baseRotation;
        this.rot = new Quaternion(base_rotation);
        spin_normal = 0;
        spin_vertical = 0;
        delta_scale = 0;
        this.norm = norm;
        this.top = top;

        Vec3d tiltV = SpaceUtil.fromDirection(norm).crossProduct(SpaceUtil.fromDirection(top));
        this.tilt = SpaceUtil.round(tiltV, norm);

        updateSize();
        setEntityBoundingBox(bounds);
    }

    public void updateValues() {
        delta_scale = (byte) Math.min(Math.max(delta_scale, -8), +6);
        scale = base_scale * Math.pow(SCALE_INCR, delta_scale);
        Quaternion rNorm = Quaternion.getRotationQuaternionRadians(spin_normal * SPIN_PER_CLICK, norm);
        Quaternion rVert = Quaternion.getRotationQuaternionRadians(spin_vertical * SPIN_PER_CLICK, top);
        Quaternion rTilt = Quaternion.getRotationQuaternionRadians(spin_tilt * SPIN_PER_CLICK, tilt);
        rot = rVert.multiply(rNorm).multiply(rTilt).multiply(base_rotation);
    }

    @SideOnly(Side.CLIENT)
    public boolean canBeCollidedWith() {
        // Only collide with the poster on the client side
        if (!world.isRemote) return false;

        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player != null) {
            if (!locked || player.isCreative()) {
                if (inv.getItem() == ModCharsetDecoration.posterItem) return true;
                if (player.isSneaking()) return true;
                ItemStack held = player.getHeldItem(EnumHand.MAIN_HAND);
                return !held.isEmpty() && (isItemTilting(held) || isItemRotating(held) || isItemScaling(held));
            }
        }

        return false;
    }

    public boolean hitByEntity(Entity ent) {
        if (!world.isRemote && ent instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) ent;
            if (!locked || player.isCreative()) {
                ItemStack held = player.getHeldItem(EnumHand.MAIN_HAND);
                if (!held.isEmpty() && (isItemTilting(held) || isItemRotating(held) || isItemScaling(held))) {
                    return false;
                } else if (spin_normal != 0 || spin_vertical != 0 || spin_tilt != 0) {
                    spin_normal = spin_vertical = spin_tilt = 0;
                } else if (delta_scale != 0) {
                    delta_scale = 0;
                } else {
                    ItemStack droppedItem = inv;
                    inv = new ItemStack(ModCharsetDecoration.posterItem);
                    if (droppedItem.getItem() == ModCharsetDecoration.posterItem) {
                        setDead();
                    } else {
                        syncData();
                    }
                    if (player.capabilities.isCreativeMode) return false;
                    Entity newItem = ItemUtils.spawnItemEntity(getEntityWorld(), getPositionVector(), droppedItem, 0, 0, 0, 0);
                    if (newItem instanceof EntityItem) {
                        EntityItem ei = (EntityItem) newItem;
                        ei.setNoPickupDelay();
                    }
                    newItem.onCollideWithPlayer(player);
                    return true;
                }
                updateValues();
                syncData();
                return true;
            }
        }

        return false;
    }

    public void syncData() {
        NBTTagCompound compound = new NBTTagCompound();
        writeEntityToNBT(compound);
        dataManager.set(PARAMETER_TAG, compound);
    }

    @Override
    protected void entityInit() {
        NBTTagCompound compound = new NBTTagCompound();
        dataManager.register(PARAMETER_TAG, compound);
    }

    private static final double SCALE_INCR = 1.125;
    private static final double SPIN_PER_CLICK = Math.PI * 2 / 32;

    public boolean isItemRotating(ItemStack held) {
        return held.getItem() == ModCharsetDecoration.posterItem
                || held.getItem().getToolClasses(held).contains("wrench");
    }

    public boolean isItemTilting(ItemStack held) {
        return ItemUtils.isOreType(held, "stickWood");
    }

    public boolean isItemScaling(ItemStack held) {
        return ItemUtils.equalsMeta(inv, held);
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        if (locked) return false;
        if (world.isRemote) return true;
        ItemStack held = player.getHeldItem(hand);
        if (held.isEmpty()) return false;
        if (inv.getItem() == ModCharsetDecoration.posterItem) {
            if (held.getItem() == ModCharsetDecoration.posterItem) return true;
            inv = held.splitStack(1);
            syncData();
            return true;
        }
        int d = player.isSneaking() ? -1 : +1;
        if (isItemScaling(held)) {
            delta_scale += d;
            updateValues();
            syncData();
            return true;
        }
        final boolean shouldTilt = isItemTilting(held);
        final boolean shouldRotate = isItemRotating(held);
        if (shouldRotate || shouldTilt) {
            EnumFacing clickDir = SpaceUtil.determineOrientation(player);
            if (shouldRotate) {
                if (clickDir == norm || clickDir == norm.getOpposite()) {
                    spin_normal -= d;
                } else {
                    spin_vertical += d;
                }
            } else {
                if (clickDir == norm || clickDir == norm.getOpposite()) {
                    spin_tilt += d;
                } else {
                    spin_vertical += d;
                }
            }
            updateValues();
            syncData();
            return true;
        }
        return super.processInitialInteract(player, hand);
    }

    @Override
    public ItemStack getPickedResult(RayTraceResult target) {
        return inv.copy();
    }

    @Override
    public int getBrightnessForRender(float partial) {
        EnumFacing front = norm;
        double dx = 0, dy = 0, dz = 0;
        if (front == EnumFacing.DOWN) {
            dy -= 1;
        } else if (front == EnumFacing.NORTH) {
            dz -= 1;
        } else if (front == EnumFacing.WEST) {
            dx = -1;
        }
        int x = MathHelper.floor(posX + dx);
        int y = MathHelper.floor(posY + dy);
        int z = MathHelper.floor(posZ + dz);
        BlockPos blockpos = new BlockPos(x, y, z);
        return world.getCombinedLight(blockpos, 0);
    }

    @Override
    public boolean isInRangeToRenderDist(double dist) {
        return dist < 32 * 32;
    }
}
