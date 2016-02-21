package uk.ac.ucl.assignment;

import org.terrier.indexing.TaggedDocument;
import org.terrier.indexing.TRECCollection;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by santteegt on 2/16/16.
 */
public class PortfolioScoring {

    private TRECCollection collection;

    /**
     * Computes the correlation between term vectors of 2 documents
     * @param document1
     * @param query
     * @param document2
     * @return
     */
    public double getCorrelation(TaggedDocument document1 , String query , TaggedDocument document2) {
        double correlation = 0.0d;
        return correlation;
    }

    /**
     * Computes variance
     * @param document
     * @param resultList
     * @return
     */
    public double computeVariance(TaggedDocument document , ArrayList <TaggedDocument> resultList ) {
        double variance = 0.0d;
        return variance;
    }

    /**
     * Computes mean
     * @param document
     * @param resultList
     * @return
     */
    public double computeMean(TaggedDocument document , ArrayList<TaggedDocument> resultList ) {
        double mean = 0.0d;
        return mean;
    }

    /**
     * Computes the score of document
     * @param document
     * @param query
     * @return
     */
    public double scoreDocument(TaggedDocument document, String query ) {
        double score = 0.0d;
        return score;

    }

    /**
     * Iteratively build the result list. The input parameters are search query and discount factor (e.g. 1/r)
     * that yields rank specific w i.e. wj in Equation 2 and the parameter b responsible for adjusting risk
     * @param query
     * @param discount
     * @param b
     * @return
     */
    public HashMap<TaggedDocument, Double> buildResultSet(String query , double discount, double b) {
        HashMap<TaggedDocument, Double> resultSet = new HashMap<TaggedDocument, Double>();
        return resultSet;
    }
}
