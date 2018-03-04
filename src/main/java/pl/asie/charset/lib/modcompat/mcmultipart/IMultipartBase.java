package pl.asie.charset.lib.modcompat.mcmultipart;

import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipart;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import pl.asie.charset.lib.block.BlockBase;

public interface IMultipartBase extends IMultipart {
	default void onPartPlacedBy(IPartInfo part, EntityLivingBase placer, ItemStack stack, EnumFacing face, float hitX, float hitY, float hitZ) {
		Block b = part.getState().getBlock();
		if (b instanceof BlockBase) {
			((BlockBase) b).onBlockPlacedBy(part.getPartWorld(), part.getPartPos(), part.getState(), placer, stack, face, hitX, hitY, hitZ);
		} else {
			b.onBlockPlacedBy(part.getPartWorld(), part.getPartPos(), part.getState(), placer, stack);
		}
	}
}
