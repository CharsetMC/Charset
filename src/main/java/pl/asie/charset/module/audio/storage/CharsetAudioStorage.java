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

package pl.asie.charset.module.audio.storage;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.api.tape.IDataStorage;
import pl.asie.charset.lib.item.ItemBlockBase;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.lib.resources.ColorPaletteUpdateEvent;
import pl.asie.charset.lib.ui.GuiHandlerCharset;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.lib.utils.RenderUtils;
import pl.asie.charset.module.audio.storage.system.DataStorage;
import pl.asie.charset.module.audio.storage.system.DataStorageCapStorage;
import pl.asie.charset.module.audio.storage.system.DataStorageManager;
import pl.asie.charset.module.storage.locks.ContainerKeyring;

import java.io.IOException;
import java.util.List;

@CharsetModule(
        name = "audio.storage",
        description = "Audio storage, recording and playback - Quartz Discs - still highly experimental",
        profile = ModuleProfile.EXPERIMENTAL
)
public class CharsetAudioStorage {
    @CapabilityInject(IDataStorage.class)
    public static Capability<IDataStorage> DATA_STORAGE;

    @CharsetModule.PacketRegistry
    public static PacketRegistry packet;

    public static DataStorageManager storageManager;

    public static BlockRecordPlayer blockRecordPlayer;
    public static Item itemRecordPlayer;
    public static ItemQuartzDisc quartzDisc;

    public static int PLAYER_LASER_COLOR = 0xBFFFFFFF;

    public static void addTimeToTooltip(List<String> tooltip, int mins, int secs) {
        String secStr = secs + " second" + (secs != 1 ? "s" : "");
        if (mins != 0) {
            tooltip.add(TextFormatting.GRAY + "" + mins + " minute" + (mins != 1 ? "s" : "") + (secs != 0 ? (" " + secStr) : ""));
        } else {
            tooltip.add(TextFormatting.GRAY + secStr);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onColorPaletteUpdate(ColorPaletteUpdateEvent event) {
        if (event.getParser().hasColor("charset:laser", "white")) {
            PLAYER_LASER_COLOR = RenderUtils.asMcIntColor(event.getParser().getColor("charset:laser", "white"));
        } else {
            PLAYER_LASER_COLOR = 0xBFFFFFFF;
        }
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        CapabilityManager.INSTANCE.register(IDataStorage.class, new DataStorageCapStorage(), DataStorage::new);

        blockRecordPlayer = new BlockRecordPlayer();
        itemRecordPlayer = new ItemBlockBase(blockRecordPlayer);

        quartzDisc = new ItemQuartzDisc();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        RegistryUtils.register(TileRecordPlayer.class, "record_player");

        packet.registerPacket(0x01, PacketUpdateProgressClient.class);
        packet.registerPacket(0x02, PacketDriveState.class);
        packet.registerPacket(0x03, PacketDriveData.class);

        GuiHandlerCharset.INSTANCE.register(GuiHandlerCharset.RECORD_PLAYER, Side.SERVER, (r) -> {
            TileEntity tile = r.getTileEntity();
            if (tile instanceof TileRecordPlayer) {
                return new ContainerRecordPlayer((TileRecordPlayer) tile, r.player.inventory);
            } else {
                return null;
            }
        });
    }

    @Mod.EventHandler
    @SideOnly(Side.CLIENT)
    public void initClient(FMLInitializationEvent event) {
        ClientRegistry.bindTileEntitySpecialRenderer(TileRecordPlayer.class, TileRecordPlayerRenderer.INSTANCE);
        GuiHandlerCharset.INSTANCE.register(GuiHandlerCharset.RECORD_PLAYER, Side.CLIENT, (r) -> new GuiRecordPlayer(r.getContainer()));
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    }

    @Mod.EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        storageManager = new DataStorageManager();
        MinecraftForge.EVENT_BUS.register(storageManager);
    }

    @Mod.EventHandler
    public void serverStop(FMLServerStoppedEvent event) {
        if (storageManager != null) {
            try {
                storageManager.save();
            } catch (IOException e) {

            }
            MinecraftForge.EVENT_BUS.unregister(storageManager);
        }
        storageManager = null;
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerModels(ModelRegistryEvent event) {
        for (int i = 0; i <= 6; i += 2) {
            RegistryUtils.registerModel(quartzDisc, i, "charset:quartz_disc#inventory_" + (i + 10) + "_blank");
            RegistryUtils.registerModel(quartzDisc, i + 1, "charset:quartz_disc#inventory_" + (i + 10));
        }

        RegistryUtils.registerModel(itemRecordPlayer, 0, "charset:record_player#inventory");
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        TileRecordPlayerRenderer.INSTANCE.arm = RenderUtils.getModelWithTextures(new ResourceLocation("charset:block/record_player_arm"), event.getMap());
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        RegistryUtils.register(event.getRegistry(), blockRecordPlayer, "record_player");
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        RegistryUtils.register(event.getRegistry(), itemRecordPlayer, "record_player");
        RegistryUtils.register(event.getRegistry(), quartzDisc, "quartz_disc");
    }
}
