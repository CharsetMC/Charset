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

import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.RayTraceUtils;

public class BlockWire extends BlockContainer {
	public BlockWire() {
		super(Material.circuits);
		this.setBlockBounds(0, 0, 0, 1.0f, 0.125f, 1.0f);
		this.setCreativeTab(ModCharsetLib.CREATIVE_TAB);
		this.setUnlocalizedName("charset.wire");
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos, EntityPlayer player) {
		TileWireContainer wire = getWire(world, pos);

		if (wire != null) {
			return new ItemStack(this, 1, wire.getItemMetadata(WireFace.VALUES[target.subHit]));
		}

		return new ItemStack(this);
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

	private TileWireContainer getWire(IBlockAccess world, BlockPos pos) {
		TileEntity tileEntity = world.getTileEntity(pos);
		return tileEntity instanceof TileWireContainer ? (TileWireContainer) tileEntity : null;
	}

	private List<AxisAlignedBB> getBoxList(World worldIn, BlockPos pos) {
		TileWireContainer wire = getWire(worldIn, pos);
		List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();

		boolean noWires;

		if (wire != null) {
			noWires = !wire.hasWires();
			// The freestanding box is reused for the potential bug
			// when a wire container with no internal remains.

			list.add(wire.hasWire(WireFace.DOWN) ? new AxisAlignedBB(0, 0, 0, 1, WireUtils.getWireHitboxHeight(wire, WireFace.DOWN), 1) : null);
			list.add(wire.hasWire(WireFace.UP) ? new AxisAlignedBB(0, 1 - WireUtils.getWireHitboxHeight(wire, WireFace.UP), 0, 1, 1, 1) : null);
			list.add(wire.hasWire(WireFace.NORTH) ? new AxisAlignedBB(0, 0, 0, 1, 1, WireUtils.getWireHitboxHeight(wire, WireFace.NORTH)) : null);
			list.add(wire.hasWire(WireFace.SOUTH) ? new AxisAlignedBB(0, 0, 1 - WireUtils.getWireHitboxHeight(wire, WireFace.SOUTH), 1, 1, 1) : null);
			list.add(wire.hasWire(WireFace.WEST) ? new AxisAlignedBB(0, 0, 0, WireUtils.getWireHitboxHeight(wire, WireFace.WEST), 1, 1) : null);
			list.add(wire.hasWire(WireFace.EAST) ? new AxisAlignedBB(1 - WireUtils.getWireHitboxHeight(wire, WireFace.EAST), 0, 0, 1, 1, 1) : null);
			if (wire.hasWire(WireFace.CENTER)) {
				switch (wire.getWireKind(WireFace.CENTER).type()) {
					case NORMAL:
					case INSULATED:
						list.add(new AxisAlignedBB(0.375, 0.375, 0.375, 0.625, 0.625, 0.625));
						break;
					case BUNDLED:
						list.add(new AxisAlignedBB(0.25, 0.25, 0.25, 0.75, 0.75, 0.75));
						break;
				}
			} else {
				list.add(null);
			}
		} else {
			noWires = true;
		}

		if (noWires) {
			list.add(new AxisAlignedBB(0.375, 0.375, 0.375, 0.625, 0.625, 0.625));
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
		TileWireContainer wire = getWire(world, pos);

		if (wire != null && wire.hasWires()) {
			RayTraceUtils.Result r = RayTraceUtils.getCollision(world, pos, player, getBoxList(world, pos));

			if (r.valid()) {
				wire.dropWire(WireFace.VALUES[r.hit.subHit], player);
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
		return new TileWireContainer();
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
		TileWireContainer wire = getWire(world, pos);

		if (wire != null) {
			return wire.canProvideStrongPower(side.getOpposite()) ? wire.getRedstoneLevel(side.getOpposite()) : 0;
		}

		return 0;
	}

	@Override
	public int getWeakPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
		TileWireContainer wire = getWire(world, pos);

		if (wire != null) {
			return wire.canProvideWeakPower(side.getOpposite()) ? wire.getRedstoneLevel(side.getOpposite()) : 0;
		}

		return 0;
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
		TileWireContainer wire = getWire(world, pos);

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
			TileWireContainer wire = getWire(world, pos);

			if (wire != null) {
				return ((IExtendedBlockState) state).withProperty(TileWireContainer.PROPERTY, wire);
			}
		}
		return state;
	}

	@Override
	protected BlockState createBlockState() {
		return new ExtendedBlockState(this, new IProperty[0], new IUnlistedProperty[]{TileWireContainer.PROPERTY});
	}

	@Override
	public boolean canProvidePower() {
		return true;
	}

	@Override
	public boolean canConnectRedstone(IBlockAccess world, BlockPos pos, EnumFacing side) {
		if (side != null) {
			TileWireContainer wire = getWire(world, pos);

			if (wire != null) {
				return wire.providesSignal(side);
			}
		}

		return false;
	}
}
