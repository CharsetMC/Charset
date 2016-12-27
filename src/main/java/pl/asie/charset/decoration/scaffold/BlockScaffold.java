package pl.asie.charset.decoration.scaffold;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import pl.asie.charset.decoration.ModCharsetDecoration;
import pl.asie.charset.lib.blocks.BlockBase;
import pl.asie.charset.lib.blocks.TileBase;
import pl.asie.charset.lib.utils.ItemUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class BlockScaffold extends BlockBase implements ITileEntityProvider {
	public static final Collection<ItemStack> PLANKS = new HashSet<>();
	private static final int MAX_OVERHANG = 8;
	private static final AxisAlignedBB COLLISION_BOX_SIDES = new AxisAlignedBB(0, 0, 0, 1, 1, 1).expand(-0.3125, 0, -0.3125);
	private static final AxisAlignedBB COLLISION_BOX_TOP = new AxisAlignedBB(-0.0625, 1 - 0.0625, -0.0625, 1 + 0.0625, 1, 1 + 0.0625);

	public BlockScaffold() {
		super(Material.WOOD);
		setHardness(1.0F);
		setHarvestLevel("axe", 0);
		setSoundType(SoundType.WOOD);
		setUnlocalizedName("charset.scaffold");
	}

	public static ItemStack createStack(ItemStack plank, int stackSize) {
		ItemStack scaffold = new ItemStack(ModCharsetDecoration.scaffoldBlock, stackSize);
		scaffold.setTagCompound(new NBTTagCompound());
		ItemUtils.writeToNBT(plank, scaffold.getTagCompound(), "plank");
		return scaffold;
	}

	@Override
	protected Collection<ItemStack> getCreativeItems() {
		return ImmutableList.of();
	}

	@Override
	protected List<Collection<ItemStack>> getCreativeItemSets() {
		List<Collection<ItemStack>> list = new ArrayList<>();
		for (ItemStack s : PLANKS) {
			list.add(ImmutableList.of(createStack(s, 1)));
		}
		return list;
	}

	private boolean canStay(IBlockAccess world, BlockPos pos) {
		return canStay(world, pos, 0);
	}

	private boolean canStay(IBlockAccess world, BlockPos pos, int overhang) {
		if (overhang >= MAX_OVERHANG)
			return false;

		if (!world.isAirBlock(pos.down()))
			return true;

		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			BlockPos pos1 = pos.offset(facing);

			if (world.getBlockState(pos1).getBlock() == this && canStay(world, pos1, overhang + 1))
				return true;
		}

		return false;
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_184something) {
   	    if (entityBox.intersectsWith(COLLISION_BOX_SIDES.offset(pos)))
   	    	collidingBoxes.add(COLLISION_BOX_SIDES.offset(pos));

		if (!(entityIn instanceof EntityLivingBase) || !((EntityLivingBase) entityIn).isOnLadder()) {
			if (pos.getY() + 0.9 <= entityBox.minY) {
				if (entityBox.intersectsWith(COLLISION_BOX_TOP.offset(pos)))
					collidingBoxes.add(COLLISION_BOX_TOP.offset(pos));
			}
		}
	}

	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		return canStay(worldIn, pos) && super.canPlaceBlockAt(worldIn, pos);
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		if (!canStay(worldIn, pos)) {
			ItemStack droppedStack = new ItemStack(this);
			TileEntity tile = worldIn.getTileEntity(pos);
			if (tile instanceof TileBase) {
				droppedStack = ((TileBase) tile).getDroppedBlock();
			}
			ItemUtils.spawnItemEntity(worldIn, new Vec3d(pos).addVector(0.5, 0.5, 0.5), droppedStack, 0.1f, 0.1f, 0.1f, 1.0f);
			worldIn.setBlockToAir(pos);
		}
	}

	@Override
	public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return side == EnumFacing.UP;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		if (blockAccess.getBlockState(pos.up()).getBlock() == this)
			return false;

		return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
	}

	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
		return false;
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
	public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
		return true;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new ExtendedBlockState(this, new IProperty[]{}, new IUnlistedProperty[]{TileScaffold.PROPERTY});
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileScaffold scaffold = (TileScaffold) world.getTileEntity(pos);
		IExtendedBlockState extendedBS = (IExtendedBlockState) super.getExtendedState(state, world, pos);
		if (scaffold != null) {
			return extendedBS.withProperty(TileScaffold.PROPERTY, ScaffoldCacheInfo.from(scaffold));
		} else {
			return extendedBS;
		}
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileScaffold();
	}
}
