package pl.asie.charset.storage.tanks;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.blocks.BlockBase;
import pl.asie.charset.lib.capability.CapabilityHelper;

import javax.annotation.Nullable;

/**
 * Created by asie on 2/11/17.
 */
public class BlockTank extends BlockBase implements ITileEntityProvider {
    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0.05f, 0, 0.05f, 0.95f, 1, 0.95f);
    private static final PropertyInteger CONNECTIONS = PropertyInteger.create("connections", 0, 3);

    public BlockTank() {
        super(Material.GLASS);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return state.withProperty(CONNECTIONS,
                (((worldIn.getBlockState(pos.up()).getBlock() == this) ? 0 : 2)
                | (((worldIn.getBlockState(pos.down()).getBlock() == this) ? 0 : 1)))
            );
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
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = playerIn.getHeldItem(hand);
        EntityEquipmentSlot slot = (hand == EnumHand.OFF_HAND) ? EntityEquipmentSlot.OFFHAND : EntityEquipmentSlot.MAINHAND;
        IFluidHandler handler = CapabilityHelper.get(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, stack, null);
        if (handler != null) {
            TileEntity tile = worldIn.getTileEntity(pos);

            if (tile instanceof TileTank) {
                TileTank tank = (TileTank) tile;
                if (!worldIn.isRemote) {
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
                                }
                            }
                        }
                    } else {
                        // holder -> tank
                        int amount = tank.fill(fluidExtracted, false);
                        if (amount > 0) {
                            fluidExtracted.amount = amount;
                            fluidExtracted = handler.drain(fluidExtracted, true);
                            if (fluidExtracted != null) {
                                tank.fill(fluidExtracted, true);
                            }
                        }
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
        TileEntity aboveEntity = worldIn.getTileEntity(pos);
        TileEntity belowEntity = worldIn.getTileEntity(pos);
        if (aboveEntity instanceof TileTank) {
            TileTank aboveTank = (TileTank) aboveEntity;
            TileTank belowTank = (TileTank) belowEntity;
            if (aboveTank.fluidStack != null && belowTank.fluidStack != null && aboveTank.fluidStack.isFluidEqual(belowTank.fluidStack)) {
                return false;
            }
        }

        return true;
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

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileTank();
    }
}
