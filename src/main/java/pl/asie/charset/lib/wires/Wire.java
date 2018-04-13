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

package pl.asie.charset.lib.wires;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.render.model.IRenderComparable;
import pl.asie.charset.lib.utils.OcclusionUtils;
import pl.asie.charset.lib.utils.UnlistedPropertyGeneric;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public abstract class Wire implements ITickable, ICapabilityProvider, IRenderComparable<Wire> {
    public static final IUnlistedProperty<Wire> PROPERTY = new UnlistedPropertyGeneric<>("wire", Wire.class);

    private final @Nonnull IWireContainer container;
    private final @Nonnull WireProvider factory;
    private final @Nonnull WireFace location;
    private final @Nullable net.minecraftforge.common.capabilities.CapabilityDispatcher capabilities;

    private byte internalConnections, externalConnections, cornerConnections, occludedSides, cornerOccludedSides;
    private boolean connectionCheckDirty;

    protected Wire(@Nonnull IWireContainer container, @Nonnull WireProvider factory, @Nonnull WireFace location) {
        this.container = container;
        this.factory = factory;
        this.location = location;

        AttachCapabilitiesEvent<Wire> event = new AttachCapabilitiesEvent<>(Wire.class, this);
        MinecraftForge.EVENT_BUS.post(event);
        this.capabilities = event.getCapabilities().size() > 0 ? new CapabilityDispatcher(event.getCapabilities()) : null;
    }

    protected final void scheduleConnectionUpdate() {
        connectionCheckDirty = true;
    }

    private ICapabilityProvider getCapabilityProviderRemoteBlock(BlockPos pos) {
        return WireUtils.getCapabilityProvider(this, pos, true);
    }

    private ICapabilityProvider getCapabilityProviderRemote(BlockPos pos, WireFace face, boolean blocks) {
        Wire wire = WireUtils.getWire(getContainer().world(), pos, face);
        if (wire != null) {
            return wire;
        } else {
            if (blocks) {
                return getCapabilityProviderRemoteBlock(pos);
            } else {
                return null;
            }
        }
    }

    private <T> T getCapabilityRemoteBlock(BlockPos pos, EnumFacing facing, Capability<T> capability) {
        return WireUtils.getCapability(this, pos, capability, facing, true);
    }

    private <T> T getCapabilityRemote(BlockPos pos, WireFace face, EnumFacing facing, boolean blocks, Capability<T> capability) {
        Wire wire = WireUtils.getWire(getContainer().world(), pos, face);
        if (wire != null && wire.hasCapability(capability, facing)) {
            return wire.getCapability(capability, facing);
        } else {
            if (blocks) {
                return getCapabilityRemoteBlock(pos, facing, capability);
            } else {
                return null;
            }
        }
    }

    protected final Iterable<Pair<ICapabilityProvider, EnumFacing>> connectedIterator(boolean connectsBelowWire) {
        return () -> new Iterator<Pair<ICapabilityProvider, EnumFacing>>() {
            private final BlockPos pos = getContainer().pos();
            private final WireFace loc = getLocation();
            private Pair<ICapabilityProvider, EnumFacing> queued = find();
            private int i = 0;

            private Pair<ICapabilityProvider, EnumFacing> find() {
                ICapabilityProvider result = null;
                EnumFacing resultFace = null;
                while (result == null && i < 20) {
                    switch (i) {
                        case 0: {
                            if (getLocation() != WireFace.CENTER && connectsBelowWire) {
                                result = getCapabilityProviderRemoteBlock(pos.offset(loc.facing));
                                resultFace = loc.facing.getOpposite();
                            }
                        } break;
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                        case 7: {
                            WireFace face = WireFace.VALUES[i - 1];
                            if (face != loc && connectsInternal(face)) {
                                result = getCapabilityProviderRemote(pos, face, false);
                                resultFace = getLocation().facing;
                            }
                        } break;
                        case 8:
                        case 9:
                        case 10:
                        case 11:
                        case 12:
                        case 13: {
                            EnumFacing facing = EnumFacing.getFront(i - 8);
                            if (connectsExternal(facing)) {
                                result = getCapabilityProviderRemote(pos.offset(facing), loc, true);
                                resultFace = facing.getOpposite();
                            }
                        } break;
                        case 14:
                        case 15:
                        case 16:
                        case 17:
                        case 18:
                        case 19: {
                            EnumFacing facing = EnumFacing.getFront(i - 14);
                            if (connectsCorner(facing)) {
                                result = getCapabilityProviderRemote(pos.offset(facing).offset(loc.facing), WireFace.get(facing.getOpposite()), false);
                                resultFace = loc.facing.getOpposite();
                            }
                        } break;
                    }

                    i++;
                }
                return result != null ? Pair.of(result, resultFace) : null;
            }

            @Override
            public boolean hasNext() {
                return queued != null;
            }

            @Override
            public Pair<ICapabilityProvider, EnumFacing> next() {
                Pair<ICapabilityProvider, EnumFacing> current = queued;
                queued = find();
                return current;
            }
        };
    }

    protected final <T> Iterable<T> connectedIterator(Capability<T> capability, boolean connectsBelowWire) {
        return () -> new Iterator<T>() {
            private final BlockPos pos = getContainer().pos();
            private final WireFace loc = getLocation();
            private T queued = find();
            private int i = 0;

            private T find() {
                T result = null;
                while (result == null && i < 20) {
                    switch (i) {
                        case 0: {
                            if (getLocation() != WireFace.CENTER && connectsBelowWire) {
                                result = getCapabilityRemoteBlock(pos.offset(loc.facing), loc.facing.getOpposite(), capability);
                            }
                        } break;
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                        case 7: {
                            WireFace face = WireFace.VALUES[i - 1];
                            if (face != loc && connectsInternal(face)) {
                                result = getCapabilityRemote(pos, face, getLocation().facing, false, capability);
                            }
                        } break;
                        case 8:
                        case 9:
                        case 10:
                        case 11:
                        case 12:
                        case 13: {
                            EnumFacing facing = EnumFacing.getFront(i - 8);
                            if (connectsExternal(facing)) {
                                result = getCapabilityRemote(pos.offset(facing), loc, facing.getOpposite(), true, capability);
                            }
                        } break;
                        case 14:
                        case 15:
                        case 16:
                        case 17:
                        case 18:
                        case 19: {
                            EnumFacing facing = EnumFacing.getFront(i - 14);
                            if (connectsCorner(facing)) {
                                result = getCapabilityRemote(pos.offset(facing).offset(loc.facing), WireFace.get(facing.getOpposite()), loc.facing.getOpposite(), false, capability);
                            }
                        } break;
                    }

                    i++;
                }
                return result;
            }

            @Override
            public boolean hasNext() {
                return queued != null;
            }

            @Override
            public T next() {
                T current = queued;
                queued = find();
                return current;
            }
        };
    }

    @Override
    public void update() {
        if (connectionCheckDirty) {
            updateConnections();
        }
    }

    public void readNBTData(NBTTagCompound nbt, boolean isClient) {
        internalConnections = nbt.getByte("iC");
        externalConnections = nbt.getByte("eC");
        cornerConnections = nbt.hasKey("cC") ? nbt.getByte("cC") : 0;
        occludedSides = nbt.hasKey("oS") ? nbt.getByte("oS") : 0;
        cornerOccludedSides = nbt.hasKey("coS") ? nbt.getByte("coS") : 0;
    }

    public NBTTagCompound writeNBTData(NBTTagCompound nbt, boolean isClient) {
        if (isClient) {
            updateConnections();
        } else {
            nbt.setByte("oS", occludedSides);
            nbt.setByte("coS", cornerOccludedSides);
        }
        nbt.setByte("iC", internalConnections);
        nbt.setByte("eC", externalConnections);
        if (location != WireFace.CENTER) {
            nbt.setByte("cC", cornerConnections);
        }
        return nbt;
    }

    public void onChanged(boolean external) {
        boolean remote = getContainer().world().isRemote;

        if (external && !remote) {
            if (!factory.canPlace(getContainer().world(), getContainer().pos(), location)) {
                getContainer().dropWire();
            }
        }

        connectionCheckDirty = true;
        if (remote && getContainer().pos() != null) {
            updateConnections();
        }
    }

    public int getRenderColor() {
        return -1;
    }

    public final IWireContainer getContainer() {
        return container;
    }

    public final WireProvider getProvider() {
        return factory;
    }

    public final WireFace getLocation() {
        return location;
    }

    public final boolean connectsInternal(WireFace side) {
        return (internalConnections & (1 << side.ordinal())) != 0;
    }

    public final boolean connectsExternal(EnumFacing side) {
        return (externalConnections & (1 << side.ordinal())) != 0;
    }

    public final boolean connectsAny(EnumFacing direction) {
        return ((internalConnections | externalConnections | cornerConnections) & (1 << (direction != null ? direction.ordinal() : 6))) != 0;
    }

    public final boolean connectsCorner(EnumFacing direction) {
        return (cornerConnections & (1 << direction.ordinal())) != 0;
    }

    public final boolean connects(EnumFacing direction) {
        return ((internalConnections | externalConnections) & (1 << (direction != null ? direction.ordinal() : 6))) != 0;
    }

    public final boolean isOccluded(EnumFacing face) {
        return (occludedSides & (1 << face.ordinal())) != 0;
    }

    public final boolean isCornerOccluded(EnumFacing face) {
        return isOccluded(face) || (cornerOccludedSides & (1 << face.ordinal())) != 0;
    }

    protected void updateConnections() {
        if (!connectionCheckDirty) {
            return;
        }

        Set<WireFace> validSides = EnumSet.noneOf(WireFace.class);
        Set<WireFace> invalidCornerSides = EnumSet.noneOf(WireFace.class);

        for (WireFace facing : WireFace.VALUES) {
            if (facing == location) {
                continue;
            }

            if (facing != WireFace.CENTER && location != WireFace.CENTER && location.facing.getAxis() == facing.facing.getAxis()) {
                continue;
            }

            validSides.add(facing);
        }

        int oldConnectionCache = getConnectionMask();
        internalConnections = externalConnections = cornerConnections = occludedSides = cornerOccludedSides = 0;

        // Occlusion test

        EnumFacing[] connFaces = WireUtils.getConnectionsForRender(location);
        for (int i = 0; i < connFaces.length; i++) {
            WireFace face = WireFace.get(connFaces[i]);
            if (validSides.contains(face)) {
                boolean found = false;
                AxisAlignedBB mask = factory.getBox(location, i + 1);
                if (mask != null) {
                    if (OcclusionUtils.INSTANCE.intersects(Collections.singletonList(mask), container.world(), container.pos())) {
                        occludedSides |= 1 << connFaces[i].ordinal();
                        validSides.remove(face);
                        found = true;
                    }
                }
/*
                if (!found && location != WireFace.CENTER) {
                    BlockPos cPos = container.pos().offset(connFaces[i]);
                    AxisAlignedBB cornerMask = factory.getCornerBox(location, i ^ 1);
                    if (cornerMask != null) {
                        if (OcclusionUtils.PRIMARY.intersects(Collections.singletonList(cornerMask), container.world(), cPos)) {
                            cornerOccludedSides |= 1 << connFaces[i].ordinal();
                            invalidCornerSides.add(face);
                        }
                    }
                } */
            }
        }

        if (validSides.contains(WireFace.CENTER)) {
            AxisAlignedBB mask = factory.getBox(WireFace.CENTER, 1 + location.ordinal());
            if (mask != null) {
                if (OcclusionUtils.INSTANCE.intersects(Collections.singletonList(mask), container.world(), container.pos())) {
                    occludedSides |= 1 << 6;
                    validSides.remove(WireFace.CENTER);
                }
            }
        }

        // Connection test
        for (WireFace facing : validSides) {
            if (WireUtils.canConnectInternal(this, facing)) {
                internalConnections |= 1 << facing.ordinal();
            } else if (facing != WireFace.CENTER) {
                if (WireUtils.canConnectExternal(this, facing.facing)) {
                    externalConnections |= 1 << facing.ordinal();
                } else if (location != WireFace.CENTER && !invalidCornerSides.contains(facing) && WireUtils.canConnectCorner(this, facing.facing)) {
                    cornerConnections |= 1 << facing.ordinal();
                }
            }
        }

        int newConnectionCache = getConnectionMask();

        if (oldConnectionCache != newConnectionCache) {
            container.requestNeighborUpdate(oldConnectionCache ^ newConnectionCache);
            container.requestRenderUpdate();
        }

        connectionCheckDirty = false;
    }

    protected int getWeakPower(EnumFacing side) {
        return 0;
    }

    protected int getStrongPower(EnumFacing side) {
        return 0;
    }

    protected int getConnectionMask() {
        return internalConnections | externalConnections << 8 | cornerConnections << 16;
    }

    protected boolean canConnectWire(Wire wire) {
        return wire.getProvider() == getProvider();
    }

    public boolean canConnectRedstone(EnumFacing side) {
        return false;
    }

    public boolean shouldCheckWeakPower(EnumFacing side) {
        return false;
    }

    protected boolean canConnectBlock(BlockPos pos, EnumFacing direction) {
        return false;
    }

    public void setConnectionsForItemRender() {
        internalConnections = 0x3F;
        externalConnections = 0;
        cornerConnections = 0;
    }

    @Override
    public boolean renderEquals(Wire other) {
        return other.factory == factory
                && other.location == location
                && other.internalConnections == internalConnections
                && other.externalConnections == externalConnections
                && other.cornerConnections == cornerConnections;
    }

    @Override
    public int renderHashCode() {
        return Objects.hash(factory, location, internalConnections, externalConnections, cornerConnections);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (connects(facing)) {
            return capabilities != null ? capabilities.hasCapability(capability, facing) : false;
        } else {
            return false;
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (connects(facing)) {
            return capabilities != null ? capabilities.getCapability(capability, facing) : null;
        } else {
            return null;
        }
    }

    public abstract String getDisplayName();
}
