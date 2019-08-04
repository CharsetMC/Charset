package pl.asie.charset.module.tweak.carry.compat.railcraft;

import net.minecraft.block.BlockJukebox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraftforge.fml.common.Loader;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.charset.module.tweak.carry.CarryTransformerEntityMinecart;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CarryTransformerEntityMinecartRailcraft extends CarryTransformerEntityMinecart {
    private static class ClassNames {
        public static final String RAILCRAFT_CHEST = "mods.railcraft.common.carts.EntityCartChest";
        public static final String RAILCRAFT_CHEST_METALS = "mods.railcraft.common.carts.EntityCartChestMetals";
        public static final String RAILCRAFT_JUKEBOX = "mods.railcraft.common.carts.EntityCartJukebox";
    }

    @Override
    protected Pair<IBlockState, TileEntity> getExtractedPair(@Nonnull Entity object, boolean simulate) {
        String className = object.getClass().getName();
        if (ClassNames.RAILCRAFT_CHEST.equals(className)) {
            TileEntityChest tile = new TileEntityChest();
            copyEntityToTile(tile, object, "Items");
            return Pair.of(Blocks.CHEST.getDefaultState(), tile);
        } else if (ClassNames.RAILCRAFT_JUKEBOX.equals(className)) {
            BlockJukebox.TileEntityJukebox tile = new BlockJukebox.TileEntityJukebox();
            copyEntityToTile(tile, object, "RecordItem", "Record");
            return Pair.of(Blocks.JUKEBOX.getDefaultState(), tile);
        } else {
            return null;
        }
    }

    @Override
    public boolean insert(@Nonnull Entity object, @Nonnull IBlockState state, @Nullable TileEntity tile, boolean simulate) {
        if (state.getBlock() == Blocks.CHEST) {
            if (tile != null && transform(object, ClassNames.RAILCRAFT_CHEST,
                    filter(tile.writeToNBT(new NBTTagCompound()), "Items"),
                    simulate) != null) {
                return true;
            }
        } else if (state.getBlock() == Blocks.JUKEBOX) {
            if (tile != null && transform(object, ClassNames.RAILCRAFT_JUKEBOX,
                    filter(tile.writeToNBT(new NBTTagCompound()), "Items"),
                    simulate) != null) {
                return true;
            }
        }
        return false;
    }
}
