package pl.asie.charset.lib;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

public class TileBuffer {
	private final TileEntity owner;
	private final TileEntity[] tiles;
	private World world;
	private boolean initialized;

	public TileBuffer(TileEntity owner) {
		this.owner = owner;
		this.tiles = new TileEntity[6];
	}

	public TileEntity getOwner() {
		return owner;
	}

	public TileEntity getTileEntity(ForgeDirection side) {
		if (side.ordinal() != 6) {
			if (tiles[side.ordinal()] != null && tiles[side.ordinal()].isInvalid()) {
				updateSide(side, true);
			}

			return tiles[side.ordinal()];
		} else {
			return null;
		}
	}

	private void updateSide(ForgeDirection direction, boolean force) {
		int i = direction.ordinal();
		int x = owner.xCoord + direction.offsetX;
		int y = owner.yCoord + direction.offsetY;
		int z = owner.zCoord + direction.offsetZ;

		if (!force) {
			if (tiles[i] != null && !tiles[i].isInvalid()) {
				return;
			}
		}


		tiles[i] = null;

		if (world == null) {
			world = owner.getWorldObj();
		}

		if (!force && !world.blockExists(x, y, z)) {
			return;
		}

		Block block = world.getBlock(x, y, z);
		if (block.hasTileEntity(world.getBlockMetadata(x, y, z))) {
			tiles[i] = world.getTileEntity(x, y, z);
		}
	}

	public void update(boolean force) {
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			updateSide(direction, force);
		}
	}
}
