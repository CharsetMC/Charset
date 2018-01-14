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

package pl.asie.charset.module.laser.system;

import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.api.laser.ILaserBeam;
import pl.asie.charset.api.laser.ILaserSource;
import pl.asie.charset.module.laser.CharsetLaser;
import pl.asie.charset.api.laser.ILaserReceiver;
import pl.asie.charset.api.laser.LaserColor;

import javax.annotation.Nonnull;

// IDEA: Add lastTileEntity to avoid double world.getTileEntity call with onAdd.
public final class LaserBeam implements ILaserBeam, ILaserEndpoint {
	public static final int MAX_DISTANCE = 64;
	private static long ID_COUNTER = 1;

	private final long id;
	private final @Nonnull
	ILaserSource source;
	private final @Nonnull World world;
	private final @Nonnull BlockPos start, end;
	private final @Nonnull LaserColor color;
	private final @Nonnull EnumFacing direction;

	private boolean endedAtAir;
	private int length;

	private boolean isValidated;

	@SideOnly(Side.CLIENT)
	protected Vec3d vcstart, vcend;
	@SideOnly(Side.CLIENT)
	protected float vcdist;

	public LaserBeam(@Nonnull ILaserSource source, @Nonnull World world, @Nonnull BlockPos start, @Nonnull EnumFacing facing, @Nonnull LaserColor color) {
		this.id = ID_COUNTER++;
		this.source = source;
		this.world = world;
		this.start = start;
		this.direction = facing;
		this.color = color;
		this.end = calculateEnd();

		validate();
	}

	public LaserBeam(long id, World world, BlockPos start, int length, int flags) {
		this.id = id;
		this.world = world;
		this.start = start;
		this.length = length;

		direction = EnumFacing.getFront(flags & 7);
		color = LaserColor.VALUES[(flags >> 3) & 7];
		endedAtAir = (flags & 0x40) != 0;

		source = new DummyLaserSource(world.getTileEntity(start));
		end = start.offset(direction, length);

		validate();
	}

	public void writeData(PacketBuffer buf) {
		buf.writeLong(id);
		buf.writeInt(world.provider.getDimension());
		buf.writeBlockPos(start);
		buf.writeShort(length);
		buf.writeByte(direction.ordinal() | (color.ordinal() << 3) | (endedAtAir ? 0x40 : 0));
	}

	public void validate() {
		isValidated = true;
	}

	public void invalidate() {
		isValidated = false;
	}

	public void onAdd(boolean updates) {
		if (updates) {
			// System.out.println("ADD " + toString());

			if (!start.equals(end)) {
				TileEntity tile = world.getTileEntity(end);
				if (tile != null && tile.hasCapability(CharsetLaser.LASER_RECEIVER, direction.getOpposite())) {
					ILaserReceiver receiver = tile.getCapability(CharsetLaser.LASER_RECEIVER, direction.getOpposite());
					if (receiver != null) {
						receiver.onLaserUpdate(color);
					}
				} else if (CharsetLaser.REDSTONE_HOOK_ACTIVE) {
					world.neighborChanged(end, Blocks.AIR, end.offset(direction.getOpposite()));
				}
			}
		}
	}

	public void onRemove(boolean updates) {
		invalidate();

		if (updates) {
			// System.out.println("DEL " + toString());

			if (!start.equals(end)) {
				TileEntity tile = world.getTileEntity(end);
				if (tile != null && tile.hasCapability(CharsetLaser.LASER_RECEIVER, direction.getOpposite())) {
					ILaserReceiver receiver = tile.getCapability(CharsetLaser.LASER_RECEIVER, direction.getOpposite());
					if (receiver != null) {
						receiver.onLaserUpdate(LaserColor.NONE);
					}
				} else if (CharsetLaser.REDSTONE_HOOK_ACTIVE) {
					world.neighborChanged(end, Blocks.AIR, end.offset(direction.getOpposite()));
					world.notifyNeighborsOfStateChange(end, Blocks.AIR, false);
				}
			}
		}
	}

	protected final Vec3d calculateStartpoint() {
		return new Vec3d(start.getX() + 0.5, start.getY() + 0.5, start.getZ() + 0.5);
	}

	protected final Vec3d calculateEndpoint() {
		BlockPos pos = end;
		Vec3d endPos = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

		IBlockState state = world.getBlockState(pos);
		if (state.isOpaqueCube()) {
			return endPos.addVector(direction.getOpposite().getFrontOffsetX() * 0.5, direction.getOpposite().getFrontOffsetY() * 0.5, direction.getOpposite().getFrontOffsetZ() * 0.5);
		} else {
			return endPos;
		}

		// TODO

		// Vec3d startPos = new Vec3d(0.5, 0.5, 0.5).addVector(pos.getX(), pos.getY(), pos.getZ());
		/* Vec3d defEndPos = endPos;
		startPos = startPos.addVector(direction.getOpposite().getFrontOffsetX() * 0.5, direction.getOpposite().getFrontOffsetY() * 0.5, direction.getOpposite().getFrontOffsetZ() * 0.5);
		endPos = endPos.addVector(direction.getFrontOffsetX() * 0.5, direction.getFrontOffsetY() * 0.5, direction.getFrontOffsetZ() * 0.5);

		RayTraceResult result = state.collisionRayTrace(world, pos, startPos, endPos);
		if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
			return result.hitVec;
		} else {
			return defEndPos;
		} */
	}

	private final boolean isBlocker(Chunk chunk, BlockPos pos) {
		IBlockState state = chunk.getBlockState(pos);

		// Quickies: air always lets through, opaque cubes never let through. Simple!
		if (state.getBlock().isAir(state, world, pos)) {
			return false;
		}

		if (state.isOpaqueCube()) {
			return true;
		}

		// Check the blacklist
		if (CharsetLaser.BLOCKING_BLOCKS.contains(state.getBlock())) {
			return true;
		}

		// If block is opaque...
		if (state.getLightOpacity(world, pos) >= 192 /* out of 255 */) {
			// ...and a full cube, nope out
			if (state.isFullCube()) {
				return true;
			}

			// ...and has a blocking shape, nope out
			BlockFaceShape shapeA = state.getBlockFaceShape(world, pos, direction);
			BlockFaceShape shapeB = state.getBlockFaceShape(world, pos, direction.getOpposite());
			if ((shapeA != BlockFaceShape.BOWL && shapeA != BlockFaceShape.UNDEFINED)
					|| (shapeB != BlockFaceShape.BOWL && shapeB != BlockFaceShape.UNDEFINED)) {
				return true;
			}
			return true;
		}

		if (state.getBlock().hasTileEntity(state)) {
			TileEntity tile = chunk.getTileEntity(pos, Chunk.EnumCreateEntityType.IMMEDIATE);
			if (tile != null && tile.hasCapability(CharsetLaser.LASER_RECEIVER, direction.getOpposite())) {
				return true;
			}
		}

		/* if (!world.isRemote && state.getMaterial().isOpaque()) {
			Vec3d startPos = new Vec3d(0.5, 0.5, 0.5).addVector(pos.getX(), pos.getY(), pos.getZ());
			Vec3d endPos = new Vec3d(0.5, 0.5, 0.5).addVector(pos.getX(), pos.getY(), pos.getZ());
			startPos = startPos.addVector(direction.getOpposite().getFrontOffsetX() * 0.5, direction.getOpposite().getFrontOffsetY() * 0.5, direction.getOpposite().getFrontOffsetZ() * 0.5);
			endPos = endPos.addVector(direction.getFrontOffsetX() * 0.5, direction.getFrontOffsetY() * 0.5, direction.getFrontOffsetZ() * 0.5);

			RayTraceResult result = state.collisionRayTrace(world, pos, startPos, endPos);
			if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
				return true;
			}
		} */

		return false;
	}

	private BlockPos calculateEnd() {
		boolean foundEnd = false;
		int i = 0;
		BlockPos.MutableBlockPos endPos = new BlockPos.MutableBlockPos(start);
		Chunk chunk = world.getChunkFromBlockCoords(endPos);

		while (i < MAX_DISTANCE && !foundEnd) {
			endPos.move(direction);
			switch (direction) {
				case UP:
				case DOWN:
					break;
				case NORTH:
					if ((endPos.getZ() & 15) == 15)
						chunk = world.getChunkFromBlockCoords(endPos);
					break;
				case SOUTH:
					if ((endPos.getZ() & 15) == 0)
						chunk = world.getChunkFromBlockCoords(endPos);
					break;
				case WEST:
					if ((endPos.getX() & 15) == 15)
						chunk = world.getChunkFromBlockCoords(endPos);
					break;
				case EAST:
					if ((endPos.getX() & 15) == 0)
						chunk = world.getChunkFromBlockCoords(endPos);
					break;
			}
			i++;
			foundEnd = isBlocker(chunk, endPos);
		}

		length = i;
		endedAtAir = !foundEnd;

		return endPos;
	}

	public boolean isValid() {
		if (!isValidated || !source.isCacheValid()) {
			/* if (!isValidated) {
				System.out.println("INVALID - flag: " + this);
			} else {
				System.out.println("INVALID - source: " + this);
			} */
			return false;
		}

		Chunk chunk = world.getChunkFromBlockCoords(start);
		Chunk endChunk = chunk;
		if (direction.getAxis() != EnumFacing.Axis.Y && (((start.getX() >> 4) != (end.getX() >> 4)) || ((start.getZ() >> 4) != (end.getZ() >> 4)))) {
			endChunk = world.getChunkFromBlockCoords(end);
		}

		if (endedAtAir == isBlocker(endChunk, end)) {
			return false;
		}

		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(start);
		for (int i = 0; i < length - 1; i++) {
			pos.move(direction);
			switch (direction) {
				case UP:
				case DOWN:
					break;
				case NORTH:
					if ((pos.getZ() & 15) == 15)
						chunk = world.getChunkFromBlockCoords(pos);
					break;
				case SOUTH:
					if ((pos.getZ() & 15) == 0)
						chunk = world.getChunkFromBlockCoords(pos);
					break;
				case WEST:
					if ((pos.getX() & 15) == 15)
						chunk = world.getChunkFromBlockCoords(pos);
					break;
				case EAST:
					if ((pos.getX() & 15) == 0)
						chunk = world.getChunkFromBlockCoords(pos);
					break;
			}

			if (isBlocker(chunk, pos)) {
				// System.out.println("INVALID - B " + pos + ": " + this);
				return false;
			}
		}

		return true;
	}

	@Override
	public World getBeamWorld() {
		return getWorld();
	}

	public World getWorld() {
		return world;
	}

	@Override
	public BlockPos getPos() {
		return getEnd();
	}

	public BlockPos getStart() {
		return start;
	}

	public BlockPos getEnd() {
		return end;
	}

	public LaserColor getColor() {
		return color;
	}

	public EnumFacing getDirection() {
		return direction;
	}

	@Override
	public String toString() {
		return String.format("LaserBeam{%s-[%s]->%s, %s}", start.toString(), direction.name(), end.toString(), color.name());
	}

	public long getId() {
		return id;
	}
}
