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

package pl.asie.charset.module.storage.tanks;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.item.ItemBlockBase;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.RegistryUtils;

@CharsetModule(
        name = "storage.tanks",
        description = "Simple BuildCraft-style vertical tanks",
        profile = ModuleProfile.STABLE
)
public class CharsetStorageTanks {
    public static BlockTank tankBlock;
    public static Item tankItem;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        tankBlock = new BlockTank();
        tankItem = new ItemBlockTank(tankBlock);
    }

    @SubscribeEvent
    public void registerModels(ModelRegistryEvent event) {
        RegistryUtils.registerModel(tankItem, 0, "charset:fluidtank");
        for (int i = 1; i <= 16; i++) {
            RegistryUtils.registerModel(tankItem, i, "charset:fluidtank#inventory_stained");
        }
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        RegistryUtils.register(event.getRegistry(), tankBlock, "fluidTank");
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        RegistryUtils.register(event.getRegistry(), tankItem, "fluidTank");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        RegistryUtils.register(TileTank.class, "fluidTank");

        FMLInterModComms.sendMessage("charset", "addLock", tankBlock.getRegistryName());
        FMLInterModComms.sendMessage("charset", "addCarry", tankBlock.getRegistryName());
    }

    @Mod.EventHandler
    @SideOnly(Side.CLIENT)
    public void initClient(FMLInitializationEvent event) {
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(TankTintHandler.INSTANCE, tankBlock);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(TankTintHandler.INSTANCE, tankItem);
        ClientRegistry.bindTileEntitySpecialRenderer(TileTank.class, new TileTankRenderer());
    }
}
