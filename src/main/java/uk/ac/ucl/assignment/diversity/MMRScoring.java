package uk.ac.ucl.assignment.diversity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.terrier.indexing.tokenisation.Tokeniser;
import org.terrier.matching.ResultSet;
import org.terrier.querying.Manager;
import org.terrier.querying.SearchRequest;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.Index;
import org.terrier.structures.Lexicon;
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

	/* Terrier Index */
//	Index index;
	
	/* Index structures*/
	/* list of terms in the index */
//	Lexicon<String> term_lexicon = null;
	/* list of documents in the index */
//	DocumentIndex doi = null;
	
	/* Collection statistics */
//	long total_tokens;
//	long total_documents;
	
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
		for(int i=0;i<100;i++) {
			System.out.println(super.term_lexicon.getIthLexiconEntry(i).getKey());
		}
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
			docIDFCache.put(document_id1, dociTF);
		}
		Map<Integer, Integer> docjTF = docIDFCache.get(document_id2);
		if(docjTF == null) {
			docjTF = Utils.docTF(document_id2, super.index);
			docIDFCache.put(document_id2, docjTF);
		}
		 
//		HashMap<Integer, Integer> docjTF = Utils.docTF(document_id2, super.index);
		double normq = 0.0;
		double normd = 0.0;
		Double sum = 0.0;
		for(Integer termjId: docjTF.keySet()) {
			Integer tf_in_i = dociTF.get(termjId) == null ? 0:dociTF.get(termjId);
			Float idf_in_i = this.termsIDF.get(termjId) == null ? 0:this.termsIDF.get(termjId);
			Double qscore = tf_in_i == 0 ? 0:(1 + Math.log10(tf_in_i) ) * idf_in_i;
			Double dscore = (1 + Math.log10(docjTF.get(termjId)) );
			sum += qscore * dscore;
			normq += Math.pow(qscore, 2);
			normd += Math.pow(dscore, 2);
		}
		
		normq = Math.sqrt(normq);
		normd = Math.sqrt(normd);
		return sum / (normq * normd);
		
	}
	
	/**
	 * Get the max similarity of the document among the already selected list of documents
	 * @param document_id
	 * @param queryTF
	 * @param result_list
	 * @return
	 */
	private double scoreDocumentWrtList(int document_id, Map<Integer, Integer> queryTF, List<Integer> result_list) {
		double maxSimScore = Double.MIN_VALUE;
		for(Integer docjId: result_list) {
			double simScore = this.getSimilarity(document_id, docjId);
			if(simScore > maxSimScore) maxSimScore = simScore;
		}
		return maxSimScore;
	}
	
	/**
	 * Implemented method to obtain the similarity between document and query, but deprecated as 
	 * it is possible to use the score obtained by using a matching model
	 * @param document_id
	 * @param queryTF
	 * @param lambda
	 * @return
	 */
	@Deprecated
//	public double scoreDocument(int document_id, String query, double lambda) {
	private double scoreDocument(int document_id, Map<Integer, Integer> queryTF, double lambda) {
		HashMap<Integer, Integer> docTF = Utils.docTF(document_id, super.index);
//		List<Double> qscoreVector = new ArrayList<>();
//		List<Double> dscoreVector = new ArrayList<>();
		double normq = 0.0;
		double normd = 0.0;
		Double sum = 0.0;
		for(Integer termId:docTF.keySet()) {
			Double qscore = (1 + Math.log10(queryTF.get(termId)) ) * this.termsIDF.get(termId);
			Double dscore = (1 + Math.log10(docTF.get(termId)) );
//			qscoreVector.add(qscore);
//			dscoreVector.add(dscore);
			sum += qscore * dscore;
			normq += Math.pow(qscore, 2);
			normd += Math.pow(dscore, 2);
			
		}
		normq = Math.sqrt(normq);
		normd = Math.sqrt(normd);
//		Double []qScores = qscoreVector.toArray(new Double[qscoreVector.size()]);
//		Double []dScores = dscoreVector.toArray(new Double[dscoreVector.size()]);
		
//		for(int i=0;i<qScores.length;i++) sum += qScores[i] * dScores[i];
		
		return lambda * (sum / (normq * normd));
	}
	
	/**
	 * 
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
		//double doc_scores [] = set.getScores();
		final String metaIndexDocumentKey = ApplicationSetup.getProperty(
				"trec.querying.outputformat.docno.meta.key", "filename");
		String doc_names [] = Utils.getDocnos(metaIndexDocumentKey, set, index);
		
		
		HashMap<String, Double> scores  = new HashMap<>();//super.buildResultSet(id, query);
		
		
		StringTokenizer queryTokens = new StringTokenizer(query);
		Map <Integer, Integer> queryTF = new HashMap<>();
		while(queryTokens.hasMoreTokens()) {
			String token = queryTokens.nextToken().toLowerCase();
			if(super.term_lexicon.getLexiconEntry(token) != null) {
				Integer tokenId = super.term_lexicon.getLexiconEntry(token).getTermId(); 
				queryTF.put(tokenId, queryTF.get(token) == null ? 1:queryTF.get(token) + 1);
			}
			
		}

		List<Integer> dq = new ArrayList<>(); 
		for(int i=0; i<doc_ids.length; i++) {
			Integer docId = doc_ids[i];
			//double docQueryScore = this.scoreDocument(docId, queryTF, lambda);
			double docQueryScore = doc_scores[i];
			double docSimScore = this.scoreDocumentWrtList(docId, queryTF, dq);
			
			double mmrScore = (lambda*docQueryScore) - ((1 - lambda)*docSimScore);
			
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
				// TODO Auto-generated method stub
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
