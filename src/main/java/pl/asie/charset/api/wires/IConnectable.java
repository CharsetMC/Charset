package pl.asie.charset.api.wires;

import net.minecraft.util.EnumFacing;

public interface IConnectable {
	/**
	 * Check if the wire can connect to this block.
	 *
	 * @param type      The wire type.
	 * @param face      The face the wire is on.
	 * @param direction The direction the wire is emitting signal to.
	 * @return Whether the wire can connect.
	 */
	boolean canConnect(WireType type, WireFace face, EnumFacing direction);
}
