package pl.asie.charset.storage.backpack;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.IInteractionObject;

import pl.asie.charset.lib.inventory.IInventoryOwner;
import pl.asie.charset.lib.inventory.InventorySimple;
import pl.asie.charset.storage.ModCharsetStorage;

/**
 * Created by asie on 1/10/16.
 */
public class TileBackpack extends TileEntity implements IInteractionObject, IInventory, IInventoryOwner {
	private InventorySimple inventory = new InventorySimple(27, this);
	private int color = -1;

	public int getColor() {
		return color >= 0 ? color : BlockBackpack.DEFAULT_COLOR;
	}

	@Override
	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
		return new ContainerBackpack(this, playerInventory);
	}

	@Override
	public String getGuiID() {
		return null;
	}

	@Override
	public Packet getDescriptionPacket() {
		if (color >= 0) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setInteger("color", color);
			return new S35PacketUpdateTileEntity(getPos(), getBlockMetadata(), tag);
		} else {
			return null;
		}
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		if (pkt != null && pkt.getNbtCompound() != null && pkt.getNbtCompound().hasKey("color")) {
			int oldColor = color;
			color = pkt.getNbtCompound().getInteger("color");
			if (oldColor != color) {
				worldObj.markBlockRangeForRenderUpdate(pos, pos);
			}
		}
	}

	public void readFromItemStack(ItemStack stack) {
		readCustomData(stack.getTagCompound());
	}

	public ItemStack writeToItemStack() {
		ItemStack stack = new ItemStack(ModCharsetStorage.backpackBlock);
		stack.setTagCompound(new NBTTagCompound());
		writeCustomData(stack.getTagCompound());
		return stack;
	}

	public void readCustomData(NBTTagCompound nbt) {
		color = nbt.hasKey("color") ? nbt.getInteger("color") : -1;
		inventory.readFromNBT(nbt, "items");
	}

	public void writeCustomData(NBTTagCompound nbt) {
		if (color >= 0) {
			nbt.setInteger("color", color);
		}
		inventory.writeToNBT(nbt, "items");
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
		return "Backpack";
	}

	@Override
	public boolean hasCustomName() {
		return true;
	}

	@Override
	public IChatComponent getDisplayName() {
		return new ChatComponentTranslation("tile.charset.backpack.name");
	}

	@Override
	public void onInventoryChanged(IInventory inventory) {
		markDirty();
	}
}
