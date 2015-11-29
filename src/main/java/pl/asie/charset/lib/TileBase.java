package pl.asie.charset.lib;

import net.minecraft.tileentity.TileEntity;

import net.minecraft.util.EnumFacing;

/**
 * Created by asie on 11/7/15.
 */
public class TileBase extends TileEntity {
	private boolean initialized = false;

	protected void initialize() {

	}

	public void update() {
		if (!initialized) {
			initialize();
			initialized = true;
		}
	}

	public TileEntity getNeighbourTile(EnumFacing side) {
		return worldObj.getTileEntity(pos.offset(side));
	}
}
