package pl.asie.charset.audio.tape;

import com.laszlosystems.libresample4j.Resampler;

import java.nio.FloatBuffer;

public final class TapeResampler {
    private static final Resampler RESAMPLER = new Resampler(true, 0.01, 100);

    private TapeResampler() {

    }

    private static float clamp(float in, float min, float max) {
        return in < min ? min : (in > max ? max : in);
    }

    public static byte[] toSigned8(byte[] data, int sampleSizeBits, int channels, boolean bigEndian, boolean signed, int freqSrc, int freqDst,
                                   boolean normalize) {
        if (freqSrc == freqDst && sampleSizeBits == 8 && channels == 1) {
            if (!signed) {
                byte[] data2 = new byte[data.length];
                for (int i = 0; i < data.length; i++) {
                    data2[i] = (byte) (data[i] ^ 0x80);
                }
                return data2;
            } else {
                return data.clone();
            }
        }

        float[] output = new float[data.length / channels / (sampleSizeBits / 8)];
        int si = 0;

        float min = 0.0f;
        float max = 0.0f;

        for(int i = 0; i < output.length; i++) {
            int v = 0;

            for(int j = 0; j < channels; j++) {
                int s = 0;
                if (sampleSizeBits >= 32) {
                    si++;
                }

                if (sampleSizeBits >= 24) {
                    int l = 0xFF&(int)data[si++];
                    int m = 0xFF&(int)data[si++];
                    int h = 0xFF&(int)data[si++];
                    s = bigEndian ? ((l << 16) | (m << 8) | h) : (l | (m << 8) | (h << 16));
                    s &= 0xFFFFFF;
                } else if (sampleSizeBits >= 16) {
                    int l = 0xFF&(int)data[si++];
                    int h = 0xFF&(int)data[si++];
                    s = bigEndian ? ((l << 8) | h) : (l | (h<<8));
                    s &= 0xFFFF;
                    s <<= 8;
                } else {
                    s = data[si++];
                    s &= 0xFF;
                    s <<= 16;
                }

                if (signed) {
                    s = (s >= 0x800000 ? s - 0x1000000 : s);
                } else {
                    s -= 0x800000;
                }

                v += s;
            }

            v = (v*2+channels)/(channels*2);
            output[i] = clamp((float) v / 0x800000, -1.0f, 1.0f);
            if (output[i] < min) {
                min = output[i];
            }
            if (output[i] > max) {
                max = output[i];
            }
        }

        float multiplier = min != 0.0f || max != 0.0f ? 1.0f / Math.max(0 - min, max) : 1.0f;

        double factor = (double) freqDst / (double) freqSrc;
        FloatBuffer resampledBuffer = FloatBuffer.allocate((int) Math.ceil(output.length * factor) + 1024);
        RESAMPLER.process(factor, FloatBuffer.wrap(output), true, resampledBuffer);
        float[] resampledOutput = resampledBuffer.array();
        byte[] preEncodeOutput = new byte[resampledBuffer.position()];

        if (normalize) {
            for (int i = 0; i < preEncodeOutput.length; i++) {
                preEncodeOutput[i] = (byte) (clamp(resampledOutput[i] * multiplier, -1.0f, 1.0f) * 127);
            }
        }

        return preEncodeOutput;
    }
}
