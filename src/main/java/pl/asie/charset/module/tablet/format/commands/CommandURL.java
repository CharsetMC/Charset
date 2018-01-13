package pl.asie.charset.module.tablet.format.commands;

import pl.asie.charset.module.tablet.format.ITokenizer;
import pl.asie.charset.module.tablet.format.api.ICommand;
import pl.asie.charset.module.tablet.format.api.ITypesetter;
import pl.asie.charset.module.tablet.format.api.TruthError;
import pl.asie.charset.module.tablet.format.words.WordURL;

import java.net.URI;

public class CommandURL implements ICommand {
	@Override
	public void call(ITypesetter typesetter, ITokenizer tokenizer) throws TruthError {
		String uriLink = tokenizer.getParameter("\\url missing parameter: uriLink");
		String content = tokenizer.getParameter("\\url missing parameter: content");
		try {
			typesetter.write(new WordURL(content, new URI(uriLink)));
		} catch (Exception e) {
			e.printStackTrace();
			throw new TruthError(e.getMessage());
		}
	}
}
