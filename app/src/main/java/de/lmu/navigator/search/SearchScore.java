package de.lmu.navigator.search;

public class SearchScore implements Comparable<SearchScore> {
    private final static float MATCH_THRESHOLD = 0.5f;
    private static MyPrefixLevenshtein mSimilarityMeasure = new MyPrefixLevenshtein();

    private Searchable mSearchable;
    private float simPrimary = 1f;
    private float simSecondary = 1f;

    public SearchScore(Searchable s) {
        mSearchable = s;
    }

    public void score(String query) {
        simPrimary = mSimilarityMeasure.getSimilarity(mSearchable.getPrimaryText(), query);
        simSecondary = mSimilarityMeasure.getSimilarity(mSearchable.getSecondaryText(), query);
    }

    public Searchable getObject() {
        return mSearchable;
    }

    public boolean isMatch() {
        return simPrimary > MATCH_THRESHOLD || simSecondary > MATCH_THRESHOLD;
    }

    public float getPrimarySimilarity() {
        return simPrimary;
    }

    public float getSecondarySimilarity() {
        return simSecondary;
    }

    @Override
    public int compareTo(SearchScore another) {
        float scoreThis = Math.max(simPrimary * 2, simSecondary);
        float scoreOther = Math.max(another.getPrimarySimilarity() * 2, another.getSecondarySimilarity());

        if (scoreThis > scoreOther)
            return -1;

        if (scoreThis < scoreOther)
            return 1;

        return mSearchable.getPrimaryText().compareTo(another.getObject().getPrimaryText());
    }
}
