package pl.asie.charset.module.tweaks.carry;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.charset.module.tweaks.carry.CarryHandler;
import pl.asie.charset.module.tweaks.carry.CharsetTweakBlockCarrying;
import pl.asie.charset.module.tweaks.carry.ICarryTransformer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CarryTransformerPlayerShare implements ICarryTransformer<Entity> {
    @Nullable
    @Override
    public Pair<IBlockState, TileEntity> extract(@Nonnull Entity object, boolean simulate) {
        return null;
    }

    @Override
    public boolean insert(@Nonnull Entity object, @Nonnull IBlockState state, @Nullable TileEntity tile, boolean simulate) {
        if (object instanceof EntityPlayer && object.hasCapability(CharsetTweakBlockCarrying.CAPABILITY, null)) {
            EntityPlayer player = (EntityPlayer) object;
            CarryHandler handler = object.getCapability(CharsetTweakBlockCarrying.CAPABILITY, null);
            if (handler == null || handler.isCarrying() || !CharsetTweakBlockCarrying.canPlayerConsiderCarryingBlock(player)) {
                return false;
            }

            if (!simulate) {
                handler.put(state, tile);
                CharsetTweakBlockCarrying.syncCarryWithAllClients(player);
            }
            return true;
        }

        return false;
    }
}
