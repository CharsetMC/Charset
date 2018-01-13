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

package pl.asie.charset.module.tablet.format;

import com.google.common.base.Strings;
import pl.asie.charset.module.tablet.format.api.ICommand;
import pl.asie.charset.module.tablet.format.api.ITypesetter;
import pl.asie.charset.module.tablet.format.api.TabletAPI;
import pl.asie.charset.module.tablet.format.api.TruthError;
import pl.asie.charset.module.tablet.format.words.WordText;

import java.util.Locale;

public abstract class AbstractTypesetter implements ITypesetter {
    public AbstractTypesetter() {
    }

    @Override
    public void write(String text) throws TruthError {
        if (Strings.isNullOrEmpty(text)) return;
        final Tokenizer tokenizer = new Tokenizer(text);
        
        while (tokenizer.nextToken()) {
            final String token = tokenizer.getToken();
            if (token.isEmpty()) continue;
            switch (tokenizer.getType()) {
            default:
                throw new TruthError("Unknown tokentype: " + tokenizer.getToken());
            case WORD:
                write(new WordText(token));
                break;
            case PARAMETER:
                write(token);
                break;
            case COMMAND:
                final String cmdName = token.toLowerCase(Locale.ROOT);
                ICommand cmd = TabletAPI.INSTANCE.getCommand(cmdName);
                if (cmd == null) {
                    throw new TruthError("Unknown command: " + cmdName);
                }
                runCommand(cmd, tokenizer);
                break;
            }
        }
    }

    public abstract void writeErrorMessage(String msg);
    protected abstract void runCommand(ICommand cmd, ITokenizer tokenizer) throws TruthError;
}
