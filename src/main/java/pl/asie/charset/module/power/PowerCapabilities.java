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

package pl.asie.charset.module.power;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import pl.asie.charset.lib.capability.DummyCapabilityStorage;
import pl.asie.charset.module.power.api.IPowerProducer;
import pl.asie.charset.module.power.api.IPowerConsumer;
import pl.asie.charset.module.power.mechanical.DefaultPowerProducer;
import pl.asie.charset.module.power.mechanical.DefaultPowerConsumer;

public class PowerCapabilities {
	// mechanical
	@CapabilityInject(IPowerProducer.class)
	public static Capability<IPowerProducer> POWER_PRODUCER;
	@CapabilityInject(IPowerConsumer.class)
	public static Capability<IPowerConsumer> POWER_CONSUMER;

	public static void preInit() {
		CapabilityManager.INSTANCE.register(IPowerProducer.class, DummyCapabilityStorage.get(), DefaultPowerProducer::new);
		CapabilityManager.INSTANCE.register(IPowerConsumer.class, DummyCapabilityStorage.get(), DefaultPowerConsumer::new);
	}
}
