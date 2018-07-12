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

package pl.asie.charset.lib.wires;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.registries.IForgeRegistryEntry;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.utils.RotationUtils;

public abstract class WireProvider implements IForgeRegistryEntry<WireProvider> {
    private final AxisAlignedBB[] boxes = new AxisAlignedBB[37];
    private final AxisAlignedBB[] cornerBoxes = new AxisAlignedBB[24];
    private ResourceLocation name;
    private ItemWire iw;

    public WireProvider() {
    }

    public final boolean hasItemWire() {
        return iw != null;
    }

    public final ItemWire getItemWire() {
        return iw;
    }

    final void setItemWire(ItemWire i) {
        if (iw == null) {
            iw = i;
        }
    }

    protected final void generateBoxes() {
        if (boxes[0] != null) {
            return;
        }

        // Generate boxes
        float xMin = 0.5f - getWidth() / 2;
        float xMax = 0.5f + getWidth() / 2;
        float y = getHeight();

        for (int j = 0; j < 6; j++) {
            EnumFacing f = EnumFacing.getFront(j);
            EnumFacing[] faces = WireUtils.getConnectionsForRender(WireFace.get(f));
            for (int i = 0; i < faces.length; i++) {
                if (i >= 2) {
                    if (faces[i].getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
                        boxes[j * 5 + i + 1] = RotationUtils.rotateFace(new AxisAlignedBB(0, 0, xMin, xMin, y, xMax), f);
                        cornerBoxes[j * 4 + i] = RotationUtils.rotateFace(new AxisAlignedBB(0, 0, xMin, y, y, xMax), f);
                    } else {
                        boxes[j * 5 + i + 1] = RotationUtils.rotateFace(new AxisAlignedBB(xMax, 0, xMin, 1, y, xMax), f);
                        cornerBoxes[j * 4 + i] = RotationUtils.rotateFace(new AxisAlignedBB(1 - y, 0, xMin, 1, y, xMax), f);
                    }
                } else {
                    if (faces[i].getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
                        boxes[j * 5 + i + 1] = RotationUtils.rotateFace(new AxisAlignedBB(xMin, 0, 0, xMax, y, xMin), f);
                        cornerBoxes[j * 4 + i] = RotationUtils.rotateFace(new AxisAlignedBB(xMin, 0, 0, xMax, y, y), f);
                    } else {
                        boxes[j * 5 + i + 1] = RotationUtils.rotateFace(new AxisAlignedBB(xMin, 0, xMax, xMax, y, 1), f);
                        cornerBoxes[j * 4 + i] = RotationUtils.rotateFace(new AxisAlignedBB(xMin, 0, 1 - y, xMax, y, 1), f);
                    }
                }
            }
            boxes[j * 5] = RotationUtils.rotateFace(new AxisAlignedBB(xMin, 0, xMin, xMax, y, xMax), f);
            boxes[31 + j] = RotationUtils.rotateFace(new AxisAlignedBB(xMin, 0, xMin, xMax, xMin, xMax), f);
        }
        boxes[30] = new AxisAlignedBB(xMin, xMin, xMin, xMax, xMax, xMax);
    }

    public abstract Wire create(IWireContainer container, WireFace location);

    public boolean canProvidePower() {
        return false;
    }

    public boolean canPlace(IBlockAccess access, BlockPos pos, WireFace face) {
        if (face == WireFace.CENTER) {
            return hasFreestandingWire();
        } else {
            return hasSidedWire() && access.isSideSolid(pos.offset(face.facing), face.facing.getOpposite(), false);
        }
    }

    public AxisAlignedBB getBox(WireFace location, int i) {
        return boxes[location.ordinal() * 5 + i];
    }

    public AxisAlignedBB getSelectionBox(WireFace location, int i) {
        return getBox(location, i);
    }

    public AxisAlignedBB getCornerCollisionBox(WireFace location, EnumFacing facing) {
        EnumFacing[] facings = WireUtils.getConnectionsForRender(location);
        for (int i = 0; i < facings.length; i++) {
            if (facing == facings[i]) {
                return getCornerBox(location, i);
            }
        }

        return null; // !?
    }

    public AxisAlignedBB getCornerBox(WireFace location, int i) {
        return cornerBoxes[location.ordinal() * 4 + i];
    }

    public abstract float getWidth(); /* 0...1 */
    public abstract float getHeight(); /* 0...1 */
    public abstract ResourceLocation getTexturePrefix();

    public boolean hasSidedWire() {
        return true;
    }

    public boolean hasFreestandingWire() {
        return !isFlat();
    }

    public boolean isFlat() {
        return false;
    }

    @Override
    public WireProvider setRegistryName(ResourceLocation name) {
        this.name = name;
        return this;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return name;
    }

    @Override
    public Class<WireProvider> getRegistryType() {
        return WireProvider.class;
    }
}
