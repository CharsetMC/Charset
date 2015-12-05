package pl.asie.charset.tweaks.minecart;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import net.minecraftforge.common.IExtendedEntityProperties;

public class MinecartProperties implements IExtendedEntityProperties {
	public static final String NAME = "charset_cart";

	private int color = -1;

	public static MinecartProperties get(EntityMinecart entity) {
		IExtendedEntityProperties p = entity.getExtendedProperties(MinecartProperties.NAME);
		return p instanceof MinecartProperties ? (MinecartProperties) p : null;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	@Override
	public void saveNBTData(NBTTagCompound compound) {
		if (color >= 0) {
			compound.setInteger("color", color);
		}
	}

	@Override
	public void loadNBTData(NBTTagCompound compound) {
		color = compound.getInteger("color");
	}

	@Override
	public void init(Entity entity, World world) {
	}
}
