package pl.asie.charset.wires;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.lib.ModCharsetLib;

public class BlockWire extends BlockContainer {
	public BlockWire() {
		super(Material.circuits);
		this.setCreativeTab(ModCharsetLib.CREATIVE_TAB);
		this.setBlockBounds(0.25F, 0.25F, 0.25F, 0.75F, 0.75F, 0.75F);
		this.setUnlocalizedName("charset.wire");
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
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileWire) {
			((TileWire) tileEntity).onNeighborBlockChange();
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof TileWire) {
			TileWire wire = (TileWire) tileEntity;
			if (!world.isRemote) {
				TileWire.WireSide sidew = player.getCurrentEquippedItem() != null ? TileWire.WireSide.FREESTANDING : TileWire.WireSide.get(side.getOpposite());
				wire.setWire(sidew, !wire.hasWire(sidew));
			}
		}

		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public EnumWorldBlockLayer getBlockLayer() {
		return EnumWorldBlockLayer.CUTOUT;
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (state instanceof IExtendedBlockState) {
			TileEntity tileEntity = world.getTileEntity(pos);

			if (tileEntity instanceof TileWire) {
				return ((IExtendedBlockState) state).withProperty(TileWire.PROPERTY, (TileWire) tileEntity);
			}
		}
		return state;
	}

	@Override
	protected BlockState createBlockState() {
		return new ExtendedBlockState(this, new IProperty[0], new IUnlistedProperty[]{TileWire.PROPERTY});
	}
}
