/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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

package pl.asie.charset.module.tablet;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import pl.asie.charset.module.tablet.format.ClientTypesetter;
import pl.asie.charset.module.tablet.format.api.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;

public class GuiTablet extends GuiScreen implements IPrintingContextMinecraft {
	private static final boolean ENABLE_HIGHLIGHT = false;

	private static final int[] PALETTE = new int[]{
			0x00000000, 0x1c000000, 0x30000000, 0x48000000,
			0x60000000, 0x78000000, 0x9a000000, 0xbc000000
	};
	private static final ResourceLocation TEXTURE = new ResourceLocation("charset", "textures/gui/tabletgui.png");
	private static final int X_SIZE = 142;
	private static final int Y_SIZE = 180;
	private static final int offsetLeft = 12;
	private static final int offsetTop = 10;
	private int guiLeft, guiTop;
	private float glScale = 1.0f;
	private int buttonState = 1;
	private int heightStart = 0;
	private int heightEnd = 0;
	private int heightPos = 0;
	private int pageWidth = 240;
	private int pageHeight = 300;
	private Deque<URI> uriQueue = new LinkedBlockingDeque<>();
	private URI currentURI = null;
	private Future<String> currentFuture;
	private ClientTypesetter typesetter;
	private List<IStyle> currentStyle;

	public GuiTablet(EntityPlayer player) {
		super();
	}

	protected final boolean insideRect(int x, int y, int x0, int y0, int w, int h) {
		return x >= x0 && y >= y0 && x < (x0 + w) && y < (y0 + h);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void initGui() {
		super.initGui();
		ScaledResolution realRes = new ScaledResolution(mc);
		int scaleFactor = 1;
		int margin = 4;
		while (mc.displayWidth / scaleFactor >= X_SIZE + margin && mc.displayHeight / scaleFactor >= Y_SIZE + margin) {
			scaleFactor++;
			if (scaleFactor >= 2 && (scaleFactor & 1) == 1) scaleFactor++;
		}
		scaleFactor--;
		if (scaleFactor > 2 && (scaleFactor & 1) == 1) scaleFactor--;

		double scaledWidth = ((double) mc.displayWidth / scaleFactor);
		double scaledHeight = ((double) mc.displayHeight / scaleFactor);

		glScale = (float) (scaledWidth / realRes.getScaledWidth_double());

		this.guiLeft = (MathHelper.ceil(scaledWidth) - X_SIZE) / 2;
		this.guiTop = (MathHelper.ceil(scaledHeight) - Y_SIZE) / 2;

		// load text
		if (currentFuture == null && currentURI == null) {
			try {
				openURI(new URI("about://index"));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		closePreviousFuture();
	}

	private void closePreviousFuture() {
		if (currentFuture != null) {
			currentFuture.cancel(true);
			currentFuture = null;
		}
	}

	private void load(String text) {
		typesetter = new ClientTypesetter(fontRenderer, pageWidth);
		try {
			typesetter.write(text);
		} catch (Exception e) {
			e.printStackTrace();
		}

		heightStart = 0;
		heightPos = 0;
		heightEnd = 10;

		for (int i = 0; i < typesetter.lines.size(); i++) {
			ClientTypesetter.Line l = typesetter.lines.get(i);
			heightEnd += l.height;
			if (i > 0) {
				heightEnd += l.paddingAbove;
			}
		}

		heightEnd -= pageHeight;
		heightEnd = heightEnd - (heightEnd % fontRenderer.FONT_HEIGHT+1);
		if (heightEnd < 0) {
			heightEnd = 0;
		}
	}

	public void bindTexture(ResourceLocation texture) {
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
	}

	@Override
	public void updateScreen() {
		super.updateScreen();

		if (currentFuture != null && currentFuture.isDone()) {
			try {
				load(currentFuture.get());
				currentFuture = null;
			} catch (Exception e) {
				e.printStackTrace();
				currentFuture = null;
				load("\\b{Error!} " + e.getMessage());
			}
		}
	}

	private boolean isButton(int mx, int my) {
		return mx >= (guiLeft + 65) && my >= (guiTop + 167) && mx < (guiLeft + 65 + 18) && my < (guiTop + 167 + 8);
	}

	private void checkTypesetterClicked(int mx, int my) {
		mx = (mx - this.guiLeft - offsetLeft) * 2;
		my = (my - this.guiTop - offsetTop) * 2;

		int y = 0;
		for (int i = 0; i < typesetter.lines.size(); i++) {
			ClientTypesetter.Line line = typesetter.lines.get(i);
			ClientTypesetter.Line nextLine = i < typesetter.lines.size() - 1 ? typesetter.lines.get(i + 1) : null;

			if (y >= heightPos) {
				int x = 0;
				for (ClientTypesetter.WordContainer word : line.words) {
					currentStyle = word.styles;
					int hOff = (line.height - word.printer.getHeight(this, word.word)) / 3;
					if (insideRect(mx, my, x, y - heightPos + hOff,
							word.printer.getWidth(this, word.word),
							word.printer.getHeight(this, word.word))) {
						if (word.printer.onClick(this, word.word)) {
							return;
						}
					}
					x += word.printer.getWidth(this, word.word);
				}
			}

			y += line.height;
			if (nextLine != null) {
				y += nextLine.paddingAbove;
			}
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);

		switch (keyCode) {
			case Keyboard.KEY_HOME:
				heightPos = heightStart;
				break;
			case Keyboard.KEY_END:
				heightPos = heightEnd;
				break;
			case Keyboard.KEY_PRIOR:
				heightPos -= pageHeight-32;
				break;
			case Keyboard.KEY_NEXT:
				heightPos += pageHeight-32;
				break;
			case Keyboard.KEY_BACK:
				if (!uriQueue.isEmpty()) {
					openURI(uriQueue.removeFirst());
					uriQueue.removeFirst(); // remove the just removed URI
				} else {
					try {
						openURI(new URI("about://index"));
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
				}
				break;
			case Keyboard.KEY_F5:
				if (currentURI != null) {
					openURI(currentURI);
					uriQueue.removeFirst();
				}
				break;
		}

		heightPos = Math.max(Math.min(heightEnd, heightPos), heightStart);
	}

	@Override
	public void handleMouseInput() {
		int x = (int) (Mouse.getEventX() * this.width / this.mc.displayWidth * glScale);
		int y = (int) ((this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1) * glScale);
		int k = Mouse.getEventButton();
		int w = Mouse.getEventDWheel();
		int oldButtonState = buttonState;

		if (w != 0) {
			if (w < 0) {
				heightPos += fontRenderer.FONT_HEIGHT+1;
			} else {
				heightPos -= fontRenderer.FONT_HEIGHT+1;
			}

			heightPos = Math.max(Math.min(heightEnd, heightPos), heightStart);
		}

		if (k == 0) {
			if (Mouse.getEventButtonState()) {
				if (isButton(x, y)) {
					buttonState = 2;
				} else if (typesetter != null) {
					checkTypesetterClicked(x, y);
				}
			} else if (buttonState == 2) {
				if (isButton(x, y)) {
					buttonState = ENABLE_HIGHLIGHT ? 0 : 1;
				} else {
					buttonState = 1;
				}
			}
		} else if (ENABLE_HIGHLIGHT && k == -1 && buttonState != 2) {
			if (isButton(x, y)) {
				buttonState = 0;
			} else {
				buttonState = 1;
			}
		}

		if (oldButtonState != buttonState && buttonState == 2) {
			if (!uriQueue.isEmpty()) {
				openURI(uriQueue.removeFirst());
				uriQueue.removeFirst(); // remove the just removed URI
			} else {
				try {
					openURI(new URI("about://index"));
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void drawScreen(int fmx, int fmy, float p) {
		int mx = (fmx - this.guiLeft - offsetLeft) * 2;
		int my = (fmy - this.guiTop - offsetTop) * 2;

		this.drawDefaultBackground();

		GlStateManager.color(1.0f, 1.0f, 1.0f);
		GlStateManager.pushMatrix();
		GlStateManager.scale(1.0f / glScale, 1.0f / glScale, 1.0f / glScale);

		// bg
		bindTexture(TEXTURE);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, X_SIZE, Y_SIZE);
		drawTexturedModalRect(guiLeft + 65, guiTop + 167, 142, 147 + (buttonState * 10), 18, 8);

		// led
		int ledColor = 0xFF000000;
		if (currentFuture != null && (System.currentTimeMillis() % 250) >= 125) {
			ledColor = 0xFF00FF00;
		}
		drawRect(guiLeft + 127, guiTop + 4, guiLeft + 127 + 4, guiTop + 4 + 1, ledColor);

		// text
		GlStateManager.enableBlend();
		// GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

		GlStateManager.scale(0.5f, 0.5f, 0.5f);
		GlStateManager.translate((guiLeft + offsetLeft) * 2, (guiTop + offsetTop) * 2, 0);

		if (typesetter != null) {
			int y = 0;
			for (int i = 0; i < typesetter.lines.size(); i++) {
				ClientTypesetter.Line line = typesetter.lines.get(i);
				ClientTypesetter.Line nextLine = i < typesetter.lines.size() - 1 ? typesetter.lines.get(i + 1) : null;

				if (y >= heightPos) {
					int x = 0;
					for (ClientTypesetter.WordContainer word : line.words) {
						currentStyle = word.styles;
						int wwidth = word.printer.getWidth(this, word.word);
						int wheight = word.printer.getHeight(this, word.word);
						int hOff = (line.height - wheight) / 3;
						boolean isHovering = insideRect(mx, my, x, y - heightPos + hOff, wwidth, wheight);
						word.printer.draw(this, word.word, x, y - heightPos + hOff, isHovering);
						if (isHovering) {
							word.printer.drawTooltip(this, word.word, mx, my);
						}
						x += word.printer.getWidth(this, word.word);
						if (line.words.size() > 1) {
							//x += (120 - line.length) / (line.words.size() - 1);
						}
					}
				}
				y += line.height;
				if (nextLine != null) {
					y += nextLine.paddingAbove;
				}
				if (y + (nextLine != null ? nextLine.height : 0) >= heightPos + pageHeight) {
					break;
				}
			}
		}

		// GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GlStateManager.disableBlend();

		GlStateManager.popMatrix();
	}

	@Override
	public FontRenderer getFontRenderer() {
		return typesetter.getFontRenderer();
	}

	@Override
	public boolean openURI(URI uri) {
		if (uri == null) {
			return false;
		}

		uri = currentURI == null ? uri : currentURI.resolve(uri);
		if (!TabletAPI.INSTANCE.matchesRoute(uri)) {
			return false;
		}

		closePreviousFuture();
		if (currentURI != null) {
			uriQueue.addFirst(currentURI);
		}
		currentURI = uri;
		currentFuture = TabletAPI.INSTANCE.getRoute(currentURI);
		return true;
	}

	@Override
	public List<IStyle> getStyleList() {
		return currentStyle;
	}
}
