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

package pl.asie.charset.shared;

import com.google.gson.JsonObject;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;
import pl.asie.charset.lib.utils.MethodHandleHelper;

import java.lang.invoke.MethodHandle;
import java.util.function.BooleanSupplier;

public class ConditionSimplelogicGateEnabledFactory implements IConditionFactory {
    private MethodHandle COND_CREATE;
    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json) {
        if (COND_CREATE == null) {
            COND_CREATE = MethodHandleHelper.findConstructor("pl.asie.simplelogic.gates.ConditionGateEnabled", ResourceLocation.class);
        }

        ResourceLocation v = new ResourceLocation(JsonUtils.getString(json, "gate"));
        try {
            return (BooleanSupplier) COND_CREATE.invoke(v);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
