package uk.ac.ucl.assignment;

import java.util.ArrayList;

/**
 * Created by santteegt on 2/16/16.
 */
public class NDCG {

    /**
     *
     * @param retrievedList list of documents: the highest ranking document first
     * @param qrelList a collection of labeled document ids from qrel
     * @param k cutoff for calculation of NDCG@k
     * @return the NDCG for the given data
     */
    public static double compute(ArrayList<Result> retrievedList , ArrayList <Qrel> qrelList , int k) {
        double result = 0.0d;
        return result;
    }

    static double computeIDCG(int n) {
        double result = 0.0d;
        return result;
    }
}
