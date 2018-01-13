package pl.asie.charset.module.tablet.format;

import pl.asie.charset.module.tablet.format.api.TruthError;

import static pl.asie.charset.module.tablet.format.Tokenizer.TokenType.*;

public class Tokenizer implements ITokenizer {
    @Override
    public String getParameter(String info) throws TruthError {
        if (!nextToken()) {
            throw new TruthError("EOF while reading parameter: " + info);
        }
        if (type != Tokenizer.TokenType.PARAMETER) {
            throw new TruthError("Expected parameter, not " + type + ": " + info);
        }
        return token;
    }

    @Override
    public String getOptionalParameter() {
        if (!nextToken()) return null;
        if (type != Tokenizer.TokenType.PARAMETER) {
            prevToken();
            return null;
        }
        return token;
    }

    public enum TokenType {
        WORD, COMMAND, PARAMETER
    }

    public Tokenizer(String src) {
        this.src = src;
    }

    private static final String NL = "\\p";
    private static final String WS = "\\ ";

    private final String src;
    private int scan = 0;
    private int prevScan = -1;

    private TokenType type;
    private String token;

    private int contigLines = 0;
    private int contigSpaces = 0;

    public String getToken() {
        return token;
    }

    public TokenType getType() {
        return type;
    }

    public boolean nextToken() {
        prevScan = scan;
        mainloop: while (true) {
            if (scan >= src.length()) return false;
            char c = src.charAt(scan);
            if (c == '\n') {
                scan++;
                contigLines++;
                if (contigLines == 2) {
                    type = COMMAND;
                    token = NL;
                    contigLines = contigSpaces = 0;
                    return true;
                }
                continue;
            } else if (c == '\r') {
                scan++;
                continue;
            } else if (Character.isWhitespace(c)) {
                scan++;
                contigSpaces++;
                continue;
            } else if (c == '%') {
                while (scan < src.length()) {
                    c = src.charAt(scan++);
                    if (c == '\n') {
                        continue mainloop;
                    }
                }
            } else {
                if (contigLines >= 1 || contigSpaces >= 1) {
                    type = COMMAND;
                    token = WS;
                } else if (c == '\\') {
                    readCommand();
                } else if (c == '{') {
                    readParameter();
                } else {
                    readWord();
                }
            }
            contigLines = contigSpaces = 0;
            return true;
        }
    }

    public void prevToken() {
        if (prevScan == -1) throw new IllegalStateException("No previous token available");
        scan = prevScan;
        prevScan = -1;
    }
    
    private void emit(TokenType type, int start, int end) {
        this.type = type;
        token = src.substring(start, end);
    }

    private void readWord() {
        final int start = scan;
        while (true) {
            scan++;
            if (scan >= src.length()) {
                break;
            }
            char c = src.charAt(scan);
            if (Character.isWhitespace(c) || c == '\\' || c == '{') {
                break;
            }
            if (c == '.' || c == '-' || c == '_') {
                // breaks if an alphanumeric is following
                if (scan + 1 < src.length()) {
                    char peak = src.charAt(scan + 1);
                    if (Character.isLetterOrDigit(peak)) {
                        scan++;
                        break;
                    }
                }
            }
        }
        emit(WORD, start, scan);
    }

    private void readParameter() {
        final int start = scan;
        int count = 0;
        while (scan < src.length()) {
            char c = src.charAt(scan++);
            if (c == '{') {
                count++;
            } else if (c == '}') {
                count--;
            }
            if (count <= 0) break;
        }
        emit(PARAMETER, start + 1, scan - 1);
    }

    private void readCommand() {
        readWord();
        type = COMMAND;
        if (scan < src.length() - 1) {
            if (Character.isWhitespace(src.charAt(scan))) {
                scan++;
            }
        }
    }
}
