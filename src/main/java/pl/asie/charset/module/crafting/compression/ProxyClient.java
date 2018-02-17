package pl.asie.charset.module.crafting.compression;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelStateComposition;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.utils.RenderUtils;

import javax.vecmath.Vector3f;
import java.util.List;

public class ProxyClient extends ProxyCommon {
	private static final ModelResourceLocation bmLoc = new ModelResourceLocation("charset:compression_crafter", "normal");
	private static final ModelRotation[] ROTATIONS = new ModelRotation[] {
			ModelRotation.X180_Y0,
			ModelRotation.X0_Y0,
			ModelRotation.X90_Y0,
			ModelRotation.X90_Y180,
			ModelRotation.X90_Y270,
			ModelRotation.X90_Y90,
			ModelRotation.X180_Y90,
			ModelRotation.X0_Y90
	};

	@SubscribeEvent
	public void onModelRegistry(ModelRegistryEvent event) {
		ModelLoader.setCustomStateMapper(CharsetCraftingCompression.blockCompressionCrafter, blockIn -> {
			ImmutableMap.Builder<IBlockState, ModelResourceLocation> builder = new ImmutableMap.Builder<>();
			for (IBlockState state : blockIn.getBlockState().getValidStates()) {
				builder.put(state, bmLoc);
			}
			return builder.build();
		});
	}

	@SubscribeEvent
	public void onTextureStitchPre(TextureStitchEvent.Pre event) {
		CTMTextureFactory.register(event.getMap(), new ResourceLocation("charset:blocks/compact/compact_bottom"));
		CTMTextureFactory.register(event.getMap(), new ResourceLocation("charset:blocks/compact/compact_top"));
		CTMTextureFactory.register(event.getMap(), new ResourceLocation("charset:blocks/compact/compact_side"));
	}

	@SubscribeEvent
	public void onModelBake(ModelBakeEvent event) {
		IBakedModel origModel = event.getModelRegistry().getObject(bmLoc);
		ModelCompressionCrafter result = new ModelCompressionCrafter(origModel);

		event.getModelRegistry().putObject(bmLoc, result);
		IModel model = RenderUtils.getModel(new ResourceLocation("charset:block/compression_crafter"));
		IBlockState defState = CharsetCraftingCompression.blockCompressionCrafter.getDefaultState();

		for (int i = 0; i < 4; i++) {
			IModel retexModel = model.retexture(
					ImmutableMap.of(
							"top", "charset:blocks/compact/compact_top#" + i,
							"bottom", "charset:blocks/compact/compact_bottom#" + i,
							"side_x", "charset:blocks/compact/compact_side#" + i,
							"side_z", "charset:blocks/compact/compact_side#" + i
					)
			);

			for (int k = 0; k < 12; k++) {
				EnumFacing facing = EnumFacing.getFront(k % 6);
				IModelState modelState = ROTATIONS[k >= 8 ? (k - 6) : k];
				if (k >= 8) {
					modelState = new ModelStateComposition(
							modelState,
							ModelRotation.X0_Y90
					);
				}

				IBakedModel bakedModel = retexModel.bake(
						modelState,
						DefaultVertexFormats.ITEM,
						ModelLoader.defaultTextureGetter()
				);

				for (EnumFacing side : EnumFacing.VALUES) {
					List<BakedQuad> list = bakedModel.getQuads(defState, side, 0);
					int j = i;
					if ((facing == EnumFacing.DOWN && (side == EnumFacing.SOUTH || side == EnumFacing.WEST))
							|| (facing == EnumFacing.UP && (side == EnumFacing.NORTH || side == EnumFacing.EAST))
							|| ((facing == EnumFacing.SOUTH || facing == EnumFacing.WEST) &&
							(side.getAxis() == EnumFacing.Axis.Y || (side.getAxis() == facing.getAxis() && k < 8)))
							|| (facing.getAxis() != EnumFacing.Axis.Y && k >= 8 && side == facing.rotateY())) {
						if (j == 1) j = 2;
						else if (j == 2) j = 1;
					}

					result.quads[k][side.ordinal()][j] = list.get(0);
				}
			}
		}
	}
}
