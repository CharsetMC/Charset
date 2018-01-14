/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
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

package pl.asie.charset.module.storage.barrels;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import pl.asie.charset.lib.block.BlockBase;
import pl.asie.charset.lib.item.ISubItemProvider;
import pl.asie.charset.lib.item.SubItemProviderCache;
import pl.asie.charset.lib.item.SubItemProviderRecipes;
import pl.asie.charset.lib.item.SubItemSetHelper;
import pl.asie.charset.lib.utils.UnlistedPropertyGeneric;

import javax.annotation.Nullable;
import java.util.*;

public class BlockBarrel extends BlockBase implements ITileEntityProvider {
    private static final List<Set<BarrelUpgrade>> CREATIVE_TAB_UPGRADE_SETS = ImmutableList.of(
            EnumSet.noneOf(BarrelUpgrade.class),
            EnumSet.of(BarrelUpgrade.STICKY),
            EnumSet.of(BarrelUpgrade.HOPPING),
            EnumSet.of(BarrelUpgrade.HOPPING, BarrelUpgrade.STICKY),
            EnumSet.of(BarrelUpgrade.SILKY)
    );

    public BlockBarrel() {
        // TODO: Adventure mode support (the Material trick doesn't work)
        super(Material.WOOD);
        setComparatorInputOverride(true);
        setHardness(2.5F);
        setHarvestLevel("axe", 0);
        setSoundType(SoundType.WOOD);
        setUnlocalizedName("charset.barrel");
    }

    @Nullable
    private ImmutableList<ItemStack> generateTypeSet(ItemStack barrel) {
        TileEntityDayBarrel rep = new TileEntityDayBarrel();
        rep.loadFromStack(barrel);
        if (rep.upgrades.size() > 0) {
            return null;
        }

        ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();

        for (Set<BarrelUpgrade> upgrades : CREATIVE_TAB_UPGRADE_SETS) {
            boolean allowed = true;
            for (BarrelUpgrade upgrade : upgrades) {
                if (!CharsetStorageBarrels.isEnabled(upgrade)) {
                    allowed = false;
                    break;
                }
            }

            if (allowed) {
                rep.upgrades.addAll(upgrades);
                builder.add(rep.getDroppedBlock(CharsetStorageBarrels.barrelBlock.getDefaultState()));
                rep.upgrades.clear();
            }
        }

        return builder.build();
    }

    @Override
    protected ISubItemProvider createSubItemProvider() {
        return new SubItemProviderCache(new SubItemProviderRecipes(() -> CharsetStorageBarrels.barrelItem) {
            @Override
            protected int compareSets(List<ItemStack> first, List<ItemStack> second) {
                return SubItemSetHelper.wrapLists(first, second, SubItemSetHelper.extractMaterial("log", SubItemSetHelper::sortByItem));
            }

            @Override
            protected List<ItemStack> createForcedItems() {
                return CharsetStorageBarrels.CREATIVE_BARRELS;
            }

            @Nullable
            @Override
            protected List<ItemStack> createSetFor(ItemStack stack) {
                return generateTypeSet(stack);
            }
        });
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityDayBarrel) {
            ((TileEntityDayBarrel) tile).neighborChanged(pos, fromPos);
        }
    }

    @Override
    public float getBlockHardness(IBlockState state, World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityDayBarrel) {
            if (((TileEntityDayBarrel) tile).upgrades.contains(BarrelUpgrade.INFINITE)) {
                return -1.0F;
            }
        }

        return this.blockHardness;
    }

    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityDayBarrel) {
            return ((TileEntityDayBarrel) tile).getFlamability(face);
        }

        return 0;
    }

    @Override
    public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityDayBarrel) {
            return ((TileEntityDayBarrel) tile).isFlammable(face);
        }

        return false;
    }

    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityDayBarrel) {
            return ((TileEntityDayBarrel) tile).getFireSpreadSpeed(face);
        }

        return 0;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState();
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityDayBarrel();
    }

    public static final IUnlistedProperty<BarrelCacheInfo> BARREL_INFO = new UnlistedPropertyGeneric<>("info", BarrelCacheInfo.class);

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[]{}, new IUnlistedProperty[]{BARREL_INFO});
    }
    
    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityDayBarrel) {
            if (!((TileEntityDayBarrel) tile).canHarvest(player)) {
                return false;
            }
        }

        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (world == null || world.isRemote) {
            return true;
        }

        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityDayBarrel) {
            return ((TileEntityDayBarrel) tile).activate(player, side, hand);
        }

        return false;
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer playerIn) {
        if (world == null || world.isRemote) {
            return;
        }

        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityDayBarrel) {
            ((TileEntityDayBarrel) tile).click(playerIn);
        }
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntityDayBarrel barrel = (TileEntityDayBarrel) world.getTileEntity(pos);
        IExtendedBlockState extendedBS = (IExtendedBlockState) super.getExtendedState(state, world, pos);
        if (barrel != null) {
            return extendedBS.withProperty(BARREL_INFO, BarrelCacheInfo.from(barrel));
        } else {
            return extendedBS;
        }
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.TRANSLUCENT || layer == BlockRenderLayer.SOLID;
    }

    @Override
    public SoundType getSoundType(IBlockState state, World world, BlockPos pos, @Nullable Entity entity) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityDayBarrel) {
            return ((TileEntityDayBarrel) tile).getSoundType();
        } else {
            return SoundType.WOOD;
        }
    }
}
