package pl.asie.charset.module.misc.shelf;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.notify.INoticeUpdater;
import pl.asie.charset.lib.notify.Notice;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.UnlistedPropertyGeneric;

import javax.annotation.Nullable;

public class TileShelf extends TileBase {
	public static final UnlistedPropertyGeneric<CacheInfoShelf> PROPERTY = new UnlistedPropertyGeneric<>("tile", CacheInfoShelf.class);
	protected ItemStackHandler handler = new ItemStackHandler(18);
	private ItemMaterial plank;

	public ItemMaterial getPlank() {
		if (plank == null) {
			plank = getPlankFromNBT(null);
		}
		return plank;
	}

	protected boolean isBook(ItemStack stack) {
		return stack.getItem().getUnlocalizedName().toLowerCase().contains("book");
	}

	private int toNonBookSlotId(int id) {
		return (int) Math.floor(id / 3.5F) + 14;
	}

	protected boolean onClicked(float hitX, float hitY, float hitZ, EntityPlayer player) {
		int slotId = getSlotId(hitX, hitY, hitZ);
		if (slotId < 0) return false;

		if (handler.getStackInSlot(slotId).isEmpty()) {
			slotId = toNonBookSlotId(slotId);
		}

		ItemStack stack = handler.getStackInSlot(slotId);
		if (stack.isEmpty()) return false;

		handler.setStackInSlot(slotId, ItemStack.EMPTY);
		markBlockForUpdate();

		ItemUtils.giveOrSpawnItemEntity(player, getWorld(), new Vec3d(pos).addVector(hitX, hitY, hitZ), stack, 0,0,0,0);
		return true;
	}

	protected boolean onActivated(float hitX, float hitY, float hitZ, ItemStack stack, EntityPlayer player) {
		int slotId = getSlotId(hitX, hitY, hitZ);
		if (slotId < 0) return false;

		if (stack.isEmpty()) {
			if (handler.getStackInSlot(slotId).isEmpty()) {
				slotId = toNonBookSlotId(slotId);
			}

			if (!handler.getStackInSlot(slotId).isEmpty()) {
				final int sentSlotId = slotId;
				new Notice(new Vec3d(pos).addVector(hitX, hitY, hitZ), new INoticeUpdater() {
					@Override
					public void update(Notice msg) {
						msg.withItem(handler.getStackInSlot(sentSlotId)).setMessage("{ITEM_NAME}{ITEM_INFOS_NEWLINE}");
					}
				}).sendTo(player);

				return true;
			} else {
				return false;
			}
		} else {
			if (!isBook(stack)) slotId = toNonBookSlotId(slotId);
			if (isSlotTaken(slotId)) return false;

			stack = stack.splitStack(1);
			handler.setStackInSlot(slotId, stack);
			markBlockForUpdate();

			return true;
		}
	}

	protected int getSlotId(float hitX, float hitY, float hitZ) {
		Vec3d placementVec = new Vec3d(hitX - 0.5F, hitY - 0.5F, hitZ - 0.5F);
		placementVec = placementVec.rotateYaw(world.getBlockState(pos).getValue(Properties.FACING4).getHorizontalAngle() / 180 * (float) Math.PI);
		if (placementVec.xCoord >= (-7F / 16F) && placementVec.xCoord <= (7F / 16F)) {
			return (int) Math.floor((placementVec.xCoord + (7F / 16F)) * 7) + (placementVec.yCoord > 0 ? 7 : 0);
		}
		return -1;
	}

	protected boolean isSlotTaken(int slotId) {
		if (!handler.getStackInSlot(slotId).isEmpty())
			return true;

		if (slotId < 14) {
			if (slotId >= 0 && slotId <= 3 && !handler.getStackInSlot(14).isEmpty())
				return true;
			if (slotId >= 3 && slotId <= 6 && !handler.getStackInSlot(15).isEmpty())
				return true;
			if (slotId >= 7 && slotId <= 10 && !handler.getStackInSlot(16).isEmpty())
				return true;
			if (slotId >= 10 && slotId <= 14 && !handler.getStackInSlot(17).isEmpty())
				return true;
		} else {
			int sSlot = (int) Math.floor((slotId - 14) * 3.5F);
			for (int i = 0; i < 4; i++) {
				if (!handler.getStackInSlot(sSlot + i).isEmpty())
					return true;
			}
		}

		return false;
	}

	public static ItemMaterial getPlankFromNBT(NBTTagCompound compound) {
		return ItemMaterialRegistry.INSTANCE.getMaterial(compound, "plank", "plank");
	}

	@Override
	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		plank = getPlankFromNBT(compound);
		if (compound.hasKey("inv", Constants.NBT.TAG_COMPOUND)) {
			handler.deserializeNBT(compound.getCompoundTag("inv"));
		}
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
		plank.writeToNBT(compound, "plank");
		compound.setTag("inv", handler.serializeNBT());
		return compound;
	}

	@Override
	public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
		loadFromStack(stack);
	}

	public void loadFromStack(ItemStack stack) {
		plank = getPlankFromNBT(stack.getTagCompound());
	}

	@Override
	public ItemStack getDroppedBlock() {
		ItemStack stack = new ItemStack(CharsetMiscShelf.shelfBlock);
		stack.setTagCompound(writeNBTData(new NBTTagCompound(), false));
		return stack;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return true;
		}

		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(handler);
		}

		return super.getCapability(capability, facing);
	}
}
