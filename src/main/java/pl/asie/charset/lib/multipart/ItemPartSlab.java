package pl.asie.charset.lib.multipart;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import mcmultipart.item.ItemMultiPart;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.MultipartHelper;
import mcmultipart.multipart.OcclusionHelper;
import mcmultipart.multipart.PartSlot;
import pl.asie.charset.lib.ModCharsetLib;

public abstract class ItemPartSlab extends ItemMultiPart {
	public ItemPartSlab() {
		super();
		setCreativeTab(ModCharsetLib.CREATIVE_TAB);
	}


	public abstract PartSlab createPartSlab(World world, BlockPos blockPos, EnumFacing facing, Vec3d vec3, ItemStack stack, EntityPlayer player);

	@Override
	public IMultipart createPart(World world, BlockPos pos, EnumFacing facing, Vec3d hit, ItemStack stack, EntityPlayer player) {
		PartSlab slab = createPartSlab(world, pos, facing, hit, stack, player);
		slab.isTop = facing == EnumFacing.UP || (hit.yCoord >= 0.5 && facing != EnumFacing.DOWN);

		IMultipartContainer container = MultipartHelper.getPartContainer(world, pos);
		if (container != null) {
			boolean occupiedDown = false;
			if (container.getPartInSlot(PartSlot.DOWN) != null || !OcclusionHelper.occlusionTest(OcclusionHelper.boxes(PartSlab.BOXES[0]), container.getParts())) {
				slab.isTop = true;
				occupiedDown = true;
			}
			if (slab.isTop && (container.getPartInSlot(PartSlot.UP) != null || !OcclusionHelper.occlusionTest(OcclusionHelper.boxes(PartSlab.BOXES[1]), container.getParts()))) {
				if (occupiedDown) {
					return null;
				} else {
					slab.isTop = false;
				}
			}
		}

		return slab;
	}
}
