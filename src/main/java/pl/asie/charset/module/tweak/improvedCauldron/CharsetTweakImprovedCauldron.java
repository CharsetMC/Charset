package pl.asie.charset.module.tweak.improvedCauldron;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.module.tweak.improvedCauldron.api.CauldronContents;
import pl.asie.charset.module.tweak.improvedCauldron.api.ICauldronRecipe;
import pl.asie.charset.module.tweak.improvedCauldron.fluid.FluidDyedWater;
import pl.asie.charset.module.tweak.improvedCauldron.fluid.FluidTextureGenerator;
import pl.asie.charset.module.tweak.improvedCauldron.recipe.RecipeBucketCraft;
import pl.asie.charset.module.tweak.improvedCauldron.recipe.RecipeDyeItem;
import pl.asie.charset.module.tweak.improvedCauldron.recipe.RecipeDyeWater;
import pl.asie.charset.module.tweak.improvedCauldron.recipe.RecipeWashDyedWater;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@CharsetModule(
		name = "tweak.improvedCauldron",
		description = "Improved Cauldron!",
		profile = ModuleProfile.STABLE
)
public class CharsetTweakImprovedCauldron {
	public static int waterAlpha = 180;
	public static BlockCauldronCharset blockCauldron;
	public static FluidDyedWater dyedWater;
	private static List<ICauldronRecipe> recipeList = new ArrayList<>();

	public static Optional<CauldronContents> craft(TileEntity cauldronCharset, CauldronContents contents) {
		for (ICauldronRecipe recipe : recipeList) {
			Optional<CauldronContents> contentsNew = recipe.apply(cauldronCharset.getWorld(), cauldronCharset.getPos(), contents);
			if (contentsNew.isPresent()) {
				return contentsNew;
			}
		}

		return Optional.empty();
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		blockCauldron = new BlockCauldronCharset();
		FluidRegistry.registerFluid(dyedWater = new FluidDyedWater("dyed_water"));
		FluidRegistry.addBucketForFluid(dyedWater);
		dyedWater.setUnlocalizedName("charset.dyed_water");
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void preInitClient(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new FluidTextureGenerator());
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		recipeList.add(new RecipeDyeWater());
		recipeList.add(new RecipeDyeItem());
		recipeList.add(new RecipeWashDyedWater());
		recipeList.add(new RecipeBucketCraft());

		RegistryUtils.register(TileCauldronCharset.class, "improved_cauldron");
		FMLInterModComms.sendMessage("charset", "addLock", "minecraft:cauldron");
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void initClient(FMLInitializationEvent event) {
		ClientRegistry.bindTileEntitySpecialRenderer(TileCauldronCharset.class, new TileRendererCauldronCharset());
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onModelBake(ModelBakeEvent event) {
		IBakedModel l0 = event.getModelRegistry().getObject(new ModelResourceLocation("minecraft:cauldron#level=0"));

		if (l0 != null) {
			event.getModelRegistry().putObject(new ModelResourceLocation("minecraft:cauldron#level=1"), l0);
			event.getModelRegistry().putObject(new ModelResourceLocation("minecraft:cauldron#level=2"), l0);
			event.getModelRegistry().putObject(new ModelResourceLocation("minecraft:cauldron#level=3"), l0);
		}
	}

	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> event) {
		event.getRegistry().register(blockCauldron.setRegistryName("minecraft:cauldron"));
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if (!event.getWorld().isRemote) {
			event.getWorld().addEventListener(CauldronLevelUpdateListener.INSTANCE);
		}
	}
}
