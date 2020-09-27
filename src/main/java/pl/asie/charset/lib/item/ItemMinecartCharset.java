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

package pl.asie.charset.lib.item;

import com.mojang.authlib.GameProfile;
import mods.railcraft.api.core.items.IMinecartItem;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMinecart;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import pl.asie.charset.ModCharset;

@Optional.Interface(iface = "mods.railcraft.api.core.items.IMinecartItem", modid = "Railcraft")
public abstract class ItemMinecartCharset extends ItemMinecart implements IMinecartItem, ISubItemProvider.Container {
    private static final IBehaviorDispenseItem MINECART_DISPENSER_BEHAVIOR = new BehaviorDefaultDispenseItem() {
        private final BehaviorDefaultDispenseItem behaviourDefaultDispenseItem = new BehaviorDefaultDispenseItem();

        @Override
        public ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
            EnumFacing facing = source.getBlockState().getValue(BlockDispenser.FACING);
            World world = source.getWorld();
            BlockPos blockpos = source.getBlockPos().offset(facing);

            if (!BlockRailBase.isRailBlock(world.getBlockState(blockpos))) {
                return behaviourDefaultDispenseItem.dispense(source, stack);
            } else {
                ((ItemMinecartCharset) stack.getItem()).placeCart(null, stack, world, blockpos);
                return stack;
            }
        }

        @Override
        protected void playDispenseSound(IBlockSource source) {
            source.getWorld().playEvent(1000, source.getBlockPos(), 0);
        }
    };

    private static final ItemStack MINECART = new ItemStack(Items.MINECART);
    private final ISubItemProvider subItemProvider;

    public ItemMinecartCharset() {
        super(EntityMinecart.Type.RIDEABLE); // yeah, right
        setHasSubtypes(true);
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, MINECART_DISPENSER_BEHAVIOR);
        subItemProvider = createSubItemProvider();
    }

    @Override
    public final ISubItemProvider getSubItemProvider() {
        return subItemProvider;
    }

    protected ISubItemProvider createSubItemProvider() {
        return new SubItemProviderSimple(this);
    }

    @Override
    public int getItemStackLimit() {
        return MINECART.getMaxStackSize();
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            items.addAll(subItemProvider.getItems());
        }
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer playerIn, World w, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!BlockRailBase.isRailBlock(w.getBlockState(pos))) {
            return EnumActionResult.FAIL;
        } else {
            if (!w.isRemote) {
                placeCart(null, playerIn.getHeldItem(hand), w, pos);
            }

            return EnumActionResult.SUCCESS;
        }
    }

    @Override
    public boolean canBePlacedByNonPlayer(ItemStack cart) {
        return true;
    }

    protected abstract EntityMinecart createCart(GameProfile owner, ItemStack cart, World world, double x, double y, double z);

    // NOTE: IMinecartItem *and* Charset rely on this
    public EntityMinecart placeCart(GameProfile owner, ItemStack cart, World world, BlockPos pos) {
        float yOffset = 0.0625f;
        IBlockState railState = world.getBlockState(pos);
        BlockRailBase.EnumRailDirection railDirection = BlockRailBase.isRailBlock(railState)
                ? railState.getValue(((BlockRailBase) railState.getBlock()).getShapeProperty())
                : null;

        if (railDirection != null && railDirection.isAscending()) {
            yOffset += 0.5f;
        }

        EntityMinecart minecart = createCart(owner, cart, world, pos.getX() + 0.5F, pos.getY() + yOffset, pos.getZ() + 0.5F);
        cart.shrink(1);
        world.spawnEntity(minecart);
        return minecart;
    }


}
