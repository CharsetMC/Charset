/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
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

package pl.asie.charset.module.tablet.format;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.gui.FontRenderer;
import pl.asie.charset.module.tablet.format.api.*;
import pl.asie.charset.module.tablet.format.words.WordText;

import java.net.URI;
import java.util.*;

public class ClientTypesetter extends AbstractTypesetter implements IPrintingContextMinecraft {
    public static class WordContainer {
        public final Word word;
        public final WordPrinterMinecraft<Word> printer;
        public final List<IStyle> styles;

        public WordContainer(Word word, WordPrinterMinecraft<Word> printer, List<IStyle> styles) {
            this.word = word;
            this.printer = printer;
            this.styles = ImmutableList.copyOf(styles);
        }
    }

    public class Line {
        public List<WordContainer> words;
        public int length, paddingAbove, height;

        public Line() {
            words = new ArrayList<>();
            length = 0;
            paddingAbove = 0;
            height = 0;
        }

        public boolean add(Word word, boolean force) {
            WordPrinterMinecraft<Word> printer = TabletAPIClient.INSTANCE.getPrinterMinecraft(word);
            if (printer == null) {
                return false;
            }

            int len = printer.getWidth(ClientTypesetter.this, word);
            if (!force) {
                if (printer.getDisplayType() == WordPrinterMinecraft.DisplayType.BLOCK && length > 0) {
                    return false;
                } else if (length + len > pageWidth) {
                    return false;
                }
            }

            if (length == 0 && word instanceof WordText && ((WordText) word).getText().equals(" ")) {
                return true;
            }

            // System.out.println("@" + words.size() + " " + word);
            words.add(new WordContainer(word, printer, styles));
            length += len;
            height = Math.max(height, printer.getHeight(ClientTypesetter.this, word));
            paddingAbove = Math.max(paddingAbove, printer.getPaddingAbove(ClientTypesetter.this, word));
            return true;
        }
    }

    final FontRenderer font;
    final int pageWidth;
    final List<IStyle> styles;
    public final List<Line> lines;

    public ClientTypesetter(FontRenderer font, int pageWidth) {
        super();
        this.font = font;
        this.pageWidth = pageWidth;
        this.styles = new ArrayList<>();
        this.lines = new ArrayList<>();
        clear();
    }

    public void clear() {
        this.lines.clear();
        this.lines.add(new Line());
    }

    @Override
    public List<IStyle> getStyleList() {
        return styles;
    }

    @Override
    public boolean hasFixedWidth() {
        return true;
    }

    @Override
    public int getWidth() {
        return pageWidth;
    }

    @Override
    public void pushStyle(IStyle... styles) throws TruthError {
        this.styles.addAll(Arrays.asList(styles));
    }

    @Override
    public void popStyle(int cnt) throws TruthError {
        for (int i = 0; i < cnt; i++) {
            if (styles.size() == 0) {
                throw new TruthError("Tried to pop style when there wasn't one on the stack!");
            }
            styles.remove(styles.size() - 1);
        }
    }

    @Override
    public void write(Word w) {
        if (!lines.get(lines.size() - 1).add(w, false)) {
            Line line = new Line();
            lines.add(line);
            line.add(w, true);
        }
    }

    @Override
    public void writeErrorMessage(String msg) {
      //  try {
            // TODO
           // write(msg.replace("\\", "\\\\ "), null, "" + TextFormatting.RED);
//        } catch (TruthError truthError) {
  //          truthError.printStackTrace();
            // Oh dear.
    //    }
    }

    @Override
    protected void runCommand(ICommand cmd, ITokenizer tokenizer) throws TruthError {
        cmd.call(this, tokenizer);
    }

    @Override
    public FontRenderer getFontRenderer() {
        return font;
    }

    @Override
    public boolean openURI(URI uri) {
        return false;
    }
}
