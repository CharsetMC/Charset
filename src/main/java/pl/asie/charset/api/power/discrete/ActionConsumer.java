package pl.asie.charset.api.power.discrete;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

public class ActionConsumer implements INBTSerializable<NBTTagCompound> {
    protected final World world;
    private final IActionListener listener;
    private long lastConsumedTime;

    public ActionConsumer(World world) {
        this.world = world;
        this.listener = this instanceof IActionListener ? (IActionListener) this : null;
    }

    public ActionConsumer(World world, IActionListener listener) {
        this.world = world;
        this.listener = listener;
    }

    final void receiveAction() {
        lastConsumedTime = world.getTotalWorldTime();
        if (listener != null) {
            listener.onActionReceived();
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setLong("lct", lastConsumedTime);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        if (compound.hasKey("lct")) {
            lastConsumedTime = compound.getLong("lct");
        } else {
            lastConsumedTime = 0;
        }
    }
}
