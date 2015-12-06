package pl.asie.charset.wires.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;

import net.minecraftforge.client.model.ISmartBlockModel;

/**
 * Created by asie on 12/5/15.
 */
public abstract class RendererWireBase implements ISmartBlockModel {
	protected IBlockState state;

	@Override
	public IBakedModel handleBlockState(IBlockState state) {
		this.state = state;
		return this;
	}

	@Override
	public boolean isAmbientOcclusion() {
		return true;
	}

	@Override
	public boolean isGui3d() {
		return false;
	}

	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return ItemCameraTransforms.DEFAULT;
	}

	public abstract void loadTextures(TextureMap map);
}
