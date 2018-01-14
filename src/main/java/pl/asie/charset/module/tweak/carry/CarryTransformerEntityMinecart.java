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

package pl.asie.charset.module.tweak.carry;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.*;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.charset.module.tweak.carry.ICarryTransformer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CarryTransformerEntityMinecart implements ICarryTransformer<Entity> {
	protected boolean isEmptyMinecart(Entity object) {
		if (object == null) {
			return false;
		}

		if (object instanceof EntityMinecartEmpty) {
			return true;
		}

		if (object instanceof EntityMinecart && ((EntityMinecart) object).getType() == EntityMinecart.Type.RIDEABLE) {
			IBlockState displayState = ((EntityMinecart) object).getDisplayTile();
			return displayState.getBlock().getMaterial(displayState) == Material.AIR;
		}

		return false;
	}

	protected Entity transform(Entity object, Class<? extends Entity> target, boolean simulate) {
		return transform(object, target, null, simulate);
	}


	protected Entity transform(Entity object, Class<? extends Entity> target, NBTTagCompound patchTag, boolean simulate) {
		if (isEmptyMinecart(object) == (target == EntityMinecartEmpty.class)) {
			return null;
		}

		Entity targetEntity = null;

		if (!object.world.isRemote && !simulate) {
			World world = object.world;
			NBTTagCompound compound = new NBTTagCompound();
			compound = object.writeToNBT(compound);
			compound.removeTag("UUIDLeast");
			compound.removeTag("UUIDMost");
			if (patchTag != null) {
				for (String tag : patchTag.getKeySet()) {
					compound.setTag(tag, patchTag.getTag(tag));
				}
			}

			object.setDropItemsWhenDead(false);
			object.setDead();

			try {
				targetEntity = target.getConstructor(World.class).newInstance(world);
				targetEntity.readFromNBT(compound);
				world.spawnEntity(targetEntity);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else {
			try {
				World world = object.world;
				targetEntity = target.getConstructor(World.class).newInstance(world);
			} catch (Exception e) {
				return null;
			}
		}

		return targetEntity;
	}

	protected Entity copyEntityToTile(TileEntity target, Entity container, String... tags) {
		NBTTagCompound compound = container.writeToNBT(new NBTTagCompound());
		NBTTagCompound targetCompound = new NBTTagCompound();
		for (String tag : tags) {
			targetCompound.setTag(tag, compound.getTag(tag));
		}
		target.readFromNBT(targetCompound);
		return container;
	}

	protected NBTTagCompound filter(NBTTagCompound compound, String... tags) {
		NBTTagCompound out = new NBTTagCompound();
		for (String tag : tags) {
			if (compound.hasKey(tag)) {
				out.setTag(tag, compound.getTag(tag));
			}
		}
		return out;
	}

	protected Pair<IBlockState, TileEntity> getExtractedPair(@Nonnull Entity object, boolean simulate) {
		if (object instanceof EntityMinecartTNT) {
			return Pair.of(Blocks.TNT.getDefaultState(), null);
		} else if (object instanceof EntityMinecartCommandBlock) {
			TileEntityCommandBlock tile = new TileEntityCommandBlock();
			NBTTagCompound compound = ((EntityMinecartCommandBlock) object).getCommandBlockLogic().writeToNBT(new NBTTagCompound());
			tile.readFromNBT(compound);
			return Pair.of(Blocks.COMMAND_BLOCK.getDefaultState(), tile);
		} else if (object instanceof EntityMinecartChest) {
			TileEntityChest tile = new TileEntityChest();
			copyEntityToTile(tile, object, "Items");
			return Pair.of(Blocks.CHEST.getDefaultState(), tile);
		} else if (object instanceof EntityMinecartHopper) {
			TileEntityHopper tile = new TileEntityHopper();
			copyEntityToTile(tile, object, "Items");
			return Pair.of(Blocks.HOPPER.getDefaultState(), tile);
		}/* else if (object instanceof EntityMinecartFurnace) {
			TileEntityFurnace tile = new TileEntityFurnace();
			copyEntityToTile(tile, object, "Fuel");
			return Pair.of(Blocks.FURNACE.getDefaultState(), tile);
		}*/ else {
			return null;
		}
	}

	@Nullable
	@Override
	public final Pair<IBlockState, TileEntity> extract(@Nonnull Entity object, boolean simulate) {
		Pair<IBlockState, TileEntity> result = getExtractedPair(object, simulate);

		if (result != null && transform(object, EntityMinecartEmpty.class, simulate) != null) {
			return result;
		} else {
			return null;
		}
	}

	@Override
	public boolean insert(@Nonnull Entity object, @Nonnull IBlockState state, @Nullable TileEntity tile, boolean simulate) {
		if (state.getBlock() == Blocks.TNT) {
			return transform(object, EntityMinecartTNT.class, simulate) != null;
		} else if (state.getBlock() == Blocks.COMMAND_BLOCK) {
			return transform(object, EntityMinecartCommandBlock.class,
					((TileEntityCommandBlock) tile).getCommandBlockLogic().writeToNBT(new NBTTagCompound()),
					simulate) != null;
		} else if (state.getBlock() == Blocks.CHEST) {
			return transform(object, EntityMinecartChest.class,
					filter(tile.writeToNBT(new NBTTagCompound()), "Items"),
					simulate) != null;
		} else if (state.getBlock() == Blocks.HOPPER) {
			return transform(object, EntityMinecartHopper.class,
					filter(tile.writeToNBT(new NBTTagCompound()), "Items"),
					simulate) != null;
		}/* else if (state.getBlock() == Blocks.FURNACE) {
			return transform(object, EntityMinecartFurnace.class,
					filter(tile.writeToNBT(new NBTTagCompound()), "Fuel"),
					simulate) != null;
		} */

		return false;
	}
}
