/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.charset.lib.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FontRendererFancy extends FontRenderer {
    private static final Pattern FANCY_COLOR = Pattern.compile("\ue51a");
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
        int origColor = color;
        int lastPos = 0;
        while (matcher.find()) {
            String subText = text.substring(lastPos, matcher.start());
            x = parent.drawString(subText, x, y, color, dropShadow);
            switch (text.charAt(matcher.start() + 1)) {
                case 'C':
                    color = 0xFF000000 | Integer.parseInt(text.substring(matcher.start() + 2, matcher.start() + 8), 16);
                    lastPos = matcher.start() + 8;
                    break;
                case 'R':
                    color = origColor;
                    lastPos = matcher.start() + 2;
                    break;
            }
            if (dropShadow) x -= 1;
        }
        return parent.drawString(text.substring(lastPos), x, y, color, dropShadow);
    }

    public static String getColorFormat(int color) {
        return "\ue51aC" + String.format("%06X", color & 0xFFFFFF);
    }

    public static String getColorResetFormat() {
        return "\ue51aR";
    }

    @Override
    public int getStringWidth(String text) {
        text = text.replaceAll("\ue51aC[0-9a-fA-F]{6}", "");
        text = text.replaceAll("\ue51aR", "");
        return parent.getStringWidth(text);
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

