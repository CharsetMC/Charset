/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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

import pl.asie.charset.module.tablet.format.ITokenizer;
import pl.asie.charset.module.tablet.format.api.ICommand;
import pl.asie.charset.module.tablet.format.api.ITypesetter;
import pl.asie.charset.module.tablet.format.api.TruthError;
import pl.asie.charset.module.tablet.format.words.WordURL;

import java.net.URI;

public class CommandURL implements ICommand {
	@Override
	public void call(ITypesetter typesetter, ITokenizer tokenizer) throws TruthError {
		String uriLink = tokenizer.getParameter("\\url missing parameter: uriLink");
		String content = tokenizer.getParameter("\\url missing parameter: content");
		try {
			typesetter.write(new WordURL(content, new URI(uriLink)));
		} catch (Exception e) {
			e.printStackTrace();
			throw new TruthError(e.getMessage());
		}
	}
}
