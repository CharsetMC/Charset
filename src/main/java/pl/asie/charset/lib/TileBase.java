package pl.asie.charset.lib;

import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

/**
 * Created by asie on 11/7/15.
 */
public class TileBase extends TileEntity {
	private boolean initialized = false;

	protected void initialize() {

	}

	@Override
	public void updateEntity() {
		if (!initialized) {
			initialize();
			initialized = true;
		}
	}

	public TileEntity getNeighbourTile(ForgeDirection side) {
		return worldObj.getTileEntity(xCoord + side.offsetX, yCoord + side.offsetY, zCoord + side.offsetZ);
	}
}
