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
            WordPrinterMinecraft<Word> printer = TabletAPI.INSTANCE.getPrinterMinecraft(word);
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

            System.out.println("@" + words.size() + " " + word);
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
