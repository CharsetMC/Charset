package pl.asie.charset.api.lib;

import java.util.List;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public abstract class CharsetHelper {
	public static CharsetHelper instance;

	// The following are temporary until the Forge Capabilities API is introduced.
	public abstract <T> T getInterface(Class<T> clazz, World world, BlockPos pos, EnumFacing side);

	public abstract <T> T getInterface(Class<T> clazz, World world, BlockPos pos, EnumFacing side, EnumFacing face);

	public abstract <T> List<T> getInterfaceList(Class<T> clazz, World world, BlockPos pos);
}
