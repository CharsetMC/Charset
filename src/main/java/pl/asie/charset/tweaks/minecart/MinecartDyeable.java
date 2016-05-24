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

package pl.asie.charset.tweaks.minecart;

import net.minecraft.entity.item.EntityMinecart;

public class MinecartDyeable {
	private int color = -1;

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		if (color >= 0 && color <= 0xFFFFFF) {
			this.color = color;
		} else {
			this.color = -1;
		}
	}

	public static MinecartDyeable get(EntityMinecart entity) {
		return entity.getCapability(TweakDyeableMinecarts.MINECART_DYEABLE, null);
	}
}
