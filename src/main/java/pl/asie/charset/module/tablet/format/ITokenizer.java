package pl.asie.charset.module.tablet.format;

import pl.asie.charset.module.tablet.format.api.TruthError;

public interface ITokenizer {
    /**
     * Reads a parameter
     * @param info Information on the parameter, to be shown if an error occurs.
     * @return The text of the parameter. Does not return null. May return the empty string.
     * @throws TruthError if there was no parameter
     */
    String getParameter(String info) throws TruthError;

    /**
     * @return null or the text of a following parameter.
     */
    String getOptionalParameter();
}
