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

package pl.asie.charset.module.storage.tanks;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.datafix.FixTypes;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.ModCharset;
import pl.asie.charset.api.laser.LaserColor;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.render.model.ModelTransformer;
import pl.asie.charset.lib.render.model.SimpleMultiLayerBakedModel;
import pl.asie.charset.lib.utils.Orientation;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.module.optics.laser.CharsetLaser;

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

        ModCharset.dataFixes.registerFix(FixTypes.ITEM_INSTANCE, new FixCharsetGlassTankMoveColorNBT());
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerModels(ModelRegistryEvent event) {
        ModelResourceLocation locationNormal = new ModelResourceLocation("charset:fluidtank", "inventory");
        ModelResourceLocation locationStained = new ModelResourceLocation("charset:fluidtank", "inventory_stained");
        ModelResourceLocation locationCreative = new ModelResourceLocation("charset:fluidtank", "inventory_creative");

        ModelLoader.setCustomMeshDefinition(tankItem, stack -> {
            if (stack.hasTagCompound()) {
                int c = stack.getTagCompound().getInteger("color");
                if (c >= 0 && c < 16) {
                    return locationStained;
                } else if (c == 16) {
                    return locationCreative;
                } else {
                    return locationNormal;
                }
            } else {
                return locationNormal;
            }
        });
        ModelLoader.registerItemVariants(tankItem, locationNormal, locationStained, locationCreative);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void bakeModels(ModelBakeEvent event) {
        for (int i = 0; i < 12; i++) {
            IBlockState state = tankBlock.getDefaultState().withProperty(BlockTank.VARIANT, i);
            ModelResourceLocation location = new ModelResourceLocation("charset:fluidtank", "connections=" + i);
            IBakedModel model = event.getModelRegistry().getObject(location);

            if (model != null) {
                SimpleMultiLayerBakedModel result = new SimpleMultiLayerBakedModel(model);
                BlockRenderLayer targetLayer = (i >= 4 && i < 8) ? BlockRenderLayer.TRANSLUCENT : BlockRenderLayer.CUTOUT;
                for (int j = 0; j <= 6; j++) {
                    EnumFacing facingIn = (j < 6) ? EnumFacing.byIndex(j) : null;
                    for (BakedQuad quadIn : model.getQuads(state, facingIn, 0)) {
                        result.addQuad(targetLayer, facingIn, quadIn);
                    }
                }
                event.getModelRegistry().putObject(location, result);
            }
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

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerColorBlock(ColorHandlerEvent.Block event) {
        event.getBlockColors().registerBlockColorHandler(TankTintHandler.INSTANCE, tankBlock);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerColorItem(ColorHandlerEvent.Item event) {
        event.getItemColors().registerItemColorHandler(TankTintHandler.INSTANCE, tankItem);
    }

    @Mod.EventHandler
    @SideOnly(Side.CLIENT)
    public void initClient(FMLInitializationEvent event) {
        ClientRegistry.bindTileEntitySpecialRenderer(TileTank.class, new TileTankRenderer());
    }
}
