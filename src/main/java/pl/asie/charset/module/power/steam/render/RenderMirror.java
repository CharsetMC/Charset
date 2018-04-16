package pl.asie.charset.module.power.steam.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.particle.ParticleBubble;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EnumFaceDirection;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.ITransformation;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import pl.asie.charset.lib.material.ColorLookupHandler;
import pl.asie.charset.lib.render.model.ModelTransformer;
import pl.asie.charset.lib.render.model.SimpleBakedModel;
import pl.asie.charset.lib.utils.*;
import pl.asie.charset.lib.utils.colorspace.Colorspaces;
import pl.asie.charset.module.optics.laser.CharsetLaser;
import pl.asie.charset.module.optics.laser.system.LaserBeam;
import pl.asie.charset.module.power.steam.BlockMirror;
import pl.asie.charset.module.power.steam.CharsetPowerSteam;
import pl.asie.charset.module.power.steam.TileMirror;
import pl.asie.charset.module.power.steam.api.IMirror;

import javax.vecmath.Vector3f;
import java.util.Collection;
import java.util.Optional;

public class RenderMirror {
	private IModel modelBase, modelFace;
	private TextureAtlasSprite beamSprite;

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
		event.getMap().registerSprite(new ResourceLocation("charset:blocks/steam"));

		modelBase = RenderUtils.getModelWithTextures(new ResourceLocation("charset:block/mirror_base"), event.getMap());
		modelFace = RenderUtils.getModelWithTextures(new ResourceLocation("charset:block/mirror_face"), event.getMap());
		beamSprite = event.getMap().registerSprite(new ResourceLocation("charset:blocks/mirror/beam"));
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

	private static final Quaternion mirrorTilt = Quaternion.getRotationQuaternionRadians(Math.toRadians(-45), 1, 0, 0);

	@SubscribeEvent
	public void onRender(RenderWorldLastEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		World world = mc.world;
		Minecraft.getMinecraft().mcProfiler.startSection("sunbeams");

		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.glBlendEquation(GL14.GL_FUNC_ADD);
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
		GlStateManager.disableLighting();

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldrenderer = tessellator.getBuffer();

		Entity cameraEntity = Minecraft.getMinecraft().getRenderViewEntity();
		Vec3d cameraPos = EntityUtils.interpolate(cameraEntity, event.getPartialTicks());

		ICamera camera = new Frustum();
		camera.setPosition(cameraPos.x, cameraPos.y, cameraPos.z);

		worldrenderer.setTranslation(-cameraPos.x, -cameraPos.y, -cameraPos.z);
		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

		for (TileEntity tile : world.loadedTileEntityList) {
			if (!(tile instanceof IMirror)) {
				continue;
			}

			IMirror mirror = (IMirror) tile;
			Optional<BlockPos> mirrorTarget = mirror.getMirrorTargetPos();

			if (!mirror.isMirrorActive() || !mirrorTarget.isPresent()) {
				continue;
			}

			Vec3d src = new Vec3d(mirror.getMirrorPos()).addVector(0.5, 0.5, 0.5);
			Vec3d dest = new Vec3d(mirrorTarget.get()).addVector(0.5, 0.5, 0.5);

			if (!camera.isBoundingBoxInFrustum(new AxisAlignedBB(src, dest))) {
				continue;
			}

			double mirrorRotation = MathHelper.atan2(
					mirrorTarget.get().getX() - mirror.getMirrorPos().getX(),
					mirrorTarget.get().getZ() - mirror.getMirrorPos().getZ()
			);

			float[] poses = new float[] { -6/16f, 6/16f };
			Quaternion trans = Quaternion.getRotationQuaternionRadians(mirrorRotation + Math.PI, EnumFacing.UP);
			trans = trans.multiply(mirrorTilt);
			Vec3d[] points = new Vec3d[4];
			int i = 0;
			for (int sx = 0; sx < 2; sx++) {
				for (int sz = 0; sz < 2; sz++) {
					Vec3d vec = new Vec3d(poses[sx], 0, poses[sz]);
					points[i++] = trans.applyRotation(vec).add(src);
				}
			}

			float N = 1.125f;
			float invN = 1 / N;

			Vec3d dif = dest.subtract(src).scale(N);
			Vec3d far = src.add(dif);

			byte[] as = new byte[] { 2, 1, 0, 3 };
			byte[] bs = new byte[] { 0, 3, 1, 2 };
			// default opacity: 38 / 0xFF
			float min_opacity = 24F / 255F;
			float opacity_per_power = 8F / 255F;
			float alpha = min_opacity + mirror.getMirrorStrength() * opacity_per_power;

			Vec3d point;

			/* int c = MirrorColorHandler.INSTANCE.colorMultiplier(CharsetPowerSteam.blockMirror.getDefaultState(), Minecraft.getMinecraft().world, mirror.getMirrorPos(), 0);
			float cRed = ((c >> 16) & 0xFF) / 255f;
			float cGreen = ((c >> 8) & 0xFF) / 255f;
			float cBlue = ((c) & 0xFF) / 255f; */

			float cRed = 1.0f;
			float cGreen = 1.0f;
			float cBlue = 1.0f;

			for (i = 0; i < 4; i++)  {
				Vec3d a = points[as[i]];
				Vec3d b = points[bs[i]];

				point = MathUtils.interpolate(b, far, invN);
				worldrenderer.pos(point.x, point.y, point.z).tex(beamSprite.getMinU(), beamSprite.getMinV()).color(cRed, cGreen, cBlue,  alpha).endVertex();
				point = MathUtils.interpolate(a, far, invN);
				worldrenderer.pos(point.x, point.y, point.z).tex(beamSprite.getMaxU(), beamSprite.getMinV()).color(cRed, cGreen, cBlue,  alpha).endVertex();
				point = a;
				worldrenderer.pos(point.x, point.y, point.z).tex(beamSprite.getMinU(), beamSprite.getMaxV()).color(cRed, cGreen, cBlue,  alpha).endVertex();
				point = b;
				worldrenderer.pos(point.x, point.y, point.z).tex(beamSprite.getMaxU(), beamSprite.getMaxV()).color(cRed, cGreen, cBlue,  alpha).endVertex();
			}
		}

		if (Minecraft.getMinecraft().gameSettings.fancyGraphics) {
			tessellator.getBuffer().sortVertexData((float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z);
		}
		tessellator.draw();

		worldrenderer.setTranslation(0,0,0);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.glBlendEquation(GL14.GL_FUNC_ADD);
		GlStateManager.disableBlend();

		Minecraft.getMinecraft().mcProfiler.endSection();
	}
}
