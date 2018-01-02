package pl.asie.charset.lib.modcompat.opencomputers;

import li.cil.oc.api.Network;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import net.minecraft.nbt.NBTTagCompound;

public class CharsetManagedEnvironment extends AbstractManagedEnvironment implements NamedBlock {
	private final String name;
	private final int priority;

	public CharsetManagedEnvironment(String name, Visibility visibility, int priority) {
		this.name = name;
		this.priority = priority;
		this.setNode(Network.newNode(this, visibility).withComponent(name, visibility).create());
	}

	@Override
	public String preferredName() {
		return name;
	}

	@Override
	public int priority() {
		return priority;
	}
}
