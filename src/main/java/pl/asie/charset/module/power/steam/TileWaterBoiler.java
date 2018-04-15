package pl.asie.charset.module.power.steam;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
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
	private int givenHeat, givenHeatClient;

	public TileWaterBoiler() {
		fluidHandler.addHandler(FluidRegistry.WATER, waterTank);
		fluidHandler.addHandler(FluidRegistry.getFluid("steam"), steamTank);

		waterTank.setCanDrain(false);
		waterTank.setCanFill(true);
		steamTank.setCanDrain(true);
		steamTank.setCanFill(false);
	}

	public float getGivenHeatClient() {
		return (float) MathHelper.clamp(Math.log10(getGivenHeat()) / 6, 0, 0.4f);
	}

	public int getGivenHeat() {
		return givenHeat;
	}

	int getReflectorStrength() {
		return mirrors.stream().filter(IMirror::isMirrorActive).map(IMirror::getMirrorStrength).reduce(0, (a, b) -> a + b);
	}

	int getHeat() {
		return Math.max(getReflectorStrength() - 3, 0);
	}

	@Override
	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		super.readNBTData(compound, isClient);
		if (!isClient) {
			if (compound.hasKey("water", Constants.NBT.TAG_COMPOUND)) {
				waterTank.readFromNBT(compound.getCompoundTag("water"));
			} else {
				waterTank.setFluid(null);
			}

			if (compound.hasKey("steam", Constants.NBT.TAG_COMPOUND)) {
				steamTank.readFromNBT(compound.getCompoundTag("steam"));
			} else {
				steamTank.setFluid(null);
			}
		}

		givenHeat = compound.getInteger("heat");
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
		compound = super.writeNBTData(compound, isClient);
		if (!isClient) {
			NBTTagCompound waterCpd = new NBTTagCompound();
			NBTTagCompound steamCpd = new NBTTagCompound();

			waterTank.writeToNBT(waterCpd);
			steamTank.writeToNBT(steamCpd);

			compound.setTag("water", waterCpd);
			compound.setTag("steam", steamCpd);
		}

		compound.setInteger("heat", isClient ? givenHeatClient : givenHeat);
		return compound;
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

		if (world.isRemote) {
			return;
		}

		long seed = world.getTotalWorldTime() + pos.toLong();
		int measureTime = 5;
		if (seed % measureTime == 0) {
			int oldGHC = givenHeatClient;
			givenHeatClient = givenHeat / measureTime;
			givenHeat = 0;
			if (oldGHC != givenHeatClient) {
				markBlockForUpdate();
			}
		}

		if (waterTank.getFluidAmount() < 1000) {
			BlockPos below = pos.down();
			FluidStack desiredAmount = new FluidStack(FluidRegistry.WATER, 1000);

			// IBlockState state = world.getBlockState(below);
			IFluidHandler handler = FluidUtil.getFluidHandler(world, below, EnumFacing.UP);

			if (handler != null) {
				// TODO: do not always actually remove water.
				FluidStack drained = handler.drain(desiredAmount, true);
				if (drained != null) {
					// no bounds-checking = 999+1000 is 1999
					waterTank.setFluid(new FluidStack(FluidRegistry.WATER, waterTank.getFluidAmount() + drained.amount));
				}
			}
		}

		int heat = getHeat();
		if (heat > 0) {
			applyHeat(heat);
		}
	}

	public void applyHeat(int heat) {
		heat *= 5;
		givenHeat += heat;

		int toBoil = Math.min(heat, waterTank.getFluidAmount());
		//toBoil = Math.min(steamTank.getCapacity() - steamTank.getFluidAmount(), toBoil);
		if (toBoil <= 0) {
			return;
		}

		int waterToSteam = 160; /* neptunepink claims CovertJaguar gives 1:160 as the water:steam ratio */
		int waterToRemove = Math.max(toBoil / waterToSteam, 1);
		if (waterToRemove > waterTank.getFluidAmount()) {
			return;
		}

		FluidStack wFluid = waterTank.getFluid();
		waterTank.setFluid(wFluid.amount == waterToRemove ? null : new FluidStack(FluidRegistry.WATER, wFluid.amount - waterToRemove));

		//steamTank.setFluid(new FluidStack(FluidRegistry.getFluid("steam"), steamTank.getFluidAmount() + toBoil));
		world.getCapability(CharsetPowerSteam.steamWorldCap, null).spawnParticle(
				new SteamParticle(
						world,
						pos.getX() + 0.25f + (world.rand.nextFloat() * 0.5f),
						pos.getY() + 0.99f,
						pos.getZ() + 0.25f + (world.rand.nextFloat() * 0.5f),
						(world.rand.nextFloat() * 0.02f) - 0.01f,
						0.05f,
						(world.rand.nextFloat() * 0.02f) - 0.01f,
						100,
						toBoil
				)
		);
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

	@Override
	public boolean hasFastRenderer() {
		return false;
	}

	@Override
	public boolean shouldRenderInPass(int pass) {
		return pass == 1;
	}
}
