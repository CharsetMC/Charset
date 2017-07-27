package pl.asie.charset.module.misc.scaffold;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
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
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import pl.asie.charset.lib.block.BlockBase;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.lib.item.ISubItemProvider;
import pl.asie.charset.lib.item.SubItemProviderCache;
import pl.asie.charset.lib.item.SubItemProviderRecipes;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.utils.ItemUtils;

import javax.annotation.Nullable;
import java.util.List;

public class BlockScaffold extends BlockBase implements ITileEntityProvider {
	private static final int MAX_OVERHANG = 8;
	private static final AxisAlignedBB COLLISION_BOX = new AxisAlignedBB(0.01, 0, 0.01, 0.99, 1, 0.99);
	private static final AxisAlignedBB COLLISION_BOX_HACKY_SIDES = new AxisAlignedBB(0.3125, 0, 0.3125, 1 - 0.3125, 1, 1 - 0.3125);
	private static final AxisAlignedBB COLLISION_BOX_HACKY_TOP = new AxisAlignedBB(-0.0625, 1 - 0.0625, -0.0625, 1 + 0.0625, 1, 1 + 0.0625);

	public BlockScaffold() {
		super(Material.WOOD);
		setHardness(1.0F);
		setHarvestLevel("axe", 0);
		setOpaqueCube(false);
		setFullCube(false);
		setSoundType(SoundType.WOOD);
		setUnlocalizedName("charset.scaffold");
	}

	public static ItemStack createStack(ItemMaterial plankMaterial, int stackSize) {
		ItemStack scaffold = new ItemStack(CharsetMiscScaffold.scaffoldBlock, stackSize);
		scaffold.setTagCompound(new NBTTagCompound());
		scaffold.getTagCompound().setString("plank", plankMaterial.getId());
		return scaffold;
	}

	@Override
	protected ISubItemProvider createSubItemProvider() {
		return new SubItemProviderCache(new SubItemProviderRecipes() {
			@Override
			protected Item getItem() {
				return CharsetMiscScaffold.scaffoldItem;
			}
		});
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
		if (ForgeModContainer.fullBoundingBoxLadders) {
			if (entityBox.intersects(COLLISION_BOX.offset(pos)))
				collidingBoxes.add(COLLISION_BOX.offset(pos));
		} else {
			// Hack!
			if (entityBox.intersects(COLLISION_BOX_HACKY_SIDES.offset(pos))) {
				collidingBoxes.add(COLLISION_BOX_HACKY_SIDES.offset(pos));
			}

			if (!(entityIn instanceof EntityLivingBase) || !((EntityLivingBase) entityIn).isOnLadder()) {
				if (pos.getY() + 0.9 <= entityBox.minY) {
					if (entityBox.intersects(COLLISION_BOX_HACKY_TOP.offset(pos)))
						collidingBoxes.add(COLLISION_BOX_HACKY_TOP.offset(pos));
				}
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
				droppedStack = ((TileBase) tile).getDroppedBlock(state);
			}
			ItemUtils.spawnItemEntity(worldIn, new Vec3d(pos).addVector(0.5, 0.5, 0.5), droppedStack, 0.1f, 0.1f, 0.1f, 1.0f);
			worldIn.setBlockToAir(pos);
		}
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess access, IBlockState state, BlockPos pos, EnumFacing side) {
		return side == EnumFacing.UP ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
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
