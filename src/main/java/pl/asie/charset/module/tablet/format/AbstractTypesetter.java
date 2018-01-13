package pl.asie.charset.module.tablet.format;

import com.google.common.base.Strings;
import pl.asie.charset.module.tablet.format.api.ICommand;
import pl.asie.charset.module.tablet.format.api.ITypesetter;
import pl.asie.charset.module.tablet.format.api.TabletAPI;
import pl.asie.charset.module.tablet.format.api.TruthError;
import pl.asie.charset.module.tablet.format.words.WordText;

import java.util.Locale;

public abstract class AbstractTypesetter implements ITypesetter {
    public AbstractTypesetter() {
    }

    @Override
    public void write(String text) throws TruthError {
        if (Strings.isNullOrEmpty(text)) return;
        final Tokenizer tokenizer = new Tokenizer(text);
        
        while (tokenizer.nextToken()) {
            final String token = tokenizer.getToken();
            if (token.isEmpty()) continue;
            switch (tokenizer.getType()) {
            default:
                throw new TruthError("Unknown tokentype: " + tokenizer.getToken());
            case WORD:
                write(new WordText(token));
                break;
            case PARAMETER:
                write(token);
                break;
            case COMMAND:
                final String cmdName = token.toLowerCase(Locale.ROOT);
                ICommand cmd = TabletAPI.INSTANCE.getCommand(cmdName);
                if (cmd == null) {
                    throw new TruthError("Unknown command: " + cmdName);
                }
                runCommand(cmd, tokenizer);
                break;
            }
        }
    }

    public abstract void writeErrorMessage(String msg);
    protected abstract void runCommand(ICommand cmd, ITokenizer tokenizer) throws TruthError;
}
