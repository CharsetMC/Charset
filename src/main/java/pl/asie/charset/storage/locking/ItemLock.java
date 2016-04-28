package pl.asie.charset.storage.locking;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.storage.ModCharsetStorage;

import java.util.List;

public class ItemLock extends Item {
    public static class Color implements IItemColor {
        @Override
        public int getColorFromItemstack(ItemStack stack, int tintIndex) {
            if (stack.hasTagCompound()) {
                for (int i = tintIndex; i >= 0; i--) {
                    String key = "color" + i;
                    if (stack.getTagCompound().hasKey(key)) {
                        return stack.getTagCompound().getInteger(key);
                    }
                }
            }

            return ModCharsetStorage.DEFAULT_LOCKING_COLOR;
        }
    }

    public ItemLock() {
        super();
        setCreativeTab(ModCharsetLib.CREATIVE_TAB);
        setUnlocalizedName("charset.lock");
    }

    public String getKey(ItemStack stack) {
        return "charset:key:" + getRawKey(stack);
    }

    public String getRawKey(ItemStack stack) {
        return stack.getTagCompound() != null && stack.getTagCompound().hasKey("key") ? stack.getTagCompound().getString("key") : "null";
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (getKey(stack) == null) {
            return EnumActionResult.FAIL;
        }

        BlockPos blockpos = pos.offset(facing);

        if (facing != EnumFacing.DOWN && facing != EnumFacing.UP && playerIn.canPlayerEdit(blockpos, facing, stack)) {
            EntityLock lockEntity = new EntityLock(worldIn, stack, blockpos, facing);

            if (lockEntity != null && lockEntity.onValidSurface()) {
                if (!worldIn.isRemote) {
                    lockEntity.playPlaceSound();
                    worldIn.spawnEntityInWorld(lockEntity);
                }

                stack.stackSize--;
            }

            return EnumActionResult.SUCCESS;
        } else {
            return EnumActionResult.FAIL;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        if (ItemKey.DEBUG_KEY_ID) {
            tooltip.add(getKey(stack));
        }
    }
}
