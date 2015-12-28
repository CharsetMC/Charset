package pl.asie.charset.gates;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.World;

import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import mcmultipart.MCMultiPartMod;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IRedstonePart;
import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.Multipart;
import mcmultipart.multipart.PartSlot;
import mcmultipart.raytrace.PartMOP;
import pl.asie.charset.api.wires.IConnectable;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.api.wires.WireType;

public abstract class PartGate extends Multipart implements IRedstonePart, ISlottedPart, IConnectable {
    public enum Connection {
        NONE,
        INPUT,
        OUTPUT,
        INPUT_ANALOG,
        OUTPUT_ANALOG,
        INPUT_BUNDLED,
        OUTPUT_BUNDLED;

        public boolean isInput() {
            return this == INPUT || this == INPUT_ANALOG || this == INPUT_BUNDLED;
        }

        public boolean isOutput() {
            return this == OUTPUT || this == OUTPUT_ANALOG || this == OUTPUT_BUNDLED;
        }

        public boolean isRedstone() {
            return this == INPUT || this == OUTPUT || this == INPUT_ANALOG || this == OUTPUT_ANALOG;
        }

        public boolean isDigital() {
            return this == INPUT || this == OUTPUT;
        }

        public boolean isAnalog() {
            return this == INPUT_ANALOG || this == OUTPUT_ANALOG;
        }

        public boolean isBundled() {
            return this == INPUT_BUNDLED || this == OUTPUT_BUNDLED;
        }
    }

    public enum State {
        NO_RENDER,
        OFF,
        ON,
        DISABLED;

        public State invert() {
            switch (this) {
                case OFF:
                    return ON;
                case ON:
                    return OFF;
                default:
                    return this;
            }
        }

        public static State input(byte i) {
            return i > 0 ? ON : OFF;
        }

        public static State bool(boolean v) {
            return v ? ON : OFF;
        }
    }

    protected byte enabledSides, invertedSides;

    private byte[] inputs = new byte[4];
    private byte[] outputClient = new byte[4];

    private EnumFacing side = EnumFacing.DOWN;
    private EnumFacing top = EnumFacing.NORTH;

    public PartGate() {
        enabledSides = getSideMask();
    }

    PartGate setSide(EnumFacing facing) {
        this.side = facing;
        return this;
    }

    PartGate setTop(EnumFacing facing) {
        this.top = facing;
        return this;
    }

    PartGate setInvertedSides(int sides) {
        this.invertedSides = (byte) sides;
        return this;
    }

    public Connection getType(EnumFacing dir) {
        return dir == EnumFacing.NORTH ? Connection.OUTPUT : Connection.INPUT;
    }
    public abstract State getLayerState(int id);
    public abstract State getTorchState(int id);
    public boolean canBlockSide(EnumFacing side) {
        return getType(side).isInput();
    }
    public boolean canInvertSide(EnumFacing side) {
        return getType(side).isInput() && getType(side).isDigital();
    }

    @Override
    public boolean canConnect(WireType type, WireFace face, EnumFacing direction) {
        if (face.facing == side && direction.getAxis() != side.getAxis()) {
            EnumFacing dir = realToGate(direction);
            if (isSideOpen(dir)) {
                Connection conn = getType(dir);
                if (conn.isRedstone()) {
                    return type != WireType.BUNDLED;
                } else if (conn.isBundled()) {
                    return type == WireType.BUNDLED;
                }
            }
        }
        return false;
    }

    @Override
    public ItemStack getPickBlock(EntityPlayer player, PartMOP hit) {
        return ItemGate.getStack(this);
    }

    @Override
    public List<ItemStack> getDrops() {
        return Arrays.asList(ItemGate.getStack(this));
    }

    private void updateInputs() {
        byte[] oldOutput = new byte[4];
        boolean changed = false;

        for (int i = 0; i <= 3; i++) {
            Connection conn = getType(side);
            if (conn.isOutput() && conn.isRedstone()) {
                oldOutput[i] = getOutputOutside(side);
            }
        }
        for (int i = 0; i <= 3; i++) {
            EnumFacing side = EnumFacing.getFront(i + 2);
            Connection conn = getType(side);
            if (conn.isInput() && conn.isRedstone()) {
                EnumFacing real = gateToReal(side);
                byte oi = inputs[i];
                World w = getWorld();
                BlockPos p = getPos().offset(real);
                IBlockState s = w.getBlockState(p);
                inputs[i] = (byte) s.getBlock().getWeakPower(w, p, s, real);
                if (conn.isDigital()) {
                    inputs[i] = inputs[i] != 0 ? (byte) 15 : 0;
                }
                if (inputs[i] != oi) {
                    changed = true;
                }
            }
        }

        if (!changed) {
            for (int i = 0; i <= 3; i++) {
                Connection conn = getType(side);
                if (conn.isOutput() && conn.isRedstone()) {
                    if (getOutputOutside(side) != oldOutput[i]) {
                        changed = true;
                        break;
                    }
                }
            }
        }

        if (changed) {
            notifyBlockUpdate();
            sendUpdatePacket();
        }
    }

    public byte getInputOutside(EnumFacing side) {
        return inputs[side.ordinal() - 2];
    }

    protected byte getInputInside(EnumFacing side) {
        if (isSideInverted(side)) {
            return inputs[side.ordinal() - 2] != 0 ? 0 : (byte) 15;
        } else {
            return inputs[side.ordinal() - 2];
        }
    }

    public boolean getInverterState(EnumFacing facing) {
        byte value = getType(facing).isInput() ? getInputOutside(facing) : getOutputInsideClient(facing);
        return value == 0;
    }

    protected byte getOutputOutside(EnumFacing side) {
        if (isSideInverted(side)) {
            return getOutputInside(side) != 0 ? 0 : (byte) 15;
        } else {
            return (byte) getOutputInside(side);
        }
    }

    public byte getOutputInsideClient(EnumFacing side) {
        return outputClient[side.ordinal() - 2];
    }


    public byte getOutputOutsideClient(EnumFacing side) {
        if (isSideInverted(side)) {
            return outputClient[side.ordinal() - 2] != 0 ? 0 : (byte) 15;
        } else {
            return outputClient[side.ordinal() - 2];
        }
    }

    @Override
    public void onPartChanged(IMultipart part) {
        updateInputs();
    }

    @Override
    public void onNeighborBlockChange(Block block) {
        updateInputs();
    }

    public EnumFacing getSide() {
        return side;
    }

    public EnumFacing getTop() {
        return top;
    }

    protected byte getSideMask() {
        byte j = 0;
        for (int i = 0; i <= 3; i++) {
            if (getType(EnumFacing.getFront(i + 2)) != Connection.NONE) {
                j |= (1 << i);
            }
        }
        return j;
    }

    protected abstract byte getOutputInside(EnumFacing side);

    public boolean isSideOpen(EnumFacing side) {
        return (enabledSides & (1 << (side.ordinal() - 2))) != 0;
    }

    public boolean isSideInverted(EnumFacing side) {
        return (invertedSides & (1 << (side.ordinal() - 2))) != 0;
    }

    private boolean isInvalidEnabled() {
        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            if (!isSideOpen(facing) && !canBlockSide(facing)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean onActivated(EntityPlayer playerIn, ItemStack stack, PartMOP hit) {
        if (!playerIn.worldObj.isRemote) {
            //this.top.rotateY();
            if (playerIn.isSneaking()) {
                invertedSides = (byte) ((invertedSides + 1) & 15);
                while ((~getSideMask() & invertedSides) != 0) {
                    invertedSides = (byte) ((invertedSides + 1) & 15);
                }
            } else {
                int z = 32;
                enabledSides = (byte) ((enabledSides + 1) & 15);
                while (z > 0 && ((~getSideMask() & enabledSides) != 0
                        || isInvalidEnabled() || enabledSides == 0)) {
                    enabledSides = (byte) ((enabledSides + 1) & 15);
                    z--;
                }
                enabledSides |= 1;
                if (z == 0) {
                    enabledSides = getSideMask();
                }
                System.out.println("E = " + Integer.toString(enabledSides, 2));
            }
            notifyBlockUpdate();
            sendUpdatePacket();
            return true;
        }
        return false;
    }

    @Override
    public float getHardness(PartMOP hit) {
        return 0.5F;
    }

    @Override
    public String getModelPath() {
        return getType();
    }

    @Override
    public EnumSet<PartSlot> getSlotMask() {
        return EnumSet.of(PartSlot.getFaceSlot(side));
    }

    @Override
    public boolean canConnectRedstone(EnumFacing direction) {
        if (side.getAxis() != direction.getAxis()) {
            EnumFacing dir = realToGate(direction);
            if (isSideOpen(dir)) {
                return getType(dir).isRedstone();
            }
        }
        return false;
    }

    @Override
    public int getWeakSignal(EnumFacing facing) {
        EnumFacing dir = realToGate(facing);
        if (getType(dir).isOutput() && getType(dir).isRedstone() && isSideOpen(dir)) {
            return getOutputOutside(dir);
        } else {
            return 0;
        }
    }

    @Override
    public int getStrongSignal(EnumFacing facing) {
        return 0;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setByte("f", (byte) side.ordinal());
        tag.setByte("t", (byte) top.ordinal());
        tag.setByte("e", enabledSides);
        tag.setByte("i", invertedSides);
    }

    public void readItemNBT(NBTTagCompound tag) {
        enabledSides = tag.getByte("e");
        invertedSides = tag.getByte("i");
    }

    public void writeItemNBT(NBTTagCompound tag) {
        tag.setByte("e", enabledSides);
        tag.setByte("i", invertedSides);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        enabledSides = tag.getByte("e");
        invertedSides = tag.getByte("i");
        side = EnumFacing.getFront(tag.getByte("f"));
        top = EnumFacing.getFront(tag.getByte("t"));
    }

    @Override
    public void writeUpdatePacket(PacketBuffer buf) {
        buf.writeByte((side.ordinal() << 4) | top.ordinal());
        buf.writeByte(enabledSides | (invertedSides << 4));
        for (int i = 0; i <= 3; i++) {
            EnumFacing dir = EnumFacing.getFront(i + 2);
            if (getType(dir) != Connection.NONE) {
                buf.writeByte(getType(dir).isInput() ? inputs[i] : getOutputInside(dir));
            }
        }
    }

    @Override
    public void readUpdatePacket(PacketBuffer buf) {
        int sides = buf.readUnsignedByte();
        side = EnumFacing.getFront(sides >> 4);
        top = EnumFacing.getFront(sides & 15);
        sides = buf.readUnsignedByte();
        enabledSides = (byte) (sides & 15);
        invertedSides = (byte) (sides >> 4);
        for (int i = 0; i <= 3; i++) {
            inputs[i] = outputClient[i] = 0;
            EnumFacing dir = EnumFacing.getFront(i + 2);
            if (getType(dir) != Connection.NONE) {
                if (getType(dir).isInput()) {
                    inputs[i] = buf.readByte();
                } else {
                    outputClient[i] = buf.readByte();
                }
            }
        }

        markRenderUpdate();
    }

    // Utility functions

    @Override
    public void addSelectionBoxes(List<AxisAlignedBB> list) {
        switch (side) {
            case DOWN:
                list.add(new AxisAlignedBB(0, 0, 0, 1, 0.125, 1));
                break;
            case UP:
                list.add(new AxisAlignedBB(0, 1 - 0.125, 0, 1, 1, 1));
                break;
            case NORTH:
                list.add(new AxisAlignedBB(0, 0, 0, 1, 1, 0.125));
                break;
            case SOUTH:
                list.add(new AxisAlignedBB(0, 0, 1 - 0.125, 1, 1, 1));
                break;
            case WEST:
                list.add(new AxisAlignedBB(0, 0, 0, 0.125, 1, 1));
                break;
            case EAST:
                list.add(new AxisAlignedBB(1 - 0.125, 0, 0, 1, 1, 1));
                break;
        }
    }
    
    public static final Property PROPERTY = new Property();

    private static class Property implements IUnlistedProperty<PartGate> {
        private Property() {

        }

        @Override
        public String getName() {
            return "gate";
        }

        @Override
        public boolean isValid(PartGate value) {
            return true;
        }

        @Override
        public Class<PartGate> getType() {
            return PartGate.class;
        }

        @Override
        public String valueToString(PartGate value) {
            return "!?";
        }
    }

    @Override
    public IBlockState getExtendedState(IBlockState state) {
        return ((IExtendedBlockState) state).withProperty(PROPERTY, this);
    }

    @Override
    public BlockState createBlockState() {
        return new ExtendedBlockState(MCMultiPartMod.multipart, new IProperty[0], new IUnlistedProperty[]{ PROPERTY });
    }

    @Override
    public boolean canRenderInLayer(EnumWorldBlockLayer layer) {
        return layer == EnumWorldBlockLayer.CUTOUT;
    }

    private final EnumFacing[][] CONNECTION_DIRS = new EnumFacing[][] {
            {EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST},
            {EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.WEST, EnumFacing.EAST},
            {EnumFacing.UP, EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.EAST},
            {EnumFacing.UP, EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.WEST},
            {EnumFacing.UP, EnumFacing.DOWN, EnumFacing.SOUTH, EnumFacing.NORTH},
            {EnumFacing.UP, EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.SOUTH}
    };

    public boolean rsToDigi(byte v) {
        return v > 0;
    }

    public byte digiToRs(boolean v) {
        return v ? (byte) 15 : 0;
    }

    public EnumFacing gateToReal(EnumFacing dir) {
        if (dir.getAxis() == EnumFacing.Axis.Y) {
            return null;
        }

        EnumFacing itop = top;
        while (itop != EnumFacing.NORTH) {
            dir = dir.rotateY();
            itop = itop.rotateYCCW();
        }

        return CONNECTION_DIRS[side.ordinal()][dir.ordinal() - 2];
    }

    public EnumFacing realToGate(EnumFacing rdir) {
        if (rdir.getAxis() == side.getAxis()) {
            return null;
        }

        for (int i = 0; i < 4; i++) {
            if (CONNECTION_DIRS[side.ordinal()][i] == rdir) {
                EnumFacing dir = EnumFacing.getFront(i + 2);
                EnumFacing itop = top;
                while (itop != EnumFacing.NORTH) {
                    dir = dir.rotateYCCW();
                    itop = itop.rotateYCCW();
                }
                return dir;
            }
        }

        return null;
    }
}
