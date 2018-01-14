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

package pl.asie.charset.module.world;

import pl.asie.charset.lib.utils.OpenSimplexNoise;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Random;

public class AlgorithmTester {
	private static final int COORD_Z = 0;
	private OpenSimplexNoise noise;

	private static final int COLOR_AIR = 0xFFffffff;
	private static final int COLOR_STONE = 0xFF999999;
	private static final int COLOR_DIRT = 0xFFaa5500;
	private static final int COLOR_GRASS = 0xFF00aa00;

	public int calc(int x, int y, int z) {
		double v = (y - 80);
		double heightBase = noise.eval(x * 0.005f, 394384848, z * 0.005f) * 64;
		double roughness = ((noise.eval(x * 0.002f, 203984, z * 0.002f) + 1) / 100) + 0.0001f;
		v += heightBase;
		v += noise.eval(x * roughness, y * 0.002f, z * roughness) * 32;
		return v < 0 ? 1 : 0;
	}

	public void update() {
		init();

		BufferedImage img = canvas.img;
		for (int x = 0; x < img.getWidth(); x++) {
			int ctr = 0;
			boolean hasWater = false;
			for (int y = 0; y < img.getHeight(); y++) {
				int ry = img.getHeight() - 1 - y;
				int out = calc(x, ry, COORD_Z);
				int color = COLOR_AIR;
				if (out == 0) {
					ctr = 0;
					if (ry < 64) {
						color = 0xFF0000aa;
						hasWater = true;
					}
				} else {
					ctr++;
					if (hasWater && ctr <= 4)
						color = 0xFFffff55;
					else if (ctr == 1)
						color = COLOR_GRASS;
					else if (ctr >= 2 && ctr <= 4)
						color = COLOR_DIRT;
					else
						color = COLOR_STONE;
				}
				img.setRGB(x, y, color);
			}
		}
	}

	private static final Random RANDOM = new Random();

	public void init() {
		noise = new OpenSimplexNoise(RANDOM.nextInt());
	}

	public static class Canvas extends JComponent {
		public final BufferedImage img;
		public final Dimension size = new Dimension(800, 256);
		public final float multiplier = 1.0f;
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

	public AlgorithmTester() {
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
				update();
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

		update();
	}

	public static void main(String[] args) {
		AlgorithmTester tester = new AlgorithmTester();
	}
}
