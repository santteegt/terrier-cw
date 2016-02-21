package uk.ac.ucl.assignment;

import java.util.ArrayList;
import java.util.HashMap;

import org.terrier.indexing.TRECCollection;
import org.terrier.indexing.TaggedDocument;


/**
 * Created by santteegt on 2/16/16.
 */
public class MMRScoring {

    private TRECCollection collection;

    /**
     * Computes the similarity between term vectors of documents .
     **/
    public double getSimilarity(TaggedDocument document1, TaggedDocument document2 ) {
        double similarity = 0.0d;
        return similarity;
    }

    /**
     * Computes document score with respect to a query and result list.
     **/
    public double scoreDocumentWrtList(TaggedDocument document, String query ,
                                       ArrayList<TaggedDocument> resultList, double lambda ) {
        double score = 0.0d;
        return score;
    }

    /**
     *  Compute score of document with respect to query
     **/
    public double scoreDocument(TaggedDocument document , String query , double lambda) {
        double score = 0.0d;
        return score;
    }

    /**
     *  Iteratively build the result list .
     *  The input parameters are search query and parameter lambda in Equation 1 which is
     *  responsible for adjusting balance between
     *  relevance and redundancy .
     **/
    public HashMap<TaggedDocument, Double> buildResultSet(String query , double lambda) {
        HashMap<TaggedDocument, Double> result = new HashMap<TaggedDocument, Double>();
        return result;
    }
}
