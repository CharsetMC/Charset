package pl.asie.charset.tweaks.minecart;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

// TODO
public class MinecartProperties /* implements IExtendedEntityProperties */ {
	public static final String NAME = "charset_cart";

	private int color = -1;

	public static MinecartProperties get(EntityMinecart entity) {
		return null;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public void saveNBTData(NBTTagCompound compound) {
		if (color >= 0) {
			compound.setInteger("color", color);
		}
	}

	public void loadNBTData(NBTTagCompound compound) {
		if (compound.hasKey("color")) {
			color = compound.getInteger("color");
		} else {
			color = -1;
		}
	}

	public void init(Entity entity, World world) {
	}
}
