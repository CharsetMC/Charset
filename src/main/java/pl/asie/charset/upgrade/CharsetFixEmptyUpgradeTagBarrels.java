package pl.asie.charset.upgrade;

import net.minecraft.item.Item;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.datafix.IFixableData;
import net.minecraftforge.common.util.Constants;
import pl.asie.charset.module.storage.barrels.ItemDayBarrel;
import pl.asie.charset.module.storage.locks.ItemLockingDyeable;

public class CharsetFixEmptyUpgradeTagBarrels implements IFixableData {
    @Override
    public int getFixVersion() {
        return 3;
    }

    @Override
    public NBTTagCompound fixTagCompound(NBTTagCompound compound) {
        if (compound.hasKey("id", 8) && compound.hasKey("tag", 10)) {
            Item item = Item.getByNameOrId(compound.getString("id"));
            if (item instanceof ItemDayBarrel) {
                NBTTagCompound tag = compound.getCompoundTag("tag");
                if (tag.hasKey("upgrades", Constants.NBT.TAG_LIST)) {
                    NBTTagList tagList = tag.getTagList("upgrades", Constants.NBT.TAG_STRING);
                    if (tagList.hasNoTags()) {
                        tag.removeTag("upgrades");
                    }
                }
            }
        }

        return compound;
    }
}
