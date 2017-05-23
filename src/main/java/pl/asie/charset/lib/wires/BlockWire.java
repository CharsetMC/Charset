package pl.asie.charset.lib.wires;

import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.slot.EnumCenterSlot;
import mcmultipart.api.slot.EnumFaceSlot;
import mcmultipart.api.slot.IPartSlot;
import mcmultipart.api.world.IMultipartWorld;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
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
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.ModCharset;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.blocks.BlockBase;
import pl.asie.charset.lib.blocks.TileBase;
import pl.asie.charset.lib.utils.RayTraceUtils;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class BlockWire extends BlockBase implements IMultipart, ITileEntityProvider {
    public BlockWire() {
        super(Material.CIRCUITS);
        setOpaqueCube(false);
        setFullCube(false);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileWire();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[0], new IUnlistedProperty[]{Wire.PROPERTY});
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        Wire wire = WireUtils.getAnyWire(worldIn, pos);
        int connMask = wire.getConnectionMask();

        super.breakBlock(worldIn, pos, state);
        requestNeighborUpdate(worldIn, pos, wire.getLocation(), connMask);
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
        RayTraceResult result = part.getState().collisionRayTrace(part.getPartWorld(), part.getPartPos(), start, end);
        if (result != null) result.hitInfo = part;
        return result;
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

            if ((connectionMask & (1 << (facing.ordinal() + 8))) != 0) {
                world.neighborChanged(pos.offset(facing), this, pos);
            } else if (location != WireFace.CENTER && location.facing.getAxis() != facing.getAxis()) {
                world.neighborChanged(pos.offset(facing).offset(location.facing), this, pos);
            }
        }
    }

}
