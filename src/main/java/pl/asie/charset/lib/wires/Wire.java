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

public abstract class Wire implements ICapabilityProvider, INBTSerializable<NBTTagCompound>, IRenderComparable<Wire> {
    public static final IUnlistedProperty<Wire> PROPERTY = new UnlistedPropertyGeneric<>("wire", Wire.class);

    protected final WireFactory factory;
    public WireFace location;

    protected Wire(WireFactory factory) {
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

    public abstract boolean connects(EnumFacing facing);

    public WireFactory getFactory() {
        return factory;
    }

    public int getRenderColor() {
        return -1;
    }

    public abstract boolean connectsAny(EnumFacing dir);

    public abstract boolean connectsCorner(EnumFacing dir);

    public abstract void setConnectionsForItemRender();
}
