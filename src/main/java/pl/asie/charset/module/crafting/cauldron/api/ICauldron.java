package pl.asie.charset.module.crafting.cauldron.api;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;

public interface ICauldron {
	World getCauldronWorld();
	BlockPos getCauldronPos();
	Collection<EntityItem> getCauldronItemEntities(boolean immersed);
}
