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

public interface IMinecartDyeable {
	class Impl implements IMinecartDyeable {
		private int color = -1;

		@Override
		public int getColor() {
			return color;
		}

		@Override
		public void setColor(int color) {
			if (color >= 0 && color < 16777216) {
				this.color = color;
			} else {
				this.color = -1;
			}
		}
	}

	static IMinecartDyeable get(EntityMinecart entity) {
		return entity.getCapability(TweakDyeableMinecarts.MINECART_DYEABLE, null);
	}

	int getColor();
	void setColor(int color);
}
