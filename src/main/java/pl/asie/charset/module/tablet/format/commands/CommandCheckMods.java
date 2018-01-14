/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
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

import net.minecraftforge.fml.common.Loader;
import pl.asie.charset.ModCharset;
import pl.asie.charset.module.tablet.format.ITokenizer;
import pl.asie.charset.module.tablet.format.api.ICommand;
import pl.asie.charset.module.tablet.format.api.ITypesetter;
import pl.asie.charset.module.tablet.format.api.TruthError;

public class CommandCheckMods implements ICommand {
	@Override
	public void call(ITypesetter out, ITokenizer tokenizer) throws TruthError {
		String mode = tokenizer.getParameter("\\checkmods mod mode: all|none|any"); // all some none
		String modList = tokenizer.getParameter("\\checkmods list of mods"); //craftguide NotEnoughItems
		String content = tokenizer.getParameter("\\checkmods when mods installed");
		String other = tokenizer.getOptionalParameter();

		int count = 0;
		String[] mods = modList.split(" ");
		for (String modId : mods) {
			if (modId.startsWith("charset:")) {
				if (ModCharset.isModuleLoaded(modId.substring(8))) {
					count++;
				}
			} else if (Loader.isModLoaded(modId)) {
				count++;
			}
		}

		boolean good = false;
		if (mode.equalsIgnoreCase("all")) {
			good = count == mods.length;
		} else if (mode.equalsIgnoreCase("none")) {
			good = count == 0;
		} else if (mode.equalsIgnoreCase("any")) {
			good = count >= 1;
		} else {
			throw new TruthError("\\checkmods first parameter must be 'all', 'none', or 'any', not '" + mode + "'");
		}

		if (good) {
			out.write(content);
		} else if (other != null) {
			out.write(other);
		}
	}
}
