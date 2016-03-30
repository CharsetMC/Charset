package pl.asie.charset.storage.backpack;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.refs.Properties;
import pl.asie.charset.storage.ModCharsetStorage;

/**
 * Created by asie on 1/10/16.
 */
public class BlockBackpack extends BlockContainer {
	public static final int DEFAULT_COLOR = 0x805038;
	private static final AxisAlignedBB AABB = new AxisAlignedBB(0.1875f, 0.0f, 0.1875f, 0.8125f, 0.75f, 0.8125f);

	public BlockBackpack() {
		super(Material.cloth);
		setCreativeTab(ModCharsetLib.CREATIVE_TAB);
		setUnlocalizedName("charset.backpack");
		setHardness(0.8f);
		setSoundType(SoundType.CLOTH);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return AABB;
	}

	@Override
	public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World world, BlockPos pos) {
		if (player.isSneaking()) {
			return -1.0f;
		} else {
			return super.getPlayerRelativeBlockHardness(state, player, world, pos);
		}
	}

	@Override
	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
		if (player.isSneaking() && player.getItemStackFromSlot(EntityEquipmentSlot.CHEST) == null) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileBackpack) {
				ItemStack stack = ((TileBackpack) tile).writeToItemStack();

				world.removeTileEntity(pos);
				world.setBlockToAir(pos);

				player.setItemStackToSlot(EntityEquipmentSlot.CHEST, stack);
			}
		}
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(world, pos, state, placer, stack);

		if (stack.hasTagCompound()) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileBackpack) {
				((TileBackpack) tile).readCustomData(stack.getTagCompound());
			}
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return true;
		}

		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileBackpack) {
			player.openGui(ModCharsetStorage.instance, 1, world, pos.getX(), pos.getY(), pos.getZ());
			return true;
		}

		return false;
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		TileEntity tile = worldIn.getTileEntity(pos);

		if (tile instanceof IInventory) {
			InventoryHelper.dropInventoryItems(worldIn, pos, (IInventory) tile);
		}

		super.breakBlock(worldIn, pos, state);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT_MIPPED;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(Properties.FACING4, placer.getHorizontalFacing());
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, Properties.FACING4);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(Properties.FACING4).ordinal() - 2;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(Properties.FACING4, EnumFacing.getFront((meta & 3) + 2));
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileBackpack();
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	// TODO
	/*
	@SideOnly(Side.CLIENT)
	public int getBlockColor() {
		return DEFAULT_COLOR;
	}

	@SideOnly(Side.CLIENT)
	public int getRenderColor(IBlockState state) {
		return DEFAULT_COLOR;
	}

	@SideOnly(Side.CLIENT)
	public int colorMultiplier(IBlockAccess worldIn, BlockPos pos, int renderPass) {
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof TileBackpack) {
			return ((TileBackpack) tile).getColor();
		} else {
			return DEFAULT_COLOR;
		}
	} */
}
