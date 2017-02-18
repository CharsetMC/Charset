package pl.asie.charset.lib.wires;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.util.INBTSerializable;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.render.model.IRenderComparable;
import pl.asie.charset.lib.utils.OcclusionUtils;
import pl.asie.charset.lib.utils.UnlistedPropertyGeneric;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public abstract class Wire implements ICapabilityProvider, INBTSerializable<NBTTagCompound>, IRenderComparable<Wire> {
    public static final IUnlistedProperty<Wire> PROPERTY = new UnlistedPropertyGeneric<>("wire", Wire.class);

    protected final WireProvider factory;
    protected World world;
    protected BlockPos pos;
    protected byte internalConnections, externalConnections, cornerConnections, occludedSides, cornerOccludedSides;
    protected boolean connectionCheckDirty;
    public WireFace location;

    protected Wire(WireProvider factory) {
        this.factory = factory;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return false;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return null;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {

    }

    public int getRenderColor() {
        return -1;
    }

    public final WireProvider getFactory() {
        return factory;
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
        if (connectionCheckDirty) {
            connectionCheckDirty = false;
            updateConnections();
        }
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

        int oldConnectionCache = internalConnections << 12 | externalConnections << 6 | cornerConnections;
        internalConnections = externalConnections = cornerConnections = occludedSides = cornerOccludedSides = 0;

        // Occlusion test

        EnumFacing[] connFaces = WireUtils.getConnectionsForRender(location);
        for (int i = 0; i < connFaces.length; i++) {
            WireFace face = WireFace.get(connFaces[i]);
            if (validSides.contains(face)) {
                boolean found = false;
                AxisAlignedBB mask = factory.getBox(location, i + 1);
                if (mask != null) {
                    if (!OcclusionUtils.INSTANCE.intersects(Collections.singletonList(mask), world, pos)) {
                        occludedSides |= 1 << connFaces[i].ordinal();
                        validSides.remove(face);
                        found = true;
                    }
                }

                if (!found && location != WireFace.CENTER) {
                    BlockPos cPos = pos.offset(connFaces[i]);
                    AxisAlignedBB cornerMask = factory.getCornerBox(location, i ^ 1);
                    if (cornerMask != null) {
                        if (!OcclusionUtils.INSTANCE.intersects(Collections.singletonList(cornerMask), world, cPos)) {
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
                if (!OcclusionUtils.INSTANCE.intersects(Collections.singletonList(mask), world, pos)) {
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

        int newConnectionCache = internalConnections << 12 | externalConnections << 6 | cornerConnections;

        if (oldConnectionCache != newConnectionCache) {
            // TODO
/*            scheduleNeighborUpdate((oldConnectionCache ^ newConnectionCache) >> 6);
            scheduleLogicUpdate();
            scheduleRenderUpdate();*/
        }
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
}
