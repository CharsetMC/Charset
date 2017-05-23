package pl.asie.charset.lib.wires;

import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.ref.MCMPCapabilities;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.block.TileBase;

public class TileWire extends TileBase implements IMultipartTile, ITickable, IWireContainer {
    protected Wire wire;

    @Override
    public void update() {
        super.update();
        if (wire != null) {
            wire.update();
        }
    }

    @Override
    public void readNBTData(NBTTagCompound nbt, boolean isClient) {
        if (nbt.hasKey("f")) {
            WireProvider factory = WireManager.REGISTRY.getObjectById(nbt.getByte("f"));
            WireFace location = WireFace.VALUES[nbt.getByte("l")];
            wire = factory.create(this, location);
            wire.readNBTData(nbt, isClient);
            if (isClient) {
                markBlockForRenderUpdate();
            }
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

    @Override
    public ItemStack getDroppedBlock() {
        if (wire != null) {
            return CharsetLibWires.itemWire.toStack(wire.getFactory(), wire.getLocation() == WireFace.CENTER, 1);
        } else {
            return ItemStack.EMPTY;
        }
    }

    public void onPlacedBy(WireFace facing, ItemStack stack) {
        wire = CharsetLibWires.itemWire.fromStack(this, stack, facing.facing);
        wire.onChanged(true);
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
        CharsetLibWires.blockWire.requestNeighborUpdate(world, pos, wire.getLocation(), connectionMask);
    }

    @Override
    public void requestNetworkUpdate() {
        markBlockForUpdate();
    }

    @Override
    public void requestRenderUpdate() {
        requestNetworkUpdate();
        markBlockForRenderUpdate();
    }

    @Override
    public void dropWire() {
        Block.spawnAsEntity(world, pos, getDroppedBlock());
        world.setBlockToAir(pos);
    }

    // I'm a horrible, horrible dev
    protected static boolean isWireCheckingForCaps;

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == MCMPCapabilities.MULTIPART_TILE || (wire != null && !isWireCheckingForCaps && wire.hasCapability(capability, facing)) || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == MCMPCapabilities.MULTIPART_TILE) {
            return MCMPCapabilities.MULTIPART_TILE.cast(this);
        }

        if (wire != null && !isWireCheckingForCaps) {
            T result = wire.getCapability(capability, facing);
            if (result != null) {
                return result;
            }
        }

        return super.getCapability(capability, facing);
    }
}
