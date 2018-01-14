/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.charset.module.audio.storage;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import pl.asie.charset.ModCharset;
import pl.asie.charset.api.tape.IDataStorage;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.lib.block.TraitItemHolder;
import pl.asie.charset.lib.ui.GuiHandlerCharset;

public class TileRecordPlayer extends TileBase implements ITickable {
	protected float progressClient;
	private TraitItemHolder holder;
	private TraitRecordPlayer player;
	private float spinLocation;

	public TileRecordPlayer() {
		registerTrait("inv", (holder = new TraitItemHolder() {
			@Override
			public void onContentsChanged() {
				super.onContentsChanged();
				if (world != null && !world.isRemote) {
					setState(TraitRecordPlayer.State.STOPPED);
				}
				markBlockForUpdate();
			}

			@Override
			public boolean isStackAllowed(ItemStack stack) {
				return stack.getItem() instanceof ItemQuartzDisc;
			}

			@Override
			public EnumFacing getTop() {
				return EnumFacing.UP;
			}
		}));
		registerTrait("player", (player = new TraitRecordPlayer(holder.getHandler()) {
			@Override
			public boolean exposesCapability(EnumFacing facing) {
				return facing != EnumFacing.UP;
			}
		}));
	}

	@Override
	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		super.readNBTData(compound, isClient);
		spinLocation = compound.getFloat("dr");
		if (isClient) {
			progressClient = compound.getFloat("pc");
		}
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
		compound = super.writeNBTData(compound, isClient);
		compound.setFloat("dr", spinLocation);
		if (isClient) {
			compound.setFloat("pc", progressClient);
		}
		return compound;
	}

	@Override
	public void invalidate(InvalidationType type) {
		super.invalidate(type);

		setState(TraitRecordPlayer.State.STOPPED);
		player.stopAudioPlayback();
	}

	public EnumFacing getFacing() {
		return world.getBlockState(pos).getValue(Properties.FACING4);
	}

	@Override
	public void getDrops(NonNullList<ItemStack> stacks, IBlockState state, int fortune, boolean silkTouch) {
		super.getDrops(stacks, state, fortune, silkTouch);
		if (!holder.getStack().isEmpty()) {
			stacks.add(holder.getStack());
		}
	}

	@Override
	public void update() {
		if (isDiscSpinning()) {
			spinLocation = (spinLocation + getDiscRotationSpeed()) % 360f;
		}

		if (!world.isRemote) {
			player.update(world, pos);
			updateProgressClient();
		}
	}

	public boolean activate(EntityPlayer player, EnumFacing side, EnumHand hand, Vec3d hitPos) {
		if (player.isSneaking() && side.getAxis() != EnumFacing.Axis.Y) {
			if (!world.isRemote) {
				player.openGui(ModCharset.instance, GuiHandlerCharset.RECORD_PLAYER, getWorld(), getPos().getX(), getPos().getY(), getPos().getZ());
			}
			return true;
		}

		Vec3d realPos = hitPos.subtract(0.5, 0.5, 0.5).rotateYaw((float) (getFacing().getHorizontalAngle() * Math.PI / 180));

		if (side == EnumFacing.UP) {
			if (realPos.x > -0.075 && realPos.z > -0.25 && !getStack().isEmpty()) {
				if (realPos.x > 0.4) {
					if (!world.isRemote) {
						setState(TraitRecordPlayer.State.STOPPED);
					}
					return true;
				} else {
					if (player.isSneaking()) {
						// TODO: Store state, allow for recording turning off
						if (getState() == TraitRecordPlayer.State.PAUSED) {
							if (!world.isRemote) {
								setState(TraitRecordPlayer.State.PLAYING);
								markBlockForUpdate();
							}
							return true;
						} else if (getState() == TraitRecordPlayer.State.PLAYING) {
							if (!world.isRemote) {
								setState(TraitRecordPlayer.State.PAUSED);
								markBlockForUpdate();
							}
							return true;
						} else if (getState() == TraitRecordPlayer.State.RECORDING) {
							return true;
						}
					} else {
						if (!world.isRemote) {
							if (getState() == TraitRecordPlayer.State.STOPPED) {
								setState(TraitRecordPlayer.State.PLAYING);
							}

							// 0.05f = 0.12f
							// 0.25f = 0.35f

							float fmul = 0.0085f;
							float fsub = 0.05f;
							float fstart = (CharsetAudioStorage.quartzDisc.getArmStartPosition(holder.getStack()) * fmul) - fsub;
							float fend = (32f * fmul) - fsub;
							// System.out.println(fstart + " . " + fend);
							float newPos = 1f - ((float) (realPos.x - fstart) / (fend - fstart));
							if (newPos < 0.0f) newPos = 0.0f;
							else if (newPos > 1.0f) newPos = 1.0f;

							IDataStorage storage = this.player.getStorage();
							if (storage != null) {
								storage.setPosition(Math.round((storage.getSize() - 1) * newPos));
								updateProgressClient();
								this.player.stopAudioPlayback();
							}

							markBlockForUpdate();
						}
						return true;
					}
				}
			}

			if (getStack().isEmpty() || (player.getHeldItem(hand).isEmpty() && player.isSneaking())) {
				if (holder.activate(this, player, side, hand)) {
					markBlockForUpdate();
					return true;
				}
			}
		}

		return false;
	}

	private void updateProgressClient() {
		float pos = 0f;
		IDataStorage storage = player.getStorage();
		if (storage != null) {
			pos = ((float) storage.getPosition() / storage.getSize());
		}
		if (Math.abs(pos - progressClient) >= 0.0125f || (pos == 0f) != (progressClient == 0f)) {
			CharsetAudioStorage.packet.sendToWatching(new PacketUpdateProgressClient(this), this);
			progressClient = pos;
		}
	}

	public float getArmRotationClient() {
		ItemStack stack = holder.getStack();
		if (stack.isEmpty() || getState() == TraitRecordPlayer.State.STOPPED) {
			return 0f;
		} else {
			float fstart = CharsetAudioStorage.quartzDisc.getArmStartPosition(stack);
			float fend = 32f;
			return fstart + (progressClient * (fend - fstart));
		}
	}

	public float getDiscRotation() {
		return spinLocation;
	}

	public float getDiscRotationSpeed() {
		return player.getSampleRate() / 4800f;
	}

	public boolean isDiscSpinning() {
		return player.getState() == TraitRecordPlayer.State.PLAYING || player.getState() == TraitRecordPlayer.State.RECORDING;
	}

	public ItemStack getStack() {
		return holder.getStack();
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}

	public IItemHandler getHandler() {
		return holder.getHandler();
	}

	public TraitRecordPlayer.State getState() {
		return player.getState();
	}

	public void setState(TraitRecordPlayer.State state) {
		player.setState(state);
		if (state == TraitRecordPlayer.State.STOPPED) {
			spinLocation = 0;

			IDataStorage storage = player.getStorage();
			if (storage != null) {
				storage.setPosition(0);
			}
		}
	}

	protected void writeData(byte[] data, boolean isLast, int totalLength) {
		IDataStorage storage = player.getStorage();
		if (storage != null) {
			if (getState() == TraitRecordPlayer.State.PLAYING || getState() == TraitRecordPlayer.State.RECORDING) {
				setState(TraitRecordPlayer.State.PAUSED);
			}
			storage.write(data);
			if (isLast) {
				storage.seek(-totalLength);
			}
		}
	}

	public IDataStorage getStorage() {
		return player.getStorage();
	}
}
