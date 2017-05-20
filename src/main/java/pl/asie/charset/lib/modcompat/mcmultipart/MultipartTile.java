package pl.asie.charset.lib.modcompat.mcmultipart;

import mcmultipart.api.multipart.IMultipartTile;
import net.minecraft.tileentity.TileEntity;

public class MultipartTile<T extends TileEntity> implements IMultipartTile {
    protected final T owner;

    public MultipartTile(T owner) {
        this.owner = owner;
    }

    @Override
    public TileEntity getTileEntity() {
        return owner;
    }
}