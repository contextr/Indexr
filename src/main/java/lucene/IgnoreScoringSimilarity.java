package lucene;

import org.apache.lucene.search.DefaultSimilarity;

public class IgnoreScoringSimilarity extends DefaultSimilarity {
	private static final long serialVersionUID = 1L;

	@Override
    public float idf(int docFreq, int numDocs) {
    	return 1.0f;
    }

    @Override
    public float tf(float freq) {
        return 1.0f;
    }

    @Override
    public float coord(int overlap, int maxOverlap) {
        return 1.0f;
    }
    
    @Override
    public float queryNorm(float sumOfSquaredWeights) {
        return 1.0f;
    }
} 