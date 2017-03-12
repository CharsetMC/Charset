package pl.asie.charset.lib.utils;

public final class ColorspaceUtils {
	private ColorspaceUtils() {

	}

	public static float[] getYIQ(int c1) {
		float r1 = ((c1 >> 16) & 0xFF) / 255.0f;
		float g1 = ((c1 >> 8) & 0xFF) / 255.0f;
		float b1 = (c1 & 0xFF) / 255.0f;

		// YUV
		/* float y1 = 0.299f * r1 + 0.587f * g1 + 0.114f * b1;
		float u1 = -0.147f * r1 - 0.289f * g1 + 0.436f * b1;
		float v1 = 0.615f * r1 - 0.515f * g1 - 0.100f * b1; */

		// YIQ
		float y1 = 0.299f * r1 + 0.587f * g1 + 0.114f * b1;
		float u1 = 0.596f * r1 - 0.274f * g1 - 0.322f * b1;
		float v1 = 0.211f * r1 - 0.523f * g1 + 0.312f * b1;

		// weighted RGB
		/* float y1 = (float) Math.sqrt(2 + (r1 / 255)) * r1;
		float u1 = 2 * g1;
		float v1 = (float) Math.sqrt(3 - (r1 / 255)) * b1; */

		return new float[]{y1, u1, v1};
	}

	public static double getColorDistance(int c1, int c2) {
		return Math.sqrt(getColorDistanceSq(c1, c2));
	}

	public static double getColorDistanceSq(int c1, int c2) {
		float[] f1 = getYIQ(c1);
		float[] f2 = getYIQ(c2);

		return (f1[0] - f2[0]) * (f1[0] - f2[0]) +
				(f1[1] - f2[1]) * (f1[1] - f2[1]) +
				(f1[2] - f2[2]) * (f1[2] - f2[2]);
	}
}
