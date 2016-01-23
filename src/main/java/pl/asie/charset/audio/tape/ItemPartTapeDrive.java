package pl.asie.charset.audio.tape;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import pl.asie.charset.lib.multipart.ItemPartSlab;
import pl.asie.charset.lib.multipart.PartSlab;

/**
 * Created by asie on 1/22/16.
 */
public class ItemPartTapeDrive extends ItemPartSlab {
	public ItemPartTapeDrive() {
		super();
		setUnlocalizedName("charset.tapedrive");
	}

	@Override
	public PartSlab createPartSlab(World world, BlockPos blockPos, EnumFacing facing, Vec3 vec3, ItemStack stack, EntityPlayer player) {
		PartTapeDrive tapeDrive = new PartTapeDrive();
		tapeDrive.facing = player.getHorizontalFacing().getOpposite();
		return tapeDrive;
	}
}
