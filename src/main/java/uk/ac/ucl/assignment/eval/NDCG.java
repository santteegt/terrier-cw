package uk.ac.ucl.assignment.eval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import uk.ac.ucl.assignment.utils.MyUtils;

/**
 * NDCG Evaluation metric
 * @author santteegt
 *
 */
public class NDCG {

	private NDCG () {
		
	}
	
	
	/**
	 * 
	 *	@param retrieved_list : list of documents, 
	 *	the highest - ranking document first .
	 *	@param qrel_list : a collection of labelled 
	 *	document ids from qrel .
	 *	@param k : cut - off for calculation of NDCG@k
	 *	@return the NDCG for given data
	*/
	public static double compute(List<Result> retrieved_list, List<Qrel> qrel_list, int k) {
		String topicNo = String.valueOf(qrel_list.iterator().next().getTopic_no());
		List<Qrel> docsQrels = new ArrayList<>();
		int i = 1;
		for(Result rs: retrieved_list) {
			Qrel docQrel = MyUtils.findQrel(rs, qrel_list);
			docsQrels.add( docQrel != null ? docQrel:new Qrel(topicNo, rs.getDocument_id(), "0") );
			if(i == k) break;
			else i++;
		}
//		System.out.println("===Size of qrels: " + docsQrels.size());
		//return computeDCG(qrel_list, k) / computeIDCG((ArrayList<Qrel>)qrel_list, k);
//		if(topicNo.equals("202")) {
//			System.out.println();
//		}
//		if(k == 30){
//			System.out.println();
//		}
		double dcg = computeDCG(docsQrels, k);
		double idcg = computeIDCG((ArrayList<Qrel>)docsQrels, k);
		return  idcg != 0.0 ? (dcg / idcg):0.0;
	}
	
	static double computeIDCG(ArrayList<Qrel> qrel_list, int k) {
		List<Qrel> sortedQrelList = (ArrayList<Qrel>)qrel_list.clone();
		
		Collections.sort(sortedQrelList, new Comparator<Qrel>() {
			@Override
			public int compare(Qrel o1, Qrel o2) {
				return o1.judgment < o2.judgment ? 1:(o1.judgment > o2.judgment ? -1:0);
			}
		});

		return computeDCG(sortedQrelList, k);
	}
	
	private static double computeDCG(List<Qrel> qrel_list, int k) {
		Iterator<Qrel> iterator = qrel_list.iterator();
		double idcg = iterator.next().judgment;
		int i = 2;
		double sum = 0.0;
		while(i <= k && iterator.hasNext()) {
			int score = iterator.next().judgment;
			score = score >=0 ? score:0;
			sum += ( score / ( Math.log(i) / Math.log(2) ) );
			i++;
		}
		return idcg + sum;
	}
}
