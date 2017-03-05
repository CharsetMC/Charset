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

package pl.asie.charset.storage.barrels;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.blocks.BlockBase;
import pl.asie.charset.lib.utils.UnlistedPropertyGeneric;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BlockBarrel extends BlockBase implements ITileEntityProvider {
    public BlockBarrel() {
        // TODO: Adventure mode support (the Material trick doesn't work)
        super(Material.WOOD);
        setCreativeTab(ModCharset.CREATIVE_TAB);
        setHardness(2.5F);
        setHarvestLevel("axe", 0);
        setSoundType(SoundType.WOOD);
        setUnlocalizedName("charset.barrel");
    }

    @Override
    protected Collection<ItemStack> getCreativeItems() {
        ImmutableSet.Builder<ItemStack> builder = ImmutableSet.builder();

        for (ItemStack barrel : BarrelRegistry.INSTANCE.getBarrels()) {
            TileEntityDayBarrel.Type type = TileEntityDayBarrel.getUpgrade(barrel);
            if (type == TileEntityDayBarrel.Type.CREATIVE) {
                builder.add(barrel);
            }
        }

        return builder.build();
    }

    @Override
    protected List<Collection<ItemStack>> getCreativeItemSets() {
        List<Collection<ItemStack>> list = new ArrayList<>();
        TileEntityDayBarrel rep = new TileEntityDayBarrel();

        for (ItemStack barrel : BarrelRegistry.INSTANCE.getBarrels(TileEntityDayBarrel.Type.NORMAL)) {
            rep.loadFromStack(barrel);
            ImmutableSet.Builder<ItemStack> builder = ImmutableSet.builder();

            for (TileEntityDayBarrel.Type type : TileEntityDayBarrel.Type.values()) {
                if (type == TileEntityDayBarrel.Type.CREATIVE) continue;
                if (!CharsetStorageBarrels.isEnabled(type)) continue;
                rep.type = type;
                builder.add(rep.getPickedBlock());
            }

            list.add(builder.build());
        }

        return list;
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
            if (((TileEntityDayBarrel) tile).type == TileEntityDayBarrel.Type.CREATIVE) {
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
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
        if (axis != null) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntityDayBarrel) {
                ((TileEntityDayBarrel) tile).rotateWrench(axis);
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

    public static final IUnlistedProperty<BarrelCacheInfo> BARREL_INFO = new UnlistedPropertyGeneric<>("info", BarrelCacheInfo.class);

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[]{}, new IUnlistedProperty[]{BARREL_INFO});
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, @Nullable TileEntity te, int fortune, boolean silkTouch) {
        if (te instanceof TileEntityDayBarrel) {
            return ((TileEntityDayBarrel) te).getDrops(silkTouch);
        } else {
            return Collections.EMPTY_LIST;
        }
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
    public boolean hasComparatorInputOverride(IBlockState state)
    {
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
}
