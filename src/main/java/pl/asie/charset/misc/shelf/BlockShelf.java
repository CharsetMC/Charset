package pl.asie.charset.misc.shelf;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.blocks.BlockBase;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.utils.RayTraceUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class BlockShelf extends BlockBase implements ITileEntityProvider {
    public static final Collection<ItemMaterial> PLANKS = new HashSet<>();
    public static final AxisAlignedBB[] BOXES = new AxisAlignedBB[] {
            new AxisAlignedBB(0, 0, 0, 1.0F, 1.0F, 0.5F),
            new AxisAlignedBB(0, 0, 0.5F, 1.0F, 1.0F, 1.0F),
            new AxisAlignedBB(0, 0, 0, 0.5F, 1.0F, 1.0F),
            new AxisAlignedBB(0.5F, 0, 0, 1.0F, 1.0F, 1.0F)
    };
    public static final PropertyBool BACK = PropertyBool.create("back");

    public BlockShelf() {
        super(Material.WOOD);
        setHardness(1.0F);
        setHarvestLevel("axe", 0);
        setSoundType(SoundType.WOOD);
        setUnlocalizedName("charset.shelf");
    }

    public static ItemStack createStack(ItemMaterial plankMaterial, int stackSize) {
        ItemStack scaffold = new ItemStack(CharsetMiscShelf.shelfBlock, stackSize);
        scaffold.setTagCompound(new NBTTagCompound());
        scaffold.getTagCompound().setString("plank", plankMaterial.getId());
        return scaffold;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (hand != EnumHand.MAIN_HAND || worldIn.isRemote || state.getValue(Properties.FACING4).getOpposite() != facing)
            return false;

        TileShelf bookshelf = (TileShelf) worldIn.getTileEntity(pos);
        if (bookshelf != null) {
            return bookshelf.onActivated(hitX, hitY, hitZ, playerIn.getHeldItem(hand), playerIn);
        } else {
            return false;
        }
    }

    @Override
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn) {
        if (worldIn.isRemote)
            return;

        IBlockState state = worldIn.getBlockState(pos);

        RayTraceResult result = RayTraceUtils.getCollision(worldIn, pos, playerIn, getBoundingBox(state, worldIn, pos), 0);
        if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK && result.sideHit == state.getValue(Properties.FACING4).getOpposite()) {
            TileShelf bookshelf = (TileShelf) worldIn.getTileEntity(pos);
            if (bookshelf != null) {
                Vec3d v = result.hitVec.subtract(pos.getX(), pos.getY(), pos.getZ());
                bookshelf.onClicked((float) v.xCoord, (float) v.yCoord, (float) v.zCoord, playerIn);
            }
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOXES[(state.getValue(Properties.FACING4).ordinal() - 2) ^ (state.getValue(BACK) ? 0 : 1)];
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[]{Properties.FACING4, BACK}, new IUnlistedProperty[]{TileShelf.PROPERTY});
    }

    @Override
    protected Collection<ItemStack> getCreativeItems() {
        return ImmutableList.of();
    }

    @Override
    protected List<Collection<ItemStack>> getCreativeItemSets() {
        List<Collection<ItemStack>> list = new ArrayList<>();
        for (ItemMaterial s : PLANKS) {
            list.add(ImmutableList.of(createStack(s, 1)));
        }
        return list;
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
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return base_state.getValue(BACK) && side == base_state.getValue(Properties.FACING4).getOpposite();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return (state.getValue(Properties.FACING4).ordinal() - 2) | (state.getValue(BACK) ? 4 : 0);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(Properties.FACING4, EnumFacing.getFront((meta & 3) + 2))
                .withProperty(BACK, meta >= 4);
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        Vec3d placementVec = new Vec3d(hitX - 0.5F, hitY - 0.5F, hitZ - 0.5F).subtract(facing.getFrontOffsetX(), facing.getFrontOffsetY(), facing.getFrontOffsetZ());
        placementVec = placementVec.rotateYaw(placer.getHorizontalFacing().getHorizontalAngle() / 180 * (float) Math.PI);
        return this.getDefaultState().withProperty(Properties.FACING4, placer.getHorizontalFacing()).withProperty(BACK, placementVec.zCoord > 0);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileShelf();
    }

    @Override
    public float getEnchantPowerBonus(World world, BlockPos pos) {
        return 1;
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileShelf bookshelf = (TileShelf) world.getTileEntity(pos);
        IExtendedBlockState extendedBS = (IExtendedBlockState) super.getExtendedState(state, world, pos);
        if (bookshelf != null) {
            return extendedBS.withProperty(TileShelf.PROPERTY, CacheInfoShelf.from(extendedBS, bookshelf));
        } else {
            return extendedBS;
        }
    }
}
