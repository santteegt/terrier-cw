package uk.ac.ucl.assignment.utils;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.terrier.applications.batchquerying.TRECQuery;
import org.terrier.matching.ResultSet;
import org.terrier.querying.Manager;
import org.terrier.querying.SearchRequest;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.Index;
import org.terrier.structures.Lexicon;
import org.terrier.utility.ApplicationSetup;

import uk.ac.ucl.assignment.diversity.MMRScoring;
import uk.ac.ucl.assignment.diversity.PortfolioScoring;
import uk.ac.ucl.assignment.eval.AlphaNDCG;
import uk.ac.ucl.assignment.eval.NDCG;
import uk.ac.ucl.assignment.eval.Qrel;
import uk.ac.ucl.assignment.eval.Result;
import uk.ac.ucl.assignment.utils.Utils.QREL;

/**
 * Simple scoring as a baseline class that also is in charge of executing all the implementations for the assignment
 * @author santteegt
 *
 */
public class SimpleScoring {
	/* Terrier Index */
	protected Index index;
	
	/* Index structures*/
	/* list of terms in the index */
	protected Lexicon<String> term_lexicon = null;
	/* list of documents in the index */
	protected DocumentIndex doi = null;
	
	/* Collection statistics */
	protected long total_tokens;
	protected long total_documents;
	
	protected String matcherClass;
	protected long maxDocs;
	
	
	/** Initialize Simple model with index. Use 
	 * @param index_path : initialize index 
	 * @param prefix : language prefix for index 
	 * with location of index created using bash script.
	 */
	public SimpleScoring(String index_path, String prefix, String matcherClass, long maxDocs) {
		
		// Load the index and collection stats
		try {
			index = Index.createIndex(index_path, prefix);
			
			System.out.println("Loaded index from path "+index_path+" "+index.toString());
			total_tokens = index.getCollectionStatistics().getNumberOfTokens();
			total_documents = index.getCollectionStatistics().getNumberOfDocuments();
			System.out.println("Number of terms and documents in index "+total_tokens+" "+total_documents);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		this.matcherClass = matcherClass;
		this.maxDocs = maxDocs;
	}
	
	public HashMap <String, Double> buildResultSet(String id, String query) {
		
		// Create a search request object.
		Manager manager = new Manager(this.index);
		SearchRequest srq = manager.newSearchRequest(id, query);
		
		srq.addMatchingModel("Matching", this.matcherClass);
		manager.runPreProcessing(srq);
		manager.runMatching(srq);
		manager.runPostProcessing(srq);
		manager.runPostFilters(srq);
		
		ResultSet set = srq.getResultSet();
		//int doc_ids [] = set.getDocids();
		double doc_scores [] = set.getScores();
		
		final String metaIndexDocumentKey = ApplicationSetup.getProperty(
				"trec.querying.outputformat.docno.meta.key", "filename");
		String doc_names [] = Utils.getDocnos(metaIndexDocumentKey, set, index);
		
		
		HashMap <String, Double> scores = new HashMap<String, Double>();
		for (int i = 0 ; i < doc_scores.length;i++){
			String docName = doc_names[i].lastIndexOf("/") > 0  ? 
					 doc_names[i].substring(doc_names[i].lastIndexOf("/")+1, doc_names[i].lastIndexOf("."))
					:doc_names[i].substring(0, doc_names[i].lastIndexOf("."));
			scores.put(docName, doc_scores[i]);
			
			if (i+1 == this.maxDocs) break;
		}
			
		
		return scores;
		
	}
	public void closeIndex() {
		try {
			index.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String [] args) {
		
		if(args.length < 5) {
			System.out.println("Not enough parameters: (terrier_home) (index_path) (topics_file) (qrels_file) (process) [score-param]");
			return;
		}
		
		String terrierHome = args[0];
		String index_path = args[1];
		String topic_file_path = args[2];
		String qrel_path = args[3];
		String process = args[4];
		String param = args.length > 5 ? args[5]:"0";
		
		System.setProperty("terrier.home", terrierHome);
		// Load the topics
		TRECQuery trec_topics = new TRECQuery(topic_file_path);

		switch (process) {
			case "NDCG": processNDCGEvaluation(trec_topics, index_path, qrel_path); break;
			case "MMR": processMMRScoring(trec_topics, index_path, qrel_path, Float.valueOf(param)); break;
			case "PORTFOLIO": processPortfolioScoring(trec_topics, index_path, qrel_path, Integer.valueOf(param)); break;
			default: System.out.println(
					String.format(
							"\n===No process found with name %s. You should specify one of this: [NDCG, ]===", process));
		}
		
	}
	
	/**
	 * Process NDCG evaluation over BM25 Matching model
	 * @param trec_topics
	 * @param index_path
	 * @param qrel_path
	 */
	public static void processNDCGEvaluation(TRECQuery trec_topics, String index_path, String qrel_path) {
		
		ArrayList <Qrel> qrels = Utils.loadQrels(qrel_path, QREL.ADHOC);
		// Initialize the scorer
		SimpleScoring scorer = new SimpleScoring(index_path, "terrier_clueweb_index", "BM25", 50);
		try {
			int i = 0;
			
			Map<Integer, Double> ndcgValues = initlogVar();
			while(trec_topics.hasNext()) {
				HashMap<String, Double> scores = scorer.buildResultSet(i+"", trec_topics.next());
				Integer queryID = Integer.valueOf(trec_topics.getQueryId());
				
				List<Result> retrievedDocs = new ArrayList<>();
				List<Qrel> queryQrels = MyUtils.filterQrelList(queryID, 0, qrels);
				int rank = 0;
				for (Map.Entry<String, Double> entry : scores.entrySet()) {
					retrievedDocs.add( new Result(entry.getKey(), rank++, entry.getValue()) );
				}
				System.out.println(String.format("NDCG for topic: %s, nqrels: %d, ndocs: %d", queryID, queryQrels.size(), retrievedDocs.size() ));
				if(retrievedDocs.size() > 0) {
					for(int k: ndcgValues.keySet()) {
						ndcgValues.put(k, ndcgValues.get(k) + NDCG.compute(retrievedDocs, queryQrels, k));
					}
				}
				i++;
			}
			System.out.println(i);
			for(int k: ndcgValues.keySet()) { //calculate the mean NDCG across queries
				ndcgValues.put(k, ndcgValues.get(k) / i);
			}
			scorer.closeIndex();
			MyUtils.saveOutputFile("bm25_ndcg.txt", "bm25\n", "K\t|NDCG@K\n", ndcgValues, false);
			System.out.println("====Final Results: " + ndcgValues.toString());
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			scorer.closeIndex();
		}
		
	}
	
	/**
	 * Initialize HashMap for evaluation metrics
	 * @return
	 */
	private static Map<Integer, Double> initlogVar() {
		Map<Integer, Double> evalValues = new HashMap<>();
		evalValues.put(1, 0.0); evalValues.put(5, 0.0); evalValues.put(10, 0.0);
		evalValues.put(20, 0.0); evalValues.put(30, 0.0); evalValues.put(40, 0.0);
		evalValues.put(50, 0.0);
		return evalValues;
	}
	
	/**
	 * Process the MMR scoring function over a pre-ordered retrieval process that used BM25 as a matching model
	 * @param trec_topics
	 * @param index_path
	 * @param qrel_path
	 * @param lambda
	 */
	public static void processMMRScoring(TRECQuery trec_topics, String index_path, String qrel_path, Float lambda) {
		
		ArrayList <Qrel> qrels = Utils.loadQrels(qrel_path, QREL.DIVERSITY);
		long start = System.currentTimeMillis();
		MMRScoring scorer = new MMRScoring(index_path, "terrier_clueweb_index", "BM25", 100);
		try {
			Map<Integer, Double> mmrValues1 = initlogVar();
			Map<Integer, Double> mmrValues5 = initlogVar();
			Map<Integer, Double> mmrValues9 = initlogVar();
			
			Boolean first = true;
			int i = 0;
			while(trec_topics.hasNext()) {
				long queryStart = System.currentTimeMillis();
				HashMap<String, Double> scores = scorer.buildResultSet(i+"", trec_topics.next(), lambda);
				Integer queryID = Integer.valueOf(trec_topics.getQueryId());
				MyUtils.saveTRECOutputFile(String.format("mmr%.2f.txt", lambda), trec_topics.getQueryId(), "Q0", 
											String.format("MMRl%.2f",lambda), scores, !first);
				System.out.println("Elapsed time for query" + trec_topics.getQueryId() + ": " 
						+ (System.currentTimeMillis() - queryStart)/1000 + " seconds");
				if(first) first = false;
				
				List<Result> retrievedDocs = new ArrayList<>();
				List<Qrel> queryQrels = MyUtils.filterQrelList(queryID, 0, qrels);
				int rank = 0;
				for (Map.Entry<String, Double> entry : scores.entrySet()) {
					retrievedDocs.add( new Result(entry.getKey(), rank++, entry.getValue()) );
				}
				System.out.println(String.format("NDCG for topic: %s, nqrels: %d, ndocs: %d", queryID, queryQrels.size(), retrievedDocs.size() ));
				if(retrievedDocs.size() > 0) {
					for(int k: mmrValues1.keySet()) {
						mmrValues1.put(k, mmrValues1.get(k) + AlphaNDCG.compute(k, 0.1, retrievedDocs, queryQrels));
						mmrValues5.put(k, mmrValues5.get(k) + AlphaNDCG.compute(k, 0.5, retrievedDocs, queryQrels));
						mmrValues9.put(k, mmrValues9.get(k) + AlphaNDCG.compute(k, 0.9, retrievedDocs, queryQrels));
					}
				}
				i++;
			}
			System.out.println("MMR scoring processed in: " + (System.currentTimeMillis()-start)/1000 + "seconds");
			
			for(int k: mmrValues1.keySet()) { //calculate the mean Alpha-NDCG across queries
				mmrValues1.put(k, mmrValues1.get(k) / i);
				mmrValues5.put(k, mmrValues5.get(k) / i);
				mmrValues9.put(k, mmrValues9.get(k) / i);
			}
			MyUtils.saveOutputFile(String.format("mmr%.2f_ndcg.txt", lambda), String.format("mmr\nlambda=%.2f\n",lambda), 
					"alpha\t|K\t|alpha-NDCG@K\n", mmrValues1, false, String.format("%.2f", 0.1));
			MyUtils.saveOutputFile(String.format("mmr%.2f_ndcg.txt", lambda), String.format("mmr\nlambda=%.2f\n",lambda), 
					"alpha\t|K\t|alpha-NDCG@K\n", mmrValues5, true, String.format("%.2f", 0.5));
			MyUtils.saveOutputFile(String.format("mmr%.2f_ndcg.txt", lambda), String.format("mmr\nlambda=%.2f\n",lambda), 
					"alpha\t|K\t|alpha-NDCG@K\n", mmrValues9, true, String.format("%.2f", 0.9));
		}catch(Exception e) {
			e.printStackTrace();
		} finally {
			scorer.closeIndex();
		}
		
		
		
	}
	
	/**
	 * Process the Portfolio scoring function over a pre-ordered retrieval process that used BM25 as a matching model
	 * @param trec_topics
	 * @param index_path
	 * @param qrel_path
	 * @param b
	 */
	public static void processPortfolioScoring(TRECQuery trec_topics, String index_path, String qrel_path, Integer b) {
		
		ArrayList <Qrel> qrels = Utils.loadQrels(qrel_path, QREL.DIVERSITY);
		long start = System.currentTimeMillis();
		PortfolioScoring scorer = new PortfolioScoring(index_path, "terrier_clueweb_index", "BM25", 100);
		try {
			Map<Integer, Double> portValues1 = initlogVar();
			Map<Integer, Double> portValues5 = initlogVar();
			Map<Integer, Double> portValues9 = initlogVar();
			Boolean first = true;
			int i = 0;
			while(trec_topics.hasNext()) {
				long queryStart = System.currentTimeMillis();
				HashMap<String, Double> scores = scorer.buildResultSet(i+"", trec_topics.next(), b);
				Integer queryID = Integer.valueOf(trec_topics.getQueryId());
				MyUtils.saveTRECOutputFile("portfolio" + b +".txt", trec_topics.getQueryId(), "Q0", "PORTFOLIOb"+b, scores, !first);
				System.out.println("Elapsed time for query" + trec_topics.getQueryId() + ": " 
						+ (System.currentTimeMillis() - queryStart)/1000 + " seconds");
				if(first) first = false;
				
				List<Result> retrievedDocs = new ArrayList<>();
				List<Qrel> queryQrels = MyUtils.filterQrelList(queryID, 0, qrels);
				int rank = 0;
				for (Map.Entry<String, Double> entry : scores.entrySet()) {
					retrievedDocs.add( new Result(entry.getKey(), rank++, entry.getValue()) );
				}
				System.out.println(String.format("NDCG for topic: %s, nqrels: %d, ndocs: %d", queryID, queryQrels.size(), retrievedDocs.size() ));
				if(retrievedDocs.size() > 0) {
					for(int k: portValues1.keySet()) {
						portValues1.put(k, portValues1.get(k) + AlphaNDCG.compute(k, 0.1, retrievedDocs, queryQrels));
						portValues5.put(k, portValues5.get(k) + AlphaNDCG.compute(k, 0.5, retrievedDocs, queryQrels));
						portValues9.put(k, portValues9.get(k) + AlphaNDCG.compute(k, 0.9, retrievedDocs, queryQrels));
					}
				}
				i++;
			}
			System.out.println("MMR scoring processed in: " + (System.currentTimeMillis()-start)/1000 + "seconds");
			
			for(int k: portValues1.keySet()) { //calculate the mean Alpha-NDCG across queries
				portValues1.put(k, portValues1.get(k) / i);
				portValues5.put(k, portValues5.get(k) / i);
				portValues9.put(k, portValues9.get(k) / i);
			}
			MyUtils.saveOutputFile("portfolio"+b+"_ndcg.txt", String.format("portfolio\nb=%d\n",b), 
					"alpha\t|K\t|alpha-NDCG@K\n", portValues1, false, String.format("%.2f", 0.1));
			MyUtils.saveOutputFile("portfolio"+b+"_ndcg.txt", String.format("portfolio\nb=%d\n",b), 
					"alpha\t|K\t|alpha-NDCG@K\n", portValues5, true, String.format("%.2f", 0.5));
			MyUtils.saveOutputFile("portfolio"+b+"_ndcg.txt", String.format("portfolio\nb=%d\n",b), 
					"alpha\t|K\t|alpha-NDCG@K\n", portValues9, true, String.format("%.2f", 0.9));
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			scorer.closeIndex();
		}

	}

}
