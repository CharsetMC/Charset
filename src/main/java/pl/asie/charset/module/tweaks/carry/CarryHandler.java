package pl.asie.charset.module.tweaks.carry;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import pl.asie.charset.api.carry.CustomCarryHandler;
import pl.asie.charset.api.carry.ICarryHandler;
import pl.asie.charset.api.lib.ICacheable;
import pl.asie.charset.api.lib.IMovable;
import pl.asie.charset.api.lib.IMultiblockStructure;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.capability.CapabilityHelper;
import pl.asie.charset.lib.capability.CapabilityProviderFactory;
import pl.asie.charset.lib.utils.RotationUtils;

import javax.annotation.Nullable;
import java.util.Iterator;

public class CarryHandler implements ICacheable, ICarryHandler {
    public static CapabilityProviderFactory<CarryHandler> PROVIDER;
    public static final BlockPos ACCESS_POS = new BlockPos(0, 64, 0);

    private Entity player;
    private Access access;
    private IBlockState block;
    private NBTTagCompound tile;
    private TileEntity tileInstance;
    private float grabbedYaw;

    protected CustomCarryHandler customCarryHandler;

    public CarryHandler() {
        this.access = new Access();
    }

    public CarryHandler setPlayer(Entity player) {
        this.player = player;
        return this;
    }

    public void update() {
        if (customCarryHandler != null) {
            customCarryHandler.tick();
        }
    }

    private void setTile(TileEntity tile) {
        if (tile != null) {
            this.tile = tile.writeToNBT(new NBTTagCompound());

            // Rendering position
            this.tile.setInteger("x", 0);
            this.tile.setInteger("y", 64);
            this.tile.setInteger("z", 0);
        } else {
            this.tile = null;
        }
        this.tileInstance = null;
    }

    protected void setCustomCarryHandler(boolean emptied) {
        if (!emptied && block != null) {
            CustomCarryHandler.Provider provider = CapabilityHelper.getBlockCapability(access, ACCESS_POS, block, Capabilities.CUSTOM_CARRY_PROVIDER);
            if (provider != null) {
                customCarryHandler = provider.create(this);
            } else {
                TileEntity tile = getTile();
                if (tile.hasCapability(Capabilities.CUSTOM_CARRY_PROVIDER, null)) {
                    customCarryHandler = tile.getCapability(Capabilities.CUSTOM_CARRY_PROVIDER, null).create(this);
                } else {
                    customCarryHandler = null;
                }
            }
        } else {
            customCarryHandler = null;
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
        setCustomCarryHandler(false);
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
            } else {
                setTile(null);
            }

            setCustomCarryHandler(false);
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

        // Check IMultiblockStructure
        IMultiblockStructure structure = CapabilityHelper.get(world, pos, Capabilities.MULTIBLOCK_STRUCTURE, null,
                true, true, false);
        if (structure != null && !structure.isSeparable()) {
            int count = 0;
            Iterator<BlockPos> it = structure.iterator();
            while (it.hasNext()) {
                if (++count >= 2) return false;
            }
        }

        // Check IMovable
        IMovable movable = CapabilityHelper.get(world, pos, Capabilities.MOVABLE, null,
                true, true, false);
        if (movable != null && !movable.canMoveFrom()) {
            return false;
        }

        float hardness = player instanceof EntityPlayer
                ? block.getPlayerRelativeBlockHardness((EntityPlayer) player, world, pos)
                : block.getBlockHardness(world, pos);

        if (hardness <= 0) {
            return false;
        }

        return true;
    }


    public boolean place(World world, BlockPos pos, EnumFacing facing, EntityLivingBase player) {
        if (block != null) {
            if (hasTileEntity()) {
                IMovable movable = CapabilityHelper.get(Capabilities.MOVABLE, getTile(), null);
                if (movable != null && !movable.canMoveTo(world, pos)) {
                    return false;
                }
            }

            if (world.mayPlace(block.getBlock(), pos, false, facing, player)) {
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

                if (customCarryHandler != null) {
                    customCarryHandler.onPlace(world, pos);
                }

                float yawDiff = player != null ? grabbedYaw - player.rotationYaw : 0.0F;
                while (yawDiff < 0)
                    yawDiff += 360.0F;
                int rotCycles = MathHelper.floor((double) (yawDiff * 4.0F / 360.0F) + 0.5D) & 3;

                if (rotCycles > 0) {
                    RotationUtils.rotateAround(world, pos, EnumFacing.UP, rotCycles);
                }

                IBlockState newState = world.getBlockState(pos);

                if (player instanceof EntityPlayer) {
                    SoundType soundtype = newState.getBlock().getSoundType(newState, world, pos, player);
                    world.playSound((EntityPlayer) player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                }

                // TODO: Check if I break something
                try {
                    newState.neighborChanged(world, pos, oldBlock.getBlock(), pos);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }

                CharsetTweakBlockCarrying.syncCarryWithAllClients(player);
                setCustomCarryHandler(true);

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
        setCustomCarryHandler(true);
    }

    @Override
    public IBlockState getState() {
        return block;
    }

    @Override
    public NBTTagCompound getTileNBT() {
        return tile;
    }

    @Override
    public TileEntity getTile() {
        if (tileInstance != null) {
            tileInstance.setWorld(player.world);
            return tileInstance;
        } else {
            return tile != null ? (tileInstance = TileEntity.create(player.world, tile)) : null;
        }
    }

    @Override
    public Entity getCarrier() {
        return player;
    }

    public static void register() {
        Capability.IStorage<CarryHandler> storage = new CarryHandler.Storage();
        CapabilityManager.INSTANCE.register(CarryHandler.class, storage, CarryHandler.class);
        CarryHandler.PROVIDER = new CapabilityProviderFactory<>(CharsetTweakBlockCarrying.CAPABILITY, storage);
    }

    @Override
    public boolean isCacheValid() {
        return !player.isDead;
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
                        instance.setCustomCarryHandler(false);
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
            return pos.equals(ACCESS_POS) ? CarryHandler.this.getTile() : null;
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
