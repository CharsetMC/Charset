package pl.asie.charset.lib.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiOverlayDebug;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.api.lib.IDebuggable;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.capability.CapabilityHelper;

import java.util.List;

public class InDevEventHandler {
	private void addDebugInformation(RayTraceResult mouseOver, World world, List<String> info, Side side) {
		ICapabilityProvider provider = null;
		switch (mouseOver.typeOfHit) {
			case BLOCK:
				TileEntity tile = world.getTileEntity(mouseOver.getBlockPos());
				if (tile != null)
					provider = tile;
				break;
			case ENTITY:
				Entity entity = world.getEntityByID(mouseOver.entityHit.getEntityId());
				if (entity != null)
					provider = entity;
				break;
		}

		if (provider != null) {
			IDebuggable debug = CapabilityHelper.get(Capabilities.DEBUGGABLE, provider, mouseOver.sideHit);
			if (debug != null) {
				debug.addDebugInformation(info, side);
			}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onGameOverlayDebugRender(RenderGameOverlayEvent.Text event) {
		if (!Minecraft.getMinecraft().gameSettings.showDebugInfo)
			return;

		// This relies on some hacks - we're getting the
		// *server* world from the *client*, so this should
		// only work in SSP.
		if (!Minecraft.getMinecraft().isSingleplayer())
			return;

		RayTraceResult mouseOver = Minecraft.getMinecraft().objectMouseOver;

		if (mouseOver != null) {
			World world = DimensionManager.getWorld(Minecraft.getMinecraft().world.provider.getDimension());
			addDebugInformation(mouseOver, world, event.getRight(), Side.SERVER);
		}
	}
}
