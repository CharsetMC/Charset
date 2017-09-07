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

package pl.asie.charset.module.tools.building.wrench;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.module.tools.building.CharsetToolsBuilding;

@CharsetModule(
        name = "quark:tools.wrench.rotate",
        profile = ModuleProfile.COMPAT,
        dependencies = {"tools.wrench", "mod:quark"}
)
public class WrenchCompatRotateQuark {
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        Block verticalPlanks = GameRegistry.findRegistry(Block.class).getValue(new ResourceLocation("quark:vertical_planks"));
        Block stainedPlanks = GameRegistry.findRegistry(Block.class).getValue(new ResourceLocation("quark:stained_planks"));
        Block verticalStainedPlanks = GameRegistry.findRegistry(Block.class).getValue(new ResourceLocation("quark:vertical_stained_planks"));

        if (verticalPlanks != null && verticalPlanks != Blocks.AIR) {
            CharsetToolsBuilding.registerRotationHandler(Blocks.PLANKS, (world, pos, state, axis) -> world.setBlockState(pos, verticalPlanks.getStateFromMeta(state.getBlock().getMetaFromState(state))));
            CharsetToolsBuilding.registerRotationHandler(verticalPlanks, (world, pos, state, axis) -> world.setBlockState(pos, Blocks.PLANKS.getStateFromMeta(state.getBlock().getMetaFromState(state))));
        }

        if (verticalStainedPlanks != null && verticalStainedPlanks != Blocks.AIR) {
            CharsetToolsBuilding.registerRotationHandler(stainedPlanks, (world, pos, state, axis) -> world.setBlockState(pos, verticalStainedPlanks.getStateFromMeta(state.getBlock().getMetaFromState(state))));
            CharsetToolsBuilding.registerRotationHandler(verticalStainedPlanks, (world, pos, state, axis) -> world.setBlockState(pos, stainedPlanks.getStateFromMeta(state.getBlock().getMetaFromState(state))));
        }
    }
}
