package pl.asie.charset.lib.capability.impl;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityMobSpawnerRenderer;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.api.carry.ICarryHandler;
import pl.asie.charset.api.carry.CustomCarryHandler;

public class CustomCarryHandlerMobSpawner extends CustomCarryHandler {
    protected MobSpawnerBaseLogic spawnerLogic;

    public CustomCarryHandlerMobSpawner(ICarryHandler handler) {
        super(handler);

        spawnerLogic = new MobSpawnerBaseLogic() {
            @Override
            public void broadcastEvent(int id) {

            }

            @Override
            public World getSpawnerWorld() {
                return owner.getCarrier().getEntityWorld();
            }

            @Override
            public BlockPos getSpawnerPosition() {
                return owner.getCarrier().getPosition();
            }
        };
        spawnerLogic.readFromNBT(owner.getTileNBT());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean renderTileCustom(float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5F, 0, 0.5F);
        TileEntityMobSpawnerRenderer.renderMob(spawnerLogic, 0, 0, 0, partialTicks);
        GlStateManager.popMatrix();
        return true;
    }

    @Override
    public void tick() {
        if (spawnerLogic != null) {
            spawnerLogic.updateSpawner();
        }
    }
}
