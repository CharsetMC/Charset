package pl.asie.charset.lib.material;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.ArrayUtils;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.RenderUtils;

public class ColorLookupHandler {
    public static final ColorLookupHandler INSTANCE = new ColorLookupHandler();
    private final TObjectIntMap<Key> COLOR_MAP = new TObjectIntHashMap<>();

    private ColorLookupHandler() {

    }

    public static class Key {
        public final RenderUtils.AveragingMode averagingMode;
        public final ItemStack stack;
        private final int hash;

        public Key(ItemStack stack, RenderUtils.AveragingMode mode) {
            this.stack = stack;
            this.averagingMode = mode;
            this.hash = Item.getIdFromItem(stack.getItem()) * 21 + stack.getMetadata() * 3 + mode.ordinal();
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Key)) {
                return false;
            }

            Key k = (Key) o;
            if (k.averagingMode != averagingMode || k.stack.getItem() != stack.getItem() || k.stack.getMetadata() != stack.getMetadata()) {
                return false;
            }

            if (stack.hasTagCompound() && k.stack.hasTagCompound()) {
                if (!stack.getTagCompound().equals(k.stack.getTagCompound())) {
                    return false;
                }
            }

            return true;
        }
    }

    public void clear() {
        COLOR_MAP.clear();
    }

    public int getDefaultColor(ItemStack stack) {
        int[] oreIDs = OreDictionary.getOreIDs(stack);
        if (ArrayUtils.contains(oreIDs, OreDictionary.getOreID("logWood"))) {
            return 0xff735e39;
        } else {
            IBlockState state = ItemUtils.getBlockState(stack);
            return state.getMaterial().getMaterialMapColor().colorValue | 0xFF000000;
        }
    }

    public int getColor(ItemStack stack, RenderUtils.AveragingMode mode) {
        Key key = new Key(stack, mode);
        if (!COLOR_MAP.containsKey(key)) {
            TextureAtlasSprite sprite = RenderUtils.getItemSprite(stack);
            if (sprite.getIconName().endsWith("missingno")) {
                COLOR_MAP.put(key, getDefaultColor(stack));
            } else {
                COLOR_MAP.put(key, RenderUtils.getAverageColor(sprite, mode));
            }
        }
        return COLOR_MAP.get(key);
    }
}
