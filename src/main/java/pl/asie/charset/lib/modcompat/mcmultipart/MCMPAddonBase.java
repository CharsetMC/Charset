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

package pl.asie.charset.lib.modcompat.mcmultipart;

import mcmultipart.api.addon.IMCMPAddon;
import mcmultipart.api.addon.IWrappedBlock;
import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.multipart.IMultipartRegistry;
import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.ref.MCMPCapabilities;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.capability.CapabilityProviderFactory;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class MCMPAddonBase implements IMCMPAddon {
    private static final ResourceLocation KEY = new ResourceLocation("charset:multipart");
    protected final Block block;
    protected final Item item;
    protected final Supplier<IMultipart> multipartSupplier;
    protected final Function<TileEntity, IMultipartTile> multipartTileSupplier;
    protected final Predicate<TileEntity> tileEntityPredicate;
    private CapabilityProviderFactory<IMultipartTile> factory;

    public MCMPAddonBase(Block block, Item item, Supplier<IMultipart> multipartSupplier, Predicate<TileEntity> tileEntityPredicate) {
        this(block, item, multipartSupplier, IMultipartTile::wrap, tileEntityPredicate);
    }

    public MCMPAddonBase(Block block, Item item, Supplier<IMultipart> multipartSupplier, Function<TileEntity, IMultipartTile> multipartTileSupplier, Predicate<TileEntity> tileEntityPredicate) {
        this.block = block;
        this.item = item;
        this.multipartSupplier = multipartSupplier;
        this.multipartTileSupplier = multipartTileSupplier;
        this.tileEntityPredicate = tileEntityPredicate;

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public final void registerParts(IMultipartRegistry registry) {
        registry.registerPartWrapper(block, multipartSupplier.get());
        registerStackWrapper(registry);
        factory = new CapabilityProviderFactory<>(MCMPCapabilities.MULTIPART_TILE);
    }

    protected IWrappedBlock registerStackWrapper(IMultipartRegistry registry) {
        IWrappedBlock b = registry.registerStackWrapper(item, (stack) -> true, block);
        b.setPartPlacementLogic(MCMPUtils::placePartAt);
        return b;
    }

    @SubscribeEvent
    public final void onAttachTile(AttachCapabilitiesEvent<TileEntity> event) {
        if (tileEntityPredicate.test(event.getObject())) {
            final IMultipartTile multipartTile = multipartTileSupplier.apply(event.getObject());
            event.addCapability(KEY, factory.create(multipartTile));
        }
    }
}
