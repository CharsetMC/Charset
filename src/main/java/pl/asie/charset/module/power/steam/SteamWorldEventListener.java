package pl.asie.charset.module.power.steam;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import pl.asie.charset.module.power.steam.api.IMirror;

import javax.annotation.Nullable;

public class SteamWorldEventListener implements IWorldEventListener {
	private final World world;

	public SteamWorldEventListener(World world) {
		this.world = world;
	}

	@Override
	public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
		if (oldState.getBlock() instanceof BlockMirror || newState.getBlock() instanceof BlockMirror) {
			MirrorChunkContainer.forEach(worldIn, pos, IMirror::requestMirrorTargetRefresh);
		} else if (oldState != newState) {
			MirrorChunkContainer.forEachListening(worldIn, pos, IMirror::requestMirrorTargetRefresh);
		}
	}

	@Override
	public void notifyLightSet(BlockPos pos) {
		if (world.isBlockLoaded(pos)) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileMirror) {
				((TileMirror) tile).requestMirrorTargetRefresh();
			}
		}
	}

	@Override
	public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {

	}

	@Override
	public void playSoundToAllNearExcept(@Nullable EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x, double y, double z, float volume, float pitch) {

	}

	@Override
	public void playRecord(SoundEvent soundIn, BlockPos pos) {

	}

	@Override
	public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {

	}

	@Override
	public void spawnParticle(int id, boolean ignoreRange, boolean p_190570_3_, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int... parameters) {

	}

	@Override
	public void onEntityAdded(Entity entityIn) {

	}

	@Override
	public void onEntityRemoved(Entity entityIn) {

	}

	@Override
	public void broadcastSound(int soundID, BlockPos pos, int data) {

	}

	@Override
	public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {

	}

	@Override
	public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {

	}
}
