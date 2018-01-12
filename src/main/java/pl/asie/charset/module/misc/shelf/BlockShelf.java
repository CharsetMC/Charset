/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.charset.module.misc.shelf;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
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
import pl.asie.charset.lib.block.BlockBase;
import pl.asie.charset.lib.item.ISubItemProvider;
import pl.asie.charset.lib.item.SubItemProviderCache;
import pl.asie.charset.lib.item.SubItemProviderRecipes;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.utils.RayTraceUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;

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
        setOpaqueCube(false);
        setFullCube(false);
    }

    public static ItemStack createStack(ItemMaterial plankMaterial, int stackSize) {
        ItemStack scaffold = new ItemStack(CharsetMiscShelf.shelfBlock, stackSize);
        scaffold.setTagCompound(new NBTTagCompound());
        plankMaterial.writeToNBT(scaffold.getTagCompound(), "plank");
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

        RayTraceResult result = RayTraceUtils.getCollision(worldIn, pos, playerIn, getBoundingBox(state, worldIn, pos));
        if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK && result.sideHit == state.getValue(Properties.FACING4).getOpposite()) {
            TileShelf bookshelf = (TileShelf) worldIn.getTileEntity(pos);
            if (bookshelf != null) {
                Vec3d v = result.hitVec.subtract(pos.getX(), pos.getY(), pos.getZ());
                bookshelf.onClicked((float) v.x, (float) v.y, (float) v.z, playerIn);
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
    protected ISubItemProvider createSubItemProvider() {
        return new SubItemProviderCache(new SubItemProviderRecipes(() -> CharsetMiscShelf.shelfItem));
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess access, IBlockState base_state, BlockPos pos, EnumFacing side) {
        return (base_state.getValue(BACK) && side == base_state.getValue(Properties.FACING4).getOpposite()) ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
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

    public static EnumFacing getFace(EnumFacing facing, boolean back) {
        return back ? facing.getOpposite() : facing;
    }

    public static EnumFacing getFacePlaced(EnumFacing facing, float hitX, float hitY, float hitZ, EntityLivingBase placer) {
        Vec3d placementVec = new Vec3d(hitX - 0.5F, hitY - 0.5F, hitZ - 0.5F).subtract(facing.getFrontOffsetX(), facing.getFrontOffsetY(), facing.getFrontOffsetZ());
        placementVec = placementVec.rotateYaw(placer.getHorizontalFacing().getHorizontalAngle() / 180 * (float) Math.PI);
        return getFace(placer.getHorizontalFacing(), placementVec.z > 0);
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        Vec3d placementVec = new Vec3d(hitX - 0.5F, hitY - 0.5F, hitZ - 0.5F).subtract(facing.getFrontOffsetX(), facing.getFrontOffsetY(), facing.getFrontOffsetZ());
        placementVec = placementVec.rotateYaw(placer.getHorizontalFacing().getHorizontalAngle() / 180 * (float) Math.PI);
        return this.getDefaultState().withProperty(Properties.FACING4, placer.getHorizontalFacing()).withProperty(BACK, placementVec.z > 0);
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
            return extendedBS.withProperty(TileShelf.PROPERTY, ShelfCacheInfo.from(extendedBS, bookshelf));
        } else {
            return extendedBS;
        }
    }
}
