package pl.asie.charset.module.misc.scaffold.modcompat.mcmultipart;

import pl.asie.charset.lib.modcompat.mcmultipart.CharsetMCMPAddon;
import pl.asie.charset.lib.modcompat.mcmultipart.MCMPAddonBase;
import pl.asie.charset.module.misc.scaffold.CharsetMiscScaffold;
import pl.asie.charset.module.misc.scaffold.TileScaffold;

@CharsetMCMPAddon("misc.scaffold")
public class MCMPAddonScaffold extends MCMPAddonBase {
    public MCMPAddonScaffold() {
        super(CharsetMiscScaffold.scaffoldBlock, CharsetMiscScaffold.scaffoldItem,
                MultipartScaffold::new, (tile) -> tile instanceof TileScaffold);
    }
}