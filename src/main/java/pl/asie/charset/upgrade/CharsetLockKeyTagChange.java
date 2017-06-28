package pl.asie.charset.upgrade;

import net.minecraft.item.Item;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.IFixableData;
import pl.asie.charset.module.storage.locks.ItemLockingDyeable;

import java.util.Set;

public class CharsetLockKeyTagChange implements IFixableData {
    @Override
    public int getFixVersion() {
        return 2;
    }

    @Override
    public NBTTagCompound fixTagCompound(NBTTagCompound compound) {
        if (compound.hasKey("id", 8) && compound.hasKey("tag", 10)) {
            Item item = Item.getByNameOrId(compound.getString("id"));
            if (item instanceof ItemLockingDyeable) {
                NBTTagCompound tag = compound.getCompoundTag("tag");
                if (tag.hasKey("color0")) {
                    tag.removeTag("color0");
                }

                if (tag.hasKey("color1")) {
                    NBTBase colorTag = tag.getTag("color1").copy();
                    tag.setTag("color", colorTag);
                    tag.removeTag("color1");
                }
            }
        }

        return compound;
    }
}
