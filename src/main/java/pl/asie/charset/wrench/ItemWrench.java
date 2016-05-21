package pl.asie.charset.wrench;

import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.MultipartContainer;
import mcmultipart.multipart.MultipartHelper;
import mcmultipart.raytrace.RayTraceUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pl.asie.charset.lib.ModCharsetLib;

public class ItemWrench extends Item {
    public ItemWrench() {
        super();
        setCreativeTab(ModCharsetLib.CREATIVE_TAB);
        setUnlocalizedName("charset.wrench");
        setHarvestLevel("wrench", 2);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return true;
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            EnumFacing targetFacing = facing != null ? facing : EnumFacing.UP;
            if (playerIn != null && playerIn.isSneaking()) {
                targetFacing = targetFacing.getOpposite();
            }

            IMultipartContainer container = MultipartHelper.getPartContainer(worldIn, pos);
            if (container != null) {
                Vec3d start = RayTraceUtils.getStart(playerIn);
                Vec3d end = RayTraceUtils.getEnd(playerIn);
                double dist = Double.POSITIVE_INFINITY;
                RayTraceUtils.AdvancedRayTraceResultPart result = null;

                for (IMultipart p : container.getParts()) {
                    RayTraceUtils.AdvancedRayTraceResultPart pResult = p.collisionRayTrace(start, end);
                    if (pResult != null) {
                        double d = pResult.squareDistanceTo(start);
                        if (d <= dist) {
                            dist = d;
                            result = pResult;
                        }
                    }
                }

                if (result != null && result.hit != null && result.hit.partHit != null) {
                    return result.hit.partHit.rotatePart(targetFacing) ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
                }
            } else {
                IBlockState state = worldIn.getBlockState(pos);
                if (state != null) {
                    return state.getBlock().rotateBlock(worldIn, pos, targetFacing) ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
                }
            }
        }
        return EnumActionResult.SUCCESS;
    }
}
