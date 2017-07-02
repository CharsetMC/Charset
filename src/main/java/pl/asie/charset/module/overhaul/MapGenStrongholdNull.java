package pl.asie.charset.module.overhaul;

import net.minecraft.world.gen.structure.MapGenStronghold;

public class MapGenStrongholdNull extends MapGenStronghold {
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
        return false;
    }
}
