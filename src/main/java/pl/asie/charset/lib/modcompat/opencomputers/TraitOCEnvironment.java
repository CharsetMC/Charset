package pl.asie.charset.lib.modcompat.opencomputers;

import li.cil.oc.api.Network;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.Constants;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.lib.block.Trait;

import javax.annotation.Nullable;

public abstract class TraitOCEnvironment extends Trait implements Environment {
	@CapabilityInject(Environment.class)
	private static Capability<Environment> CAPABILITY;

	protected final TileEntity tile;
	protected Node node;

	public TraitOCEnvironment(TileEntity tile, Visibility visibility, String componentName) {
		this.tile = tile;
		this.node = Network.newNode(this, visibility)
				.withComponent(componentName, visibility)
				.withConnector()
				.create();
	}

	@Override
	public void onLoad() {
		Network.joinOrCreateNetwork(tile);
	}

	@Override
	public void onInvalidate(TileBase.InvalidationType type) {
		if (node != null) {
			node.remove();
		}
	}

	@Override
	public Node node() {
		return node;
	}

	@Override
	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		if (!isClient && node != null && compound.hasKey("node", Constants.NBT.TAG_COMPOUND)) {
			node.load(compound.getCompoundTag("node"));
		}
	}

	@Override
	public NBTTagCompound writeNBTData(boolean isClient) {
		NBTTagCompound nbt = new NBTTagCompound();
		if (node != null) {
			NBTTagCompound nodeNbt = new NBTTagCompound();
			node.save(nodeNbt);
			nbt.setTag("node", nodeNbt);
		}
		return nbt;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		return capability == CAPABILITY;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		return capability == CAPABILITY ? CAPABILITY.cast(this) : null;
	}
}
