package uk.ac.ucl.assignment.eval;

import java.util.ArrayList;

public class AlphaNDCG {
	
	private AlphaNDCG () {
		
	}
	
	/**
		@param k : cut-off for calculation
		of Alpha - NDCG@k
		@param alpha : alpha in Alpha - NDCG@k
		@param retrieved_list : list of
		documents , the highest - ranking
		document first .
		@param qrel_list : a collection 
		of labeled document ids from qrel .
		@return the Alpha - NDCG for the given data
	*/
	public static double compute (int k, double alpha, ArrayList<Result> retrieved_list, ArrayList<Qrel> qrel_list) {
		
		return 0;

	}
	
	static double computeIDCG(ArrayList<Qrel> qrel_list, int k ) {
		
		return 0;
	}

}
