package pl.asie.charset.storage.shelf;

import mcmultipart.MCMultiPartMod;
import mcmultipart.multipart.PartSlot;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import pl.asie.charset.lib.multipart.PartSlab;
import pl.asie.charset.lib.refs.Properties;

import java.util.EnumSet;

public class PartShelf extends PartSlab {
    protected static final AxisAlignedBB[] BOXES = new AxisAlignedBB[8];
    protected final ItemStackHandler inventory = new ItemStackHandler(4);
    protected EnumFacing facing = EnumFacing.NORTH;

    static {
        for (int i = 0; i < 16; i += 8) {
            int j = i >> 1;
            BOXES[0 + j] = new AxisAlignedBB(0, (i + 1) / 16.0, 0.75, 1, (i + 6) / 16.0, 1);
            BOXES[1 + j] = new AxisAlignedBB(0, (i + 1) / 16.0, 0, 1, (i + 6) / 16.0, 0.25);
            BOXES[2 + j] = new AxisAlignedBB(0.75, (i + 1) / 16.0, 0, 1, (i + 6) / 16.0, 1);
            BOXES[3 + j] = new AxisAlignedBB(0, (i + 1) / 16.0, 0, 0.25, (i + 6) / 16.0, 1);
        }
    }

    @Override
    public IBlockState getActualState(IBlockState state) {
        return state.withProperty(PartSlab.IS_TOP, isTop()).withProperty(Properties.FACING4, facing);
    }

    @Override
    public BlockStateContainer createBlockState() {
        return new BlockStateContainer(MCMultiPartMod.multipart, PartSlab.IS_TOP, Properties.FACING4);
    }

    @Override
    public ResourceLocation getModelPath() {
        return new ResourceLocation("charsetstorage:shelf");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setTag("items", inventory.serializeNBT());
        nbt.setByte("facing", (byte) facing.ordinal());
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (nbt.hasKey("items")) {
            inventory.deserializeNBT(nbt.getCompoundTag("items"));
        }
        if (nbt.hasKey("facing")) {
            facing = EnumFacing.getFront(nbt.getByte("facing"));
            if (facing == null || facing.getAxis() == EnumFacing.Axis.Y) {
                facing = EnumFacing.NORTH;
            }
        } else {
            facing = EnumFacing.NORTH;
        }
    }

    @Override
    public void writeUpdatePacket(PacketBuffer buf) {
        super.writeUpdatePacket(buf);
        buf.writeByte(facing.ordinal());
    }

    @Override
    public void readUpdatePacket(PacketBuffer buf) {
        super.readUpdatePacket(buf);
        facing = EnumFacing.getFront(buf.readByte());
    }

    @Override
    public AxisAlignedBB getBox() {
        return BOXES[facing.ordinal() - 2 + (isTop ? 4 : 0)];
    }

    @Override
    public EnumSet<PartSlot> getSlotMask() {
        return EnumSet.of(PartSlot.getFaceSlot(facing));
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
                && (this.facing == facing || (isTop ? facing == EnumFacing.UP : facing == EnumFacing.DOWN));
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
        }
        return null;
    }
}
