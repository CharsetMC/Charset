/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.charset.module.tablet.format.routers;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import pl.asie.charset.module.tablet.format.api.IRouter;

import javax.annotation.Nullable;
import java.io.FileNotFoundException;
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
			} catch (FileNotFoundException e) {
				return "\\title{Not found!}\n\nThe documentation you are looking for cannot be found.";
			} catch (Exception e) {
				e.printStackTrace();
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
