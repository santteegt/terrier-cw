package uk.ac.ucl.assignment.eval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import uk.ac.ucl.assignment.utils.MyUtils;

/**
 * Alpha-NDCG evaluation metric
 * @author santteegt
 *
 */
public class AlphaNDCG {
	
	private AlphaNDCG () {
		
	}

	/**
	 * 
	 * @param k: cut-off for calculation of Alpha - NDCG@k
	 * @param alpha: Alpha parameter
	 * @param retrieved_list: list of documents , the highest - ranking document first .
	 * @param qrel_list: a collection of labeled document ids from qrel
	 * @return the Alpha - NDCG for the given data
	 */
	public static double compute (int k, double alpha, List<Result> retrieved_list, List<Qrel> qrel_list) {
		
		String topicNo = String.valueOf(qrel_list.iterator().next().getTopic_no());
		List<Qrel> docsQrels = new ArrayList<>();
		int i = 1;
		for(Result rs: retrieved_list) {
			Qrel docQrel = MyUtils.findQrel(rs, qrel_list);
			docsQrels.add( docQrel != null ? docQrel:new Qrel(topicNo, rs.getDocument_id(), "0") );
			if(i == k) break;
			else i++;
		}
		double dcg = computeDCG(docsQrels, k, alpha);
		double idcg = computeIDCG((ArrayList<Qrel>)docsQrels, k, alpha);
		return  idcg != 0.0 ? (dcg / idcg):0.0;

	}
	
	static double computeIDCG(ArrayList<Qrel> qrel_list, int k, double alpha) {
		List<Qrel> sortedQrelList = (ArrayList<Qrel>)qrel_list.clone();
		
		Collections.sort(sortedQrelList, new Comparator<Qrel>() {
			@Override
			public int compare(Qrel o1, Qrel o2) {
				return o1.judgment < o2.judgment ? 1:(o1.judgment > o2.judgment ? -1:0);
			}
		});

		return computeDCG(sortedQrelList, k, alpha);
	}
	
	private static double computeDCG(List<Qrel> qrel_list, int k, double alpha) {
		Iterator<Qrel> iterator = qrel_list.iterator();
		double idcg = iterator.next().judgment;
		int i = 2;
		double sum = 0.0;
		while(i <= k && iterator.hasNext()) {
			int score = iterator.next().judgment;
			score = score >=0 ? score:0;
			sum += ( (score * (1-alpha)) / ( Math.log(i) / Math.log(2) ) );
			i++;
		}
		return idcg + sum;
	}

}
