package pl.asie.charset.module.tablet.format.parsers;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserBase {
	protected static final Pair<Pattern, Function<Matcher, String>> replace(String from, int flags, String to) {
		return Pair.of(Pattern.compile(from, flags), (m) -> to);
	}

	protected final List<Pair<Pattern, Function<Matcher, String>>> replacers = new ArrayList<>();
}
