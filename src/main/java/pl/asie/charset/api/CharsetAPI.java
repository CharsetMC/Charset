package pl.asie.charset.api;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.api.lib.IBlockCapabilityProvider;
import pl.asie.charset.api.lib.ISimpleInstantiatingRegistry;

import javax.annotation.Nullable;

public class CharsetAPI {
	public static CharsetAPI INSTANCE = new CharsetAPI();

	public boolean isPresent() {
		return false;
	}

	public <T> boolean mayHaveBlockCapability(Capability<T> capability, IBlockState state) {
		return false;
	}

	@Nullable
	public final <T> T getBlockCapability(Capability<T> capability, IBlockAccess world, BlockPos pos, EnumFacing facing) {
		return getBlockCapability(capability, world, pos, world.getBlockState(pos), facing);
	}

	@Nullable
	public <T> T getBlockCapability(Capability<T> capability, IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing facing) {
		return null;
	}

	// The following methods should be called after isPresent() ONLY.

	public <T> void registerBlockCapabilityProvider(Capability<T> capability, Block block, IBlockCapabilityProvider<T> provider) {
		throw new RuntimeException("Charset API not initialized - please use isPresent()!");
	}

	@Nullable
	public <T> ISimpleInstantiatingRegistry<T> findSimpleInstantiatingRegistry(Class<T> c) {
		throw new RuntimeException("Charset API not initialized - please use isPresent()!");
	}
}
