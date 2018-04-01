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

package pl.asie.charset.module.decoration.stacks;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;

public class StackShapes {
	private static final Vec3d[] INGOT_POSITIONS_X, INGOT_POSITIONS_Z;
	protected static final Vec3d[][] INGOT_POSITIONS;
	protected static final Vec3d[] STACK_POSITIONS;

	static {
		double TOTAL_WIDTH = 4;
		double TOTAL_HEIGHT = 8;
		double Y_SIZE = 2;
		double TRAPEZOID_WIDTH_BOTTOM = 3.5;
		double TRAPEZOID_WIDTH_TOP = 3;
		double TRAPEZOID_HEIGHT_BOTTOM = 8;
		double TRAPEZOID_HEIGHT_TOP = 7;

		double TRAPEZOID_X_NEG_BOT = (TOTAL_WIDTH - TRAPEZOID_WIDTH_BOTTOM) / 2;
		double TRAPEZOID_Y_NEG_BOT = (TOTAL_HEIGHT - TRAPEZOID_HEIGHT_BOTTOM) / 2;
		double TRAPEZOID_X_NEG_TOP = (TOTAL_WIDTH - TRAPEZOID_WIDTH_TOP) / 2;
		double TRAPEZOID_Y_NEG_TOP = (TOTAL_HEIGHT - TRAPEZOID_HEIGHT_TOP) / 2;

		double TRAPEZOID_X_POS_BOT = TRAPEZOID_X_NEG_BOT + TRAPEZOID_WIDTH_BOTTOM;
		double TRAPEZOID_Y_POS_BOT = TRAPEZOID_Y_NEG_BOT + TRAPEZOID_HEIGHT_BOTTOM;
		double TRAPEZOID_X_POS_TOP = TRAPEZOID_X_NEG_TOP + TRAPEZOID_WIDTH_TOP;
		double TRAPEZOID_Y_POS_TOP = TRAPEZOID_Y_NEG_TOP + TRAPEZOID_HEIGHT_TOP;

		INGOT_POSITIONS_X = new Vec3d[]{
				new Vec3d(TRAPEZOID_X_NEG_BOT, 0, TRAPEZOID_Y_NEG_BOT),
				new Vec3d(TRAPEZOID_X_POS_BOT, 0, TRAPEZOID_Y_NEG_BOT),
				new Vec3d(TRAPEZOID_X_POS_BOT, 0, TRAPEZOID_Y_POS_BOT),
				new Vec3d(TRAPEZOID_X_NEG_BOT, 0, TRAPEZOID_Y_POS_BOT),
				new Vec3d(TRAPEZOID_X_NEG_TOP, Y_SIZE, TRAPEZOID_Y_NEG_TOP),
				new Vec3d(TRAPEZOID_X_POS_TOP, Y_SIZE, TRAPEZOID_Y_NEG_TOP),
				new Vec3d(TRAPEZOID_X_POS_TOP, Y_SIZE, TRAPEZOID_Y_POS_TOP),
				new Vec3d(TRAPEZOID_X_NEG_TOP, Y_SIZE, TRAPEZOID_Y_POS_TOP)
		};
		INGOT_POSITIONS_Z = new Vec3d[8];
		for (int i = 0; i < 8; i++) {
			INGOT_POSITIONS_Z[i] = new Vec3d(INGOT_POSITIONS_X[i].z, INGOT_POSITIONS_X[i].y, INGOT_POSITIONS_X[i].x);
		}

		INGOT_POSITIONS = new Vec3d[64][];
		for (int i = 0; i < 64; i++) {
			Vec3d[] base;
			int y = (i >> 2) & (~1);
			int x, z;
			int target_i = i;

			if ((y & 2) == 2) {
				if ((i & 7) >= 2 && (i & 7) <= 5) {
					// swap 2..3 with 4..5
					target_i = (i & 1) | (6 - (i & 6)) | (i & (~7));
				}
				base = INGOT_POSITIONS_Z;
				z = ((i & 1) | ((i >> 1) & 2)) * 4;
				x = (i & 2) * 4;
			} else {
				base = INGOT_POSITIONS_X;
				x = ((i & 1) | ((i >> 1) & 2)) * 4;
				z = (i & 2) * 4;
			}
			INGOT_POSITIONS[target_i] = new Vec3d[8];
			for (int j = 0; j < 8; j++) {
				INGOT_POSITIONS[target_i][j] = base[j].addVector(x, y, z);
			}
		}

		STACK_POSITIONS = new Vec3d[64];
		for (int i = 0; i < 64; i++) {
			int y = (i & 1) + ((i >> 2) & (~1));
			int x = (i & 4) * 2;
			int z = (i & 2) * 4;
			STACK_POSITIONS[i] = new Vec3d(x, y, z);
		}
	}

	public static boolean isGearPlate(ItemStack stack) {
		int[] ids = OreDictionary.getOreIDs(stack);
		for (int id : ids) {
			String name = OreDictionary.getOreName(id);
			if (name.startsWith("gear") || name.startsWith("plate")) {
				return true;
			}
		}

		return false;
	}

	public static boolean isIngot(ItemStack stack) {
		ItemMaterial material = ItemMaterialRegistry.INSTANCE.getMaterialIfPresent(stack);
		if (material == null || !(material.getTypes().contains("ingot"))) {
			return false;
		}

		return true;
	}

	public static AxisAlignedBB getIngotBox(int i, ItemStack stack) {
		if (stack != null && isIngot(stack)) {
			return new AxisAlignedBB(
					INGOT_POSITIONS[i][0].x / 16f,
					INGOT_POSITIONS[i][0].y / 16f,
					INGOT_POSITIONS[i][0].z / 16f,
					INGOT_POSITIONS[i][2].x / 16f,
					INGOT_POSITIONS[i][6].y / 16f,
					INGOT_POSITIONS[i][2].z / 16f
			);
		} else {
			return new AxisAlignedBB(
					STACK_POSITIONS[i].x / 16f,
					STACK_POSITIONS[i].y / 16f,
					STACK_POSITIONS[i].z / 16f,
					(STACK_POSITIONS[i].x + 8) / 16f,
					(STACK_POSITIONS[i].y + 1) / 16f,
					(STACK_POSITIONS[i].z + 8) / 16f
			);
		}
	}
}
