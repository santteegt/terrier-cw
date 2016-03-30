package uk.ac.ucl.assignment.diversity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.terrier.matching.ResultSet;
import org.terrier.querying.Manager;
import org.terrier.querying.SearchRequest;
import org.terrier.utility.ApplicationSetup;

import uk.ac.ucl.assignment.utils.SimpleScoring;
import uk.ac.ucl.assignment.utils.Utils;

public class PortfolioScoring extends SimpleScoring {

	private HashMap<Integer, Float> termsIDF;
	
	private Map<Integer, Map<Integer, Integer>> docIDFCache = new HashMap<>();

	public PortfolioScoring(String index_path, String prefix, String matcherClass, long maxDocs) {
		super(index_path, prefix, matcherClass, maxDocs);
		super.term_lexicon = super.index.getLexicon();
		System.out.print("Number of entries: " + super.term_lexicon.numberOfEntries());
		long time = System.currentTimeMillis();

		this.termsIDF = Utils.populateTermIDFMap(super.total_documents, super.term_lexicon);
		System.out.println("IDF terms for collection loaded in :" + (System.currentTimeMillis()-time)/1000 + " seconds.");
	}

	/**
	 * Computes correlation between term vectors of 2 documents. 
	 * @param document_id1
	 * @param document_id2
	 * @param discount
	 * @return
	 */
	public double getCorrelation(int document_id1, int document_id2, double discount) {
		Map<Integer, Integer> dociTF = docIDFCache.get(document_id1);
		if(dociTF == null) {
			dociTF = Utils.docTF(document_id1, super.index);
			docIDFCache.put(document_id1, dociTF);
		}
		Map<Integer, Integer> docjTF = docIDFCache.get(document_id2);
		if(docjTF == null) {
			docjTF = Utils.docTF(document_id2, super.index);
			docIDFCache.put(document_id2, docjTF);
		}

		double normd1 = 0.0, normd2 = 0.0;
		Double sum = 0.0;
		int i = 0, n = docjTF.keySet().size();
		double[] d1Weights = new double[n];
		double[] d2Weights = new double[n];
		double meanWd1 = 0.0, meanWd2 = 0.0;
		for(Integer termjId: docjTF.keySet()) {
			Integer tf_in_i = dociTF.get(termjId) == null ? 0:dociTF.get(termjId);
			Float idf_in_i = this.termsIDF.get(termjId) == null ? 0:this.termsIDF.get(termjId);
			d1Weights[i] = tf_in_i == 0 ? 0:(1 + Math.log10(tf_in_i) ) * idf_in_i; //ter
			d2Weights[i] = (1 + Math.log10(docjTF.get(termjId)) );
			meanWd1 += d1Weights[i];
			meanWd2 += d2Weights[i];
		}
		meanWd1 = meanWd1 / n; //get the mean of the doc1 terms vector 
		meanWd2 = meanWd2 / n; //get the mean of the doc2 terms vector
		
		for(int k=0; k<n; k++) {
			sum += (d1Weights[i] - meanWd1) * (d2Weights[i] - meanWd2); 
			normd1 += Math.pow(d1Weights[i] - meanWd1, 2);
			normd2 += Math.pow(d2Weights[i] - meanWd2, 2);
		}
		
		normd1 = Math.sqrt(normd1);
		normd2 = Math.sqrt(normd2);
		return discount * 1 * (sum / (normd1 * normd2)); //pearson correlation multiplied by the discount
	}


	/**
	 * Compute variance
	 * @param document_id
	 * @param result_list
	 * @return
	 */
	public double computeVariance(Integer document_id, List<Integer> result_list ) {
		return 1; //assumes variance=1
	}

	/**
	 * Computes Mean
	 * @param document_id
	 * @param result_list
	 * @return
	 */
	@Deprecated
	public double computeMean(int document_id, ArrayList<Integer> result_list ) {
		return 0;
	}
	
	/**
	 * Computes score of document
	 * @param document_id
	 * @param result_list
	 * @return
	 */
	public double scoreDocument(int document_id, List<Integer> result_list) {
		double maxSimScore = 0.0;
		int j = 1;
		for(Integer docjId: result_list) {
			double discount = j == 1 ? 1:(1/(Math.log10(j)/Math.log10(2)));
			double simScore = this.getCorrelation(document_id, docjId, discount);
			if(simScore > maxSimScore) maxSimScore = simScore;
		}
		return maxSimScore;
	}

	/**
	 * Iteratively build the result list . The input parameters are search query and 
	 * discount factor ( e.g. 1/r ) that yields rank specific w i.e. wj and the parameter b 
	 * responsible for adjusting risk.
	 * @param id
	 * @param query
	 * @param b
	 * @return
	 */
	public HashMap<String, Double> buildResultSet(String id, String query , double b) {

		Manager manager = new Manager(this.index);
		SearchRequest srq = manager.newSearchRequest(id, query);
		srq.addMatchingModel("Matching", super.matcherClass);
		manager.runPreProcessing(srq);
		manager.runMatching(srq);
		manager.runPostProcessing(srq);
		manager.runPostFilters(srq);
		
		ResultSet set = srq.getResultSet();
		int doc_ids [] = set.getDocids();
		double doc_scores [] = set.getScores();
		final String metaIndexDocumentKey = ApplicationSetup.getProperty(
				"trec.querying.outputformat.docno.meta.key", "filename");
		String doc_names [] = Utils.getDocnos(metaIndexDocumentKey, set, index);
		
		
		HashMap<String, Double> scores  = new HashMap<>();

		double maxScore = doc_scores.length > 0 ? doc_scores[0]: 0.0;
		List<Integer> dq = new ArrayList<>(); 
		for(int i=0; i<doc_ids.length; i++) {
			Integer docId = doc_ids[i];
			double mean_d = doc_scores[i] / maxScore; //normalized score
			double docSimScore = this.scoreDocument(docId, dq);
			double discount = i+1== 1 ? 1:1/(Math.log10(i+1)/Math.log(2)); //discount weights by rank position
			double variance_d = this.computeVariance(docId, dq); //variance is always 1
			double fmvaScore = mean_d - (b*discount*variance_d) - (2*b*Math.sqrt(variance_d)*docSimScore); //portfolio
			
			String docName = doc_names[i].lastIndexOf("/") > 0  ? 
					 doc_names[i].substring(doc_names[i].lastIndexOf("/")+1, doc_names[i].lastIndexOf("."))
					:doc_names[i].substring(0, doc_names[i].lastIndexOf("."));
					 
			scores.put(docName, fmvaScore);
			dq.add(docId);
			if (i+1 == super.maxDocs)
				break;
		}
		
		//re-sorting the retrieved docs
		List<Map.Entry<String, Double>> scoreList = new LinkedList<>(scores.entrySet());
		Collections.sort(scoreList, new Comparator<Map.Entry<String, Double>>() {

			@Override
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
				return o1.getValue() < o2.getValue() ? 1:(o1.getValue() > o2.getValue() ? -1:0);
			}
		});
		HashMap<String, Double> sortedScores = new LinkedHashMap<>();
		for(Map.Entry<String, Double> entry: scoreList) {
			sortedScores.put(entry.getKey(), entry.getValue());
		}
		return sortedScores;
	}

}
