package pl.asie.charset.module.crafting.compression;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.render.model.WrappedBakedModel;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class ModelCompressionCrafter extends WrappedBakedModel {
	protected final BakedQuad[][][] quads = new BakedQuad[12][6][4]; // [facing][side][ctm]

	public ModelCompressionCrafter(IBakedModel parent) {
		super(parent);
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
		if (side == null || state == null) {
			return Collections.emptyList();
		} else {
			int facing = state.getValue(Properties.FACING).ordinal();

			int offY = state.getValue(BlockCompressionCrafter.OFFSET_Y);
			if (offY >= 4) {
				facing += 6;
				offY -= 4;
			}

			switch (side.getAxis()) {
				case Y:
				default:
					return ImmutableList.of(quads[facing][side.ordinal()][offY]);
				case X:
					return ImmutableList.of(quads[facing][side.ordinal()][state.getValue(BlockCompressionCrafter.OFFSET_X)]);
				case Z:
					return ImmutableList.of(quads[facing][side.ordinal()][state.getValue(BlockCompressionCrafter.OFFSET_Z)]);
			}
		}
	}
}
