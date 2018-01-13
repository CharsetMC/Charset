package pl.asie.charset.module.tablet.format.api;

import pl.asie.charset.module.tablet.format.ITokenizer;

public interface ICommand {
	void call(ITypesetter typesetter, ITokenizer tokenizer) throws TruthError;
}
