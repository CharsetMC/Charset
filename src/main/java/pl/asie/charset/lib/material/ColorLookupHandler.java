package pl.asie.charset.lib.material;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.RenderUtils;

import java.util.concurrent.TimeUnit;

public class ColorLookupHandler {
    public static final ColorLookupHandler INSTANCE = new ColorLookupHandler();
    private final Cache<Key, Integer> COLOR_MAP = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build();
    private final TIntIntMap DEFAULT_COLOR_MAP = new TIntIntHashMap();

    private ColorLookupHandler() {
        DEFAULT_COLOR_MAP.put(OreDictionary.getOreID("logWood"), 0xff735e39);
    }

    public static final class Key {
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
            if (k.averagingMode != averagingMode || !ItemUtils.equals(stack, k.stack, false, k.stack.getHasSubtypes(), true)) {
                return false;
            }

            return true;
        }
    }

    public void clear() {
        COLOR_MAP.invalidateAll();
    }

    public int getDefaultColor(ItemStack stack) {
        int[] oreIDs = OreDictionary.getOreIDs(stack);
        for (int o : oreIDs) {
            if (DEFAULT_COLOR_MAP.containsKey(o))
                return DEFAULT_COLOR_MAP.get(o);
        }

        IBlockState state = ItemUtils.getBlockState(stack);
        return state.getMaterial().getMaterialMapColor().colorValue | 0xFF000000;
    }

    public int getColor(ItemStack stack, RenderUtils.AveragingMode mode) {
        Key key = new Key(stack, mode);
        Integer result = COLOR_MAP.getIfPresent(key);
        if (result == null) {
            TextureAtlasSprite sprite = RenderUtils.getItemSprite(stack);
            int out;
            if (sprite.getIconName().endsWith("missingno")) {
                out = getDefaultColor(stack);
            } else {
                out = RenderUtils.getAverageColor(sprite, mode);
                int tintColor = Minecraft.getMinecraft().getItemColors().getColorFromItemstack(stack, 0);
                if (tintColor != -1) {
                    out = RenderUtils.multiplyColor(out, tintColor);
                }
            }
            COLOR_MAP.put(key, out);
            return out;
        } else {
            return result;
        }
    }
}
