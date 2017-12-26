package pl.asie.charset.module.tweaks.slide;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.util.Constants;
import pl.asie.charset.module.tweaks.carry.CarryHandler;

import javax.annotation.Nullable;
import java.util.Optional;

public class EntitySlidingBlock extends Entity {
	public static final BlockPos ACCESS_POS = new BlockPos(0, 64, 0);
	private static final DataParameter<Integer> STATE_PARAM = EntityDataManager.createKey(EntitySlidingBlock.class, DataSerializers.VARINT);
	private static final DataParameter<NBTTagCompound> TILE_PARAM = EntityDataManager.createKey(EntitySlidingBlock.class, DataSerializers.COMPOUND_TAG);

	private class Access implements IBlockAccess {
		@Nullable
		@Override
		public TileEntity getTileEntity(BlockPos pos) {
			return pos.equals(ACCESS_POS) ? getTile() : null;
		}

		@Override
		public int getCombinedLight(BlockPos pos, int lightValue) {
			return world.getCombinedLight(new BlockPos(Math.floor(posX), Math.ceil(posY), Math.floor(posZ)), lightValue);
		}

		@Override
		public IBlockState getBlockState(BlockPos pos) {
			return pos.equals(ACCESS_POS) ? block : Blocks.AIR.getDefaultState();
		}

		@Override
		public boolean isAirBlock(BlockPos pos) {
			return !pos.equals(ACCESS_POS);
		}

		@Override
		public Biome getBiome(BlockPos pos) {
			return world.getBiome(getPosition());
		}

		@Override
		public int getStrongPower(BlockPos pos, EnumFacing direction) {
			return 0;
		}

		@Override
		public WorldType getWorldType() {
			return world.getWorldType();
		}

		@Override
		public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
			return false;
		}
	}

	protected IBlockState block;
	protected NBTTagCompound tile;
	protected IBlockAccess access;
	private TileEntity tileInstance;

	public EntitySlidingBlock(World worldIn) {
		super(worldIn);
		this.preventEntitySpawning = true;
		this.isImmuneToFire = true;
		this.setSize(0.98F, 0.98F);
	}

	public EntitySlidingBlock(World worldIn, BlockPos pos, EnumFacing direction, IBlockState block, NBTTagCompound tile) {
		this(worldIn);
		this.setPosition(pos.getX() + 0.5f, pos.getY(), pos.getZ() + 0.5f);
		this.block = block;
		this.tile = tile;
		this.prevPosX = posX;
		this.prevPosY = posY;
		this.prevPosZ = posZ;
		this.motionX = 0.3f * direction.getFrontOffsetX();
		this.motionY = 0.00f;
		this.motionZ = 0.3f * direction.getFrontOffsetZ();

		dataManager.set(STATE_PARAM, Block.getStateId(block != null ? block : Blocks.AIR.getDefaultState()));
		dataManager.set(TILE_PARAM, tile != null ? tile : new NBTTagCompound());
	}

	public TileEntity getTile() {
		if (tileInstance != null) {
			tileInstance.setWorld(world);
			return tileInstance;
		} else {
			return tile != null ? (tileInstance = TileEntity.create(world, tile)) : null;
		}
	}

	public static boolean canSlideOn(World world, BlockPos pos, Entity entity) {
		if (!world.isAirBlock(pos.down())) {
			IBlockState ds = world.getBlockState(pos.down());
			float slip = ds.getBlock().getSlipperiness(ds, world, pos, entity);
			return slip >= 0.95f;
		} else {
			return true;
		}
	}

	@Override
	public void onUpdate() {
		if (world.isRemote && dataManager.isDirty()) {
			block = Block.getStateById(dataManager.get(STATE_PARAM));
			tile = dataManager.get(TILE_PARAM);
			if (!tile.hasKey("x")) {
				tile = null;
			}
		}

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if (!this.hasNoGravity()) {
			this.motionY -= 0.04;
		}

		this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);

		if (!world.isRemote) {
			boolean shouldPlace = this.collidedHorizontally;
			BlockPos pos = new BlockPos(Math.floor(posX), Math.ceil(posY), Math.floor(posZ));
			if (!shouldPlace && this.collidedVertically && !canSlideOn(world, pos, this)) {
				shouldPlace = true;
			}

			if (shouldPlace) {
				// huh
				this.setDead();
				if (world.isAirBlock(pos)) {
					world.setBlockState(pos, block);
					if (tile != null) {
						tile.setInteger("x", pos.getX());
						tile.setInteger("y", pos.getY());
						tile.setInteger("z", pos.getZ());
						world.setTileEntity(pos, TileEntity.create(world, tile));
					}
				}
			}
		}
	}

	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	@Override
	public boolean canBeCollidedWith() {
		return !this.isDead;
	}

	@Override
	protected void entityInit() {
		access = new Access();

		dataManager.register(STATE_PARAM, Block.getStateId(block != null ? block : Blocks.AIR.getDefaultState()));
		dataManager.register(TILE_PARAM, tile != null ? tile : new NBTTagCompound());
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		block = Block.getStateById(compound.getInteger("state"));
		if (compound.hasKey("tile", Constants.NBT.TAG_COMPOUND)) {
			tile = compound.getCompoundTag("tile");
		}
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		compound.setInteger("state", Block.getStateId(block));
		if (tile != null) {
			compound.setTag("tile", tile);
		}
	}
}
