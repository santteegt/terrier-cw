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

/**
 * Maximal Margin Relevance Score function
 * 
 * @author santteegt
 *
 */
public class MMRScoring extends SimpleScoring {
	
	private HashMap<Integer, Float> termsIDF;
	
	private Map<Integer, Map<Integer, Integer>> docIDFCache = new HashMap<>();
	
	
	/* Initialize MMR model with index. Use 
	 * @param index_path : initialize index 
	 * @param prefix : language prefix for index (default = 'en')
	 * with location of index created using bash script.
	 */
	public MMRScoring(String index_path, String prefix, String matcherClass, long maxDocs) {
		super(index_path, prefix, matcherClass, maxDocs);
		super.term_lexicon = super.index.getLexicon();
		System.out.print("Number of entries: " + super.term_lexicon.numberOfEntries());
		long time = System.currentTimeMillis();

		this.termsIDF = Utils.populateTermIDFMap(super.total_documents, super.term_lexicon);
		System.out.println("IDF terms for collection loaded in :" + (System.currentTimeMillis()-time)/1000 + " seconds.");
		
	}
	
	/**
	 * Cosine similarity between two documents
	 * @param document_id1 new document
	 * @param document_id2 document to be compared with
	 * @return
	 */
	private double getSimilarity(int document_id1, int document_id2) {
		
		Map<Integer, Integer> dociTF = docIDFCache.get(document_id1);
		if(dociTF == null) {
			dociTF = Utils.docTF(document_id1, super.index);
			docIDFCache.put(document_id1, dociTF); //doc term frequencies are cached in order to improve performance
		}
		Map<Integer, Integer> docjTF = docIDFCache.get(document_id2);
		if(docjTF == null) {
			docjTF = Utils.docTF(document_id2, super.index);
			docIDFCache.put(document_id2, docjTF); //doc term frequencies are cached in order to improve performance
		}
		 
		double normd1 = 0.0;
		double normd2 = 0.0;
		Double sum = 0.0;
		for(Integer termjId: docjTF.keySet()) {
			Integer tf_in_i = dociTF.get(termjId) == null ? 0:dociTF.get(termjId);
			Float idf_in_i = this.termsIDF.get(termjId) == null ? 0:this.termsIDF.get(termjId);
			Double d1Score = tf_in_i == 0 ? 0:(1 + Math.log10(tf_in_i) ) * idf_in_i;
			Double d2Score = (1 + Math.log10(docjTF.get(termjId)) );
			sum += d1Score * d2Score; //dot product between d1 and d2 scores
			normd1 += Math.pow(d1Score, 2);
			normd2 += Math.pow(d2Score, 2);
		}
		
		normd1 = Math.sqrt(normd1); //norm of d1 term vector
		normd2 = Math.sqrt(normd2); //norm of d2 term vector
		return sum / (normd1 * normd2); //cosine similarity measure 
		
	}
	
	/**
	 * Get the max similarity of the document among the already selected list of documents
	 * @param document_id
	 * @param queryTF
	 * @param result_list
	 * @return
	 */
	private double scoreDocumentWrtList(int document_id, List<Integer> result_list) {
		double maxSimScore = 0.0;
		for(Integer docjId: result_list) { //iterate over the retrieved documents
			double simScore = this.getSimilarity(document_id, docjId);
			if(simScore > maxSimScore) maxSimScore = simScore;
		}
		return maxSimScore; //return the max score
	}
	
	/**
	 * Implemented method to obtain the similarity between document and query, but deprecated as 
	 * it is possible to use the score obtained by using a matching model
	 * @param document_id
	 * @param queryTF
	 * @param lambda
	 * @return
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private double scoreDocument(int document_id, Map<Integer, Integer> queryTF, double lambda) {
		HashMap<Integer, Integer> docTF = Utils.docTF(document_id, super.index);
		double normq = 0.0;
		double normd = 0.0;
		Double sum = 0.0;
		for(Integer termId:docTF.keySet()) {
			Double qscore = (1 + Math.log10(queryTF.get(termId)) ) * this.termsIDF.get(termId);
			Double dscore = (1 + Math.log10(docTF.get(termId)) );
			sum += qscore * dscore;
			normq += Math.pow(qscore, 2);
			normd += Math.pow(dscore, 2);
			
		}
		normq = Math.sqrt(normq);
		normd = Math.sqrt(normd);
		
		return lambda * (sum / (normq * normd));
	}
	
	/**
	 * Computes the MMR Scoring of a related query
	 * @param id
	 * @param query
	 * @param lambda
	 * @return
	 */
	public HashMap<String, Double> buildResultSet(String id, String query, double lambda) {
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

		List<Integer> dq = new ArrayList<>();
		double maxScore = doc_scores.length > 0 ? doc_scores[0]: 0.0;
		for(int i=0; i<doc_ids.length; i++) {
			Integer docId = doc_ids[i];
			double docQueryScore = doc_scores[i] / maxScore; //normalized score
			double docSimScore = this.scoreDocumentWrtList(docId, dq); //get the document similarity score
			
			double mmrScore = (lambda*docQueryScore) - ((1 - lambda)*docSimScore); //mmr formula
			
			String docName = doc_names[i].lastIndexOf("/") > 0  ? 
					 doc_names[i].substring(doc_names[i].lastIndexOf("/")+1, doc_names[i].lastIndexOf("."))
					:doc_names[i].substring(0, doc_names[i].lastIndexOf("."));
					 
			scores.put(docName, mmrScore);
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
