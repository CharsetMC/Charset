package pl.asie.charset.wires.logic;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;

import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import mcmultipart.MCMultiPartMod;
import mcmultipart.client.multipart.IHitEffectsPart;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IRedstonePart;
import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.Multipart;
import mcmultipart.multipart.MultipartHelper;
import mcmultipart.multipart.MultipartRegistry;
import mcmultipart.multipart.PartSlot;
import mcmultipart.raytrace.PartMOP;
import pl.asie.charset.api.wires.IBundledUpdatable;
import pl.asie.charset.api.wires.IRedstoneUpdatable;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.api.wires.WireType;
import pl.asie.charset.wires.ModCharsetWires;
import pl.asie.charset.wires.ProxyClient;
import pl.asie.charset.wires.WireKind;
import pl.asie.charset.wires.WireUtils;

public abstract class PartWireBase extends Multipart implements ISlottedPart, IHitEffectsPart, IRedstonePart, ITickable {
    protected static final boolean DEBUG = true;

    public static final Property PROPERTY = new Property();

    private static class Property implements IUnlistedProperty<PartWireBase> {
        private Property() {

        }

        @Override
        public String getName() {
            return "wireTile";
        }

        @Override
        public boolean isValid(PartWireBase value) {
            return true;
        }

        @Override
        public Class<PartWireBase> getType() {
            return PartWireBase.class;
        }

        @Override
        public String valueToString(PartWireBase value) {
            return "!?";
        }
    }

	public WireKind type;
	public WireFace location;
	protected byte internalConnections, externalConnections, cornerConnections;
    private boolean suPropagation, suNeighbor, suRender, suConnection;

	public PartWireBase() {
        scheduleConnectionUpdate();
	}

	public abstract void propagate(int color);
	public abstract int getSignalLevel();
	public abstract int getRedstoneLevel();

    @Override
    public String getModelPath() {
        return getType();
    }

    @Override
    public String getType() {
        return "charsetwires:wire";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(IHitEffectsPart.AdvancedEffectRenderer advancedEffectRenderer) {
        advancedEffectRenderer.addBlockDestroyEffects(getPos(), ProxyClient.rendererWire.handlePartState(getExtendedState(MultipartRegistry.getDefaultState(this).getBaseState())).getParticleTexture());
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(PartMOP partMOP, IHitEffectsPart.AdvancedEffectRenderer advancedEffectRenderer) {
        return true;
    }

    @Override
    public void onPartChanged(IMultipart part) {
        scheduleConnectionUpdate();
        schedulePropagationUpdate();
    }

    @Override
    public void onNeighborBlockChange(Block block) {
        scheduleConnectionUpdate();
        schedulePropagationUpdate();
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
    public EnumSet<PartSlot> getSlotMask() {
        return EnumSet.of(location.slot);
    }

    private ItemStack getItemStack() {
        return new ItemStack(ModCharsetWires.wire, 1, type.ordinal() << 1 | (location == WireFace.CENTER ? 1 : 0));
    }

    @Override
    public ItemStack getPickBlock(EntityPlayer player, PartMOP hit) {
        return getItemStack();
    }

    @Override
    public List<ItemStack> getDrops() {
        return Arrays.asList(getItemStack());
    }

    @Override
    public void writeUpdatePacket(PacketBuffer buf) {
        buf.writeByte(type.ordinal());
        buf.writeByte(location.ordinal());
        buf.writeByte(internalConnections);
        buf.writeByte(externalConnections);
        if (location != WireFace.CENTER) {
            buf.writeByte(cornerConnections);
        }
    }

    @Override
    public void readUpdatePacket(PacketBuffer buf) {
        type = WireKind.VALUES[buf.readByte()];
        location = WireFace.VALUES[buf.readByte()];
        internalConnections = buf.readByte();
        externalConnections = buf.readByte();
        cornerConnections = location == WireFace.CENTER ? 0 : buf.readByte();

        scheduleRenderUpdate();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        type = WireKind.VALUES[nbt.getByte("t")];
        location = WireFace.VALUES[nbt.getByte("l")];
        internalConnections = nbt.getByte("iC");
        externalConnections = nbt.getByte("eC");
        cornerConnections = nbt.getByte("cC");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setByte("t", (byte) type.ordinal());
        nbt.setByte("l", (byte) location.ordinal());
        nbt.setByte("iC", internalConnections);
        nbt.setByte("eC", externalConnections);
        if (location != WireFace.CENTER) {
            nbt.setByte("cC", cornerConnections);
        }
    }

    @Override
    public void onRemoved() {
        pokeExtendedNeighbors();
    }

    @Override
    public void addSelectionBoxes(List<AxisAlignedBB> list) {
        switch (location) {
            case DOWN:
                list.add(new AxisAlignedBB(0, 0, 0, 1, WireUtils.getWireHitboxHeight(this), 1));
                break;
            case UP:
                list.add(new AxisAlignedBB(0, 1 - WireUtils.getWireHitboxHeight(this), 0, 1, 1, 1));
                break;
            case NORTH:
                list.add(new AxisAlignedBB(0, 0, 0, 1, 1, WireUtils.getWireHitboxHeight(this)));
                break;
            case SOUTH:
                list.add(new AxisAlignedBB(0, 0, 1 - WireUtils.getWireHitboxHeight(this), 1, 1, 1));
                break;
            case WEST:
                list.add(new AxisAlignedBB(0, 0, 0, WireUtils.getWireHitboxHeight(this), 1, 1));
                break;
            case EAST:
                list.add(new AxisAlignedBB(1 - WireUtils.getWireHitboxHeight(this), 0, 0, 1, 1, 1));
                break;
            case CENTER:
                list.add(getCenterBox(type));
                break;
        }
    }

    private AxisAlignedBB getCenterCollisionBox(WireKind kind) {
        switch (kind.type()) {
            case NORMAL:
                return new AxisAlignedBB(0.4375, 0.4375, 0.4375, 0.5625, 0.5625, 0.5625);
            case INSULATED:
                return new AxisAlignedBB(0.375, 0.375, 0.375, 0.625, 0.625, 0.625);
            case BUNDLED:
                return new AxisAlignedBB(0.3125, 0.3125, 0.3125, 0.6875, 0.6875, 0.6875);
            default:
                return null;
        }
    }

    private AxisAlignedBB getCenterBox(WireKind kind) {
        switch (kind.type()) {
            case NORMAL:
            case INSULATED:
                return new AxisAlignedBB(0.375, 0.375, 0.375, 0.625, 0.625, 0.625);
            case BUNDLED:
                return new AxisAlignedBB(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
            default:
                return null;
        }
    }

    public void scheduleRenderUpdate() {
        if (getWorld() == null) {
            return;
        }

        if (getWorld().isRemote) {
            markRenderUpdate();
        } else {
            suRender = true;
        }
    }

    @Override
    public boolean canRenderInLayer(EnumWorldBlockLayer layer) {
        return layer == EnumWorldBlockLayer.CUTOUT;
    }

    private void pokeExtendedNeighbors() {
        if (getContainer() != null) {
            for (IMultipart multipart : getContainer().getParts()) {
                if (multipart instanceof PartWireBase) {
                    multipart.onNeighborBlockChange(MCMultiPartMod.multipart);
                }
            }
        }

        World world = this.getWorld();
        BlockPos pos = this.getPos();
        if (world != null) {
            world.notifyNeighborsRespectDebug(pos, MCMultiPartMod.multipart);
            for (EnumFacing facing : EnumFacing.VALUES) {
                world.notifyNeighborsOfStateExcept(pos.offset(facing), MCMultiPartMod.multipart, facing.getOpposite());
            }
        }
    }

    @Override
    public void update() {
        if (suConnection) {
            suConnection = false;
            updateConnections();
        }

        if (suNeighbor) {
            suNeighbor = false;
            pokeExtendedNeighbors();
        }

        if (suPropagation) {
            suPropagation = false;
            if (!getWorld().isRemote) {
                onSignalChanged(-1);
            }
        }

        if (suRender) {
            suRender = false;
            if (getWorld().isRemote) {
                getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
            } else {
                sendUpdatePacket();
            }
        }
    }

	public void updateConnections() {
		Set<WireFace> validSides = EnumSet.noneOf(WireFace.class);

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
		internalConnections = externalConnections = cornerConnections = 0;

		for (WireFace facing : validSides) {
			if (WireUtils.canConnectInternal(this, facing)) {
				internalConnections |= 1 << facing.ordinal();
			} else if (facing != WireFace.CENTER) {
				if (WireUtils.canConnectExternal(this, facing.facing)) {
					externalConnections |= 1 << facing.ordinal();
				} else if (location != WireFace.CENTER && WireUtils.canConnectCorner(this, facing.facing)) {
					cornerConnections |= 1 << facing.ordinal();
				}
			}
		}

		int newConnectionCache = internalConnections << 12 | externalConnections << 6 | cornerConnections;

		if (oldConnectionCache != newConnectionCache) {
			scheduleNeighborUpdate();
			schedulePropagationUpdate();
			scheduleRenderUpdate();
		}
	}

    protected void scheduleNeighborUpdate() {
        suNeighbor = true;
    }

    protected void schedulePropagationUpdate() {
        suPropagation = true;
    }

    protected void scheduleConnectionUpdate() {
        suConnection = true;
    }

    protected abstract void onSignalChanged(int color);

	protected void propagateNotifyCorner(EnumFacing side, EnumFacing direction, int color) {
        PartWireBase wire = WireUtils.getWire(MultipartHelper.getPartContainer(getWorld(), getPos().offset(side).offset(direction)), location);
        if (wire != null) {
            wire.onSignalChanged(color);
        }
	}

	protected void propagateNotify(EnumFacing facing, int color) {
        PartWireBase wire = WireUtils.getWire(MultipartHelper.getPartContainer(getWorld(), getPos().offset(facing)), location);
        if (wire != null) {
            wire.onSignalChanged(color);
        }

		TileEntity nt = getWorld().getTileEntity(getPos().offset(facing));
		if (nt instanceof IBundledUpdatable) {
			((IBundledUpdatable) nt).onBundledInputChanged(facing.getOpposite());
		} else if (nt instanceof IRedstoneUpdatable) {
			((IRedstoneUpdatable) nt).onRedstoneInputChanged(facing.getOpposite());
		} else {
			getWorld().notifyBlockOfStateChange(getPos().offset(facing), MCMultiPartMod.multipart);
		}
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

	@SideOnly(Side.CLIENT)
	public int getRenderColor() {
		return -1;
	}

	public int getBundledSignalLevel(int i) {
		return 0;
	}

    @Override
    public boolean canConnectRedstone(EnumFacing facing) {
        return type.type() != WireType.BUNDLED && connectsExternal(facing);
    }

    @Override
    public int getWeakSignal(EnumFacing facing) {
        if (connects(facing) || location.facing == facing) {
            return getRedstoneLevel();
        } else {
            return 0;
        }
    }

    @Override
    public int getStrongSignal(EnumFacing facing) {
        if (type.type() != WireType.NORMAL) {
            return 0;
        } else if (location.facing == facing.getOpposite()) {
            return 0;
        } else if (location.facing != facing && !connectsExternal(facing)) {
            return 0;
        } else {
            return getRedstoneLevel();
        }
    }
}
