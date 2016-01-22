package pl.asie.charset.audio.tape;

import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import mcmultipart.MCMultiPartMod;
import mcmultipart.multipart.PartSlot;
import mcmultipart.raytrace.PartMOP;
import pl.asie.charset.api.audio.IAudioSource;
import pl.asie.charset.api.audio.IDataStorage;
import pl.asie.charset.audio.ModCharsetAudio;
import pl.asie.charset.lib.inventory.IInventoryOwner;
import pl.asie.charset.lib.inventory.InventorySimple;
import pl.asie.charset.lib.multipart.PartSlab;
import pl.asie.charset.lib.refs.Properties;

public class PartTapeDrive extends PartSlab implements IAudioSource, ITickable, IInventoryOwner {
	public final InventorySimple inventory = new InventorySimple(1, this) {
		@Override
		public boolean isItemValidForSlot(int index, ItemStack stack) {
			return stack == null || stack.getItem() instanceof ItemTape;
		}
	};

	protected EnumFacing facing = EnumFacing.NORTH;

	private final TapeDriveState state = new TapeDriveState(this, inventory);

	public State getState() {
		return this.state.getState();
	}

	public void setState(State state) {
		this.state.setState(state);
	}

	@Override
	public String getModelPath() {
		return "charsetaudio:tapedrive";
	}

	@Override
	public boolean onActivated(EntityPlayer player, ItemStack stack, PartMOP hit) {
		player.openGui(ModCharsetAudio.instance, isTop ? PartSlot.UP.ordinal() : PartSlot.DOWN.ordinal(), getWorld(), getPos().getX(), getPos().getY(), getPos().getZ());
		ModCharsetAudio.packet.sendToWatching(new PacketDriveState(this, getState()), this);
		return true;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		inventory.writeToNBT(nbt, "items");
		NBTTagCompound stateNbt = state.serializeNBT();
		nbt.setTag("state", stateNbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		inventory.readFromNBT(nbt, "items");
		state.deserializeNBT(nbt.getCompoundTag("state"));
	}

	@Override
	public IBlockState getExtendedState(IBlockState state) {
		return state.withProperty(PartSlab.IS_TOP, isTop()).withProperty(Properties.FACING4, facing);
	}

	@Override
	public BlockState createBlockState() {
		return new BlockState(MCMultiPartMod.multipart, PartSlab.IS_TOP, Properties.FACING4);
	}

	@Override
	public void update() {
		if (getWorld() != null && !getWorld().isRemote) {
			state.update();
		}
	}

	@Override
	public void onInventoryChanged(IInventory inventory) {
		markDirty();
	}

	public void writeData(byte[] data, boolean isLast, int totalLength) {
		IDataStorage storage = state.getStorage();
		if (storage != null) {
			setState(State.STOPPED);
			storage.write(data);
			if (isLast) {
				storage.seek(-totalLength);
			}
		}
	}
}
