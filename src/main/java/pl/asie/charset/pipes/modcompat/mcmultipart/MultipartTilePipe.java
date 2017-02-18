package pl.asie.charset.pipes.modcompat.mcmultipart;

import mcmultipart.api.multipart.IMultipartTile;
import net.minecraft.tileentity.TileEntity;
import pl.asie.charset.lib.modcompat.mcmultipart.MultipartTile;
import pl.asie.charset.pipes.pipe.TilePipe;

public class MultipartTilePipe extends MultipartTile<TilePipe> {
    public MultipartTilePipe(TilePipe owner) {
        super(owner);
    }
}
