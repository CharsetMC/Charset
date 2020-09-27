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

package pl.asie.charset.lib.modcompat.chiselsandbits;

import mod.chiselsandbits.api.ChiselsAndBitsAddon;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import mod.chiselsandbits.api.IChiselsAndBitsAddon;
import pl.asie.charset.lib.loader.AnnotatedPluginHandler;

@ChiselsAndBitsAddon
public class ChiselsAndBitsPluginCharset extends AnnotatedPluginHandler<IChiselsAndBitsAddon> implements IChiselsAndBitsAddon {
    public ChiselsAndBitsPluginCharset() {
        super(CharsetChiselsAndBitsPlugin.class);
    }

    @Override
    public void onReadyChiselsAndBits(IChiselAndBitsAPI api) {
        for (IChiselsAndBitsAddon addon : getPlugins()) {
            addon.onReadyChiselsAndBits(api);
        }
    }
}
