package pl.asie.charset.lib.wires;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.util.INBTSerializable;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.render.model.IRenderComparable;
import pl.asie.charset.lib.utils.UnlistedPropertyGeneric;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public abstract class Wire implements ICapabilityProvider, INBTSerializable<NBTTagCompound>, IRenderComparable<Wire> {
    public static final IUnlistedProperty<Wire> PROPERTY = new UnlistedPropertyGeneric<>("wire", Wire.class);

    protected final WireProvider factory;
    protected byte internalConnections, externalConnections, cornerConnections, occludedSides, cornerOccludedSides;
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

    public WireProvider getFactory() {
        return factory;
    }

    public int getRenderColor() {
        return -1;
    }

    public boolean connectsInternal(WireFace side) {
        return (internalConnections & (1 << side.ordinal())) != 0;
    }

    public boolean connectsExternal(EnumFacing side) {
        return (externalConnections & (1 << side.ordinal())) != 0;
    }

    public boolean connectsAny(EnumFacing direction) {
        return ((internalConnections | externalConnections | cornerConnections) & (1 << direction.ordinal())) != 0;
    }

    public boolean connectsCorner(EnumFacing direction) {
        return (cornerConnections & (1 << direction.ordinal())) != 0;
    }

    public boolean connects(EnumFacing direction) {
        return ((internalConnections | externalConnections) & (1 << direction.ordinal())) != 0;
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
