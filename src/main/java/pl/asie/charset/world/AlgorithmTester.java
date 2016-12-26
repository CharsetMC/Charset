package pl.asie.charset.world;

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
		return COLOR_GRASS;
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

	public void update() {
		init();

		BufferedImage img = canvas.img;
		for (int y = 0; y < img.getHeight(); y++) {
			int ry = img.getHeight() - 1 - y;
			for (int x = 0; x < img.getWidth(); x++) {
				img.setRGB(x, y, calc(x, ry, COORD_Z));
			}
		}
	}

	public static void main(String[] args) {
		AlgorithmTester tester = new AlgorithmTester();
	}
}
