package pl.asie.charset.decoration.scaffold;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IRetexturableModel;
import net.minecraftforge.common.model.TRSRTransformation;
import pl.asie.charset.lib.render.model.ModelFactory;
import pl.asie.charset.lib.render.model.WrappedBakedModel;
import pl.asie.charset.lib.utils.RenderUtils;

public class ModelScaffold extends ModelFactory<ScaffoldCacheInfo> {
	public static IRetexturableModel scaffoldModel;

	public ModelScaffold() {
		super(TileScaffold.PROPERTY, TextureMap.LOCATION_MISSING_TEXTURE);
	}

	@Override
	public IBakedModel bake(ScaffoldCacheInfo info, boolean isItem, BlockRenderLayer layer) {
		IModel retexturedModel = scaffoldModel.retexture(ImmutableMap.of("plank", info.plank.getIconName()));
		return new WrappedBakedModel(retexturedModel.bake(TRSRTransformation.identity(),
				DefaultVertexFormats.BLOCK, RenderUtils.textureGetter), info.plank).addDefaultBlockTransforms();
	}

	@Override
	public ScaffoldCacheInfo fromItemStack(ItemStack stack) {
		return ScaffoldCacheInfo.from(stack);
	}
}
