/*
 * Copyright (c) 2016 neptunepink
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.storage.barrel;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import pl.asie.charset.lib.BlockBase;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.utils.GenericExtendedProperty;
import pl.asie.charset.storage.ModCharsetStorage;
import pl.asie.charset.storage.backpack.TileBackpack;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BlockBarrel extends BlockBase implements ITileEntityProvider {
    public static final Material materialBarrel = new Material(MapColor.WOOD) {{
        setAdventureModeExempt();
        // TODO
        // NORELEASE.fixme("Test adventure mode barrel breaking");
    }};

    private static final boolean SHOW_ALL_BARRELS = true;

    public BlockBarrel() {
        super(materialBarrel);
        setUnlocalizedName("charset.barrel");
        setCreativeTab(ModCharsetLib.CREATIVE_TAB);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityDayBarrel) {
            return ((TileEntityDayBarrel) tile).getPickedBlock();
        }

        return new ItemStack(this);
    }

    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityDayBarrel) {
            return ((TileEntityDayBarrel) tile).getFlamability();
        }

        return 0;
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityDayBarrel) {
            if (((TileEntityDayBarrel) tile).canRotate(axis)) {
                ((TileEntityDayBarrel) tile).rotate(axis);
                return true;
            }
        }

        return false;
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

    public static final IUnlistedProperty<BarrelCacheInfo> BARREL_INFO = new GenericExtendedProperty<>("info", BarrelCacheInfo.class);

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[]{}, new IUnlistedProperty[]{BARREL_INFO});
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityDayBarrel) {
            if (!((TileEntityDayBarrel) tile).removedByPlayer(player, willHarvest)) {
                return false;
            }
        }

        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }

        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityDayBarrel) {
            return ((TileEntityDayBarrel) tile).activate(player, side, hand);
        }

        return false;
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state)
    {
        return false;
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer playerIn) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityDayBarrel) {
            ((TileEntityDayBarrel) tile).click(playerIn, playerIn.getActiveHand());
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
    public void getSubBlocks(Item me, CreativeTabs tab, List<ItemStack> itemList) {
        if (todaysBarrels != null) {
            itemList.addAll(todaysBarrels);
            return;
        }
        Calendar cal = Calendar.getInstance();
        int doy = cal.get(Calendar.DAY_OF_YEAR) - 1 /* start at 0, not 1 */;

        ArrayList<ItemStack> weeklyBarrels = new ArrayList<>();
        todaysBarrels = new ArrayList<ItemStack>();

        for (ItemStack barrel : BarrelRegistry.INSTANCE.getBarrels()) {
            TileEntityDayBarrel.Type type = TileEntityDayBarrel.getUpgrade(barrel);
            if (type == TileEntityDayBarrel.Type.NORMAL) {
                weeklyBarrels.add(barrel);
            } else if (type == TileEntityDayBarrel.Type.CREATIVE) {
                todaysBarrels.add(barrel);
            }
        }

        Collections.shuffle(weeklyBarrels, new Random(doy));
        int barrelsToAdd = 1;

        TileEntityDayBarrel rep = new TileEntityDayBarrel();
        for (ItemStack barrel : weeklyBarrels) {
            rep.loadFromStack(barrel);
            for (TileEntityDayBarrel.Type type : TileEntityDayBarrel.Type.values()) {
                if (type == TileEntityDayBarrel.Type.CREATIVE) continue;
                if (type == TileEntityDayBarrel.Type.LARGER) continue;
                rep.type = type;
                todaysBarrels.add(rep.getPickedBlock());
            }
            barrelsToAdd--;
            if (!SHOW_ALL_BARRELS && barrelsToAdd <= 0) {
                break;
            }
        }

        itemList.addAll(todaysBarrels);
    }

    ArrayList<ItemStack> todaysBarrels = null;

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.TRANSLUCENT || layer == BlockRenderLayer.SOLID;
    }
}
