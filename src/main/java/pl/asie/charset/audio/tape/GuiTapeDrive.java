/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.audio.tape;

import java.io.IOException;
import java.util.Arrays;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.translation.I18n;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.lwjgl.opengl.Display;
import pl.asie.charset.audio.ModCharsetAudio;
import pl.asie.charset.lib.container.GuiContainerCharset;

public class GuiTapeDrive extends GuiContainerCharset {
	public class DialogThread implements Runnable {
		public JFileChooser chooser = new JFileChooser();
		public int result;

		@Override
		public void run() {
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setDialogType(JFileChooser.OPEN_DIALOG);
			chooser.setFileFilter(new FileNameExtensionFilter("Audio file " + Arrays.toString(TapeRecordThread.getSupportedExtensions()), TapeRecordThread.getSupportedExtensions()));
			result = chooser.showOpenDialog(Display.getParent());
		}
	}

	private static final ResourceLocation TEXTURE = new ResourceLocation("charsetaudio:textures/gui/tape_drive.png");
	private static final int BUTTON_START_X = 88 - (Button.values().length * 10);
	private static final int BUTTON_START_Y = 58;

	private State state = State.STOPPED;
	public enum Button {
		REWIND,
		PLAY,
		STOP,
		FAST_FORWARD,
		RECORD_FILE,
		RECORD_AUDIO
	}
	private PartTapeDrive tapeDrive;
	private int counter;
	private Button buttonHovering = null;
	private boolean ctrResetHover = false;
	private DialogThread tapeDialog;
	private TapeRecordThread tapeRecord;
	private Thread tapeRecordThread, tapeDialogThread;

	public boolean isRecordingFromFile(boolean inProgress) {
		if (!inProgress && tapeDialogThread != null && tapeDialogThread.isAlive()) {
			return true;
		}

		return tapeRecordThread != null && tapeRecordThread.isAlive();
	}
	
	public GuiTapeDrive(Container container, PartTapeDrive tapeDrive) {
		super(container, 176, 166);
		this.tapeDrive = tapeDrive;
	}
	
	public boolean isButtonPressed(Button button) {
		if (button == buttonHovering) {
			return true;
		}

		if (isRecordingFromFile(false) && button == Button.RECORD_FILE) {
			return true;
		}

		switch (state) {
			case FORWARDING:
				return button == Button.FAST_FORWARD;
			case PLAYING:
				return button == Button.PLAY;
			case REWINDING:
				return button == Button.REWIND;
			case RECORDING:
				return button == Button.RECORD_AUDIO;
			case STOPPED:
			default:
				return false;
		}
	}
	
	public void setState(State state) {
		if (tapeDrive != null) {
			try {
				PacketDriveState packet = new PacketDriveState(tapeDrive, state);
				ModCharsetAudio.packet.sendToServer(packet);
				tapeDrive.setState(state);
			} catch (Exception e) {
				//NO-OP
			}
		}
	}
	
	public void handleButtonPress(Button button) {
		if (isRecordingFromFile(false)) {
			return;
		}

		switch(button) {
			case REWIND:
				if(state == State.REWINDING) setState(State.STOPPED);
				else setState(State.REWINDING);
				break;
			case PLAY:
				setState(State.PLAYING);
				break;
			case FAST_FORWARD:
				if(state == State.FORWARDING) setState(State.STOPPED);
				else setState(State.FORWARDING);
				break;
			case STOP:
				setState(State.STOPPED);
				break;
			case RECORD_FILE:
				if (tapeDrive.inventory.getStackInSlot(0) != null) {
					setState(State.STOPPED);
					if (Minecraft.getMinecraft().isFullScreen()) {
						Minecraft.getMinecraft().toggleFullscreen();
					}

					tapeDialog = new DialogThread();
					tapeDialogThread = new Thread(tapeDialog);
					tapeDialogThread.start();
				}
				break;
			case RECORD_AUDIO:
				setState(State.RECORDING);
				break;
		}
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		this.state = tapeDrive.getState();
		this.counter = tapeDrive.state.counter;

		if (tapeDialogThread != null && !tapeDialogThread.isAlive()) {
			if (tapeDialog.result == JFileChooser.APPROVE_OPTION) {
				if (tapeDialog.chooser.getSelectedFile() != null) {
					tapeRecord = new TapeRecordThread(tapeDialog.chooser.getSelectedFile(), tapeDrive);
					tapeRecordThread = new Thread(tapeRecord);
					tapeRecordThread.start();
				}
			}
			tapeDialogThread = null;
			tapeDialog = null;
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (isRecordingFromFile(false)) {
			if (keyCode == 1 || keyCode == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
				return;
			}
		}

		super.keyTyped(typedChar, keyCode);
	}

	@Override
	public void mouseClicked(int x, int y, int mb) throws IOException {
		if (isRecordingFromFile(false)) {
			return;
		}

		super.mouseClicked(x, y, mb);
		if(mb == 0) {
			for(Button button: Button.values()) {
				int button_x = this.xCenter + BUTTON_START_X + (button.ordinal() * 20);
				int button_y = this.yCenter + BUTTON_START_Y;
				if(x >= button_x && x < (button_x + 20) && y >= button_y && y < (button_y + 15)) {
					if(!isButtonPressed(button)) {
						buttonHovering = button;
						return;
					}
				}
			}

			int reset_x = this.xCenter + 122;
			int reset_y = this.yCenter + 39;
			if (x >= reset_x && x <= (reset_x + 5) && y >= reset_y && y <= (reset_y + 6)) {
				ctrResetHover = true;
				return;
			}
		}
	}
	
	@Override
	public void mouseReleased(int x, int y, int which) {
		if (isRecordingFromFile(false)) {
			return;
		}

		super.mouseReleased(x, y, which);
		if(which >= 0 && buttonHovering != null) {
			this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(new SoundEvent(new ResourceLocation("gui.button.press")), 1.0F));
			handleButtonPress(buttonHovering);
			buttonHovering = null;
		}

		if (which >= 0 && ctrResetHover) {
			this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(new SoundEvent(new ResourceLocation("gui.button.press")), 1.0F));
			ModCharsetAudio.packet.sendToServer(new PacketDriveCounter(tapeDrive, 0));
			ctrResetHover = false;
		}
	}
	
	// Uses NBT data.
	private String getLabel() {
		ItemStack stack = this.inventorySlots.getInventory().get(0);
		if (stack != null && stack.getItem() instanceof ItemTape) {
			String label = I18n.translateToLocal("tooltip.charset.tape.unnamed");
			if (stack.hasDisplayName()) {
				label = stack.getDisplayName();
			}
			return label;
		} else return null;
	}

	public void drawTexturedModalRectFloatY(int x, int y, int textureX, float textureY, int width, int height) {
		float f = 0.00390625F;
		float f1 = 0.00390625F;
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer vertexbuffer = tessellator.getBuffer();
		vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		vertexbuffer.pos((double)(x + 0), (double)(y + height), (double)this.zLevel).tex((double)((float)(textureX + 0) * f), (double)((float)(textureY + height) * f1)).endVertex();
		vertexbuffer.pos((double)(x + width), (double)(y + height), (double)this.zLevel).tex((double)((float)(textureX + width) * f), (double)((float)(textureY + height) * f1)).endVertex();
		vertexbuffer.pos((double)(x + width), (double)(y + 0), (double)this.zLevel).tex((double)((float)(textureX + width) * f), (double)((float)(textureY + 0) * f1)).endVertex();
		vertexbuffer.pos((double)(x + 0), (double)(y + 0), (double)this.zLevel).tex((double)((float)(textureX + 0) * f), (double)((float)(textureY + 0) * f1)).endVertex();
		tessellator.draw();
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		super.drawGuiContainerBackgroundLayer(f, i, j);

		this.mc.getTextureManager().bindTexture(TEXTURE);
		this.drawTexturedModalRect(this.xCenter, this.yCenter, 0, 0, this.xSize, this.ySize);

		// Draw buttons
		for(Button button: Button.values()) {
			int button_ty = 166 + (button.ordinal() * 15);
			int button_tx = isButtonPressed(button) ? 20 : 0;
			int button_x = BUTTON_START_X + (button.ordinal() * 20);
			this.drawTexturedModalRect(this.xCenter + button_x, this.yCenter + BUTTON_START_Y, button_tx, button_ty, 20, 15);
		}

		if (ctrResetHover) {
			this.drawTexturedModalRect(this.xCenter + 121, this.yCenter + 39, 121, 38, 6, 6);
		}

		// Draw counter
		float counterReal = (float) counter / 48000.0f;
		if (counterReal < 0) {
			counterReal = (counterReal % 1000f) + 1000.0f;
		}
		float[] counterPos = new float[3];
		counterPos[0] = (counterReal / 100.0f) % 10;
		counterPos[1] = (counterReal / 10.0f) % 10;
		counterPos[2] = (counterReal) % 10;
		if (counterPos[2] < 9f) {
			counterPos[1] = (float) Math.floor(counterPos[1]);
		} else {
			counterPos[1] = (float) Math.floor(counterPos[1]) + counterReal % 1;
		}
		if ((counterReal % 100) < 99f) {
			counterPos[0] = (float) Math.floor(counterPos[0]);
		} else {
			counterPos[0] = (float) Math.floor(counterPos[0]) + counterReal % 1;
		}

		for (int c = 0; c < 3; c++) {
			float cpy = counterPos[c] * 10;
			if (cpy < 0) cpy += 100;
			this.drawTexturedModalRectFloatY(this.xCenter + 98 + (c * 7), this.yCenter + 38, 248, cpy, 8, 9);
		}

		GlStateManager.enableBlend();
		this.drawTexturedModalRect(this.xCenter + 98, this.yCenter + 34, 98, 34, 22, 16);
		GlStateManager.disableBlend();

		// Draw label
		String label = getLabel();
		int labelColor = 0xFFFFFF;

		if (isRecordingFromFile(true)) {
			label = tapeRecord.getStatusBar();
			labelColor = 0x90E0B0;
		} else {
			if (label == null) {
				label = I18n.translateToLocal("tooltip.charset.tape.none");
				labelColor = 0xFF3333;
			}
			int width = fontRendererObj.getStringWidth(label);
			if (width > 142) {
				while (width > (144 - 8) && label.length() > 4) {
					label = label.substring(0, label.length() - 1);
					width = fontRendererObj.getStringWidth(label);
				}
				label += "...";
			}
		}
		this.drawCenteredString(this.fontRendererObj, label, this.xCenter + 88, this.yCenter + 15, labelColor);
	}
}
