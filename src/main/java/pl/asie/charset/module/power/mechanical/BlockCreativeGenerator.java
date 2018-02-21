package pl.asie.charset.module.power.mechanical;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import pl.asie.charset.lib.block.BlockBase;

import javax.annotation.Nullable;

public class BlockCreativeGenerator extends BlockBase implements ITileEntityProvider {
	public BlockCreativeGenerator() {
		super(Material.ROCK);
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileCreativeGenerator();
	}
}
