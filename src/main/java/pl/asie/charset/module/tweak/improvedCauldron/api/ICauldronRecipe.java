package pl.asie.charset.module.tweak.improvedCauldron.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

@FunctionalInterface
public interface ICauldronRecipe {
	/**
	 * Apply the recipe.
	 * @param contents The current contents of the cauldron.
	 * @return A non-empty optional if the recipe matches; an empty optional if the recipe does not match.
	 */
	Optional<CauldronContents> apply(World world, BlockPos pos, CauldronContents contents);
}
