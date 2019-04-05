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
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import pl.asie.charset.lib.inventory.GuiContainerCharset;
import pl.asie.simplelogic.gates.PacketTimerChangeTT;
import pl.asie.simplelogic.gates.PacketTransposerConnection;
import pl.asie.simplelogic.gates.SimpleLogicGates;
import pl.asie.simplelogic.gates.logic.GateLogic;
import pl.asie.simplelogic.gates.logic.GateLogicBundledTransposer;
import pl.asie.simplelogic.gates.logic.GateLogicTimer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiTransposer extends GuiContainerCharset<ContainerGate> {
	private static final ResourceLocation TEXTURE = new ResourceLocation("simplelogic:textures/gui/transposer.png");
	public static class TinyButton {
		private final int texY;
		private final int x, y;
		private final int w, h;
		private final int color;
		private final boolean bottomRow;
		private boolean pressed;

		public TinyButton(int texY, int x, int y, int color, boolean bottomRow) {
			this.texY = texY;
			this.x = x;
			this.y = y;
			this.w = 9;
			this.h = 9;
			this.color = color;
			this.bottomRow = bottomRow;
		}
	}

	private final List<TinyButton> buttonList = new ArrayList<>(32);
	private TinyButton pressedButton, pressedButtonSecond;
	private int presses;

	public GuiTransposer(ContainerGate container) {
		super(container, 167, 76);

		for (int i = 0; i <= 16; i++) {
			buttonList.add(new TinyButton(i == 16 ? 85 : 76, 7 + i * 9 ,8, i, true));
			buttonList.add(new TinyButton(i == 16 ? 85 : 76, 7 + i * 9 ,59, i, false));
		}
	}

	protected void drawLine(int x1, int y1, int x2, int y2, int color1, int color2) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder builder = tessellator.getBuffer();

		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GL11.glLineWidth(2.0F);
		builder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
		float x = x1;
		float y = y1;
		float xd = (x2 - x1) / 8f;
		float yd = (y2 - y1) / 8f;

		for (int step = 0; step < 9; step++, x += xd, y += yd) {
			int col = (step & 1) == 0 ? color1 : color2;
			if (col == 0) continue;
			builder.pos(x, y, 0).color((col >> 16) & 0xFF, (col >> 8) & 0xFF, (col) & 0xFF, (col >> 24) & 0xFF).endVertex();
			if (step < 8) builder.pos(x+xd, y+yd, 0).color((col >> 16) & 0xFF, (col >> 8) & 0xFF, (col) & 0xFF, (col >> 24) & 0xFF).endVertex();
		}

		tessellator.draw();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partial, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partial, mouseX, mouseY);

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(TEXTURE);
		drawTexturedModalRect(xBase, yBase, 0, 0, xSize, ySize);

		GateLogicBundledTransposer logic = (GateLogicBundledTransposer) container.gate.logic;

		for (int from = 0; from < 16; from++) {
			int v = logic.transpositionMap[from];
			int i = 0;
			while (v != 0) {
				if ((v & 1) != 0) {
					int c1 = EnumDyeColor.byMetadata(from).getColorValue() | 0xDF000000;
					int c2 = EnumDyeColor.byMetadata(i).getColorValue() | 0xDF000000;
					drawLine(xBase + 12 + i * 9, yBase + 17, xBase + 12 + from * 9, yBase + 58, c1, c2);
				}
				v >>= 1; i++;
			}
		}

		if (pressedButton != null && pressedButtonSecond == null) {
			int col = pressedButton.color;
			if (col == 16) {
				for (int i = 0; i < 16; i++) {
					int c1 = EnumDyeColor.byMetadata(i).getColorValue() | 0xFF000000;
					int c2 = GuiScreen.isShiftKeyDown() ? 0 : c1;
					drawLine(xBase + 12 + i * 9, yBase + pressedButton.y + (!pressedButton.bottomRow ? 0 : 9), mouseX, mouseY, c1, c2);
				}
			} else {
				int c1 = EnumDyeColor.byMetadata(col).getColorValue() | 0xFF000000;
				int c2 = GuiScreen.isShiftKeyDown() ? 0 : c1;
				drawLine(xBase + pressedButton.x + 5, yBase + pressedButton.y + (!pressedButton.bottomRow ? 0 : 9), mouseX, mouseY, c1, c2);
			}
		}

		int mxb = mouseX - xBase;
		int myb = mouseY - yBase;
		for (TinyButton tb : buttonList) {
			int texX = tb.pressed ? 18 : ((mxb >= tb.x && mxb < tb.x+tb.w && myb >= tb.y && myb < tb.y+tb.h) ? 9 : 0);
			if (texX == 9 && pressedButton != null && pressedButton.bottomRow == tb.bottomRow) {
				texX = 0;
			}

			drawRect(xBase + tb.x, yBase + tb.y, xBase + tb.x + 9, yBase + tb.y + 9, EnumDyeColor.byMetadata(tb.color).getColorValue() | 0xFF000000);
			drawTexturedModalRect(xBase + tb.x, yBase + tb.y, texX, tb.texY, 9, 9);
		}
	}

	@Override
	public void mouseClicked(int x, int y, int mb) throws IOException {
		super.mouseClicked(x, y, mb);
		if (mb == 0 && presses < 2) {
			for (TinyButton button : buttonList) {
				int bx = this.xBase + button.x;
				int by = this.yBase + button.y;
				if (x >= bx && x < (bx + button.w) && y >= by && y < (by + button.h)) {
					button.pressed = true;
					if (pressedButton == null) {
						pressedButton = button;
					} else if (pressedButtonSecond == null) {
						pressedButtonSecond = button;
					}
				}
			}

			if (presses == 1 || (presses == 0 && pressedButton != null)) presses++;
		}
	}

	private void clearPresses() {
		if (pressedButton != null) pressedButton.pressed = false;
		if (pressedButtonSecond != null) pressedButtonSecond.pressed = false;
		pressedButton = pressedButtonSecond = null;
		presses = 0;
	}

	private void sendPackets(int from, int to, boolean remove) {
		if (from == 16) from = -1;
		if (to == 16) to = -1;

		if (from != -1 && to != -1) {
			SimpleLogicGates.packet.sendToServer(new PacketTransposerConnection(container.gate, from, to, remove));
		} else if (from != -1) {
			for (int t = 0; t < 16; t++) {
				SimpleLogicGates.packet.sendToServer(new PacketTransposerConnection(container.gate, from, t, remove));
			}
		} else if (to != -1) {
			for (int t = 0; t < 16; t++) {
				SimpleLogicGates.packet.sendToServer(new PacketTransposerConnection(container.gate, t, to, remove));
			}
		} else {
			if (remove) {
				for (int t = 0; t < 256; t++) {
					SimpleLogicGates.packet.sendToServer(new PacketTransposerConnection(container.gate, t & 15, t >> 4, remove));
				}
			} else {
				for (int t = 0; t < 16; t++) {
					SimpleLogicGates.packet.sendToServer(new PacketTransposerConnection(container.gate, t, t, remove));
				}
			}
		}
	}

	@Override
	public void mouseReleased(int x, int y, int which) {
		super.mouseReleased(x, y, which);
		if (which >= 0) {
			if ((presses == 1 && pressedButton != null) || (presses == 2 && pressedButtonSecond != null)) {
				this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			}

			if (pressedButton != null && pressedButtonSecond != null && pressedButton.bottomRow == pressedButtonSecond.bottomRow) {
				clearPresses();
				return;
			}

			int from = -1;
			int to = -1;
			if (pressedButton != null) {
				if (pressedButton.bottomRow) {
					to = pressedButton.color;
					from = pressedButtonSecond != null ? pressedButtonSecond.color : -1;
				} else {
					from = pressedButton.color;
					to = pressedButtonSecond != null ? pressedButtonSecond.color : -1;
				}
			}

			if (GuiScreen.isShiftKeyDown()) {
				if (from != -1 || to != -1) {
					sendPackets(from, to, true);
				}

				clearPresses();
			} else if (presses >= 2) {
				if (from >= 0 && to >= 0) {
					sendPackets(from, to, false);
				}

				clearPresses();
			}
		}
	}
}
