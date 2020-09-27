/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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

package pl.asie.charset.module.storage.tanks;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.block.BlockBase;
import pl.asie.charset.lib.capability.CapabilityHelper;
import pl.asie.charset.lib.notify.Notice;
import pl.asie.charset.lib.notify.component.NotificationComponentFluidStack;
import pl.asie.charset.lib.notify.component.NotificationComponentString;
import pl.asie.charset.lib.utils.FluidUtils;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.SoundUtils;

public class BlockTank extends BlockBase implements ITileEntityProvider {
    public static final int VARIANTS = 18;
    protected static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0.05f, 0, 0.05f, 0.95f, 1, 0.95f);
    protected static final PropertyInteger VARIANT = PropertyInteger.create("connections", 0, 11);

    public BlockTank() {
        super(Material.GLASS);
        setHardness(0.6F);
        setHarvestLevel("pickaxe", 0);
        setTranslationKey("charset.tank");
        setFullCube(false);
        setOpaqueCube(false);
        setComparatorInputOverride(true);
        setSoundType(SoundType.GLASS);
    }

    protected int getVariant(IBlockAccess access, BlockPos pos) {
        IBlockState state = access.getBlockState(pos);
        if (!(state.getBlock() instanceof BlockTank)) {
            return -1;
        }

        TileEntity tile = access.getTileEntity(pos);
        if (tile instanceof TileTank) {
            return ((TileTank) tile).getVariant();
        } else {
            return 0;
        }
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        Item item = Item.getItemFromBlock(this);

        for (int i = 0; i <= 17; i++) {
            ItemStack stack = new ItemStack(item);
            ItemUtils.getTagCompound(stack, true).setInteger("color", i - 1);
            items.add(stack);
        }
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        switch (side) {
            case DOWN:
                if (getVariant(blockAccess, pos) == getVariant(blockAccess, pos.down())) {
                    return false;
                }
                return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
            case UP:
                if (getVariant(blockAccess, pos) == getVariant(blockAccess, pos.up())) {
                    return false;
                }
                return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
            default:
                return true;
        }
    }

    @Override
    public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
        TileEntity tankEntity = worldIn.getTileEntity(pos);
        if (tankEntity instanceof TileTank) {
            TileTank tank = (TileTank) tankEntity;
            FluidStack contents = tank.getContents();
            if (contents != null) {
                // TODO: Somehow work around isToolEffective instead of this ugly hack
                return this.blockHardness * 75.0f;
            } else {
                return this.blockHardness;
            }
        } else {
            return this.blockHardness;
        }
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        int variant = getVariant(worldIn, pos);
        int variantUp = getVariant(worldIn, pos.up());
        int variantDown = getVariant(worldIn, pos.down());

        return state.withProperty(VARIANT,
                ((variantUp == variant) ? 0 : 2)
                | ((variantDown == variant) ? 0 : 1)
                | (variant > 0 ? (variant == 17 ? 8 : 4) : 0));
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        // Empty the fluid if player is in creative mode so that it doesn't spill
        if (player.isCreative()) {
            TileEntity tankEntity = world.getTileEntity(pos);
            if (tankEntity instanceof TileTank) {
                TileTank tank = (TileTank) tankEntity;
                tank.fluidStack = null;
            }
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tankEntity = worldIn.getTileEntity(pos);
        if (tankEntity instanceof TileTank) {
            TileTank tank = (TileTank) tankEntity;
            tank.spillFluid();
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void onBlockExploded(World worldIn, BlockPos pos, Explosion explosionIn) {
        TileEntity tankEntity = worldIn.getTileEntity(pos);
        if (tankEntity instanceof TileTank) {
            TileTank tank = (TileTank) tankEntity;
            tank.spillFluid();
        }
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, VARIANT);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOUNDING_BOX;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.TRANSLUCENT || layer == BlockRenderLayer.CUTOUT;
    }

    private void notice(World worldIn, TileEntity tankEntity, EntityPlayer playerIn, float noticeX, float noticeY, float noticeZ) {
        if (!worldIn.isRemote) {
            if (tankEntity instanceof TileTank) {
                FluidStack stack = ((TileTank) tankEntity).getContents();
	            new Notice(tankEntity, new NotificationComponentFluidStack(stack, true)).sendTo(playerIn);
            }
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (hand != EnumHand.MAIN_HAND)
            return false;

        TileEntity tankEntity = worldIn.getTileEntity(pos);
        if (tankEntity instanceof TileTank) {
            TileTank tank = (TileTank) tankEntity;
            ItemStack held = playerIn.getHeldItem(hand);
            if (held.isEmpty()) {
                notice(worldIn, tankEntity, playerIn,
                        tankEntity.getPos().getX() + hitX,
                        tankEntity.getPos().getY() + hitY,
                        tankEntity.getPos().getZ() + hitZ);
                return true;
            }

            IFluidHandlerItem handlerHeld = CapabilityHelper.get(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, held, null);
            boolean hasFluid = false;
            if (handlerHeld != null) {
            	IFluidTankProperties[] properties = handlerHeld.getTankProperties();
            	hasFluid = (properties.length > 0 && properties[0].getContents() != null) || (handlerHeld.drain(1, false) != null);
            }

            if (tank.doubleClickInsertion.isDoubleClick(playerIn)) {
                if (FluidUtils.handleTank((IFluidHandler) tankEntity, ((TileTank) tankEntity).getContents(), worldIn, pos, playerIn, hand, false, true)) {
                    return true;
                }
            } else if (tank.doubleClickExtraction.isDoubleClick(playerIn)) {
                if (FluidUtils.handleTank((IFluidHandler) tankEntity, ((TileTank) tankEntity).getContents(), worldIn, pos, playerIn, hand, true, false)) {
                    return true;
                }
            } else if (FluidUtils.handleTank((IFluidHandler) tankEntity, ((TileTank) tankEntity).getContents(), worldIn, pos, playerIn, hand)) {
                if (hasFluid) {
                    tank.doubleClickInsertion.markLastClick(playerIn);
                } else {
                    tank.doubleClickExtraction.markLastClick(playerIn);
                }
                return true;
            }

            return false;
        } else {
            return false;
        }
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        // Must not place an empty tank between two tanks which differ only in liquid
        return !TileTank.checkPlacementConflict(worldIn.getTileEntity(pos.down()), worldIn.getTileEntity(pos.up()), -1);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        // Tank connection refresh logic
        if (blockIn == this || worldIn.isAirBlock(fromPos)) {
            BlockPos tankPos = pos;
            TileEntity tankEntity = worldIn.getTileEntity(tankPos);
            if (tankEntity instanceof TileTank) {
                ((TileTank) tankEntity).onTankStructureChanged();
            }
        }
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileTank();
    }

    @Override
    public boolean recolorBlock(World world, BlockPos pos, EnumFacing side, net.minecraft.item.EnumDyeColor color) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileTank) {
            if (((TileTank) tile).setVariant(color.getMetadata() + 1)) {
                return true;
            }
        }

        return false;
    }
}
