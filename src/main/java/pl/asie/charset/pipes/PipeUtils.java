package pl.asie.charset.pipes;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import mcmultipart.microblock.IMicroblock;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.MultipartHelper;
import mcmultipart.multipart.PartSlot;

public final class PipeUtils {
	private PipeUtils() {

	}

	public static PartPipe getPipe(World world, BlockPos blockPos, EnumFacing side) {
		IMultipartContainer container = MultipartHelper.getPartContainer(world, blockPos);
		if (container == null) {
			return null;
		}

		if (side != null) {
			ISlottedPart part = container.getPartInSlot(PartSlot.getFaceSlot(side));
			if (part instanceof IMicroblock.IFaceMicroblock && !((IMicroblock.IFaceMicroblock) part).isFaceHollow()) {
				return null;
			}
		}

		ISlottedPart part = container.getPartInSlot(PartSlot.CENTER);
		if (part instanceof PartPipe) {
			return (PartPipe) part;
		} else {
			return null;
		}
	}
}
