package pl.asie.charset.lib.modcompat.mcmultipart;

import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.item.ItemBlockMultipart;
import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.multipart.MultipartHelper;
import mcmultipart.api.slot.IPartSlot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class MCMPUtils {
	private MCMPUtils() {

	}

	public static boolean placePartAt(ItemStack stack, EntityPlayer player, EnumHand hand, World world, BlockPos pos, EnumFacing facing,
	                                  float hitX, float hitY, float hitZ, IMultipart multipartBlock, IBlockState state) {
		IPartSlot slot = multipartBlock.getSlotForPlacement(world, pos, state, facing, hitX, hitY, hitZ, player);
		if (!multipartBlock.canPlacePartAt(world, pos) || !multipartBlock.canPlacePartOnSide(world, pos, facing, slot))
			return false;

		if (MultipartHelper.addPart(world, pos, slot, state, false)) {
			if (!world.isRemote) {
				IPartInfo info = MultipartHelper.getContainer(world, pos).flatMap(c -> c.get(slot)).orElse(null);
				if (info != null) {
					ItemBlockMultipart.setMultipartTileNBT(player, stack, info);
					if (multipartBlock instanceof IMultipartBase) {
						((IMultipartBase) multipartBlock).onPartPlacedBy(info, player, stack, facing, hitX, hitY, hitZ);
					} else {
						multipartBlock.onPartPlacedBy(info, player, stack);
					}
				}
			}
			return true;
		}
		return false;
	}
}
