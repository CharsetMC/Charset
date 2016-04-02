package pl.asie.charset.wires;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.EnumFacing;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.api.wires.WireType;
import pl.asie.charset.lib.utils.ClientUtils;
import pl.asie.charset.wires.logic.PartWireBase;
import pl.asie.charset.wires.render.RendererWire;

/**
 * Created by asie on 12/5/15.
 */
public class ProxyClient extends ProxyCommon {
	public static RendererWire rendererWire = new RendererWire();

	@Override
	public void drawWireHighlight(PartWireBase wire) {
		int lineMaskCenter = 0xFFF;
		EnumFacing[] faces = WireUtils.getConnectionsForRender(wire.location);
		for (int i = 0; i < faces.length; i++) {
			EnumFacing face = faces[i];
			if (wire.connectsAny(face)) {
				int lineMask = 0xfff;
				lineMask &= ~ClientUtils.getLineMask(face.getOpposite());
				ClientUtils.drawSelectionBoundingBox(wire.getSelectionBox(i + 1), lineMask);
				lineMaskCenter &= ~ClientUtils.getLineMask(face);
			}
		}
		if (lineMaskCenter != 0) {
			ClientUtils.drawSelectionBoundingBox(wire.getSelectionBox(0), lineMaskCenter);
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onPostBake(ModelBakeEvent event) {
		event.getModelRegistry().putObject(new ModelResourceLocation("charsetwires:wire", "multipart"), rendererWire);
		event.getModelRegistry().putObject(new ModelResourceLocation("charsetwires:wire", "inventory"), rendererWire);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureStitch(TextureStitchEvent.Pre event) {
		for (WireType type : WireType.values()) {
			rendererWire.registerSheet(event.getMap(), type, new ResourceLocation("charsetwires", "blocks/wire_" + type.name().toLowerCase()));
		}
	}
}
