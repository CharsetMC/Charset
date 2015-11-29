package pl.asie.charset.pipes;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.ForgeDirection;

import pl.asie.charset.pipes.client.RendererShifterBlock;

public class BlockShifter extends BlockContainer {
	private static final ForgeDirection[] FILTER_RENDER_ORDER = {
			ForgeDirection.UP,
			ForgeDirection.DOWN,
			ForgeDirection.NORTH,
			ForgeDirection.EAST,
			ForgeDirection.SOUTH,
			ForgeDirection.WEST
	};

	@SideOnly(Side.CLIENT)
	private IIcon[] icons;

	public BlockShifter() {
		super(Material.iron);
		setHardness(0.5F);
		setBlockName("charset.shifter");
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hx, float hy, float hz) {
		TileEntity tileEntity = world.getTileEntity(x, y, z);

		if (tileEntity instanceof TileShifter) {
			TileShifter shifter = (TileShifter) tileEntity;

			if (side == shifter.getDirection().ordinal()) {
				return false;
			}

			ItemStack heldItem = player.getHeldItem();
			if (shifter.getFilters()[side] != null) {
				if (!world.isRemote) {
					shifter.setFilter(side, null);
				}
				return true;
			} else if (heldItem != null) {
				if (!world.isRemote) {
					ItemStack filter = heldItem.copy();
					filter.stackSize = 1;
					shifter.setFilter(side, filter);
				}
				return true;
			}
		}

		return false;
	}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float hx, float hy, float hz, int meta) {
		ForgeDirection forward = ForgeDirection.getOrientation(side);

		TileEntity entityForward = world.getTileEntity(
				x + forward.offsetX,
				y + forward.offsetY,
				z + forward.offsetZ
		);
		TileEntity entityBackward = world.getTileEntity(
				x - forward.offsetX,
				y - forward.offsetY,
				z - forward.offsetZ
		);

		if (entityBackward instanceof TilePipe) {
			return side ^ 1;
		} else if (entityForward instanceof TilePipe) {
			return side;
		} else {
			for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
				TileEntity entity = world.getTileEntity(
						x + direction.offsetX,
						y + direction.offsetY,
						z + direction.offsetZ
				);

				if (entity instanceof TilePipe) {
					return direction.ordinal();
				}
			}

			return side;
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileShifter();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess access, int x, int y, int z, int side) {
		TileEntity tileEntity = access.getTileEntity(x, y, z);
		int meta = access.getBlockMetadata(x, y, z);

		if (tileEntity instanceof TileShifter) {
			TileShifter shifter = (TileShifter) tileEntity;

			if (side == meta) {
				return icons[1];
			} else if (shifter.getFilters()[side] != null) {
				return icons[5];
			} else if (side == (meta ^ 1)) {
				return shifter.getMode() == TileShifter.Mode.Push ? icons[3] : icons[0];
			} else {
				return shifter.getRedstoneLevel() > 8 ? icons[4] : (shifter.getRedstoneLevel() > 0 ? icons[6] : icons[2]);
			}
		} else {
			return getIcon(side, meta);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		if (side == meta) {
			return icons[1];
		} else if (side == (meta ^ 1)) {
			return icons[0];
		} else {
			return icons[2];
		}
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block b) {
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof TileShifter) {
			((TileShifter) tile).updateRedstoneLevel();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		icons = new IIcon[7];
		icons[0] = register.registerIcon("charsetpipes:shifter_in");
		icons[1] = register.registerIcon("charsetpipes:shifter_out");
		icons[2] = register.registerIcon("charsetpipes:shifter_side");
		icons[3] = register.registerIcon("charsetpipes:shifter_in_push");
		icons[4] = register.registerIcon("charsetpipes:shifter_side_on");
		icons[5] = register.registerIcon("charsetpipes:shifter_in_filtered");
		icons[6] = register.registerIcon("charsetpipes:shifter_side_weak");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderType() {
		return RendererShifterBlock.id();
	}
}
