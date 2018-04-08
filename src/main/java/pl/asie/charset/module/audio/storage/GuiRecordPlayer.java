/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
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

package pl.asie.charset.module.audio.storage;

import java.io.IOException;
import java.util.Arrays;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.translation.I18n;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.lwjgl.opengl.Display;
import pl.asie.charset.api.tape.IDataStorage;
import pl.asie.charset.lib.ui.GuiContainerCharset;

public class GuiRecordPlayer extends GuiContainerCharset {
	public class DialogThread implements Runnable {
		public JFileChooser chooser = new JFileChooser();
		public int result;

		@Override
		public void run() {
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setDialogType(JFileChooser.OPEN_DIALOG);
			chooser.setFileFilter(new FileNameExtensionFilter("Audio file " + Arrays.toString(AudioRecordThread.getSupportedExtensions()), AudioRecordThread.getSupportedExtensions()));
			result = chooser.showOpenDialog(Display.getParent());
		}
	}

	private static final ResourceLocation TEXTURE = new ResourceLocation("charset:textures/gui/recordplayergui.png");
	private static final int BUTTON_START_X = 88 - (Button.values().length * 10);
	private static final int BUTTON_START_Y = 58;

	private TraitRecordPlayer.State state = TraitRecordPlayer.State.STOPPED;
	public enum Button {
		PLAY,
		PAUSE,
		STOP,
		RECORD_FILE,
		RECORD_AUDIO
	}
	private TileRecordPlayer owner;
	private Button buttonHovering = null;
	private DialogThread fileDialog;
	private AudioRecordThread record;
	private Thread recordThread, fileDialogThread;

	public boolean isRecordingFromFile(boolean inProgress) {
		if (!inProgress && fileDialogThread != null && fileDialogThread.isAlive()) {
			return true;
		}

		return recordThread != null && recordThread.isAlive();
	}

	public GuiRecordPlayer(Container container) {
		super(container, 176, 166);
		this.owner = ((ContainerRecordPlayer) container).owner;
	}

	public boolean isButtonPressed(Button button) {
		if (button == buttonHovering) {
			return true;
		}

		if (isRecordingFromFile(false) && button == Button.RECORD_FILE) {
			return true;
		}

		switch (state) {
			case PLAYING:
				return button == Button.PLAY;
			case RECORDING:
				return button == Button.RECORD_AUDIO;
			case PAUSED:
				return button == Button.PAUSE;
			case STOPPED:
			default:
				return false;
		}
	}

	public void setState(TraitRecordPlayer.State state) {
		if (owner != null) {
			try {
				PacketDriveState packet = new PacketDriveState(owner, state);
				CharsetAudioStorage.packet.sendToServer(packet);
				owner.setState(state);
			} catch (Exception e) {
				// no-op
			}
		}
	}

	public void handleButtonPress(Button button) {
		if (isRecordingFromFile(false)) {
			return;
		}

		switch(button) {
			case PLAY:
				setState(TraitRecordPlayer.State.PLAYING);
				break;
			case PAUSE:
				setState(TraitRecordPlayer.State.PAUSED);
				break;
			case STOP:
				setState(TraitRecordPlayer.State.STOPPED);
				break;
			case RECORD_FILE:
				if (owner.getStack() != null) {
					if (owner.getState() == TraitRecordPlayer.State.PLAYING || owner.getState() == TraitRecordPlayer.State.RECORDING) {
						setState(TraitRecordPlayer.State.PAUSED);
					}
					if (Minecraft.getMinecraft().isFullScreen()) {
						Minecraft.getMinecraft().toggleFullscreen();
					}

					fileDialog = new DialogThread();
					fileDialogThread = new Thread(fileDialog);
					fileDialogThread.start();
				}
				break;
			case RECORD_AUDIO:
				setState(TraitRecordPlayer.State.RECORDING);
				break;
		}
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		this.state = owner.getState();

		if (fileDialogThread != null && !fileDialogThread.isAlive()) {
			if (fileDialog.result == JFileChooser.APPROVE_OPTION) {
				if (fileDialog.chooser.getSelectedFile() != null) {
					int rate = owner.getSampleRate();
					record = new AudioRecordThread(fileDialog.chooser.getSelectedFile(), rate, CharsetAudioStorage.quartzDisc.getSize(owner.getStack()));
					recordThread = new Thread(record);
					recordThread.start();
				}
			}
			fileDialogThread = null;
			fileDialog = null;
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
		}
	}

	@Override
	public void mouseReleased(int x, int y, int which) {
		if (isRecordingFromFile(false)) {
			return;
		}

		super.mouseReleased(x, y, which);
		if(which >= 0 && buttonHovering != null) {
			this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			handleButtonPress(buttonHovering);
			buttonHovering = null;
		}
	}

	// Uses NBT data.
	private String getLabel() {
		ItemStack stack = this.inventorySlots.getInventory().get(0);
		if (!stack.isEmpty() && stack.getItem() instanceof ItemQuartzDisc) {
			String label = I18n.translateToLocal("tooltip.charset.disc.unnamed");
			if (stack.hasDisplayName()) {
				label = stack.getDisplayName();
			}
			return label;
		} else return null;
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

		GlStateManager.enableBlend();
		this.drawTexturedModalRect(this.xCenter + 98, this.yCenter + 34, 98, 34, 22, 16);
		GlStateManager.disableBlend();

		// Draw label
		String label = getLabel();
		int labelColor = 0xFFFFFF;

		if (isRecordingFromFile(true)) {
			label = record.getStatusBar();
			labelColor = 0x90E0B0;
		} else {
			if (label == null) {
				label = I18n.translateToLocal("tooltip.charset.disc.none");
				labelColor = 0xFF3333;
			}
			int width = fontRenderer.getStringWidth(label);
			if (width > 142) {
				while (width > (144 - 8) && label.length() > 4) {
					label = label.substring(0, label.length() - 1);
					width = fontRenderer.getStringWidth(label);
				}
				label += "...";
			}
		}
		this.drawCenteredString(this.fontRenderer, label, this.xCenter + 88, this.yCenter + 15, labelColor);
	}
}
