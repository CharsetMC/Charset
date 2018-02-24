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

import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.slot.EnumCenterSlot;
import mcmultipart.api.slot.EnumFaceSlot;
import mcmultipart.api.slot.IPartSlot;
import mcmultipart.api.world.IMultipartWorld;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.block.BlockBase;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class BlockWire extends BlockBase implements IMultipart, ITileEntityProvider {
    protected static final PropertyBool REDSTONE = PropertyBool.create("redstone");

    public BlockWire() {
        super(Material.CIRCUITS);
        setHardness(0.0f);
        setOpaqueCube(false);
        setFullCube(false);
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return state.getValue(REDSTONE);
    }

    @Override
    public boolean canConnectRedstone(IBlockState blockState, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
        if (blockState.getValue(REDSTONE)) {
            Wire wire = WireUtils.getAnyWire(world, pos);
            if (wire != null) {
                return wire.canConnectRedstone(side);
            }
        }

        return false;
    }

    @Override
    public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        if (blockState.getValue(REDSTONE)) {
            Wire wire = WireUtils.getAnyWire(blockAccess, pos);
            if (wire != null) {
                return wire.getStrongPower(side);
            }
        }

        return super.getStrongPower(blockState, blockAccess, pos, side);
    }

    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        if (blockState.getValue(REDSTONE)) {
            Wire wire = WireUtils.getAnyWire(blockAccess, pos);
            if (wire != null) {
                return wire.getWeakPower(side);
            }
        }

        return super.getWeakPower(blockState, blockAccess, pos, side);
    }

    @Override
    public boolean shouldCheckWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        if (blockState.getValue(REDSTONE)) {
            Wire wire = WireUtils.getAnyWire(blockAccess, pos);
            if (wire != null) {
                return wire.shouldCheckWeakPower(side);
            }
        }

        return false;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileWire();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(REDSTONE) ? 1 : 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(REDSTONE, meta > 0);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[] { REDSTONE }, new IUnlistedProperty[]{Wire.PROPERTY});
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        Wire wire = WireUtils.getAnyWire(worldIn, pos);
        if (wire != null) {
            int connMask = wire.getConnectionMask();

            super.breakBlock(worldIn, pos, state);
            requestNeighborUpdate(worldIn, pos, wire.getLocation(), connMask);
        } else {
            super.breakBlock(worldIn, pos, state);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return ((IExtendedBlockState) state).withProperty(Wire.PROPERTY, WireUtils.getAnyWire(world, pos));
    }

    @Override
    public List<AxisAlignedBB> getOcclusionBoxes(IPartInfo part) {
        Wire wire = WireUtils.getAnyWire(part.getPartWorld(), part.getPartPos());
        if (wire != null) {
            return Collections.singletonList(wire.getFactory().getSelectionBox(wire.getLocation(), 0));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public RayTraceResult collisionRayTrace(IPartInfo part, Vec3d start, Vec3d end) {
        Wire wire = WireUtils.getAnyWire(part.getTile().getTileEntity());
        if (wire != null) {
            RayTraceResult result = rayTrace(part.getPartPos(), start, end, wire.getFactory().getSelectionBox(wire.getLocation(), 0));
            if (result != null) {
                result.hitInfo = part;
                return result;
            }

            EnumFacing[] faces = WireUtils.getConnectionsForRender(wire.getLocation());
            for (int i = 0; i < faces.length; i++) {
                EnumFacing face = faces[i];
                if (wire.connectsAny(face)) {
                    result = rayTrace(part.getPartPos(), start, end, wire.getFactory().getSelectionBox(wire.getLocation(), i + 1));
                    if (result != null) {
                        result.hitInfo = part;
                        return result;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        Wire wire = WireUtils.getAnyWire(source, pos);
        if (wire != null) {
            return wire.getFactory().getSelectionBox(wire.getLocation(), 0);
        } else {
            return FULL_BLOCK_AABB;
        }
    }

    public static WireFace getFace(IPartSlot slot) {
        return slot instanceof EnumFaceSlot ? WireFace.get(((EnumFaceSlot) slot).getFacing()) : WireFace.CENTER;
    }

    @Override
    public void onPartPlacedBy(IPartInfo part, EntityLivingBase placer, ItemStack stack) {
        ((TileWire) part.getTile().getTileEntity()).onPlacedBy(getFace(part.getSlot()), stack);
    }

    @Override
    public void onPartChanged(IPartInfo part, IPartInfo otherPart) {
        if (part != otherPart) {
            Wire wire = WireUtils.getAnyWire(part.getPartWorld(), part.getPartPos());
            if (wire != null) {
                wire.onChanged(false);
            }
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        Wire wire = WireUtils.getAnyWire(worldIn, pos);
        if (wire != null) {
            wire.onChanged(true);
        }
    }

    @Override
    public IPartSlot getSlotForPlacement(World world, BlockPos pos, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ, EntityLivingBase placer) {
        ItemStack stack = placer.getHeldItemMainhand();
        WireFace location = (stack.getMetadata() & 1) != 0 ? WireFace.CENTER : WireFace.get(facing.getOpposite());
        return WireUtils.toPartSlot(location);
    }

    @Override
    public IPartSlot getSlotFromWorld(IBlockAccess world, BlockPos pos, IBlockState state) {
        Wire wire = WireUtils.getAnyWire(world, pos);
        if (wire != null) {
            return WireUtils.toPartSlot(wire.getLocation());
        } else {
            return EnumCenterSlot.CENTER;
        }
    }

    public void requestNeighborUpdate(World world, BlockPos pos, WireFace location, int connectionMask) {
        if ((connectionMask & 0xFF) != 0 && world instanceof IMultipartWorld) {
            IPartInfo info = ((IMultipartWorld) world).getPartInfo();
            info.getContainer().notifyChange(info);
        }

        for (EnumFacing facing : EnumFacing.VALUES) {
            // TODO: figure out bug
            // if (location != WireFace.CENTER && (connectionMask & (1 << (facing.ordinal() + 16))) != 0) {

            if (world.isRemote) {
                if ((connectionMask & (1 << (facing.ordinal() + 8))) != 0) {
                    WireUtils.getAllWires(world, pos.offset(facing)).forEach((wire) -> wire.onChanged(true));
                } else if (location != WireFace.CENTER && location.facing.getAxis() != facing.getAxis()) {
                    WireUtils.getAllWires(world, pos.offset(facing).offset(location.facing)).forEach((wire) -> wire.onChanged(true));
                }
            } else {
                if ((connectionMask & (1 << (facing.ordinal() + 8))) != 0) {
                    world.neighborChanged(pos.offset(facing), this, pos);
                } else if (location != WireFace.CENTER && location.facing.getAxis() != facing.getAxis()) {
                    world.neighborChanged(pos.offset(facing).offset(location.facing), this, pos);
                }
            }
        }
    }

}
