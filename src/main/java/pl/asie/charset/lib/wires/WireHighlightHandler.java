package pl.asie.charset.lib.wires;

import mcmultipart.api.container.IPartInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.utils.RenderUtils;

@SideOnly(Side.CLIENT)
public class WireHighlightHandler {
    @SubscribeEvent
    public void drawWireHighlight(DrawBlockHighlightEvent event) {
        if (event.getTarget() != null && event.getTarget().typeOfHit == RayTraceResult.Type.BLOCK) {
            Wire wire;
            BlockPos pos = event.getTarget().getBlockPos();

            if (event.getTarget().hitInfo instanceof IPartInfo) {
                wire = WireUtils.getAnyWire(((IPartInfo) event.getTarget().hitInfo).getTile().getTileEntity());
            } else {
                wire = WireUtils.getAnyWire(event.getPlayer().getEntityWorld(), pos);
            }

            if (wire != null) {
                event.setCanceled(true);
                int lineMaskCenter = 0xFFF;
                EnumFacing[] faces = WireUtils.getConnectionsForRender(wire.getLocation());

                GlStateManager.pushMatrix();
                GlStateManager.translate(pos.getX(), pos.getY(), pos.getZ());

                for (int i = 0; i < faces.length; i++) {
                    EnumFacing face = faces[i];
                    if (wire.connectsAny(face)) {
                        int lineMask = 0xfff;
                        lineMask &= ~RenderUtils.getSelectionMask(face.getOpposite());
                        RenderUtils.drawSelectionBoundingBox(wire.getFactory().getSelectionBox(wire.getLocation(), i + 1), lineMask);
                        lineMaskCenter &= ~RenderUtils.getSelectionMask(face);
                    }
                }

                if (lineMaskCenter != 0) {
                    RenderUtils.drawSelectionBoundingBox(wire.getFactory().getSelectionBox(wire.getLocation(), 0), lineMaskCenter);
                }

                GlStateManager.popMatrix();
            }
        }
    }
}
