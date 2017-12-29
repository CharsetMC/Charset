package pl.asie.charset.module.tweaks.slide;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.util.Constants;
import pl.asie.charset.lib.utils.SpaceUtils;
import pl.asie.charset.module.tweaks.carry.CarryHandler;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;

public class EntitySlidingBlock extends Entity {
	public static final BlockPos ACCESS_POS = new BlockPos(0, 64, 0);
	private static final DataParameter<NBTTagCompound> DATA_PARAMETER = EntityDataManager.createKey(EntitySlidingBlock.class, DataSerializers.COMPOUND_TAG);

	private class Access implements IBlockAccess {
		@Nullable
		@Override
		public TileEntity getTileEntity(BlockPos pos) {
			return getTile(pos);
		}

		@Override
		public int getCombinedLight(BlockPos pos, int lightValue) {
			return world.getCombinedLight(new BlockPos(Math.floor(posX), Math.ceil(posY), Math.floor(posZ)), lightValue);
		}

		@Override
		public IBlockState getBlockState(BlockPos pos) {
			return blockStates.getOrDefault(pos, Blocks.AIR.getDefaultState());
		}

		@Override
		public boolean isAirBlock(BlockPos pos) {
			return !blockStates.containsKey(pos);
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

	protected IBlockAccess access;
	protected Map<BlockPos, IBlockState> blockStates;
	private Map<BlockPos, NBTTagCompound> blockTileNBTs;
	private Map<BlockPos, TileEntity> blockTileInstances;

	private void recalcBoundingBox() {
		if (world.isRemote) {
			return;
		}

		AxisAlignedBB bb = new AxisAlignedBB(ACCESS_POS);
		if (blockStates != null) {
			for (BlockPos pos : blockStates.keySet()) {
				bb = SpaceUtils.include(bb, pos);
			}
		}
		System.out.println(bb);
		// now we want the center to be posX/posY/posZ
		bb = bb.offset(
				posX - ACCESS_POS.getX() - 0.5f,
				posY - ACCESS_POS.getY(),
				posZ - ACCESS_POS.getZ() - 0.5f
		).grow(-0.01f, 0.00f, -0.01f);
		setEntityBoundingBox(bb);
		this.height = (float) (bb.maxY - posY);
	}

	@Override
	protected void setSize(float width, float height) {
		super.setSize(width, height);
		recalcBoundingBox();
	}

	@Override
	public void setPosition(double x, double y, double z) {
		super.setPosition(x, y, z);
		recalcBoundingBox();
	}

	public NBTTagCompound serializeTileData() {
		NBTTagList list = new NBTTagList();

		for (BlockPos pos : blockStates.keySet()) {
			IBlockState state = blockStates.get(pos);
			NBTTagCompound tileTag = blockTileNBTs.get(pos);
			NBTTagCompound entry = new NBTTagCompound();
			entry.setLong("pos", pos.toLong());
			entry.setInteger("state", Block.getStateId(state != null ? state : Blocks.AIR.getDefaultState()));
			if (tileTag != null) {
				entry.setTag("tile", tileTag);
			}

			list.appendTag(entry);
		}

		NBTTagCompound container = new NBTTagCompound();
		container.setTag("entries", list);
		return container;
	}

	public void deserializeTileData(NBTTagCompound compound) {
		blockStates.clear();
		blockTileInstances.clear();
		blockTileNBTs.clear();

		NBTTagList list = compound.getTagList("entries", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound entry = list.getCompoundTagAt(i);
			BlockPos pos = BlockPos.fromLong(entry.getLong("pos"));
			IBlockState state = Block.getStateById(entry.getInteger("state"));
			blockStates.put(pos, state);
			if (entry.hasKey("tile", Constants.NBT.TAG_COMPOUND)) {
				blockTileNBTs.put(pos, entry.getCompoundTag("tile"));
			}
		}
	}

	public EntitySlidingBlock(World worldIn) {
		super(worldIn);

		blockStates = new HashMap<>();
		blockTileNBTs = new HashMap<>();
		blockTileInstances = new HashMap<>();

		this.preventEntitySpawning = true;
		this.isImmuneToFire = true;
		this.setSize(0.98F, 0.98F);
	}

	public EntitySlidingBlock(World worldIn, BlockPos pos, EnumFacing direction) {
		this(worldIn);

		Queue<BlockPos> blockPosQueue = new ArrayDeque<>();
		blockPosQueue.add(pos);

		while (!blockPosQueue.isEmpty()) {
			BlockPos nextPos = blockPosQueue.remove();
			if (!blockStates.containsKey(nextPos)) {
				BlockPos sPos = nextPos.subtract(pos).add(ACCESS_POS);
				IBlockState state = world.getBlockState(nextPos);
				blockStates.put(sPos, state);
				TileEntity tile = worldIn.getTileEntity(nextPos);
				if (tile != null) {
					blockTileNBTs.put(sPos, tile.writeToNBT(new NBTTagCompound()));
				}
				world.setBlockToAir(nextPos);

				if (state.getBlock().isStickyBlock(state)) {
					for (EnumFacing facing : EnumFacing.VALUES) {
						BlockPos addPos = nextPos.offset(facing);
						if (!addPos.equals(pos.down()) && !addPos.equals(pos.offset(direction.getOpposite())) && !worldIn.isAirBlock(addPos)) {
							blockPosQueue.add(addPos);
						}
					}
				}
			}
		}

		this.setPosition(pos.getX() + 0.5f, pos.getY(), pos.getZ() + 0.5f);
		this.prevPosX = posX;
		this.prevPosY = posY;
		this.prevPosZ = posZ;
		this.motionX = 0.3f * direction.getFrontOffsetX();
		this.motionY = 0.00f;
		this.motionZ = 0.3f * direction.getFrontOffsetZ();

		dataManager.set(DATA_PARAMETER, serializeTileData());
	}

	public TileEntity getTile(BlockPos pos) {
		if (blockTileInstances.containsKey(pos)) {
			TileEntity tile = blockTileInstances.get(pos);
			tile.setWorld(world);
			return tile;
		} else if (blockTileNBTs.containsKey(pos)) {
			TileEntity tile = TileEntity.create(world, blockTileNBTs.get(pos));
			blockTileInstances.put(pos, tile);
			return tile;
		} else {
			return null;
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
			deserializeTileData(dataManager.get(DATA_PARAMETER));
		}

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if (!this.hasNoGravity()) {
			this.motionY -= 0.04;
		}

		this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);

		if (!world.isRemote) {
			boolean shouldPlace = false;
			BlockPos pos = new BlockPos(Math.floor(posX), Math.ceil(posY), Math.floor(posZ));

			if (this.collidedVertically && !canSlideOn(world, pos, this)) {
				shouldPlace = true;
			}

			if (!shouldPlace) {
				List<AxisAlignedBB> list = new ArrayList<>();
				Set<BlockPos> checkedPos = new HashSet<>();

				for (BlockPos origPos : blockStates.keySet()) {
					Vec3d collidePos = new Vec3d(
							origPos.getX() + posX - 0.5f - ACCESS_POS.getX(),
							origPos.getY() + posY - ACCESS_POS.getY(),
							origPos.getZ() + posZ - 0.5f - ACCESS_POS.getZ()
					);
					AxisAlignedBB testBox = new AxisAlignedBB(collidePos.x,
							collidePos.y,
							collidePos.z,
							collidePos.x + 1,
							collidePos.y + 1,
							collidePos.z + 1);

					for (int iz = (int) Math.floor(collidePos.z); iz <= Math.ceil(collidePos.z); iz++)
						for (int iy = (int) Math.floor(collidePos.y); iy <= Math.ceil(collidePos.y); iy++)
							for (int ix = (int) Math.floor(collidePos.x); ix <= Math.ceil(collidePos.x); ix++) {
								BlockPos testPos = new BlockPos(ix, iy, iz);
								if (!checkedPos.contains(testPos)) {
									IBlockState state = world.getBlockState(testPos);
									if (!state.getBlock().isReplaceable(world, testPos)) {
										world.getBlockState(testPos).addCollisionBoxToList(
												world, testPos, testBox,
												list, this, false
										);
										if (!list.isEmpty()) {
											shouldPlace = true;
											break;
										}
									}

									checkedPos.add(testPos);
								}
							}
				}
			}

			if (shouldPlace) {
				// huh
				this.setDead();
				for (BlockPos origPos : blockStates.keySet()) {
					BlockPos targetPos = origPos.subtract(ACCESS_POS).add(pos);
					IBlockState state = world.getBlockState(targetPos);
					if (state.getBlock().isReplaceable(world, targetPos)) {
						world.setBlockState(targetPos, blockStates.get(origPos));
						NBTTagCompound tile = blockTileNBTs.get(origPos);
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

		dataManager.register(DATA_PARAMETER, new NBTTagCompound());
	}


	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		if (compound.hasKey("data", Constants.NBT.TAG_COMPOUND)) {
			deserializeTileData(compound.getCompoundTag("data"));
		} else if (compound.hasKey("state", Constants.NBT.TAG_ANY_NUMERIC)) {
			blockStates.clear();
			blockTileNBTs.clear();
			blockTileInstances.clear();

			blockStates.put(ACCESS_POS, Block.getStateById(compound.getInteger("state")));
			if (compound.hasKey("tile", Constants.NBT.TAG_COMPOUND)) {
				blockTileNBTs.put(ACCESS_POS, compound.getCompoundTag("tile"));
			}
		}
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		compound.setTag("data", serializeTileData());
	}
}
