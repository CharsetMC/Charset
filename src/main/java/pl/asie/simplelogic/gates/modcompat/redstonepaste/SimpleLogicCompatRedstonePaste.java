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

package pl.asie.simplelogic.gates.modcompat.redstonepaste;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.simplelogic.gates.IRedstoneGetter;
import pl.asie.simplelogic.gates.RedstoneGetterHandler;

import java.util.EnumMap;

/**
 * Due to lack of an API on Redstone Paste's side and the BY-NC-ND license
 * potentially forbidding all forms of linking, I had to get a little bit
 * creative.
 *
 * My apologies.
 */
@CharsetModule(
        name = "redstonepaste:simplelogic.gates",
        profile = ModuleProfile.COMPAT,
        dependencies = {"mod:redstonepaste", "simplelogic.gates"}
)
public class SimpleLogicCompatRedstonePaste {
    private static final Table<EnumFacing, EnumFacing, Integer> faceEdgeBit = Tables.newCustomTable(
            new EnumMap<>(EnumFacing.class),
            () -> new EnumMap<>(EnumFacing.class)
    );

    private static final Table<EnumFacing, EnumFacing, Integer> faceEdgeBitTwo = Tables.newCustomTable(
            new EnumMap<>(EnumFacing.class),
            () -> new EnumMap<>(EnumFacing.class)
    );

    private static void initFEB(EnumFacing face, EnumFacing edge, int v, int v2) {
        faceEdgeBit.put(face, edge, v);
        faceEdgeBit.put(face.getOpposite(), edge, v);
        faceEdgeBitTwo.put(face, edge, v2);
        faceEdgeBitTwo.put(face.getOpposite(), edge, v2);
    }

    static {
        initFEB(EnumFacing.DOWN, EnumFacing.NORTH, 1, 2);
        initFEB(EnumFacing.DOWN, EnumFacing.SOUTH, 2, 0);
        initFEB(EnumFacing.DOWN, EnumFacing.WEST, 4, 1);
        initFEB(EnumFacing.DOWN, EnumFacing.EAST, 8, 3);

        initFEB(EnumFacing.WEST, EnumFacing.DOWN, 1, 2);
        initFEB(EnumFacing.WEST, EnumFacing.UP, 2, 0);
        initFEB(EnumFacing.WEST, EnumFacing.NORTH, 4, 1);
        initFEB(EnumFacing.WEST, EnumFacing.SOUTH, 8, 3);

        initFEB(EnumFacing.NORTH, EnumFacing.DOWN, 1, 2);
        initFEB(EnumFacing.NORTH, EnumFacing.UP, 2, 0);
        initFEB(EnumFacing.NORTH, EnumFacing.WEST, 4, 1);
        initFEB(EnumFacing.NORTH, EnumFacing.EAST, 8, 3);
    }

    public static class RedstoneGetterPaste implements IRedstoneGetter {
        @Override
        public int get(IBlockAccess world, BlockPos pos, EnumFacing face, EnumFacing edge) {
            if (edge == null) {
                return -1;
            }

            TileEntity tile = world.getTileEntity(pos);
            if (tile != null && TileEntity.getKey(tile.getClass()).getResourcePath().equals("redstonepastete")) {
                System.out.println(face + " " + edge);

                NBTTagCompound tag = tile.writeToNBT(new NBTTagCompound());
                if (
                    tag.hasKey("facetype", Constants.NBT.TAG_INT_ARRAY) &&
                    tag.hasKey("faces", Constants.NBT.TAG_INT_ARRAY) &&
                    tag.hasKey("facedata", Constants.NBT.TAG_INT_ARRAY)
                ) {
                    int idx = edge.ordinal();
                    int[] facetype = tag.getIntArray("facetype");
                    int[] faces = tag.getIntArray("faces");
                    int[] facedata = tag.getIntArray("facedata");
                    if (facetype.length == 6 && faces.length == 6 && facedata.length == 6) {
                        if (facetype[idx] == 0) {
                            if ((faces[idx] & faceEdgeBit.get(edge, face.getOpposite())) == 0) {
                                return 0;
                            }

                            return facedata[0];
                        } else if (facetype[idx] == 2 || facetype[idx] == 3) {
                            // Call it manually here. For reasons.
                            return world.getBlockState(pos).getWeakPower(
                                    world, pos, face
                            );
                        }
                    }
                }
            }

            return -1;
        }
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        RedstoneGetterHandler.GETTERS.add(new RedstoneGetterPaste());
    }
}
