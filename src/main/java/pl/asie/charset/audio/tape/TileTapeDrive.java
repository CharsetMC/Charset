package pl.asie.charset.audio.tape;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ITickable;
import net.minecraft.world.IInteractionObject;

import pl.asie.charset.api.audio.IAudioSource;
import pl.asie.charset.lib.inventory.IInventoryOwner;
import pl.asie.charset.lib.inventory.InventorySimple;

public class TileTapeDrive extends TileEntity implements IInteractionObject, ITickable, IInventory, IInventoryOwner, IAudioSource {
	private final TapeDriveState state = new TapeDriveState(this);

	private InventorySimple inventory = new InventorySimple(1, this) {
		@Override
		public boolean isItemValidForSlot(int index, ItemStack stack) {
			return stack == null || stack.getItem() instanceof ItemTape;
		}
	};

	public State getState() {
		return this.state.getState();
	}

	public void setState(State state) {
		this.state.setState(state);
	}

	@Override
	public void update() {
		if (worldObj != null && !worldObj.isRemote) {
			state.update();
		}
	}

	@Override
	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
		return new ContainerTapeDrive(this, playerInventory);
	}

	@Override
	public String getGuiID() {
		return null;
	}

	public void readCustomData(NBTTagCompound nbt) {
		inventory.readFromNBT(nbt, "items");
		state.deserializeNBT(nbt.getCompoundTag("state"));
	}

	public void writeCustomData(NBTTagCompound nbt) {
		inventory.writeToNBT(nbt, "items");
		NBTTagCompound stateNbt = state.serializeNBT();
		nbt.setTag("state", stateNbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		readCustomData(nbt);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		writeCustomData(nbt);
	}

	@Override
	public int getSizeInventory() {
		return inventory.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		return inventory.getStackInSlot(index);
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		return inventory.decrStackSize(index, count);
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		return inventory.removeStackFromSlot(index);
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		inventory.setInventorySlotContents(index, stack);
	}

	@Override
	public int getInventoryStackLimit() {
		return inventory.getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return inventory.isUseableByPlayer(player);
	}

	@Override
	public void openInventory(EntityPlayer player) {
		inventory.openInventory(player);
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		inventory.closeInventory(player);
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return inventory.isItemValidForSlot(index, stack);
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {

	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {

	}

	@Override
	public String getName() {
		return "Tape Drive";
	}

	@Override
	public boolean hasCustomName() {
		return true;
	}

	@Override
	public IChatComponent getDisplayName() {
		return new ChatComponentTranslation("tile.charset.tape_drive.name");
	}

	@Override
	public void onInventoryChanged(IInventory inventory) {
		markDirty();
	}
}
