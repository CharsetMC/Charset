package pl.asie.charset.lib.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.api.lib.IDebuggable;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.capability.CapabilityHelper;

import java.util.ArrayList;
import java.util.List;

public class DebugInfoProvider {
	private void addDebugInformation(RayTraceResult mouseOver, World world, List<String> info, Side side) {
		ICapabilityProvider provider = null;
		switch (mouseOver.typeOfHit) {
			case BLOCK:
				provider = world.getTileEntity(mouseOver.getBlockPos());
				break;
			case ENTITY:
				provider = world.getEntityByID(mouseOver.entityHit.getEntityId());
				break;
		}

		if (provider != null) {
			IDebuggable debug = CapabilityHelper.get(Capabilities.DEBUGGABLE, provider, mouseOver.sideHit);
			if (debug != null) {
				List<String> targetInfo = new ArrayList<>();
				debug.addDebugInformation(targetInfo, side);
				if (targetInfo.size() > 0) {
					info.add("");
					info.add(TextFormatting.AQUA + "" + TextFormatting.BOLD + "" + TextFormatting.UNDERLINE + "" + TextFormatting.ITALIC + side.name());
					info.addAll(targetInfo);
				}
			}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onGameOverlayDebugRender(RenderGameOverlayEvent.Text event) {
		Minecraft mc = Minecraft.getMinecraft();

		if (!mc.gameSettings.showDebugInfo)
			return;

		RayTraceResult mouseOver = mc.objectMouseOver;

		if (mouseOver != null) {
			addDebugInformation(mouseOver, mc.world, event.getRight(), Side.CLIENT);

			// The following relies on some hacks - we're getting the
			// *server* world from the *client*, so this should
			// only work in SSP.
			if (!mc.isSingleplayer())
				return;

			World world = DimensionManager.getWorld(mc.world.provider.getDimension());
			addDebugInformation(mouseOver, world, event.getRight(), Side.SERVER);
		}
	}
}
