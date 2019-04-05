/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.lib.wires;

import mcmultipart.api.item.ItemBlockMultipart;
import mcmultipart.api.multipart.IMultipart;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Loader;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.CharsetLib;
import pl.asie.charset.lib.modcompat.mcmultipart.MCMPUtils;

import javax.annotation.Nullable;

public class ItemWire extends ItemBlockMultipart {
    private final WireProvider provider;
    private CreativeTabs wireTab;

    public ItemWire(WireProvider provider) {
        super(CharsetLibWires.blockWire, CharsetLibWires.blockWire);
        this.provider = provider;
        this.provider.setItemWire(this);
        setHasSubtypes(true);
    }

    @Override
    @Nullable
    public CreativeTabs getCreativeTab() {
        return wireTab;
    }

    @Override
    public Item setCreativeTab(CreativeTabs tab) {
        this.wireTab = tab;
        return this;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        Wire wire = fromStack(new IWireContainer.Dummy(), stack, EnumFacing.DOWN);
        String tr = "tile.wire.null";
        if (wire != null) {
            tr = wire.getDisplayName();
        }
        return I18n.translateToLocal(tr);
    }

    public Wire fromStack(IWireContainer container, ItemStack stack, EnumFacing facing) {
        if (provider != null) {
            WireFace location = (stack.getMetadata() & 1) != 0 ? WireFace.CENTER : WireFace.get(facing);
            return provider.create(container, location);
        } else {
            return null;
        }
    }

    public ItemStack toStack(boolean freestanding, int amount) {
        return new ItemStack(this, amount, (freestanding ? 1 : 0));
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX,
                                      float hitY, float hitZ) {
        return place(player, world, pos, hand, facing, hitX, hitY, hitZ, this, this.block::getStateForPlacement, multipartBlock,
                this::placeBlockAtTested, this::placeWirePartAt);
    }

    public boolean placeWirePartAt(ItemStack stack, EntityPlayer player, EnumHand hand, World world, BlockPos pos, EnumFacing facing,
                                      float hitX, float hitY, float hitZ, IMultipart multipartBlock, IBlockState state) {
        WireFace location = (stack.getMetadata() & 1) != 0 ? WireFace.CENTER : WireFace.get(facing.getOpposite());
        if (!provider.canPlace(world, pos, location)) {
            return false;
        }

        return ItemBlockMultipart.placePartAt(stack, player, hand, world, pos, facing, hitX, hitY, hitZ, multipartBlock, state);
    }

    @Override
    public boolean placeBlockAtTested(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing facing, float hitX,
                                      float hitY, float hitZ, IBlockState newState) {
        WireFace location = (stack.getMetadata() & 1) != 0 ? WireFace.CENTER : WireFace.get(facing.getOpposite());
        if (!provider.canPlace(world, pos, location)) {
            return false;
        }

        if (super.placeBlockAtTested(stack, player, world, pos, facing, hitX, hitY, hitZ, newState)) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof TileWire) {
                ((TileWire) tileEntity).onPlacedBy(WireFace.get(facing != null ? facing.getOpposite() : null), stack);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
        if (this.isInCreativeTab(tab)) {
            if (provider.hasSidedWire()) {
                subItems.add(new ItemStack(this, 1, 0));
            }
            if (provider.hasFreestandingWire()) {
                subItems.add(new ItemStack(this, 1, 1));
            }
        }
    }

    public WireProvider getWireProvider() {
        return provider;
    }
}
