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

package pl.asie.charset.module.transport.carts.compat.rails;

import net.minecraft.block.BlockRailBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.module.transport.carts.CharsetTransportCarts;
import pl.asie.charset.module.transport.rails.BlockRailCharset;
import pl.asie.charset.module.transport.rails.CharsetTransportRails;
import pl.asie.charset.module.tweak.carry.CarryTransformerRegistry;
import pl.asie.charset.module.tweak.carry.compat.barrels.CarryTransformerEntityMinecartDayBarrel;

@CharsetModule(
        name = "transport.rails:transport.carts",
        profile = ModuleProfile.COMPAT,
        dependencies = {"transport.rails", "transport.carts"}
)
public class CharsetCombinerCompatRails {
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // TODO: This needs a redesign... Possibly move the Combiner to Lib.
        if (CharsetTransportCarts.combiner != null) {
            CharsetTransportCarts.combiner.register(Blocks.RAIL, CharsetTransportRails.blockRailCross.getDefaultState(), new ItemStack(Blocks.RAIL));
            CharsetTransportCarts.combiner.register(Blocks.RAIL, CharsetTransportRails.blockRailCross.getDefaultState().withProperty(BlockRailCharset.DIRECTION, BlockRailBase.EnumRailDirection.EAST_WEST), new ItemStack(Blocks.RAIL));
        }
    }
}
