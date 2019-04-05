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

package pl.asie.charset.module.world;

import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.EnumDyeColor;
import pl.asie.charset.lib.utils.ColorUtils;
import pl.asie.charset.lib.utils.RenderUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class TestWools {
	private float[] colorMultiplier(String prefix, EnumDyeColor color) {
		float[] dOrig = color.getColorComponentValues();
		float[] d = Arrays.copyOf(dOrig, 3);

		if (color == EnumDyeColor.BLUE) {
			d[0] *= 0.925F;
			d[1] *= 0.925F;
			d[2] *= 0.875F;
		} else if (color == EnumDyeColor.ORANGE) {
			d[0] *= 1.075F;
			d[1] *= 1.075F;
		} else if (color == EnumDyeColor.YELLOW) {
			d[0] *= 1.10F;
			d[1] *= 0.95F;
			d[2] *= 0.95F;
		} else if (color == EnumDyeColor.MAGENTA) {
			d[0] *= 1.1F;
			d[1] *= 1.05F;
			d[2] *= 1.1F;
		} else if (color == EnumDyeColor.LIGHT_BLUE) {
			d[0] *= 1.05F;
			d[1] *= 1.05F;
			d[2] *= 1.05F;
		} else if (color == EnumDyeColor.PINK) {
			d[0] *= 1.025F;
			d[1] *= 1.075F;
			d[2] *= 1.025F;
		} else if (color == EnumDyeColor.CYAN) {
			d[0] *= 0.9F;
			d[1] *= 0.95F;
			d[2] *= 1.05F;
		} else if (color == EnumDyeColor.PURPLE) {
			d[0] *= 1F;
			d[1] *= 1.075F;
			d[2] *= 1F;
		} else if (color == EnumDyeColor.BROWN) {
			d[0] *= 1.0F;
			d[1] *= 0.925F;
			d[2] *= 1.0F;
		} else if (color == EnumDyeColor.BLACK) {
			d[0] *= 1.33F;
			d[1] *= 1.33F;
			d[2] *= 1.33F;
		} else if (color == EnumDyeColor.GRAY) {
			d[0] *= 1.125F;
			d[1] *= 1.125F;
			d[2] *= 1.125F;
		}

		if (prefix.contains("hardened_clay")) {
			float lum = d[0] * 0.3F + d[1] * 0.59F + d[2] * 0.11F;
			float mul = (color == EnumDyeColor.YELLOW || color == EnumDyeColor.ORANGE || color == EnumDyeColor.RED) ? 0.6f : 0.7f;
			d[0] *= 0.9F;
			d[1] *= 0.9F;
			d[2] *= 0.9F;
			d[0] += (lum - d[0]) * mul;
			d[1] += (lum - d[1]) * mul;
			d[2] += (lum - d[2]) * mul;
		}

		return d;
	}

	private int toIntColor(float[] d) {
		return    (Math.min(Math.round(d[0] * 255.0F), 255) << 16)
				| (Math.min(Math.round(d[1] * 255.0F), 255) << 8)
				| (Math.min(Math.round(d[2] * 255.0F), 255))
				| 0xFF000000;
	}

	public void update() throws Exception {
		System.out.println("{");
		for (int i = 0; i < 16; i++) {
			float[] color1 = colorMultiplier("", EnumDyeColor.byMetadata(i));
			String name = ColorUtils.getUnderscoredSuffix(EnumDyeColor.byMetadata(i));
			System.out.println("\t\"" + name + "\": [" + color1[0] + ", " + color1[1] + ", " + color1[2] + "],");
		}
		System.out.println("}");

		BufferedImage img = canvas.img;
		BufferedImage wool = ImageIO.read(getClass().getClassLoader().getResourceAsStream("assets/minecraft/textures/blocks/wool_colored_white.png"));
		for (int wc2 = 0; wc2 < 16; wc2++) {
			for (int wc1 = wc2; wc1 < 16; wc1++) {
				float[] color1 = colorMultiplier("", EnumDyeColor.byMetadata(wc1));
				float[] color2 = colorMultiplier("", EnumDyeColor.byMetadata(wc2));
				float[] color = new float[3];
				for (int i = 0; i < 3; i++)
					color[i] = (color1[i] + color2[i]) / 2;
				int colorTarget = toIntColor(color);
				for (int iy = 0; iy < 16; iy++) {
					for (int ix = 0; ix < 16; ix++) {
						img.setRGB(wc1*16 + ix, wc2*16 + iy, RenderUtils.multiplyColor(wool.getRGB(ix, iy), colorTarget));
					}
				}
			}
		}
	}

	public static class Canvas extends JComponent {
		public final BufferedImage img;
		public final Dimension size = new Dimension(256, 256);
		public final float multiplier = 2.0f;
		public final Dimension renderSize = multiply(size, multiplier);

		private static Dimension multiply(Dimension size, float f) {
			return f == 1.0f ? size : new Dimension(Math.round(size.width * f), Math.round(size.height * f));
		}

		public Canvas() {
			setSize(renderSize);
			setPreferredSize(renderSize);
			img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
		}

		@Override
		public void paintComponent(Graphics g) {
			if (multiplier != 1.0f) {
				Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g2.drawRenderedImage(img, AffineTransform.getScaleInstance(multiplier, multiplier));
			} else {
				g.drawImage(img, 0, 0, null);
			}
		}
	}

	private JFrame window;
	private Canvas canvas;

	public TestWools() {
		window = new JFrame(getClass().getName());
		window.add(canvas = new Canvas());
		window.pack();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);

		window.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent mouseEvent) {

			}

			@Override
			public void mousePressed(MouseEvent mouseEvent) {
				try {
					update();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				canvas.repaint();
			}

			@Override
			public void mouseReleased(MouseEvent mouseEvent) {

			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent) {

			}

			@Override
			public void mouseExited(MouseEvent mouseEvent) {

			}
		});

		try {
			update();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		TestWools tester = new TestWools();
	}
}
