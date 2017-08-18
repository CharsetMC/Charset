package pl.asie.charset.api.carry;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityMobSpawnerRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CustomCarryHandler {
    protected final ICarryHandler owner;

    public CustomCarryHandler(ICarryHandler handler) {
        this.owner = handler;
    }

    @FunctionalInterface
    public interface Provider {
        CustomCarryHandler create(ICarryHandler handler);
    }

    @SideOnly(Side.CLIENT)
    public boolean renderTileCustom(float partialTicks) {
        return false;
    }

    public void onPlace(World world, BlockPos pos) {
    }

    public void tick() {

    }
}
