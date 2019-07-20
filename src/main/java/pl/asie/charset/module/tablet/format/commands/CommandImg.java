/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.module.tablet.format.commands;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import pl.asie.charset.module.tablet.format.ITokenizer;
import pl.asie.charset.module.tablet.format.api.ICommand;
import pl.asie.charset.module.tablet.format.api.ITypesetter;
import pl.asie.charset.module.tablet.format.api.TruthError;
import pl.asie.charset.module.tablet.format.words.WordImage;
import scala.Int;

public class CommandImg implements ICommand {
	@Override
	public void call(ITypesetter out, ITokenizer tokenizer) throws TruthError {
		String imgName = tokenizer.getParameter("domain:path/to/image.png");
		String scaleOrWidth = tokenizer.getOptionalParameter();
		String heightS = tokenizer.getOptionalParameter();

		ResourceLocation rl = new ResourceLocation(imgName);
		try (IResource r = Minecraft.getMinecraft().getResourceManager().getResource(rl)) {
			if (r == null) {
				throw new TruthError("Not found: " + imgName);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			throw new TruthError(e.getMessage());
		}

		WordImage img;
		if (heightS != null) {
			int width = Integer.parseInt(scaleOrWidth);
			int height = Integer.parseInt(heightS);
			img = new WordImage(rl, width, height);
		} else {
			img = new WordImage(rl);
			if (scaleOrWidth != null) {
				img.scale(Double.parseDouble(scaleOrWidth));
			}
		}

		if (out.hasFixedWidth()) {
			img.fitToPage(out.getWidth(), Integer.MAX_VALUE);
		}

		out.write(img);
	}
}
