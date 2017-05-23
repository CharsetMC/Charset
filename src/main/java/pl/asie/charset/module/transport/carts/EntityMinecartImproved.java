package pl.asie.charset.module.transport.carts;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;

public class EntityMinecartImproved extends EntityMinecart {
    public EntityMinecartImproved(World worldIn) {
        super(worldIn);
    }

    public EntityMinecartImproved(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    @Override
    public Type getType() {
        return Type.CHEST;
    }
}
