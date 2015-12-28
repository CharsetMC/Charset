package pl.asie.charset.lib.utils;

import java.util.Iterator;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.MultipartHelper;
import mcmultipart.multipart.PartSlot;

public final class MultipartUtils {
    private MultipartUtils() {

    }

    public static <T> T getInterface(Class<T> clazz, World world, BlockPos pos, EnumFacing side) {
        IMultipartContainer container = MultipartHelper.getPartContainer(world, pos);
        if (container == null) {
            TileEntity tile = world.getTileEntity(pos);
            return tile != null && clazz.isAssignableFrom(tile.getClass()) ? (T) tile : null;
        } else {
            if (side == null) {
                return getInterfaceCenter(clazz, container);
            }

            EnumFacing[] var2 = EnumFacing.VALUES;
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                EnumFacing face = var2[var4];
                if(face != side && face != side.getOpposite()) {
                    T tmp = getInterface(clazz, container, side, face);
                    if (tmp != null) {
                        return tmp;
                    }
                }
            }

            return null;
        }
    }

    public static <T> T getInterfaceCenter(Class<T> clazz, IMultipartContainer container) {
        ISlottedPart part = container.getPartInSlot(PartSlot.CENTER);
        if (part != null && clazz.isAssignableFrom(part.getClass())) {
            return (T) part;
        } else {
            Iterator var4 = container.getParts().iterator();

            IMultipart p;
            do {
                do {
                    if(!var4.hasNext()) {
                        return null;
                    }

                    p = (IMultipart)var4.next();
                } while(p instanceof ISlottedPart && !((ISlottedPart)p).getSlotMask().isEmpty());
            } while(p == null || !clazz.isAssignableFrom(p.getClass()));

            return (T) p;
        }
    }

    public static <T> T getInterface(Class<T> clazz, IMultipartContainer container, EnumFacing side, EnumFacing face) {
        if (container == null) {
            return null;
        } else if (side == null) {
            return getInterfaceCenter(clazz, container);
        }

        ISlottedPart part = container.getPartInSlot(PartSlot.getFaceSlot(side));
        if (part != null) {
            return clazz.isAssignableFrom(part.getClass()) ? (T) part : null;
        } else {
            part = container.getPartInSlot(PartSlot.getEdgeSlot(side, face));
            if (part != null) {
                return clazz.isAssignableFrom(part.getClass()) ? (T) part : null;
            } else {
                part = container.getPartInSlot(PartSlot.getFaceSlot(face));
                if (part != null) {
                    return clazz.isAssignableFrom(part.getClass()) ? (T) part : null;
                } else {
                    return getInterfaceCenter(clazz, container);
                }
            }
        }
    }

    public static <T> T getInterface(Class<T> clazz, World world, BlockPos pos, EnumFacing side, EnumFacing face) {
        IMultipartContainer container = MultipartHelper.getPartContainer(world, pos);
        if (container == null) {
            TileEntity tile = world.getTileEntity(pos);
            return tile != null && clazz.isAssignableFrom(tile.getClass()) ? (T) tile : null;
        } else {
            if (side == null) {
                return getInterfaceCenter(clazz, container);
            }

            return getInterface(clazz, container, side, face);
        }
    }
}
