package pl.asie.charset.misc.scaffold.modcompat.mcmultipart;

import pl.asie.charset.lib.modcompat.mcmultipart.CharsetMCMPAddon;
import pl.asie.charset.lib.modcompat.mcmultipart.MCMPAddonBase;
import pl.asie.charset.misc.scaffold.CharsetMiscScaffold;
import pl.asie.charset.misc.scaffold.TileScaffold;

@CharsetMCMPAddon("misc.scaffold")
public class MCMPAddonScaffold extends MCMPAddonBase {
    public MCMPAddonScaffold() {
        super(CharsetMiscScaffold.scaffoldBlock, CharsetMiscScaffold.scaffoldItem,
                MultipartScaffold::new, (tile) -> tile instanceof TileScaffold);
    }
}