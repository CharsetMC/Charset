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

package pl.asie.charset.lib.modcompat.mcmultipart;

import mcmultipart.api.addon.IMCMPAddon;
import mcmultipart.api.addon.MCMPAddon;
import mcmultipart.api.multipart.IMultipartRegistry;
import pl.asie.charset.lib.loader.AnnotatedPluginHandler;
import pl.asie.charset.lib.utils.OcclusionUtils;

@MCMPAddon
public class MCMPAddonCharset extends AnnotatedPluginHandler<IMCMPAddon> implements IMCMPAddon {
    public MCMPAddonCharset() {
        super(CharsetMCMPAddon.class);
    }

    @Override
    public void registerParts(IMultipartRegistry registry) {
        OcclusionUtils.INSTANCE = new OcclusionUtilsMultipart();

        for (IMCMPAddon addon : getPlugins()) {
            addon.registerParts(registry);
        }
    }
}