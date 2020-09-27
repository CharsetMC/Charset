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

package pl.asie.charset.module.power.steam;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import pl.asie.charset.lib.utils.MathUtils;
import pl.asie.charset.lib.utils.RayTraceUtils;
import pl.asie.charset.module.power.steam.api.ISteamParticle;

public class SteamParticle implements ISteamParticle {
	protected final World world;

	protected double x, y, z;
	protected double xMotion, yMotion, zMotion;
	protected int lifetime, value;
	private boolean invalid;

	public SteamParticle(World world) {
		this.world = world;
	}

	public SteamParticle(World world, double x, double y, double z, double xM, double yM, double zM, int lifetime, int value) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.xMotion = xM;
		this.yMotion = yM;
		this.zMotion = zM;
		this.lifetime = lifetime;
		this.value = value;
	}

	@Override
	public World getWorld() {
		return world;
	}

	@Override
	public Vec3d getPosition(float partialTicks) {
		return MathUtils.interpolate(new Vec3d(x, y, z), new Vec3d(x + xMotion, y + yMotion, z + zMotion), partialTicks);
	}

	@Override
	public int getValue() {
		return value;
	}

	public boolean isInvalid() {
		return invalid;
	}

	@Override
	public void update() {
		if (invalid || lifetime <= 0) {
			invalid = true;
			return;
		} else {
			lifetime--;
		}

		double newX = x + xMotion;
		double newY = y + yMotion;
		double newZ = z + zMotion;

		RayTraceUtils.Result result = RayTraceUtils.getCollision(world, new Vec3d(x, y, z), new Vec3d(newX, newY, newZ), (a) -> false);
		if (!result.valid()) {
			x = newX;
			y = newY;
			z = newZ;
		} else {
			BlockPos pos = result.hit.getBlockPos();
			IFluidHandler handler = FluidUtil.getFluidHandler(world, pos, result.hit.sideHit);
			if (handler != null) {
				handler.fill(new FluidStack(FluidRegistry.getFluid("steam"), value), true);
			}

			invalid = true;
		}
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setDouble("x", x);
		compound.setDouble("y", y);
		compound.setDouble("z", z);
		compound.setFloat("xm", (float) xMotion);
		compound.setFloat("ym", (float) yMotion);
		compound.setFloat("zm", (float) zMotion);
		compound.setInteger("life", lifetime);
		return compound;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		x = nbt.getDouble("x");
		y = nbt.getDouble("y");
		z = nbt.getDouble("z");
		xMotion = nbt.getDouble("xM");
		yMotion = nbt.getDouble("yM");
		zMotion = nbt.getDouble("zM");
		lifetime = nbt.getInteger("life");
	}
}
