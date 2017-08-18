package pl.asie.charset.lib.capability.impl;

import net.minecraft.block.BlockChest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.charset.api.carry.CustomCarryHandler;
import pl.asie.charset.api.carry.ICarryHandler;

public class CustomCarryHandlerCactus extends CustomCarryHandler {
    public CustomCarryHandlerCactus(ICarryHandler handler) {
        super(handler);
    }

    @Override
    public void tick() {
        Entity carrier = owner.getCarrier();
        if (carrier.getEntityWorld().getTotalWorldTime() % 40 == 0) {
            carrier.attackEntityFrom(DamageSource.CACTUS, 1.0F);
        }
    }
}
