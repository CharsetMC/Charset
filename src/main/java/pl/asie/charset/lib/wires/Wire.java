package pl.asie.charset.lib.wires;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.render.model.IRenderComparable;
import pl.asie.charset.lib.utils.OcclusionUtils;
import pl.asie.charset.lib.utils.UnlistedPropertyGeneric;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public abstract class Wire implements ICapabilityProvider, IRenderComparable<Wire>, IWireProxy {
    public static final IUnlistedProperty<Wire> PROPERTY = new UnlistedPropertyGeneric<>("wire", Wire.class);

    private final IWireContainer container;
    private final WireProvider factory;
    private final WireFace location;
    private final net.minecraftforge.common.capabilities.CapabilityDispatcher capabilities;

    private byte internalConnections, externalConnections, cornerConnections, occludedSides, cornerOccludedSides;
    private boolean connectionCheckDirty;

    protected Wire(IWireContainer container, WireProvider factory, WireFace location) {
        this.container = container;
        this.factory = factory;
        this.location = location;

        AttachCapabilitiesEvent<Wire> event = new AttachCapabilitiesEvent<Wire>(Wire.class,this);
        MinecraftForge.EVENT_BUS.register(event);
        this.capabilities = event.getCapabilities().size() > 0 ? new CapabilityDispatcher(event.getCapabilities()) : null;
    }

    public void readNBTData(NBTTagCompound nbt, boolean isClient) {
        internalConnections = nbt.getByte("iC");
        externalConnections = nbt.getByte("eC");
        cornerConnections = nbt.getByte("cC");
        occludedSides = nbt.getByte("oS");
        cornerOccludedSides = nbt.getByte("coS");
    }

    public NBTTagCompound writeNBTData(NBTTagCompound nbt, boolean isClient) {
        nbt.setByte("l", (byte) location.ordinal());
        nbt.setByte("iC", internalConnections);
        nbt.setByte("eC", externalConnections);
        if (location != WireFace.CENTER) {
            nbt.setByte("cC", cornerConnections);
        }
        if (!isClient) {
            nbt.setByte("oS", occludedSides);
            nbt.setByte("coS", cornerOccludedSides);
        }
        return nbt;
    }

    public void onChanged() {
        connectionCheckDirty = true;
    }

    public int getRenderColor() {
        return -1;
    }

    public final IWireContainer getContainer() {
        return container;
    }

    public final WireProvider getFactory() {
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
        return ((internalConnections | externalConnections | cornerConnections) & (1 << direction.ordinal())) != 0;
    }

    public final boolean connectsCorner(EnumFacing direction) {
        return (cornerConnections & (1 << direction.ordinal())) != 0;
    }

    public final boolean connects(EnumFacing direction) {
        return ((internalConnections | externalConnections) & (1 << direction.ordinal())) != 0;
    }

    public final boolean isOccluded(EnumFacing face) {
        return (occludedSides & (1 << face.ordinal())) != 0;
    }

    public final boolean isCornerOccluded(EnumFacing face) {
        if (connectionCheckDirty) {
            connectionCheckDirty = false;
            updateConnections();
        }
        return isOccluded(face) || (cornerOccludedSides & (1 << face.ordinal())) != 0;
    }

    protected final void updateConnections() {
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

        int oldConnectionCache = internalConnections | externalConnections << 8 | cornerConnections << 16;
        internalConnections = externalConnections = cornerConnections = occludedSides = cornerOccludedSides = 0;

        // Occlusion test

        EnumFacing[] connFaces = WireUtils.getConnectionsForRender(location);
        for (int i = 0; i < connFaces.length; i++) {
            WireFace face = WireFace.get(connFaces[i]);
            if (validSides.contains(face)) {
                boolean found = false;
                AxisAlignedBB mask = factory.getBox(location, i + 1);
                if (mask != null) {
                    if (!OcclusionUtils.INSTANCE.intersects(Collections.singletonList(mask), container.world(), container.pos())) {
                        occludedSides |= 1 << connFaces[i].ordinal();
                        validSides.remove(face);
                        found = true;
                    }
                }

                if (!found && location != WireFace.CENTER) {
                    BlockPos cPos = container.pos().offset(connFaces[i]);
                    AxisAlignedBB cornerMask = factory.getCornerBox(location, i ^ 1);
                    if (cornerMask != null) {
                        if (!OcclusionUtils.INSTANCE.intersects(Collections.singletonList(cornerMask), container.world(), cPos)) {
                            cornerOccludedSides |= 1 << connFaces[i].ordinal();
                            invalidCornerSides.add(face);
                        }
                    }
                }
            }
        }

        if (validSides.contains(WireFace.CENTER)) {
            AxisAlignedBB mask = factory.getBox(WireFace.CENTER, 1 + location.ordinal());
            if (mask != null) {
                if (!OcclusionUtils.INSTANCE.intersects(Collections.singletonList(mask), container.world(), container.pos())) {
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

        int newConnectionCache = internalConnections | externalConnections << 8 | cornerConnections << 16;

        if (oldConnectionCache != newConnectionCache) {
            container.requestNeighborUpdate(oldConnectionCache ^ newConnectionCache);
            container.requestRenderUpdate();
        }

        connectionCheckDirty = false;
    }

    protected boolean canConnectWire(Wire wire) {
        return wire.getFactory() == getFactory();
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
}
