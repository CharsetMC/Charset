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

package pl.asie.charset.lib.utils.colorspace;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;

import java.util.*;
import java.util.function.Function;

public class Colorspaces {
	private static boolean initialized = false;
	private static Table<Colorspace, Colorspace, Function<float[], float[]>> conversionTable;

	private static void buildConversionTable(ValueGraph<Colorspace, Function<float[], float[]>> conversionGraph) {
		for (Colorspace from : Colorspace.values()) {
			int[] distances = new int[Colorspace.values().length];
			Colorspace[] predecessors = new Colorspace[distances.length];

			PriorityQueue<Colorspace> nodesToTraverse = new PriorityQueue<>(distances.length, Comparator.comparingInt(colorspace -> distances[colorspace.ordinal()]));

			for (Colorspace c : Colorspace.values()) {
				if (c != from) {
					distances[c.ordinal()] = Integer.MAX_VALUE;
				} else {
					distances[c.ordinal()] = 0;
				}

				nodesToTraverse.add(c);
			}

			while (!nodesToTraverse.isEmpty()) {
				Colorspace c = nodesToTraverse.poll();
				for (Colorspace other : conversionGraph.successors(c)) {
					if ((distances[other.ordinal()] - 1) > distances[c.ordinal()]) {
						distances[other.ordinal()] = distances[c.ordinal()] + 1;
						predecessors[other.ordinal()] = c;
					}
				}
			}

			// System.out.println(Arrays.toString(distances));

			for (Colorspace to : Colorspace.values()) {
				if (from == to || predecessors[to.ordinal()] == null) continue;

				Colorspace[] path = new Colorspace[distances[to.ordinal()] + 1];
				Colorspace current = to;
				for (int i = path.length - 1; i >= 0; i--) {
					path[i] = current;
					current = predecessors[current.ordinal()];
				}

				Function<float[], float[]> function = null;

				for (int i = 0; i < path.length - 1; i++) {
					if (function == null) {
						function = conversionGraph.edgeValue(path[i], path[i + 1]);
					} else {
						function = conversionGraph.edgeValue(path[i], path[i + 1]).compose(function);
					}
				}

				if (function != null) {
					conversionTable.put(from, to, function);
				}
			}
		}
	}

	private static int asFF(float f) {
		if (f >= 1.0f) return 255;
		else if (f <= 0.0f) return 0;
		else return (Math.round(f * 255.0f) & 0xFF);
	}

	public static float[] convert(float[] data, Colorspace from, Colorspace to) {
		if (from == to) {
			return data;
		} else {
			Function<float[], float[]> converter = conversionTable.get(from, to);
			if (converter != null) {
				return converter.apply(data);
			} else {
				throw new RuntimeException("Could not convert from colorspace " + from + " to " + to + "!");
			}
		}
	}

	public static int convertToRGB(float[] data, Colorspace from) {
		float[] v = convert(data, from, Colorspace.sRGB);
		return (asFF(v[0]) << 16) | (asFF(v[1]) << 8) | asFF(v[2]);
	}

	public static float[] convertFromRGB(int v, Colorspace to) {
		float[] data = new float[] {
				((v >> 16) & 0xFF) / 255.0f,
				((v >> 8) & 0xFF) / 255.0f,
				(v & 0xFF) / 255.0f
		};
		return convert(data, Colorspace.sRGB, to);
	}

	public static double getColorDistance(float[] c1, float[] c2) {
		return Math.sqrt(getColorDistanceSq(c1, c2));
	}

	public static double getColorDistance(int c1, int c2, Colorspace space) {
		return Math.sqrt(getColorDistanceSq(c1, c2, space));
	}

	public static double getColorDistanceSq(float[] f1, float[] f2) {
		return (f1[0] - f2[0]) * (f1[0] - f2[0]) +
				(f1[1] - f2[1]) * (f1[1] - f2[1]) +
				(f1[2] - f2[2]) * (f1[2] - f2[2]);
	}

	public static double getColorDistanceSq(int c1, int c2, Colorspace space) {
		float[] f1 = convertFromRGB(c1, space);
		float[] f2 = convertFromRGB(c2, space);

		return (f1[0] - f2[0]) * (f1[0] - f2[0]) +
				(f1[1] - f2[1]) * (f1[1] - f2[1]) +
				(f1[2] - f2[2]) * (f1[2] - f2[2]);
	}

	public static void init() {
		if (!initialized) {
			initialized = true;
			MutableValueGraph<Colorspace, Function<float[], float[]>> conversionGraph = ValueGraphBuilder.directed().allowsSelfLoops(false).build();
			for (Colorspace c : Colorspace.values()) {
				conversionGraph.addNode(c);
			}

			conversionGraph.putEdgeValue(Colorspace.sRGB, Colorspace.XYZ, ColorspaceFunctions::sRGBtoXYZ);
			conversionGraph.putEdgeValue(Colorspace.XYZ, Colorspace.sRGB, ColorspaceFunctions::XYZtosRGB);

			conversionGraph.putEdgeValue(Colorspace.XYZ, Colorspace.LAB, ColorspaceFunctions::XYZtoLAB);
			conversionGraph.putEdgeValue(Colorspace.LAB, Colorspace.XYZ, ColorspaceFunctions::LABtoXYZ);

			conversionGraph.putEdgeValue(Colorspace.sRGB, Colorspace.YUV, ColorspaceFunctions::sRGBtoYUV);
			conversionGraph.putEdgeValue(Colorspace.YUV, Colorspace.sRGB, ColorspaceFunctions::YUVtosRGB);

			conversionGraph.putEdgeValue(Colorspace.sRGB, Colorspace.YIQ, ColorspaceFunctions::sRGBtoYIQ);

			conversionTable = Tables.newCustomTable(new EnumMap<>(Colorspace.class), () -> new EnumMap<>(Colorspace.class));
			buildConversionTable(conversionGraph);
		}
	}

	public static void main(String[] args) {
		init();
	}
}
