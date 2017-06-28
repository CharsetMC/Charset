package pl.asie.charset.lib.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import pl.asie.charset.lib.utils.MethodHandleHelper;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FontRendererFancy extends FontRenderer {
    private static final Pattern FANCY_COLOR = Pattern.compile("\ue51aC[0-9a-fA-F]{6}");
    private static final Field LOCATION_FONT_TEXTURE = ReflectionHelper.findField(FontRenderer.class,
            "locationFontTexture", "field_111273_g");
    private final FontRenderer parent;

    public FontRendererFancy(FontRenderer parent) throws IllegalAccessException {
        super(Minecraft.getMinecraft().gameSettings, (ResourceLocation) LOCATION_FONT_TEXTURE.get(parent),
                Minecraft.getMinecraft().getTextureManager(), parent.getUnicodeFlag());
        this.parent = parent;
    }

    @Override
    public int drawString(String text, float x, float y, int color, boolean dropShadow) {
        Matcher matcher = FANCY_COLOR.matcher(text);
        int lastPos = 0;
        while (matcher.find()) {
            String subText = text.substring(lastPos, matcher.start());
            x = parent.drawString(subText, x, y, color, dropShadow);
            color = 0xFF000000 | Integer.parseInt(text.substring(matcher.start() + 2, matcher.start() + 8), 16);
            lastPos = matcher.start() + 8;
        }
        return parent.drawString(text.substring(lastPos), x, y, color, dropShadow);
    }

    public static String getColorFormat(int color) {
        return "\ue51aC" + String.format("%06X", color & 0xFFFFFF);
    }

    @Override
    public int getStringWidth(String text) {
        return parent.getStringWidth(text.replaceAll("\ue51aC[0-9a-fA-F]{6}", ""));
    }

    @Override
    public int getCharWidth(char character) {
        return parent.getCharWidth(character);
    }

    @Override
    public String trimStringToWidth(String text, int width, boolean reverse) {
        return parent.trimStringToWidth(text, width, reverse);
    }

    @Override
    public void drawSplitString(String str, int x, int y, int wrapWidth, int textColor) {
        parent.drawSplitString(str, x, y, wrapWidth, textColor);
    }

    @Override
    public int getWordWrappedHeight(String str, int maxLength) {
        return parent.getWordWrappedHeight(str, maxLength);
    }

    @Override
    public void setUnicodeFlag(boolean unicodeFlagIn) {
        super.setUnicodeFlag(unicodeFlagIn);
        parent.setUnicodeFlag(unicodeFlagIn);
    }

    @Override
    public void setBidiFlag(boolean bidiFlagIn) {
        super.setBidiFlag(bidiFlagIn);
        parent.setBidiFlag(bidiFlagIn);
    }

    @Override
    public List<String> listFormattedStringToWidth(String str, int wrapWidth) {
        return parent.listFormattedStringToWidth(str, wrapWidth);
    }
}

