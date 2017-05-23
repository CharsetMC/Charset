package pl.asie.charset.module.tweaks.carry;

import net.minecraft.block.Block;
import net.minecraft.block.BlockMobSpawner;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import pl.asie.charset.api.lib.IMovable;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.capability.CapabilityHelper;
import pl.asie.charset.lib.capability.CapabilityProviderFactory;
import pl.asie.charset.lib.utils.RotationUtils;

import javax.annotation.Nullable;

public class CarryHandler {
    public static CapabilityProviderFactory<CarryHandler> PROVIDER;
    public static final BlockPos ACCESS_POS = new BlockPos(0, 64, 0);

    private Entity player;
    private Access access;
    private IBlockState block;
    private NBTTagCompound tile;
    private TileEntity tileInstance;
    private float grabbedYaw;

    // TODO: Refactor into ICustomCarryHandler
    protected MobSpawnerBaseLogic spawnerLogic;

    public CarryHandler() {
        this.access = new Access();
    }

    public CarryHandler setPlayer(Entity player) {
        this.player = player;
        return this;
    }

    public void update() {
        if (spawnerLogic != null) {
            spawnerLogic.updateSpawner();
        }
    }

    private void setTile(TileEntity tile) {
        if (tile != null) {
            this.tile = tile.writeToNBT(new NBTTagCompound());
            this.tile.setInteger("x", 0);
            this.tile.setInteger("y", 64);
            this.tile.setInteger("z", 0);
        } else {
            this.tile = null;
        }
        onSetTile(tile == null);
        this.tileInstance = null;
    }

    private void onSetTile(boolean emptied) {
        if (!emptied && block != null && block.getBlock() instanceof BlockMobSpawner) {
            spawnerLogic = new MobSpawnerBaseLogic() {
                @Override
                public void broadcastEvent(int id) {

                }

                @Override
                public World getSpawnerWorld() {
                    return player.getEntityWorld();
                }

                @Override
                public BlockPos getSpawnerPosition() {
                    return player.getPosition();
                }
            };
            spawnerLogic.readFromNBT(tile);
        } else {
            spawnerLogic = null;
        }
    }

    public IBlockAccess getBlockAccess() {
        return access;
    }

    public float getGrabbedYaw() { return grabbedYaw; }

    public boolean isCarrying() {
        return block != null;
    }

    public void put(IBlockState state, TileEntity tile) {
        grabbedYaw = player != null ? player.rotationYaw : 0.0F;
        this.block = state;
        setTile(tile);
    }

    public boolean grab(World world, BlockPos pos) {
        if (block == null) {
            grabbedYaw = player != null ? player.rotationYaw : 0.0F;

            block = world.getBlockState(pos);

            if (block.getBlock().isAir(block, world, pos) || !canPickUp(world, pos, block)) {
                block = null;
                return false;
            }

            if (block.getBlock().hasTileEntity(block)) {
                setTile(world.getTileEntity(pos));
                world.removeTileEntity(pos);
            }

            world.setBlockToAir(pos);

            return true;
        } else {
            return false;
        }
    }

    private boolean canPickUp(World world, BlockPos pos, IBlockState block) {
        if (player instanceof EntityPlayer && ((EntityPlayer) player).isCreative()) {
            return true;
        }

        float hardness = player instanceof EntityPlayer
                ? block.getPlayerRelativeBlockHardness((EntityPlayer) player, world, pos)
                : block.getBlockHardness(world, pos);

        if (hardness <= 0) {
            return false;
        }

        return true;
    }


    public boolean place(World world, BlockPos pos, EnumFacing facing) {
        if (block != null) {
            if (hasTileEntity()) {
                IMovable movable = CapabilityHelper.get(Capabilities.MOVABLE, getTileEntity(), null);
                if (movable != null && !movable.canMoveTo(world, pos)) {
                    return false;
                }
            }

            if (world.mayPlace(block.getBlock(), pos, false, facing, null)) {
                world.setBlockState(pos, block);
                IBlockState oldBlock = block;
                block = null;

                if (tile != null) {
                    tile.setInteger("x", pos.getX());
                    tile.setInteger("y", pos.getY());
                    tile.setInteger("z", pos.getZ());
                    world.setTileEntity(pos, TileEntity.create(world, tile));
                    setTile(null);
                }

                float yawDiff = player != null ? grabbedYaw - player.rotationYaw : 0.0F;
                while (yawDiff < 0)
                    yawDiff += 360.0F;
                int rotCycles = MathHelper.floor((double) (yawDiff * 4.0F / 360.0F) + 0.5D) & 3;

                if (rotCycles > 0) {
                    RotationUtils.rotateAround(world, pos, EnumFacing.UP, rotCycles);
                }

                // TODO: Check if I break something
                world.getBlockState(pos).neighborChanged(world, pos, oldBlock.getBlock(), pos);

                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean hasTileEntity() {
        return tile != null;
    }

    public void empty() {
        block = null;
        setTile(null);
    }

    public IBlockState getBlockState() {
        return block;
    }

    public TileEntity getTileEntity() {
        return tileInstance != null ? tileInstance : (tile != null ? (tileInstance = TileEntity.create(player.world, tile)) : null);
    }

    public static void register() {
        Capability.IStorage<CarryHandler> storage = new CarryHandler.Storage();
        CapabilityManager.INSTANCE.register(CarryHandler.class, storage, CarryHandler.class);
        CarryHandler.PROVIDER = new CapabilityProviderFactory<>(CharsetTweakBlockCarrying.CAPABILITY, storage);
    }

    public static class Storage implements Capability.IStorage<CarryHandler> {
        @Override
        public NBTBase writeNBT(Capability<CarryHandler> capability, CarryHandler instance, EnumFacing side) {
            NBTTagCompound compound = new NBTTagCompound();
            if (instance.block != null) {
                compound.setString("b:name", instance.block.getBlock().getRegistryName().toString());
                compound.setByte("b:meta", (byte) instance.block.getBlock().getMetaFromState(instance.block));
                compound.setFloat("p:yaw", instance.grabbedYaw);

                if (instance.tile != null) {
                    compound.setTag("b:tile", instance.tile);
                }
            }
            return compound;
        }

        @Override
        public void readNBT(Capability<CarryHandler> capability, CarryHandler instance, EnumFacing side, NBTBase nbt) {
            instance.empty();

            if (nbt instanceof NBTTagCompound) {
                NBTTagCompound compound = (NBTTagCompound) nbt;
                if (compound.hasKey("b:name") && compound.hasKey("b:meta")) {
                    Block block = Block.getBlockFromName(compound.getString("b:name"));
                    instance.block = block.getStateFromMeta(compound.getByte("b:meta"));
                    instance.grabbedYaw = compound.getFloat("p:yaw");

                    if (compound.hasKey("b:tile")) {
                        instance.tile = compound.getCompoundTag("b:tile");
                        instance.onSetTile(false);
                    }
                }
            }
        }
    }

    private class Access implements IBlockAccess {
        private BlockPos getPlayerPos() {
            return player != null ? new BlockPos(player.posX, player.posY + player.getEyeHeight(), player.posZ) : ACCESS_POS;
        }

        @Nullable
        @Override
        public TileEntity getTileEntity(BlockPos pos) {
            return pos.equals(ACCESS_POS) ? CarryHandler.this.getTileEntity() : null;
        }

        @Override
        public int getCombinedLight(BlockPos pos, int lightValue) {
            return player != null
                ? player.world.getCombinedLight(getPlayerPos(), lightValue)
                : (15 << 20 | 15 << 4);
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
            return player != null ? player.world.getBiome(getPlayerPos()) : Biome.getBiome(1);
        }

        @Override
        public int getStrongPower(BlockPos pos, EnumFacing direction) {
            return 0;
        }

        @Override
        public WorldType getWorldType() {
            return player != null ? player.world.getWorldType() : WorldType.DEFAULT;
        }

        @Override
        public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
            return false;
        }
    }
}
