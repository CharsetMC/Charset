package pl.asie.charset.misc.shelf;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IModelUVLock;
import net.minecraftforge.client.model.IRetexturableModel;
import net.minecraftforge.client.model.ModelStateComposition;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import pl.asie.charset.lib.render.model.ModelFactory;
import pl.asie.charset.lib.render.model.WrappedBakedModel;
import pl.asie.charset.lib.utils.RenderUtils;

import javax.vecmath.Vector3f;

public class ModelShelf extends ModelFactory<CacheInfoShelf> {
	public static final ModelShelf INSTANCE = new ModelShelf();
	public static final TRSRTransformation STATE_BACK = new TRSRTransformation(
			new Vector3f(0, 0, 0.5F),
			null,
			null,
			null
	);
	public static IRetexturableModel shelfModel;

	public ModelShelf() {
		super(TileShelf.PROPERTY, TextureMap.LOCATION_MISSING_TEXTURE);
	}

	@Override
	public IBakedModel bake(CacheInfoShelf info, boolean isItem, BlockRenderLayer layer) {
		IModel retexturedModel = ((IModelUVLock) shelfModel.retexture(ImmutableMap.of("plank", info.plank.getIconName()))).uvlock(false);
		IModelState state = ModelRotation.getModelRotation(0, (int) info.facing.getHorizontalAngle());
		if (info.back) {
			state = new ModelStateComposition(state, STATE_BACK);
		}
		return new WrappedBakedModel(retexturedModel.bake(state, DefaultVertexFormats.BLOCK, RenderUtils.textureGetter), info.plank).addDefaultBlockTransforms();
	}

	@Override
	public CacheInfoShelf fromItemStack(ItemStack stack) {
		return CacheInfoShelf.from(stack);
	}
}
