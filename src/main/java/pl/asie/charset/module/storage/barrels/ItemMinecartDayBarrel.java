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

package pl.asie.charset.module.storage.barrels;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.item.ISubItemProvider;
import pl.asie.charset.lib.item.ItemMinecartCharset;
import pl.asie.charset.lib.item.SubItemProviderCache;
import pl.asie.charset.lib.material.ColorLookupHandler;
import pl.asie.charset.lib.utils.RenderUtils;

import java.util.*;

public class ItemMinecartDayBarrel extends ItemMinecartCharset {
    @SideOnly(Side.CLIENT)
    public static class Color implements IItemColor {
        @Override
        public int getColorFromItemstack(ItemStack stack, int tintIndex) {
            if (tintIndex == 1) {
                BarrelCacheInfo info = BarrelCacheInfo.from(stack);
                return ColorLookupHandler.INSTANCE.getColor(info.logStack, RenderUtils.AveragingMode.V_EDGES_ONLY);
            }

            return -1;
        }
    }

    public ItemMinecartDayBarrel() {
        super();
        setUnlocalizedName("charset.barrelCart");
    }

    @Override
    protected EntityMinecart createCart(GameProfile owner, ItemStack cart, World world, double x, double y, double z) {
        EntityMinecartDayBarrel minecart = new EntityMinecartDayBarrel(world, x, y, z);
        minecart.initFromStack(cart);
        return minecart;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack is, World world, List list, ITooltipFlag verbose) {
        super.addInformation(is, world, list, verbose);
        CharsetStorageBarrels.barrelItem.addExtraInformation(is, world, list, verbose);
    }

    @Override
    public String getItemStackDisplayName(ItemStack is) {
        if (is.hasTagCompound()) {
            String name = CharsetStorageBarrels.barrelItem.getItemStackDisplayName(is);
            return I18n.translateToLocalFormatted("item.charset.barrelCart.known.name", name);
        }
        return super.getItemStackDisplayName(is);
    }

    @Override
    protected ISubItemProvider createSubItemProvider() {
        return new SubItemProviderCache(new ISubItemProvider() {
            private List<ItemStack> convert(Collection<ItemStack> items) {
                List<ItemStack> itemsOut = new ArrayList<>();

                for (ItemStack barrel : items) {
                    Set<TileEntityDayBarrel.Upgrade> upgradeSet = EnumSet.noneOf(TileEntityDayBarrel.Upgrade.class);
                    TileEntityDayBarrel.populateUpgrades(upgradeSet, barrel.getTagCompound());

                    if (upgradeSet.size() == 0 || upgradeSet.contains(TileEntityDayBarrel.Upgrade.INFINITE)) {
                        ItemStack barrelCart = makeBarrelCart(barrel);
                        itemsOut.add(barrelCart);
                    }
                }

                return itemsOut;
            }

            @Override
            public List<ItemStack> getItems() {
                return convert(CharsetStorageBarrels.barrelBlock.getSubItemProvider().getItems());
            }

            @Override
            public List<ItemStack> getAllItems() {
                return convert(CharsetStorageBarrels.barrelBlock.getSubItemProvider().getAllItems());
            }
        });
    }

    public ItemStack makeBarrelCart(ItemStack barrelItem) {
        ItemStack ret = new ItemStack(this, 1, barrelItem.getItemDamage());
        ret.setTagCompound(barrelItem.getTagCompound().copy());
        return ret;
    }
/*
    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        TileEntityDayBarrel barrel = new TileEntityDayBarrel();
        barrel.loadFromStack(stack);
        return barrel.getPickedBlock(CharsetStorageBarrels.barrelBlock.getDefaultState());
    }
*/
}
