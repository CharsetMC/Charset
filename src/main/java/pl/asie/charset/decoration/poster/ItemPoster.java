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

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import pl.asie.charset.decoration.ModCharsetDecoration;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.factorization.Quaternion;
import pl.asie.charset.lib.factorization.SpaceUtil;
import pl.asie.charset.lib.items.ItemBase;

import java.util.ArrayList;

public class ItemPoster extends ItemBase {
    public static class PosterPlacer {
        private ItemStack is;
        private EntityPlayer player;
        private World w;
        private EnumFacing dir;

        final BlockPos at;
        AxisAlignedBB blockBox = null;

        public EntityPoster result;

        double bestWidth;
        AxisAlignedBB bounds, plane;
        Quaternion rot;
        EnumFacing top;

        public PosterPlacer(ItemStack is, EntityPlayer player, World w, BlockPos pos, EnumFacing side) {
            this.is = is;
            this.player = player;
            this.w = w;
            this.dir = side;
            this.at = pos;
        }

        public boolean calculate() {
            if (determineBoundingBox()) return true;
            if (determineSize()) return true;
            determineOrientation();

            // Create the thing
            EntityPoster poster = new EntityPoster(w);
            poster.setBase(bestWidth, rot, dir, top, bounds);
            final Vec3d spot = SpaceUtil.getMiddle(plane);
            if (SpaceUtil.sign(dir) == -1) {
                spot.add(SpaceUtil.scale(SpaceUtil.fromDirection(dir), 1.0 / 2560.0));
            }
            SpaceUtil.toEntPos(poster, spot);
            result = poster;
            return false;
        }

        private void determineOrientation() {
            // Determine rotations & orientations
            double rotationAngle = 0;
            if (dir.getDirectionVec().getY() == 0) {
                top = EnumFacing.UP;
                if (dir == EnumFacing.WEST) rotationAngle = 1;
                if (dir == EnumFacing.SOUTH) rotationAngle = 2;
                if (dir == EnumFacing.EAST) rotationAngle = 3;
            } else {
                top = EnumFacing.WEST;
                rotationAngle = -dir.getDirectionVec().getY();
            }
            rot = Quaternion.getRotationQuaternionRadians(rotationAngle * Math.PI / 2, top);
        }

        private boolean determineSize() {
            // Setup box areas
            plane = SpaceUtil.flatten(blockBox, dir);

            final double pix = 1.0 / 16.0;
            bounds = SpaceUtil.addCoord(plane, SpaceUtil.scale(new Vec3d(dir.getDirectionVec()), pix));

            for (Object ent : w.getEntitiesWithinAABB(EntityPoster.class, bounds)) {
                if (ent instanceof EntityPoster) {
                    EntityPoster poster = (EntityPoster) ent;
                    if (poster.inv != null && poster.inv.getItem() == ModCharsetDecoration.posterItem) {
                        return true;
                    }
                    // Allow multiple posters if there are no empty ones
                } else if (ent instanceof EntityItemFrame) {
                    // There may be a reasonable use for this
                    continue;
                } else {
                    return true;
                }
            }
            final double xwidth = plane.maxX - plane.minX;
            final double ywidth = plane.maxY - plane.minY;
            final double zwidth = plane.maxZ - plane.minZ;
            bestWidth = SpaceUtil.getDiagonalLength(plane);

            if (xwidth != 0) bestWidth = xwidth;
            if (ywidth != 0 && ywidth < bestWidth) bestWidth = ywidth;
            if (zwidth != 0 && zwidth < bestWidth) bestWidth = zwidth;
            if (bestWidth <= 2.0 / 16.0) return true;
            return false;
        }

        private boolean determineBoundingBox() {
            // Determine what the box should be. Ray tracing for multi-box blocks; fallbacks to the selection bounding box.

            final ArrayList<AxisAlignedBB> boxes = new ArrayList<AxisAlignedBB>();
            final AxisAlignedBB query = new AxisAlignedBB(at.add(-9, -9, -9), at.add(+9, +9, +9));
            final IBlockState state = w.getBlockState(at);
            state.addCollisionBoxToList(w, at, query, boxes, player);

            final Vec3d playerEye = SpaceUtil.fromPlayerEyePos(player);
            Vec3d look = player.getLookVec();
            look = SpaceUtil.scale(look, 8);
            final Vec3d reachEnd = look.add(playerEye);

            double minDist = Double.POSITIVE_INFINITY;
            for (AxisAlignedBB box : boxes) {
                RayTraceResult mop = box.calculateIntercept(playerEye, reachEnd);
                if (mop == null) continue;
                if (mop.typeOfHit != RayTraceResult.Type.BLOCK) continue;
                if (mop.hitVec == null) continue;
                double vecLen = mop.hitVec.lengthVector();
                if (vecLen > minDist) continue;
                minDist = vecLen;
                dir = mop.sideHit;
                blockBox = box;
            }

            if (blockBox == null) blockBox = state.getCollisionBoundingBox(w, at);
            if (blockBox == null) {
                RayTraceResult mop = state.collisionRayTrace(w, at, playerEye, reachEnd);
                if (mop != null) {
                    // Oh, look, the mop doesn't actually help us! Let's just act like this block's like BlockTorch and sets its bounds idiotically like it does
                    blockBox = state.getBoundingBox(w, at);
                }
            }
            //Client-side only: if (blockBox == null) blockBox = at.getSelectedBoundingBoxFromPool();
            if (blockBox == null) return true;
            return false;
        }

        public void spawn() {
            w.spawnEntityInWorld(result);
            result.syncData();

            if (!player.capabilities.isCreativeMode) {
                is.stackSize--;
            }
        }
    }

    public ItemPoster() {
        super();
        setCreativeTab(ModCharsetLib.CREATIVE_TAB);
        setUnlocalizedName("charset.poster");
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        final PosterPlacer placer = new PosterPlacer(stack, playerIn, worldIn, pos, facing);
        if (placer.calculate()) return EnumActionResult.FAIL;
        if (!worldIn.isRemote) {
            placer.spawn();
        }
        return EnumActionResult.SUCCESS;
    }
}
