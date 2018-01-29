package pl.asie.charset.lib.render.model;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.MinecraftForgeClient;

import javax.annotation.Nullable;
import java.util.*;

public class SimpleMultiLayerBakedModel extends SimpleBakedModel {
	private final Table<BlockRenderLayer, EnumFacing, List<BakedQuad>> quads = Tables.newCustomTable(
			new EnumMap<>(BlockRenderLayer.class), () -> new EnumMap<>(EnumFacing.class)
	);
	private final Map<BlockRenderLayer, List<BakedQuad>> quadsUnsided = new EnumMap<>(BlockRenderLayer.class);

	public SimpleMultiLayerBakedModel() {
		super();
	}

	public SimpleMultiLayerBakedModel(IBakedModel parent) {
		super(parent);
	}

	public void addQuad(BlockRenderLayer layer, EnumFacing side, BakedQuad quad) {
		if (side == null) {
			quadsUnsided.computeIfAbsent(layer, (a) -> new ArrayList<>()).add(quad);
		} else {
			List<BakedQuad> list = quads.get(layer, side);
			if (list == null) {
				quads.put(layer, side, (list = new ArrayList<>()));
			}
			list.add(quad);
		}

		addQuad(side, quad);
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
		BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
		if (layer != null) {
			if (side != null) {
				List<BakedQuad> list = quads.get(layer, side);
				return list != null ? list : Collections.emptyList();
			} else {
				return quadsUnsided.getOrDefault(layer, Collections.emptyList());
			}
		} else {
			return super.getQuads(state, side, rand);
		}
	}
}
