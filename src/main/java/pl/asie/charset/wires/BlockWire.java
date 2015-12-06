package pl.asie.charset.wires;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.RayTraceUtils;
import pl.asie.charset.wires.internal.WireLocation;

public class BlockWire extends BlockContainer {
	public BlockWire() {
		super(Material.circuits);
		this.setBlockBounds(0, 0, 0, 1.0f, 0.125f, 1.0f);
		this.setCreativeTab(ModCharsetLib.CREATIVE_TAB);
		this.setUnlocalizedName("charset.wire");
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		List<ItemStack> drops = new ArrayList<ItemStack>();
		return drops;
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		super.breakBlock(worldIn, pos, state);

		// Notify extended neighbors (corners!)
		for (EnumFacing facing : EnumFacing.VALUES) {
			worldIn.notifyNeighborsOfStateExcept(pos.offset(facing), state.getBlock(), facing.getOpposite());
		}
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side) {
		return false;
	}

	private TileWire getWire(IBlockAccess world, BlockPos pos) {
		TileEntity tileEntity = world.getTileEntity(pos);
		return tileEntity instanceof TileWire ? (TileWire) tileEntity : null;
	}

	private List<AxisAlignedBB> getBoxList(World worldIn, BlockPos pos) {
		TileWire wire = getWire(worldIn, pos);
		List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();

		if (wire != null) {
			// The freestanding box is reused for the potential bug
			// when a wire container with no internal remains.

			list.add(wire.hasWire(WireLocation.DOWN) ? new AxisAlignedBB(0, 0, 0, 1, 0.125, 1) : null);
			list.add(wire.hasWire(WireLocation.UP) ? new AxisAlignedBB(0, 0.875, 0, 1, 1, 1) : null);
			list.add(wire.hasWire(WireLocation.NORTH) ? new AxisAlignedBB(0, 0, 0, 1, 1, 0.125) : null);
			list.add(wire.hasWire(WireLocation.SOUTH) ? new AxisAlignedBB(0, 0, 0.875, 1, 1, 1) : null);
			list.add(wire.hasWire(WireLocation.WEST) ? new AxisAlignedBB(0, 0, 0, 0.125, 1, 1) : null);
			list.add(wire.hasWire(WireLocation.EAST) ? new AxisAlignedBB(0.875, 0, 0, 1, 1, 1) : null);
			list.add(wire.hasWire(WireLocation.FREESTANDING) || !wire.hasWires() ? new AxisAlignedBB(0.375, 0.375, 0.375, 0.625, 0.625, 0.625) : null);
		}

		return list;
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World worldIn, BlockPos pos, Vec3 start, Vec3 end) {
		RayTraceUtils.Result result = RayTraceUtils.getCollision(worldIn, pos, start, end, getBoxList(worldIn, pos));
		return result.valid() ? result.hit : null;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getSelectedBoundingBox(World worldIn, BlockPos pos) {
		RayTraceUtils.Result result = RayTraceUtils.getCollision(worldIn, pos, Minecraft.getMinecraft().thePlayer, getBoxList(worldIn, pos));
		return result.valid() ? result.box.offset(pos.getX(), pos.getY(), pos.getZ()) : super.getSelectedBoundingBox(worldIn, pos).expand(-0.85F, -0.85F, -0.85F);
	}

	@Override
	public boolean removedByPlayer(World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
		TileWire wire = getWire(world, pos);

		if (wire != null && wire.hasWires()) {
			RayTraceUtils.Result r = RayTraceUtils.getCollision(world, pos, player, getBoxList(world, pos));

			if (r.valid()) {
				wire.dropWire(WireLocation.VALUES[r.hit.subHit], player);
			}

			if (wire.hasWires()) {
				return false;
			}
		}

		super.removedByPlayer(world, pos, player, willHarvest);
		return true;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
		return null;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileWire();
	}

	@Override
	public int getRenderType() {
		return 3;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean isFullCube() {
		return false;
	}

	@Override
	public int getStrongPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
		TileWire wire = getWire(world, pos);

		if (wire != null) {
			return wire.canProvideStrongPower(side.getOpposite()) ? wire.getRedstoneLevel() : 0;
		}

		return 0;
	}

	@Override
	public int getWeakPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
		TileWire wire = getWire(world, pos);

		if (wire != null) {
			return wire.canProvideWeakPower(side.getOpposite()) ? wire.getRedstoneLevel() : 0;
		}

		return 0;
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
		TileWire wire = getWire(world, pos);

		if (wire != null) {
			wire.onNeighborBlockChange();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public EnumWorldBlockLayer getBlockLayer() {
		return EnumWorldBlockLayer.CUTOUT;
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (state instanceof IExtendedBlockState) {
			TileWire wire = getWire(world, pos);

			if (wire != null) {
				return ((IExtendedBlockState) state).withProperty(TileWire.PROPERTY, wire);
			}
		}
		return state;
	}

	@Override
	protected BlockState createBlockState() {
		return new ExtendedBlockState(this, new IProperty[0], new IUnlistedProperty[]{TileWire.PROPERTY});
	}

	@Override
	public boolean canProvidePower() {
		return true;
	}

	@Override
	public boolean canConnectRedstone(IBlockAccess world, BlockPos pos, EnumFacing side) {
		TileWire wire = getWire(world, pos);

		if (wire != null) {
			return wire.providesSignal(side);
		}

		return false;
	}
}
