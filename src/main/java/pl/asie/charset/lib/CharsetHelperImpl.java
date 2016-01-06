package pl.asie.charset.lib;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.MultipartHelper;
import pl.asie.charset.api.lib.CharsetHelper;
import pl.asie.charset.lib.utils.MultipartUtils;

/**
 * Created by asie on 1/6/16.
 */
public class CharsetHelperImpl extends CharsetHelper {
    @Override
    public <T> T getInterface(Class<T> clazz, World world, BlockPos pos, EnumFacing side) {
        return MultipartUtils.getInterface(clazz, world, pos, side);
    }

    @Override
    public <T> T getInterface(Class<T> clazz, World world, BlockPos pos, EnumFacing side, EnumFacing face) {
        return MultipartUtils.getInterface(clazz, world, pos, side, face);
    }

    @Override
    public <T> List<T> getInterfaceList(Class<T> clazz, World world, BlockPos pos) {
        IMultipartContainer container = MultipartHelper.getPartContainer(world, pos);
        List<T> list = new ArrayList<T>();

        if (container == null) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile != null && clazz.isAssignableFrom(tile.getClass())) {
                list.add((T) tile);
            }
        } else {
            for (IMultipart part : container.getParts()) {
                if (clazz.isAssignableFrom(part.getClass())) {
                    list.add((T) part);
                }
            }
        }

        return list;
    }
}
