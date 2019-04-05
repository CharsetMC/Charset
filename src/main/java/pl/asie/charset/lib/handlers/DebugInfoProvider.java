/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.charset.lib.handlers;

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
import pl.asie.charset.ModCharset;
import pl.asie.charset.api.lib.IDebuggable;
import pl.asie.charset.lib.CharsetLib;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.capability.CapabilityHelper;
import pl.asie.charset.lib.modcompat.mcmultipart.DebugInfoProviderMCMP;

import java.util.ArrayList;
import java.util.List;

public class DebugInfoProvider {
	private static final List<Handler> handlers = new ArrayList<>();

	public interface Handler {
		boolean addDebugInformation(RayTraceResult mouseOver, World world, List<String> info, Side side);
	}

	public static void registerHandler(Handler handler) {
		handlers.add(handler);
	}

	public static void addDebugInformation(IDebuggable debug, World world, List<String> info, Side side) {
		List<String> targetInfo = new ArrayList<>();
		debug.addDebugInformation(targetInfo, side);
		if (targetInfo.size() > 0) {
			info.add("");
			info.add(TextFormatting.AQUA + "" + TextFormatting.BOLD + "" + TextFormatting.UNDERLINE + "" + TextFormatting.ITALIC + side.name());
			info.addAll(targetInfo);
		}
	}

	private void addDebugInformationDefault(RayTraceResult mouseOver, World world, List<String> info, Side side) {
		if (mouseOver.hitInfo instanceof IDebuggable) {
			addDebugInformation((IDebuggable) mouseOver.hitInfo, world, info, side);
			return;
		}

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
				addDebugInformation(debug, world, info, side);
			}
		}
	}

	private void addDebugInformation(RayTraceResult mouseOver, World world, List<String> info, Side side) {
		for (Handler h : handlers) {
			if (h.addDebugInformation(mouseOver, world, info, side)) {
				return;
			}
		}

		addDebugInformationDefault(mouseOver, world, info, side);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onGameOverlayDebugRender(RenderGameOverlayEvent.Text event) {
		if (!ModCharset.INDEV && !CharsetLib.enableDebugInfo)
			return;

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
