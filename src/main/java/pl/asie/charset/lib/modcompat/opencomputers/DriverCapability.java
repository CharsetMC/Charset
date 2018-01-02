package pl.asie.charset.lib.modcompat.opencomputers;

import li.cil.oc.api.Driver;
import li.cil.oc.api.driver.DriverBlock;
import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.ManagedEnvironment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.lib.capability.CapabilityHelper;

import java.util.function.Function;
import java.util.function.Supplier;

public class DriverCapability<T> implements DriverBlock {
	private final Capability<T> capability;
	private final Function<T, ManagedEnvironment> supplier;

	public DriverCapability(Capability<T> capability, Function<T, ManagedEnvironment> supplier) {
		this.capability = capability;
		this.supplier = supplier;
	}

	protected void register() {
		if (capability != null) {
			Driver.add(this);
		}
	}

	@Override
	public boolean worksWith(World world, BlockPos blockPos, EnumFacing enumFacing) {
		return CapabilityHelper.has(world, blockPos, capability, enumFacing.getOpposite(),
				true, true, false);
	}

	@Override
	public ManagedEnvironment createEnvironment(World world, BlockPos blockPos, EnumFacing enumFacing) {
		T object = CapabilityHelper.get(world, blockPos, capability, enumFacing.getOpposite(),
				true, true, false);
		if (object != null) {
			return supplier.apply(object);
		} else {
			return null;
		}
	}
}
