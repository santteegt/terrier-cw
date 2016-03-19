package uk.ac.ucl.assignment.eval;

import java.util.ArrayList;

/**
 * 
 * @author santteegt
 *
 */
public class NDCG {

	private NDCG () {
		
	}
	
	
	/**
	 * 
		@param retrieved_list : list of documents, 
		the highest - ranking document first .
		@param qrel_list : a collection of labelled 
		document ids from qrel .
		@param k : cut - off for calculation of NDCG@k
		@return the NDCG for given data
	*/
	public static double compute(ArrayList <Result> retrieved_list, ArrayList <Qrel> qrel_list, int k) {
		
		return 0;
		
	}
	
	static double computeIDCG(ArrayList <Qrel> qrel_list, int k) {
		
		return 0;
	}
}
