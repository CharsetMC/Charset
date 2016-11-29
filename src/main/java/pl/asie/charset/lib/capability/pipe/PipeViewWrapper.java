/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.lib.capability.pipe;

import com.google.common.collect.ImmutableList;
import mcmultipart.capabilities.ICapabilityWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.api.pipes.IPipeView;
import pl.asie.charset.lib.capability.Capabilities;

import java.util.Collection;

public class PipeViewWrapper implements ICapabilityWrapper<IPipeView> {
    private class Wrapped implements IPipeView {
        private final Collection<IPipeView> receivers;

        Wrapped(Collection<IPipeView> receivers) {
            this.receivers = receivers;
        }

        @Override
        public Collection<ItemStack> getTravellingStacks() {
            ImmutableList.Builder<ItemStack> builder = new ImmutableList.Builder<>();
            for (IPipeView view : receivers)
                builder.addAll(view.getTravellingStacks());
            return builder.build();
        }
    }

    @Override
    public Capability<IPipeView> getCapability() {
        return Capabilities.PIPE_VIEW;
    }

    @Override
    public IPipeView wrapImplementations(Collection<IPipeView> implementations) {
        return new Wrapped(implementations);
    }
}
