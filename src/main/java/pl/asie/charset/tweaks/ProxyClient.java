package pl.asie.charset.tweaks;

import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderMinecart;
import net.minecraft.entity.Entity;

import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.tweaks.minecart.ModelMinecartWrapped;
import pl.asie.charset.tweaks.shard.ItemShard;
import pl.asie.charset.tweaks.shard.TweakGlassShards;

public class ProxyClient extends ProxyCommon {
	@Override
	public void initShardsTweakClient() {
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new ItemShard.Color(), TweakGlassShards.shardItem);
	}

	@Override
	public void initMinecartTweakClient() {
		Map<Class<? extends Entity>, Render<? extends Entity>> entityRenderMap = Minecraft.getMinecraft().getRenderManager().entityRenderMap;

		for (Render<? extends Entity> e : entityRenderMap.values()) {
			if (e instanceof RenderMinecart) {
				Field f;

				try {
					f = RenderMinecart.class.getDeclaredField("modelMinecart");
				} catch (NoSuchFieldException eee) {
					try {
						f = RenderMinecart.class.getDeclaredField("field_77013_a");
					} catch (NoSuchFieldException ee) {
						f = null;
					}
				}

				if (f != null) {
					try {
						f.setAccessible(true);
						f.set(e, new ModelMinecartWrapped((ModelBase) f.get(e)));
					} catch (IllegalAccessException eee) {
						eee.printStackTrace();
					}
				}
			}
		}
	}
}
