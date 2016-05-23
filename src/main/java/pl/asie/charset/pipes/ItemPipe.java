package pl.asie.charset.pipes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraftforge.fmp.item.ItemMultipart;
import net.minecraftforge.fmp.multipart.IMultipart;
import pl.asie.charset.lib.ModCharsetLib;

public class ItemPipe extends ItemMultipart {
	public ItemPipe() {
		setUnlocalizedName("charset.pipe");
		setCreativeTab(ModCharsetLib.CREATIVE_TAB);
	}

	@Override
	public IMultipart createPart(World world, BlockPos pos, EnumFacing side, Vec3d hit, ItemStack stack, EntityPlayer player) {
		return new PartPipe();
	}
}
