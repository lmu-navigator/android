package de.lmu.navigator.search;

import java.util.Locale;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

/**
 * A custom class using the Levenshtein algorithm, but slightly modified to
 * be suitable for search-as-you-type.
 *
 * The Levenshtein algorithm is a similarity measure for strings.
 *
 * The standard version is not suitable for search-as-you-type, because it
 * only operates on the whole token. This means, that a long data string
 * will always have a low similarity when the user starts typing and the
 * search string is still very short.
 *
 * This is a slight modification, so that the algorithm operates on string
 * prefixes. That means, when the search string is only n characters long,
 * only the first n characters of the data string is taken into account for
 * similarity calculation. With that, a data string starting with the same
 * letters as the search string will get a high similarity score just as we
 * need it for search-as-you-type.
 */
public class MyPrefixLevenshtein extends AbstractStringMetric {
    private Levenshtein levenshtein;

    public MyPrefixLevenshtein() {
        this.levenshtein = new Levenshtein();
    }

    @Override
    public String getLongDescriptionString() {
        return null;
    }

    @Override
    public String getShortDescriptionString() {
        return null;
    }

    @Override
    public float getSimilarity(String compString, String searchString) {
        compString = compString.replace(" ", "");
        searchString = searchString.replace(" ", "");

        // call the standard levenshtein, but only with the prefix of the
        // data string
        if (searchString.length() < compString.length())
            compString = compString.substring(0, searchString.length());

        return levenshtein.getSimilarity(
                compString.toLowerCase(Locale.GERMAN),
                searchString.toLowerCase(Locale.GERMAN));
    }

    @Override
    public String getSimilarityExplained(String arg0, String arg1) {
        return null;
    }

    @Override
    public float getSimilarityTimingEstimated(String arg0, String arg1) {
        return 0;
    }

    @Override
    public float getUnNormalisedSimilarity(String compString,
                                           String searchString) {
        // call the standard levenshtein, but only with the prefix of the
        // data string
        if (searchString.length() < compString.length())
            compString = compString.substring(0, searchString.length());

        return levenshtein.getUnNormalisedSimilarity(
                compString.toLowerCase(Locale.GERMAN),
                searchString.toLowerCase(Locale.GERMAN));
    }
}
