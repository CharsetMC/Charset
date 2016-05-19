package pl.asie.charset.pipes;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ProxyCommon {
	public void registerRenderers() {

	}

	public boolean stopsRenderFast(World world, ItemStack stack) {
		return false;
	}
}
