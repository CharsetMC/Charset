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

package pl.asie.charset.module.tweak.carry;

import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.CharsetIMC;
import pl.asie.charset.lib.config.CharsetLoadConfigEvent;
import pl.asie.charset.lib.config.ConfigUtils;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.lib.utils.ThreeState;

import java.util.HashSet;
import java.util.Set;

@CharsetModule(
    name = "tweak.carry",
    description = "Allow players to carry blocks via shift-pickblock",
    profile = ModuleProfile.STABLE
)
public class CharsetTweakBlockCarrying {
    public static final ResourceLocation CAP_IDENTIFIER = new ResourceLocation("charsettweaks:carry");

    @CapabilityInject(CarryHandler.class)
    public static Capability<CarryHandler> CAPABILITY;

    @CharsetModule.Configuration
    public static Configuration config;

    @CharsetModule.PacketRegistry
    public static PacketRegistry packet;

    public static boolean enabledCreative;
    // public static boolean enabledSharing;

    @Mod.EventHandler
    public void loadConfig(CharsetLoadConfigEvent event) {
        enabledCreative = ConfigUtils.getBoolean(config, "general", "enabledInCreative", true, "Should block carrying be enabled in creative mode?", true);
        // enabledSharing = ConfigUtils.getBoolean(config, "general", "enablePlayerSharing", true, "Should players be able to give blocks they are carrying to other players?", true);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        packet.registerPacket(0x01, PacketCarryGrab.class);
        packet.registerPacket(0x02, PacketCarrySync.class);

        CarryTransformerRegistry.INSTANCE.registerEntityTransformer(new CarryTransformerEntityMinecart());

        /* if (enabledSharing) {
            CarryTransformerRegistry.INSTANCE.registerEntityTransformer(new CarryTransformerPlayerShare());
        } */

        CarryHandler.register();

        MinecraftForge.EVENT_BUS.register(new TweakCarryEventHandler());
    }

    @Mod.EventHandler
    @SideOnly(Side.CLIENT)
    public void initClient(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new TweakCarryRender());
    }

    protected static boolean canCarry(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        boolean hasTileEntity = state.getBlock().hasTileEntity(state);
        boolean isVanilla = "minecraft".equals(block.getRegistryName().getResourceDomain());

        // Set<String> names = new HashSet<>();
        Set<ResourceLocation> locs = new HashSet<>();

        locs.add(state.getBlock().getRegistryName());
        // names.add(state.getBlock().getClass().getName());

        if (hasTileEntity) {
            TileEntity tile = world.getTileEntity(pos);

            if (tile != null) {
                Class<? extends TileEntity> tileClass = tile.getClass();
                locs.add(TileEntity.getKey(tileClass));
                // names.add(tileClass.getName());
            }
        }

        /* for (ResourceLocation r : locs)
            names.add(r.toString()); */

        ThreeState allowedIMC = CharsetIMC.INSTANCE.allows("carry", locs);

        if (allowedIMC == ThreeState.NO) {
            return false;
        } else if (allowedIMC == ThreeState.YES) {
            return true;
        }

        // We support all vanilla tile entities.
        if (!isVanilla && hasTileEntity) return false;

        // Class-based bans
        // if (block instanceof IPlantable) return false;
        if (block instanceof BlockPistonExtension || block instanceof BlockPistonMoving) return false;
        if (block instanceof BlockPistonBase) {
            // If we don't have the BlockPistonBase.EXTENDED key, this piston
            // uses foreign states - do not trust it!
            if (!state.getPropertyKeys().contains(BlockPistonBase.EXTENDED)
                    || state.getValue(BlockPistonBase.EXTENDED)) {
                return false;
            }
        }
        if (block instanceof IFluidBlock || block instanceof BlockLiquid) return false;
        if (block instanceof BlockPortal || block instanceof BlockEndPortal) return false;

        return true;
    }

    protected static boolean canCarry(Entity entity) {
        Class<? extends Entity> entityClass = entity.getClass();
        EntityEntry entry = EntityRegistry.getEntry(entityClass);
        if (entry == null) {
            ModCharset.logger.warn(entityClass.getName() + " has no EntityEntry!");
        } else {
            ThreeState allowedIMC = CharsetIMC.INSTANCE.allows("carry", entry.getRegistryName());

            if (allowedIMC == ThreeState.NO) {
                return false;
            } else if (allowedIMC == ThreeState.YES) {
                return true;
            }
        }

        return true;
    }

    public static boolean canPlayerConsiderCarryingBlock(EntityPlayer player) {
        return player.getHeldItem(EnumHand.MAIN_HAND).isEmpty()
                && player.getHeldItem(EnumHand.OFF_HAND).isEmpty()
                && !player.isPlayerSleeping()
                && !player.isRiding()
                && (player.openContainer == null || player.openContainer == player.inventoryContainer);
    }

    public static void grabBlock(EntityPlayer player, World world, BlockPos pos) {
        if (!(player instanceof EntityPlayerMP)) {
            packet.sendToServer(new PacketCarryGrab(world, pos));
            // TODO: Figure out a way to revert grabs
            return;
        }

        CarryHandler carryHandler = player.getCapability(CharsetTweakBlockCarrying.CAPABILITY, null);
        if (carryHandler != null && !carryHandler.isCarrying()) {
            boolean canCarry = canCarry(world, pos);

            if (canCarry) {
                // Can the player break this block?
                if (world.getBlockState(pos).getPlayerRelativeBlockHardness(player, world, pos) < 0f) {
                    canCarry = false;
                }
            }

            if (canCarry) {
                // Can the player modify this block position? (world border, spawn protection)
                if (!world.isBlockModifiable(player, pos)) {
                    canCarry = false;
                }
            }

            if (canCarry) {
                // Can the player /really/ break this block? (last, most expensive)
                BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, world.getBlockState(pos), player);
                if (MinecraftForge.EVENT_BUS.post(event)) {
                    canCarry = false;
                }
            }

            if (canCarry) {
                carryHandler.grab(world, pos);
                syncCarryWithAllClients(player);
            } else {
                // Sync in case the client said "yes".
                syncCarryWithClient(player, player);
            }
        }
    }

    public static void grabEntity(EntityPlayer player, World world, Entity entity) {
        if (!(player instanceof EntityPlayerMP)) {
            packet.sendToServer(new PacketCarryGrab(world, entity));
        }

        CarryHandler carryHandler = player.getCapability(CharsetTweakBlockCarrying.CAPABILITY, null);
        if (carryHandler != null && !carryHandler.isCarrying()) {
            if (canCarry(entity)) {
                for (ICarryTransformer<Entity> transformer : CarryTransformerRegistry.INSTANCE.getEntityTransformers()) {
                    if (transformer.extract(entity, true) != null) {
                        Pair<IBlockState, TileEntity> pair = transformer.extract(entity, false);
                        carryHandler.put(pair.getLeft(), pair.getRight());
                        syncCarryWithAllClients(player);
                        return;
                    }
                }
            } else {
                // Sync in case the client said "yes", and revert the block's changes.
                syncCarryWithClient(player, player);
            }
        }
    }

    protected static boolean dropCarriedBlock(EntityPlayer entity, boolean must) {
        return dropCarriedBlock(entity, must, (must ? 4 : 2));
    }

    protected static boolean dropCarriedBlock(EntityPlayer entity, boolean must, int maxRadius) {
        CarryHandler carryHandler = entity.getCapability(CAPABILITY, null);
        if (carryHandler != null && carryHandler.isCarrying()) {
            World world = entity.getEntityWorld();
            if (world.isRemote) {
                carryHandler.empty();
                return true;
            }

            BlockPos base = entity.getPosition();
            for (int method = 0; method <= (must ? 2 : 1); method++) {
                for (int radius = 0; radius <= maxRadius; radius++) {
                    Vec3i radiusVec = new Vec3i(radius, radius, radius);
                    for (BlockPos pos : BlockPos.getAllInBoxMutable(base.subtract(radiusVec), base.add(radiusVec))) {
                        if (world.getBlockState(pos).getBlock().isReplaceable(world, pos)
                                && (method > 1 || !world.isAirBlock(pos.down()))
                                && (method > 0 || world.isSideSolid(pos.down(), EnumFacing.UP))
                                ) {
                            carryHandler.place(world, pos.toImmutable(), EnumFacing.UP, entity);
                        }
                        if (!carryHandler.isCarrying()) break;
                    }
                    if (!carryHandler.isCarrying()) break;
                }
                if (!carryHandler.isCarrying()) break;
            }

            if (carryHandler.isCarrying()) {
                if (must) {
                    ModCharset.logger.error("Could not drop carried block from player " + entity.getName() + "! This is a bug!");
                }
                return false;
            } else {
                CharsetTweakBlockCarrying.syncCarryWithAllClients(entity);
                return true;
            }
        } else {
            return true;
        }
    }

    protected static void syncCarryWithClient(Entity who, EntityPlayer target) {
        if (who instanceof EntityPlayerMP && who.hasCapability(CAPABILITY, null)) {
            packet.sendTo(new PacketCarrySync(who, who == target), target);
        }
    }

    protected static void syncCarryWithAllClients(Entity who) {
        if (who instanceof EntityPlayerMP && who.hasCapability(CAPABILITY, null)) {
            packet.sendTo(new PacketCarrySync(who, true), (EntityPlayer) who);
            packet.sendToWatching(new PacketCarrySync(who, false), who, who);
        }
    }
}
