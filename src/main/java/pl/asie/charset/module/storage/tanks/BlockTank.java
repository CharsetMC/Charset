package pl.asie.charset.module.storage.tanks;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.block.BlockBase;
import pl.asie.charset.lib.capability.CapabilityHelper;

/**
 * Created by asie on 2/11/17.
 */
public class BlockTank extends BlockBase implements ITileEntityProvider {
    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0.05f, 0, 0.05f, 0.95f, 1, 0.95f);
    private static final PropertyInteger CONNECTIONS = PropertyInteger.create("connections", 0, 3);

    public BlockTank() {
        super(Material.GLASS);
        setHardness(0.6F);
        setUnlocalizedName("charset.tank");
        setFullCube(false);
        setOpaqueCube(false);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return state.withProperty(CONNECTIONS,
                (((worldIn.getBlockState(pos.up()).getBlock() == this) ? 0 : 2)
                | (((worldIn.getBlockState(pos.down()).getBlock() == this) ? 0 : 1)))
            );
    }

    @Override
    public void onBlockDestroyedByExplosion(World worldIn, BlockPos pos, Explosion explosionIn) {
        BlockPos tankPos = pos;
        TileEntity tankEntity = worldIn.getTileEntity(tankPos);
        if (tankEntity instanceof TileTank) {
            TileTank tank = (TileTank) tankEntity;
            if (tank.fluidStack != null && tank.fluidStack.amount >= 1000) {
                float chance = (float) (tank.fluidStack.amount) / 8000;
                if (worldIn.rand.nextFloat() <= chance) {
                    worldIn.setBlockState(pos, tank.fluidStack.getFluid().getBlock().getDefaultState());
                }
            }
        }
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        worldIn.notifyNeighborsRespectDebug(pos, state.getBlock(), false);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, CONNECTIONS);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOUNDING_BOX;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (hand != EnumHand.MAIN_HAND)
            return false;

        ItemStack stack = playerIn.getHeldItem(hand);
        IFluidHandlerItem handler = CapabilityHelper.get(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, stack, null);
        if (handler != null) {
            TileEntity tile = worldIn.getTileEntity(pos);

            if (tile instanceof TileTank) {
                TileTank tank = (TileTank) tile;
                if (!worldIn.isRemote) {
                    boolean changed = false;

                    FluidStack fluidContained = tank.getBottomTank().fluidStack;
                    FluidStack fluidExtracted;
                    if (fluidContained != null) {
                        FluidStack f = fluidContained.copy();
                        f.amount = Fluid.BUCKET_VOLUME;
                        fluidExtracted = handler.drain(f, false);
                    } else {
                        fluidExtracted = handler.drain(Fluid.BUCKET_VOLUME, false);
                    }

                    if (fluidExtracted == null) {
                        // tank -> holder
                        fluidExtracted = tank.drain(Fluid.BUCKET_VOLUME, false);
                        if (fluidExtracted != null) {
                            int amount = handler.fill(fluidExtracted, false);
                            if (amount > 0) {
                                fluidExtracted.amount = amount;
                                fluidExtracted = tank.drain(fluidExtracted, true);
                                if (fluidExtracted != null) {
                                    handler.fill(fluidExtracted, true);
                                    changed = true;
                                }
                            }
                        }
                    } else {
                        // holder -> tank
                        int amount = tank.fill(fluidExtracted, false);
                        if (amount > 0) {
                            fluidExtracted.amount = amount;
                            fluidExtracted = handler.drain(fluidExtracted, !playerIn.isCreative());
                            if (fluidExtracted != null) {
                                tank.fill(fluidExtracted, true);
                                changed = true;
                            }
                        }
                    }

                    if (changed) {
                        playerIn.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, handler.getContainer());
                    }
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        // People may not place an empty tank between two tanks which differ only in liquid
        return !TileTank.checkPlacementConflict(worldIn.getTileEntity(pos.down()), worldIn.getTileEntity(pos.up()));
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        // Tank connection refresh logic
        if (blockIn == this || worldIn.isAirBlock(fromPos)) {
            BlockPos tankPos = pos;
            TileEntity tankEntity = worldIn.getTileEntity(tankPos);
            if (tankEntity instanceof TileTank) {
                ((TileTank) tankEntity).updateAboveTank();
                while (tankEntity instanceof TileTank) {
                    ((TileTank) tankEntity).findBottomTank();
                    tankPos = tankPos.up();
                    tankEntity = worldIn.getTileEntity(tankPos);
                }
            }
        }
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileTank();
    }
}
