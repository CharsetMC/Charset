package pl.asie.charset.module.power.steam;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import pl.asie.charset.lib.block.BlockBase;

import javax.annotation.Nullable;

public class BlockWaterBoiler extends BlockBase implements ITileEntityProvider {
    public BlockWaterBoiler() {
        super(Material.IRON);
        setFullCube(true);
        setOpaqueCube(true);
        setUnlocalizedName("charset.water_boiler");
        setHardness(4.0F);
        setResistance(10.0F);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileWaterBoiler();
    }
}
