package pl.asie.charset.api.carry;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public interface ICarryHandler {
    IBlockState getState();
    NBTTagCompound getTileNBT();
    TileEntity getTile();
    Entity getCarrier();
}
