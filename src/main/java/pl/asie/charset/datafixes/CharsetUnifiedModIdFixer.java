package pl.asie.charset.datafixes;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.IFixableData;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.Set;

public class CharsetUnifiedModIdFixer {
    public static class Entity implements IFixableData {
        private final Set<String> prefixes;

        public Entity(Set<String> prefixes) {
            this.prefixes = prefixes;
        }

        @Override
        public int getFixVersion() {
            return 1;
        }

        @Override
        public NBTTagCompound fixTagCompound(NBTTagCompound compound) {
            String id = compound.getString("id");
            if (id.indexOf(':') > 0) {
                ResourceLocation idLoc = new ResourceLocation(id);
                if (prefixes.contains(idLoc.getResourceDomain())) {
                    compound.setString("id", "charset:" + idLoc.getResourcePath());
                }
            }
            return compound;
        }
    }
}
