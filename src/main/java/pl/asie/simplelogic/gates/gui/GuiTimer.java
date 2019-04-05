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

package pl.asie.simplelogic.gates.gui;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import pl.asie.charset.lib.inventory.GuiContainerCharset;
import pl.asie.charset.module.audio.storage.GuiRecordPlayer;
import pl.asie.simplelogic.gates.PacketTimerChangeTT;
import pl.asie.simplelogic.gates.SimpleLogicGates;
import pl.asie.simplelogic.gates.logic.GateLogicTimer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiTimer extends GuiContainerCharset<ContainerGate> {
	public static class TinyButton {
		private final int texY;
		private final int x, y;
		private final int w, h;
		private final int change;
		private boolean pressed;

		public TinyButton(int texY, int x, int y, int change) {
			this.texY = texY;
			this.x = x;
			this.y = y;
			this.w = 9;
			this.h = 9;
			this.change = change;
		}
	}

	private static final ResourceLocation TEXTURE = new ResourceLocation("simplelogic:textures/gui/timer.png");
	private final List<TinyButton> buttonList = new ArrayList<>(12);

	public GuiTimer(ContainerGate container) {
		super(container, 85, 56);

		buttonList.add(new TinyButton(56, 8, 33, -12000));
		buttonList.add(new TinyButton(56, 18, 33, -1200));
		buttonList.add(new TinyButton(56, 33, 33, -200));
		buttonList.add(new TinyButton(56, 43, 33, -20));
		buttonList.add(new TinyButton(56, 58, 33, -2));
		buttonList.add(new TinyButton(56, 68, 33, -1));
		buttonList.add(new TinyButton(65, 8, 13, 12000));
		buttonList.add(new TinyButton(65, 18, 13, 1200));
		buttonList.add(new TinyButton(65, 33, 13, 200));
		buttonList.add(new TinyButton(65, 43, 13, 20));
		buttonList.add(new TinyButton(65, 58, 13, 2));
		buttonList.add(new TinyButton(65, 68, 13, 1));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partial, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partial, mouseX, mouseY);

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(TEXTURE);
		drawTexturedModalRect(xBase, yBase, 0, 0, xSize, ySize);

		GateLogicTimer glt = (GateLogicTimer) container.gate.logic;

		int[] digits = new int[] {
				(glt.getTicksTotal() / 12000) % 10,
				(glt.getTicksTotal() / 1200) % 10,
				(glt.getTicksTotal() / 200) % 6,
				(glt.getTicksTotal() / 20) % 10,
				(glt.getTicksTotal() / 2) % 10,
				(glt.getTicksTotal() & 1) * 5
		};

		for (TinyButton tb : buttonList) {
			int texX = tb.pressed ? 9 : 0;
			drawTexturedModalRect(xBase + tb.x, yBase + tb.y, texX, tb.texY, 9, 9);
		}

		int ix = 10;
		for (int i = 0; i < 6; i++) {
			this.mc.fontRenderer.drawString(Integer.toString(digits[i]), xBase + ix, yBase + 24, -1);
			ix += ((i & 1) == 0) ? 10 : 15;
		}
	}

	@Override
	public void mouseClicked(int x, int y, int mb) throws IOException {
		super.mouseClicked(x, y, mb);
		if (mb == 0) {
			for (TinyButton button : buttonList) {
				int bx = this.xBase + button.x;
				int by = this.yBase + button.y;
				if (x >= bx && x < (bx + button.w) && y >= by && y < (by + button.h)) {
					button.pressed = true;
				}
			}
		}
	}

	@Override
	public void mouseReleased(int x, int y, int which) {
		super.mouseReleased(x, y, which);
		if (which >= 0) {
			for (TinyButton button : buttonList) {
				if (!button.pressed) continue;

				this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));

				GateLogicTimer glt = (GateLogicTimer) container.gate.logic;
				//glt.setTicksTotal(container.gate, glt.getTicksTotal() + button.change);
				SimpleLogicGates.packet.sendToServer(new PacketTimerChangeTT(container.gate, button.change));

				button.pressed = false;
			}
		}
	}
}
