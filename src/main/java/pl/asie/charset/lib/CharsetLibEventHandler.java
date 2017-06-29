package pl.asie.charset.lib;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.api.lib.EntityGatherItemsEvent;

public class CharsetLibEventHandler {
    @SubscribeEvent
    public void onGatherItemsEvent(EntityGatherItemsEvent event) {
        Entity entity = event.getEntity();
        if (event.collectsHeld() && entity instanceof EntityLivingBase) {
            event.addStack(((EntityLivingBase) entity).getHeldItemMainhand());
            event.addStack(((EntityLivingBase) entity).getHeldItemOffhand());
        }

        if (event.collectsWorn()) {
            for (ItemStack stack : entity.getArmorInventoryList())
                event.addStack(stack);
        }
    }
}
