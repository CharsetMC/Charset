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
