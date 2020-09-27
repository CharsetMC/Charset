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

package pl.asie.charset.module.optics.laser;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockPistonExtension;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.api.laser.ILaserSource;
import pl.asie.charset.lib.capability.DummyCapabilityStorage;
import pl.asie.charset.lib.command.CommandCharset;
import pl.asie.charset.lib.handlers.ShiftScrollHandler;
import pl.asie.charset.lib.item.ItemBlockBase;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.lib.recipe.IngredientGroup;
import pl.asie.charset.lib.render.ArrowHighlightHandler;
import pl.asie.charset.lib.resources.ColorPaletteUpdateEvent;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.api.laser.ILaserReceiver;
import pl.asie.charset.api.laser.LaserColor;
import pl.asie.charset.lib.utils.RenderUtils;
import pl.asie.charset.module.optics.laser.blocks.*;
import pl.asie.charset.module.optics.laser.system.*;
import pl.asie.charset.patchwork.LaserRedstoneHook;
import pl.asie.charset.patchwork.PatchworkHelper;

import java.util.HashSet;
import java.util.Set;

@CharsetModule(
        name = "optics.laser",
        description = "Lasers!",
        profile = ModuleProfile.EXPERIMENTAL
)
public class CharsetLaser {
    public static final PropertyEnum<LaserColor> LASER_COLOR = PropertyEnum.create("color", LaserColor.class);
    public static final Set<Block> BLOCKING_BLOCKS = new HashSet<>();

    public static boolean REDSTONE_HOOK_ACTIVE;

    @CapabilityInject(ILaserSource.class)
    public static Capability<ILaserSource> LASER_SOURCE;
    @CapabilityInject(ILaserReceiver.class)
    public static Capability<ILaserReceiver> LASER_RECEIVER;

    public static final int[] LASER_COLORS = new int[8];

    public static final String[] LASER_LANG_STRINGS = {
            "charset.color.black", // doesn't need a gem; just use coal if necessary
            "charset.color.blue", // sapphire?
            "charset.color.green", // emerald
            "charset.color.cyan", // opal?
            "charset.color.red", // ruby
            "charset.color.magenta", // amethyst?
            "charset.color.yellow", // amber
            "charset.color.white" // quartz
    };

    public static LaserStorage laserStorage = new LaserStorage();

    public static Block blockCrystal, blockReflector, blockJar, blockPrism;
    public static Item itemCrystal, itemReflector, itemJar, itemPrism;

    @CharsetModule.PacketRegistry
    public static PacketRegistry packet;

    @CharsetModule.SidedProxy(clientSide = "pl.asie.charset.module.optics.laser.ProxyClient", serverSide = "pl.asie.charset.module.optics.laser.ProxyCommon")
    public static ProxyCommon proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        CapabilityManager.INSTANCE.register(ILaserSource.class, DummyCapabilityStorage.get(), DummyLaserSource::new);

        CharsetLaser.REDSTONE_HOOK_ACTIVE = PatchworkHelper.getBoolean("LASER_REDSTONE");
        LaserRedstoneHook.handler = new LaserRedstoneHelper();

        IngredientGroup.register("charset:laser_gem", 1, "gemSapphire", "gemLapis");
        IngredientGroup.register("charset:laser_gem", 2, "gemEmerald");
        IngredientGroup.register("charset:laser_gem", 3, "gemOpal");
        IngredientGroup.register("charset:laser_gem", 4, "gemRuby");
        IngredientGroup.register("charset:laser_gem", 5, "gemAmethyst");
        IngredientGroup.register("charset:laser_gem", 6, "gemAmber", "gemTopaz");
        IngredientGroup.register("charset:laser_gem", 7, "gemQuartz");

        blockCrystal = new BlockCrystal();
        blockCrystal.setTranslationKey("charset.laser_crystal");
        itemCrystal = new ItemBlockCrystal(blockCrystal);

        blockReflector = new BlockReflector();
        blockReflector.setTranslationKey("charset.laser_reflector");
        itemReflector = new ItemBlockReflector(blockReflector);

        blockJar = new BlockJar();
        blockJar.setTranslationKey("charset.beam_torch");
        itemJar = new ItemBlockJar(blockJar);

        blockPrism = new BlockPrism();
        blockPrism.setTranslationKey("charset.prism");
        itemPrism = new ItemBlockBase(blockPrism);

        MinecraftForge.EVENT_BUS.register(proxy);
    }

    private static final String[] COLOR_NAMES = { "black", "blue", "green", "cyan", "red", "magenta", "yellow", "white" };

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onColorPaletteUpdate(ColorPaletteUpdateEvent event) {
        for (int i = 0; i < 8; i++) {
            LASER_COLORS[i] = RenderUtils.asMcIntColor(event.getParser().getColor("charset:laser", COLOR_NAMES[i]));
        }
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        RegistryUtils.register(event.getRegistry(), blockCrystal, "laser_crystal");
        RegistryUtils.register(event.getRegistry(), blockReflector, "laser_reflector");
        RegistryUtils.register(event.getRegistry(), blockJar, "light_jar");
        RegistryUtils.register(event.getRegistry(), blockPrism, "laser_prism");
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        RegistryUtils.register(event.getRegistry(), itemCrystal, "laser_crystal");
        RegistryUtils.register(event.getRegistry(), itemReflector, "laser_reflector");
        RegistryUtils.register(event.getRegistry(), itemJar, "light_jar");
        RegistryUtils.register(event.getRegistry(), itemPrism, "laser_prism");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        for (Block b : ForgeRegistries.BLOCKS) {
            if (b instanceof BlockPistonBase || b instanceof BlockPistonExtension) {
                BLOCKING_BLOCKS.add(b);
            }
        }

        // Small optimization - these always block, no need to check the TE
        BLOCKING_BLOCKS.add(blockCrystal);
        BLOCKING_BLOCKS.add(blockJar);
        BLOCKING_BLOCKS.add(blockReflector);
        BLOCKING_BLOCKS.add(blockPrism);

        RegistryUtils.register(TileCrystal.class, "laser_crystal");
        RegistryUtils.register(TileReflector.class, "laser_reflector");
        RegistryUtils.register(TileJar.class, "light_jar");
        RegistryUtils.register(TilePrism.class, "laser_prism");

        FMLInterModComms.sendMessage("charset", "addCarry", blockCrystal.getRegistryName());
        FMLInterModComms.sendMessage("charset", "addCarry", blockReflector.getRegistryName());
        FMLInterModComms.sendMessage("charset", "addCarry", blockJar.getRegistryName());
        FMLInterModComms.sendMessage("charset", "addCarry", blockPrism.getRegistryName());

        packet.registerPacket(0x01, PacketBeamAdd.class);
        packet.registerPacket(0x02, PacketBeamRemove.class);

        ShiftScrollHandler.INSTANCE.register(new ShiftScrollHandler.ItemGroup(itemCrystal));
        ShiftScrollHandler.INSTANCE.register(new ShiftScrollHandler.ItemGroup(itemJar));
        ShiftScrollHandler.INSTANCE.register(new ShiftScrollHandler.ItemGroupMetadataLimited(itemReflector, 1, 7));
        ShiftScrollHandler.INSTANCE.register(new ShiftScrollHandler.ItemGroupMetadataLimited(itemReflector, 9, 15));

        MinecraftForge.EVENT_BUS.register(laserStorage);
        CommandCharset.register(new SubCommandDebugLasers());
        CommandCharset.register(new SubCommandSetSpeedOfLight());

        proxy.init();
    }

    @Mod.EventHandler
    @SideOnly(Side.CLIENT)
    public void initClient(FMLInitializationEvent event) {
        ArrowHighlightHandler.register(itemReflector);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    }
}
