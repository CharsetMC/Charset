package pl.asie.charset.module.tablet.format.routers;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import pl.asie.charset.module.tablet.format.api.IRouter;

import javax.annotation.Nullable;
import java.net.URI;

public class RouterModDocumentation implements IRouter {
	private final String modid;

	public RouterModDocumentation(String modid) {
		this.modid = modid;
	}

	@Nullable
	@Override
	public String get(URI path) {
		ResourceLocation loc = null;
		if ("item".equals(path.getScheme())) {
			loc = new ResourceLocation(modid, "doc/item" + path.getPath() + ".txt");
		} else if ("mod".equals(path.getScheme())) {
			loc = new ResourceLocation(modid, "doc" + path.getPath() + ".txt");
		}

		if (loc != null) {
			try {
				byte[] data = ByteStreams.toByteArray(Minecraft.getMinecraft().getResourceManager().getResource(loc).getInputStream());
				return new String(data, Charsets.UTF_8);
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	@Override
	public boolean matches(URI path) {
		if ("item".equals(path.getScheme()) || "mod".equals(path.getScheme())) {
			return modid.equals(path.getHost());
		}

		return false;
	}
}
