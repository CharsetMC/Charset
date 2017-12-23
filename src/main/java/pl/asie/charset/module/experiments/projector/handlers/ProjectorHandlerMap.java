package pl.asie.charset.module.experiments.projector.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.MapData;
import pl.asie.charset.lib.utils.MethodHandleHelper;
import pl.asie.charset.module.experiments.projector.IProjectorHandler;
import pl.asie.charset.module.experiments.projector.IProjectorSurface;
import pl.asie.charset.module.experiments.projector.PacketRequestMapData;
import pl.asie.charset.module.experiments.projector.ProjectorHelper;

import java.lang.invoke.MethodHandle;

public class ProjectorHandlerMap implements IProjectorHandler<ItemStack> {
	private static MethodHandle MAP_DATA_LOCATION_GETTER;

	@Override
	public boolean matches(ItemStack target) {
		return target.getItem() == Items.FILLED_MAP;
	}

	@Override
	public float getAspectRatio(ItemStack target) {
		return 1.0f;
	}

	@Override
	public void render(ItemStack stack, IProjectorSurface surface) {
		MapData mapData = ((ItemMap) stack.getItem()).getMapData(stack, surface.getWorld());
		if (mapData != null && mapData.mapName != null) {
			MapItemRenderer mapItemRenderer = Minecraft.getMinecraft().entityRenderer.getMapItemRenderer();
			mapItemRenderer.updateMapTexture(mapData);

			Object o = mapItemRenderer.getMapInstanceIfExists(mapData.mapName);
			if (o != null) {
				if (MAP_DATA_LOCATION_GETTER == null) {
					MAP_DATA_LOCATION_GETTER = MethodHandleHelper.findFieldGetter(o.getClass(), "location", "field_148240_d");
				}

				try {
					Minecraft.getMinecraft().getTextureManager().bindTexture((ResourceLocation) MAP_DATA_LOCATION_GETTER.invoke(o));
					ProjectorHelper.INSTANCE.renderTexture(surface, 0, 256, 0, 256);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		} else {
			PacketRequestMapData.requestMap(stack);
		}
	}
}
