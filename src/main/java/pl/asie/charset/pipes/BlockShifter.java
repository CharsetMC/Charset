package pl.asie.charset.pipes;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.lib.PropertyConstants;
import pl.asie.charset.pipes.api.IShifter;

public class BlockShifter extends BlockContainer {
	public static final PropertyBool EXTRACT = PropertyBool.create("extract");
	public static final PropertyInteger STRENGTH = PropertyInteger.create("strength", 0, 2);

	public BlockShifter() {
		super(Material.iron);
		setUnlocalizedName("shifter");
		setDefaultState(this.blockState.getBaseState().withProperty(PropertyConstants.FACING, EnumFacing.NORTH));
		setHardness(0.5F);
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileShifter) {
			int[] shiftedCoordinates = new int[6];
			TileShifter shifter = (TileShifter) tileEntity;

			for (EnumFacing side : EnumFacing.VALUES) {
				EnumFacing newSide = side;

				switch (shifter.getDirection()) {
					case DOWN:
						newSide = side.rotateAround(EnumFacing.Axis.X).rotateAround(EnumFacing.Axis.X);
						break;
					case NORTH:
						newSide = side.rotateAround(EnumFacing.Axis.X);
						break;
					case EAST:
						newSide = side.rotateAround(EnumFacing.Axis.X).rotateAround(EnumFacing.Axis.Y);
						break;
					case SOUTH:
						newSide = side.rotateAround(EnumFacing.Axis.X).rotateAround(EnumFacing.Axis.Y).rotateAround(EnumFacing.Axis.Y);
						break;
					case WEST:
						newSide = side.rotateAround(EnumFacing.Axis.X).rotateAround(EnumFacing.Axis.Y).rotateAround(EnumFacing.Axis.Y).rotateAround(EnumFacing.Axis.Y);
						break;
				}

				shiftedCoordinates[side.ordinal()] = newSide.ordinal();
			}

			return state
					.withProperty(EXTRACT, shifter.getMode() == IShifter.Mode.Extract)
					.withProperty(STRENGTH, shifter.getRedstoneLevel() >= 8 ? 2 : (shifter.getRedstoneLevel() > 0 ? 1 : 0))
					.withProperty(PropertyConstants.DOWN, shifter.getFilters()[shiftedCoordinates[0]] != null)
					.withProperty(PropertyConstants.UP, shifter.getFilters()[shiftedCoordinates[1]] != null)
					.withProperty(PropertyConstants.NORTH, shifter.getFilters()[shiftedCoordinates[2]] != null)
					.withProperty(PropertyConstants.SOUTH, shifter.getFilters()[shiftedCoordinates[3]] != null)
					.withProperty(PropertyConstants.WEST, shifter.getFilters()[shiftedCoordinates[4]] != null)
					.withProperty(PropertyConstants.EAST, shifter.getFilters()[shiftedCoordinates[5]] != null);
		} else {
			return state;
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (side == null) {
			return false;
		}

		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileShifter) {
			TileShifter shifter = (TileShifter) tileEntity;

			if (side == shifter.getDirection()) {
				return false;
			}

			ItemStack heldItem = player.getHeldItem();
			if (shifter.getFilters()[side.ordinal()] != null) {
				if (!world.isRemote) {
					shifter.setFilter(side.ordinal(), null);
				}
				return true;
			} else if (heldItem != null) {
				if (!world.isRemote) {
					ItemStack filter = heldItem.copy();
					filter.stackSize = 1;
					shifter.setFilter(side.ordinal(), filter);
				}
				return true;
			}
		}

		return false;
	}

	@Override
	public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		TileEntity entityForward = world.getTileEntity(pos.offset(facing));
		TileEntity entityBackward = world.getTileEntity(pos.offset(facing.getOpposite()));

		if (entityBackward instanceof TilePipe) {
			return this.getStateFromMeta(facing.getOpposite().ordinal());
		} else if (entityForward instanceof TilePipe) {
			return this.getStateFromMeta(facing.ordinal());
		} else {
			for (EnumFacing direction : EnumFacing.VALUES) {
				TileEntity entity = world.getTileEntity(pos.offset(direction));

				if (entity instanceof TilePipe) {
					return this.getStateFromMeta(direction.ordinal());
				}
			}

			return this.getStateFromMeta(facing.ordinal());
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IBlockState getStateForEntityRender(IBlockState state) {
		return this.getDefaultState().withProperty(PropertyConstants.FACING, EnumFacing.UP);
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, new IProperty[]{
				STRENGTH, EXTRACT,
				PropertyConstants.FACING,
				PropertyConstants.DOWN,
				PropertyConstants.UP,
				PropertyConstants.NORTH,
				PropertyConstants.SOUTH,
				PropertyConstants.WEST,
				PropertyConstants.EAST
		});
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(PropertyConstants.FACING, EnumFacing.getFront(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(PropertyConstants.FACING).ordinal();
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileShifter();
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileShifter) {
			((TileShifter) tile).updateRedstoneLevel();
		}
	}

	@Override
	public int getRenderType() {
		return 3;
	}
}
