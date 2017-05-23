package pl.asie.charset.lib.modcompat.mcmultipart;

import mcmultipart.api.addon.IMCMPAddon;
import mcmultipart.api.addon.MCMPAddon;
import mcmultipart.api.multipart.IMultipartRegistry;
import pl.asie.charset.lib.loader.AnnotatedPluginHandler;
import pl.asie.charset.lib.utils.OcclusionUtils;

@MCMPAddon
public class MCMPAddonCharset extends AnnotatedPluginHandler<IMCMPAddon> implements IMCMPAddon {
    public MCMPAddonCharset() {
        super(CharsetMCMPAddon.class);
    }

    @Override
    public void registerParts(IMultipartRegistry registry) {
        OcclusionUtils.INSTANCE = new OcclusionUtilsMultipart();

        for (IMCMPAddon addon : getPlugins()) {
            addon.registerParts(registry);
        }
    }
}