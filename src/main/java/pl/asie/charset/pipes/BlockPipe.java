package pl.asie.charset.pipes;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockPipe extends BlockContainer {
	private int ImmibisMicroblocks_TransformableBlockMarker;

	// TODO
	@SideOnly(Side.CLIENT)
	public static IIcon[] icons;

	public BlockPipe() {
		super(Material.glass);
		setHardness(0.3F);
		setBlockName("charset.pipe");
	}

	private TilePipe getTilePipe(World world, int x, int y, int z) {
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		return tileEntity instanceof TilePipe ? (TilePipe) tileEntity : null;
	}

	private TilePipe removedPipeTile;

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int m) {
		if (!world.isRemote) {
			removedPipeTile = getTilePipe(world, x, y, z);
		}
		super.breakBlock(world, x, y, z, block, m);
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		if (world.isRemote) {
			return null;
		}

		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
		ret.add(new ItemStack(this));

		TilePipe tilePipe = getTilePipe(world, x, y, z);
		if (tilePipe == null && removedPipeTile != null) {
			tilePipe = removedPipeTile;
			if (tilePipe.xCoord != x || tilePipe.yCoord != y || tilePipe.zCoord != z || tilePipe.getWorldObj() != world) {
				tilePipe = null;
			}

			removedPipeTile = null;
		}

		if (tilePipe != null) {
			for (PipeItem p : tilePipe.getPipeItems()) {
				if (p.isValid()) {
					ret.add(p.getStack());
				}
			}
		}

		return ret;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		TilePipe tilePipe = getTilePipe(world, x, y, z);
		if (tilePipe != null) {
			tilePipe.onNeighborBlockChange();
		}
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return icons[meta & 1];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		icons = new IIcon[2];
		icons[0] = register.registerIcon("charsetpipes:pipe");
		icons[1] = register.registerIcon("charsetpipes:pipe_sided");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderType() {
		return ProxyClient.pipeRender.getRenderId();
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TilePipe();
	}
}
