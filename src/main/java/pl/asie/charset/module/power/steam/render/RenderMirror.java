package pl.asie.charset.module.power.steam.render;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.ITransformation;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.render.model.ModelTransformer;
import pl.asie.charset.lib.render.model.SimpleBakedModel;
import pl.asie.charset.lib.utils.RenderUtils;
import pl.asie.charset.module.power.steam.BlockMirror;
import pl.asie.charset.module.power.steam.CharsetPowerSteam;

import javax.vecmath.Vector3f;

public class RenderMirror {
	private IModel modelBase, modelFace;

	@SubscribeEvent
	public void onBlockColor(ColorHandlerEvent.Block event) {
		event.getBlockColors().registerBlockColorHandler(MirrorColorHandler.INSTANCE, CharsetPowerSteam.blockMirror);
	}

	@SubscribeEvent
	public void onItemColor(ColorHandlerEvent.Item event) {
		event.getItemColors().registerItemColorHandler(MirrorColorHandler.INSTANCE, CharsetPowerSteam.itemMirror);
	}

	@SubscribeEvent
	public void onTextureStitchPre(TextureStitchEvent.Pre event) {
		modelBase = RenderUtils.getModelWithTextures(new ResourceLocation("charset:block/mirror_base"), event.getMap());
		modelFace = RenderUtils.getModelWithTextures(new ResourceLocation("charset:block/mirror_face"), event.getMap());
	}

	@SubscribeEvent
	public void onModelBake(ModelBakeEvent event) {
		for (int i = 0; i <= BlockMirror.ROTATIONS; i++) {
			ITransformation faceState = new TRSRTransformation(
					new Vector3f(0.5f, 8f / 16f, 0.5f),
					i < BlockMirror.ROTATIONS ? TRSRTransformation.quatFromXYZDegrees(new Vector3f(0, (float) i * 360.0F / BlockMirror.ROTATIONS, 0)) : null,
					null,
					i < BlockMirror.ROTATIONS ? TRSRTransformation.quatFromXYZDegrees(new Vector3f(-45, 0, 0)) : null
			);

			IBakedModel base = modelBase.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
			IBakedModel face = modelFace.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());

			face = ModelTransformer.transform(face, CharsetPowerSteam.blockMirror.getDefaultState(), 0L, ModelTransformer.IVertexTransformer.transform(faceState));

			SimpleBakedModel bakedModel = new SimpleBakedModel(base);
			bakedModel.addModel(base);
			bakedModel.addModel(face);
			bakedModel.addDefaultBlockTransforms();
			event.getModelRegistry().putObject(new ModelResourceLocation("charset:solar_mirror", "rotation=" + i), bakedModel);
		}
/*
		IBakedModel face = modelFace.bake(new TRSRTransformation(
				new Vector3f(7f/16f, 7f/16f, 7f/16f),
				TRSRTransformation.quatFromXYZDegrees(new Vector3f(90, 0, 0)),
				null,
				null
		), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());

		SimpleBakedModel bakedModel = new SimpleBakedModel(face);
		bakedModel.addModel(face);
		bakedModel.addDefaultItemTransforms();
		event.getModelRegistry().putObject(new ModelResourceLocation("charset:solar_mirror", "inventory"), bakedModel); */

		event.getModelRegistry().putObject(new ModelResourceLocation("charset:solar_mirror", "inventory"),
				event.getModelRegistry().getObject(new ModelResourceLocation("charset:solar_mirror", "rotation=0")));
	}
}
