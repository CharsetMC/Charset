package pl.asie.charset.lib.wires;

import mcmultipart.api.capability.MCMPCapabilities;
import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.world.IMultipartWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.blocks.TileBase;

public class TileWire extends TileBase implements IMultipartTile, IWireContainer {
    private Wire wire;

    @Override
    public void invalidate() {
        super.invalidate();
        wire = null;
    }

    @Override
    public void readNBTData(NBTTagCompound nbt, boolean isClient) {
        if (nbt.hasKey("f", Constants.NBT.TAG_BYTE)) {
            WireProvider factory = WireManager.REGISTRY.getObjectById(nbt.getByte("f"));
            WireFace location = WireFace.VALUES[nbt.getByte("l")];
            wire = factory.create(this, location);
            wire.readNBTData(nbt, isClient);
        } else {
            wire = null;
        }
    }

    @Override
    public NBTTagCompound writeNBTData(NBTTagCompound nbt, boolean isClient) {
        if (wire != null) {
            nbt.setByte("f", (byte) WireManager.REGISTRY.getId(wire.getFactory()));
            nbt.setByte("l", (byte) wire.getLocation().ordinal());
            nbt = wire.writeNBTData(nbt, isClient);
        }
        return nbt;
    }

    private int getItemMetadata() {
        return WireManager.REGISTRY.getId(wire.getFactory()) * 2 + (wire.getLocation() == WireFace.CENTER ? 1 : 0);
    }

    @Override
    public ItemStack getDroppedBlock() {
        return new ItemStack(CharsetLibWires.itemWire, 1, getItemMetadata());
    }

    public void onPlacedBy(EnumFacing facing, ItemStack stack) {
        wire = WireManager.ITEM.fromStack(this, stack, facing);
        markBlockForUpdate();
    }

    @Override
    public World world() {
        return world;
    }

    @Override
    public BlockPos pos() {
        return pos;
    }

    @Override
    public void requestNeighborUpdate(int connectionMask) {
        if ((connectionMask & 0xFF) != 0) {
            IPartInfo info = ((IMultipartWorld) world).getPartInfo();
            info.getContainer().notifyChange(info);
        }

        for (EnumFacing facing : EnumFacing.VALUES) {
            if ((connectionMask & (1 << (facing.ordinal() + 8))) != 0) {
                world.neighborChanged(pos.offset(facing), getBlockType(), pos);
            }

            if (wire.getLocation() != WireFace.CENTER && (connectionMask & (1 << (facing.ordinal() + 16))) != 0) {
                world.neighborChanged(pos.offset(facing).offset(wire.getLocation().facing), getBlockType(), pos);
            }
        }
    }

    @Override
    public void requestNetworkUpdate() {
        markBlockForUpdate();
    }

    @Override
    public void requestRenderUpdate() {
        markBlockForRenderUpdate();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == MCMPCapabilities.MULTIPART_TILE || (wire != null && wire.hasCapability(capability, facing)) || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == MCMPCapabilities.MULTIPART_TILE) {
            return MCMPCapabilities.MULTIPART_TILE.cast(this);
        }

        if (wire != null) {
            T result = wire.getCapability(capability, facing);
            if (result != null) {
                return result;
            }
        }

        return super.getCapability(capability, facing);
    }
}
