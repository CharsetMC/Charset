package pl.asie.charset.farming;

import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.RayTraceUtils;

import java.util.List;

public class UniversalClayBucket extends UniversalBucket {
    public UniversalClayBucket() {
        super(Fluid.BUCKET_VOLUME, null, false);
        setCreativeTab(ModCharsetLib.CREATIVE_TAB);
        setUnlocalizedName("charset.clayBucket");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
        subItems.add(getEmpty());
        super.getSubItems(itemIn, tab, subItems);
    }

    @Override
    public ItemStack getEmpty() {
        return new ItemStack(ModCharsetFarming.clayBucket);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemstack, World world, EntityPlayer player, EnumHand hand) {
        if (ItemStack.areItemStacksEqual(itemstack, getEmpty())) {
            FillBucketEvent fakeEvent = new FillBucketEvent(player, itemstack, world, world.rayTraceBlocks(RayTraceUtils.getStart(player), RayTraceUtils.getEnd(player), true));
            if (fakeEvent.getTarget() != null && fakeEvent.getTarget().typeOfHit == RayTraceResult.Type.BLOCK) {
                IBlockState state = world.getBlockState(fakeEvent.getTarget().getBlockPos());
                if (state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.LAVA) {
                    // TODO
                } else {
                    onFillBucket(fakeEvent);
                    if (!fakeEvent.isCanceled() && fakeEvent.getResult() != Event.Result.DENY
                            && fakeEvent.getFilledBucket() != null) {
                        return ActionResult.newResult(EnumActionResult.SUCCESS, fakeEvent.getFilledBucket());
                    }
                }
            }
        }

        return super.onItemRightClick(itemstack, world, player, hand);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        FluidStack fluidStack = getFluid(stack);
        if (fluidStack == null) {
            return I18n.translateToLocal(getUnlocalizedName() + ".empty.name");
        }

        String prefix = this.getUnlocalizedName() + ".filled";
        if (I18n.canTranslate(prefix + "." + fluidStack.getFluid().getName())) {
            return I18n.translateToLocal(prefix + "." + fluidStack.getFluid().getName());
        } else {
            return I18n.translateToLocalFormatted(prefix + ".name", fluidStack.getLocalizedName());
        }
    }

    @Override
    public int fill(ItemStack container, FluidStack resource, boolean doFill) {
        if (container.stackSize != 1) {
            return 0;
        }

        if (resource == null || resource.amount != getCapacity()
                || resource.getFluid().getTemperature(resource) > 400
                || resource.getFluid().isGaseous(resource)) {
            return 0;
        }

        if (doFill) {
            NBTTagCompound tag = ItemUtils.getTagCompound(container, true);
            resource.writeToNBT(tag);
        }

        return getCapacity();
    }
}
