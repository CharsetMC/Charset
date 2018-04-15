package pl.asie.charset.module.power.steam;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerConcatenate;
import net.minecraftforge.fluids.capability.templates.FluidHandlerFluidMap;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.module.power.steam.api.IMirror;
import pl.asie.charset.module.power.steam.api.IMirrorTarget;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class TileWaterBoiler extends TileBase implements IMirrorTarget, ITickable {
	private final Set<IMirror> mirrors = new HashSet<>();

	private final FluidTank waterTank = new FluidTank(2000);
	private final FluidTank steamTank = new FluidTank(1000);
	private final FluidHandlerFluidMap fluidHandler = new FluidHandlerFluidMap();

	public TileWaterBoiler() {
		fluidHandler.addHandler(FluidRegistry.WATER, waterTank);
		fluidHandler.addHandler(FluidRegistry.getFluid("steam"), steamTank);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return true;
		} else if (capability == CharsetPowerSteam.MIRROR_TARGET) {
			return true;
		}

		return super.hasCapability(capability, facing);
	}


	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			if (facing == null) {
				return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler);
			} else if (facing == EnumFacing.UP) {
				return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(steamTank);
			} else {
				return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(waterTank);
			}
		} else if (capability == CharsetPowerSteam.MIRROR_TARGET) {
			return CharsetPowerSteam.MIRROR_TARGET.cast(this);
		}

		return super.getCapability(capability, facing);
	}

	@Override
	public void update() {
		super.update();

		int mirrorStrength = mirrors.stream().filter(IMirror::isMirrorActive).map(IMirror::getMirrorStrength).reduce(0, (a, b) -> a + b);
		if (mirrorStrength > 0) {
			// TODO
		}
	}

	@Override
	public void invalidate(InvalidationType type) {
		super.invalidate(type);
		if (type == InvalidationType.REMOVAL) {
			mirrors.forEach(IMirror::requestMirrorTargetRefresh);
		}
	}

	@Override
	public void registerMirror(IMirror mirror) {
		mirrors.add(mirror);
	}

	@Override
	public void unregisterMirror(IMirror mirror) {
		mirrors.remove(mirror);
	}
}
