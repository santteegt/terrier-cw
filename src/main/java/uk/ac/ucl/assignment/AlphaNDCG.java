package uk.ac.ucl.assignment;

import java.util.ArrayList;

/**
 * Created by santteegt on 2/16/16.
 */
public class AlphaNDCG {

    private AlphaNDCG() {

    }

    /**
     *
     * @param k cu-toff for calculation of Alpha-NDCG@k
     * @param alpha alpha in Alpha-NDCG@k
     * @param retrievedList list of documents, the highest ranking document first
     * @param qrelList a collection of labeled document ids from qrel
     * @return the Alpha-NDCG for the given data
     */
    public static double compute(int k, double alpha, ArrayList<Result> retrievedList,
                                 ArrayList <Qrel> qrelList ) {
        double result = 0.0d;
        return result;
    }

    static double computeIDCG(int n) {
        double result = 0.0d;
        return result;
    }
}
