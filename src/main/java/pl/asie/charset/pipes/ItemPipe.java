package pl.asie.charset.pipes;

import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import mcmultipart.item.ItemMultiPart;
import mcmultipart.multipart.IMultipart;
import pl.asie.charset.lib.ModCharsetLib;

public class ItemPipe extends ItemMultiPart {
    public ItemPipe() {
        setUnlocalizedName("charset.pipe");
        setCreativeTab(ModCharsetLib.CREATIVE_TAB);
    }

    @Override
    public IMultipart createPart(World world, BlockPos blockPos, EnumFacing enumFacing, Vec3 vec3, ItemStack itemStack) {
        return new PartPipe();
    }
}
