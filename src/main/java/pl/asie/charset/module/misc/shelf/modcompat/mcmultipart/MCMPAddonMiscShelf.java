package pl.asie.charset.module.misc.shelf.modcompat.mcmultipart;

import pl.asie.charset.lib.modcompat.mcmultipart.CharsetMCMPAddon;
import pl.asie.charset.lib.modcompat.mcmultipart.MCMPAddonBase;
import pl.asie.charset.module.misc.shelf.CharsetMiscShelf;
import pl.asie.charset.module.misc.shelf.TileShelf;

@CharsetMCMPAddon("misc.shelf")
public class MCMPAddonMiscShelf extends MCMPAddonBase {
    public MCMPAddonMiscShelf() {
        super(CharsetMiscShelf.shelfBlock, CharsetMiscShelf.shelfItem,
                MultipartShelf::new, (tile) -> tile instanceof TileShelf);
    }
}
