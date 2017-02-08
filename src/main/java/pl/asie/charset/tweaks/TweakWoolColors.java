package pl.asie.charset.tweaks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.render.model.WrappedBakedModel;
import pl.asie.charset.lib.utils.ColorUtils;
import pl.asie.charset.lib.utils.RenderUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TweakWoolColors extends Tweak {
	@SideOnly(Side.CLIENT)
	public static class WoolColor implements IBlockColor, IItemColor {
		public static final WoolColor INSTANCE = new WoolColor();

		private int colorMultiplier(EnumDyeColor color) {
			float[] dOrig = EntitySheep.getDyeRgb(color);
			float[] d = Arrays.copyOf(dOrig, 3);

			if (color == EnumDyeColor.BLUE) {
				d[0] *= 0.925F;
				d[1] *= 0.925F;
				d[2] *= 0.875F;
			} else if (color == EnumDyeColor.ORANGE) {
				d[0] *= 1.075F;
				d[1] *= 1.075F;
			} else if (color == EnumDyeColor.YELLOW) {
				d[0] *= 1.10F;
				d[2] *= 0.95F;
			} else if (color == EnumDyeColor.MAGENTA) {
				d[0] *= 1.1F;
				d[1] *= 1.05F;
				d[2] *= 1.1F;
			} else if (color == EnumDyeColor.LIGHT_BLUE) {
				d[0] *= 1.05F;
				d[1] *= 1.05F;
				d[2] *= 1.05F;
			} else if (color == EnumDyeColor.PINK) {
				d[0] *= 1.025F;
				d[1] *= 1.075F;
				d[2] *= 1.025F;
			} else if (color == EnumDyeColor.CYAN) {
				d[0] *= 0.9F;
				d[1] *= 0.95F;
				d[2] *= 1.05F;
			} else if (color == EnumDyeColor.PURPLE) {
				d[0] *= 1F;
				d[1] *= 1.075F;
				d[2] *= 1F;
			} else if (color == EnumDyeColor.BROWN) {
				d[0] *= 1.0F;
				d[1] *= 0.925F;
				d[2] *= 1.0F;
			} else if (color == EnumDyeColor.BLACK) {
				d[0] *= 1.33F;
				d[1] *= 1.33F;
				d[2] *= 1.33F;
			} else if (color == EnumDyeColor.GRAY) {
				d[0] *= 1.125F;
				d[1] *= 1.125F;
				d[2] *= 1.125F;
			}

			int c = (Math.min(Math.round(d[0] * 255.0F), 255) << 16)
					| (Math.min(Math.round(d[1] * 255.0F), 255) << 8)
					| (Math.min(Math.round(d[2] * 255.0F), 255));
			return c;
		}

		@Override
		public int colorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
			return colorMultiplier(EnumDyeColor.byMetadata(state.getBlock().getMetaFromState(state)));
		}

		@Override
		public int getColorFromItemstack(ItemStack stack, int tintIndex) {
			return colorMultiplier(EnumDyeColor.byMetadata(stack.getItemDamage()));
		}
	}

	public TweakWoolColors() {
		super("client", "tweakedWoolColors", "Makes wool a bit prettier.", true);

	}

	@Override
	public void enable() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public boolean canTogglePostLoad() {
		return false;
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onModelBake(ModelBakeEvent event) {
		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(WoolColor.INSTANCE, Blocks.WOOL);
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(WoolColor.INSTANCE, Blocks.WOOL);

		IModel model = RenderUtils.getModel(new ResourceLocation("charsettweaks:block/blank_wool"));
		IBakedModel bakedModel = model.bake(TRSRTransformation.identity(), DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter());
		bakedModel = new WrappedBakedModel(bakedModel, bakedModel.getParticleTexture()).addDefaultBlockTransforms();

		IRegistry<ModelResourceLocation, IBakedModel> registry = event.getModelRegistry();
		Set<String> woolNames = new HashSet<String>();
		for (String s : ColorUtils.UNDERSCORE_DYE_SUFFIXES) {
			woolNames.add("minecraft:" + s + "_wool");
		}

		for (ModelResourceLocation location : registry.getKeys()) {
			String name = location.getResourceDomain() + ":" + location.getResourcePath();
			if (woolNames.contains(name)) {
				registry.putObject(location, bakedModel);
			}
		}
	}
}
