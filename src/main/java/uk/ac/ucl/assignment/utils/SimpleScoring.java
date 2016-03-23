package uk.ac.ucl.assignment.utils;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.terrier.applications.batchquerying.TRECQuery;
import org.terrier.matching.FatQueryResultSet;
import org.terrier.matching.ResultSet;
import org.terrier.querying.Manager;
import org.terrier.querying.SearchRequest;
import org.terrier.querying.parser.Query;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.Index;
import org.terrier.structures.Lexicon;
import org.terrier.utility.ApplicationSetup;

import uk.ac.ucl.assignment.diversity.MMRScoring;
import uk.ac.ucl.assignment.eval.NDCG;
import uk.ac.ucl.assignment.eval.Qrel;
import uk.ac.ucl.assignment.eval.Result;
import uk.ac.ucl.assignment.utils.Utils.QREL;

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
	
	
	/* Initialize Simple model with index. Use 
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
		// Just find documents and their posting list for the query. 
		

		// Create a search request object.
		Manager manager = new Manager(this.index);
		SearchRequest srq = manager.newSearchRequest(id, query);
		
		// Get the results using tfidf
//		srq.addMatchingModel("Matching", "TF_IDF");
		srq.addMatchingModel("Matching", this.matcherClass);
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
		
		
		HashMap <String, Double> scores = new HashMap<String, Double>();
		for (int i = 0 ; i < doc_scores.length;i++){
			//System.out.println(doc_ids[i]+" "+doc_scores[i]);
			String docName = doc_names[i].lastIndexOf("/") > 0  ? 
					 doc_names[i].substring(doc_names[i].lastIndexOf("/")+1, doc_names[i].lastIndexOf("."))
					:doc_names[i].substring(0, doc_names[i].lastIndexOf("."));
			scores.put(docName, doc_scores[i]);
//			if (i==10)
			if (i+1 == this.maxDocs)
				break;
		}
			
		
		return scores;
		
	}
	public void closeIndex() {
		try {
			index.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String [] args) {
		
		if(args.length < 5) {
			System.out.println("Not enough parameters: [terrier_home] [index_path] [topics_file] [qrels_file] [process]");
			return;
		}
		
		String terrierHome = args[0];
		String index_path = args[1];
		String topic_file_path = args[2];
		String qrel_path = args[3];
		String process = args[4];
		
		
		
		System.setProperty("terrier.home", terrierHome);
		
		// Topic file path
//		String topic_file_path = "/path/to/trec2013-topics.txt";
		
		// Index path
//		String index_path = "/path/to/terrier-core-4.1/var/index/";
		
		// Qrel path
//		String qrel_path = "/path/to/qrels.adhoc.txt";
		
		// Load the topics
		TRECQuery trec_topics = new TRECQuery(topic_file_path);
		
		// Load the qrels
//		ArrayList <Qrel> qrels = Utils.loadQrels(qrel_path, QREL.ADHOC);

		switch (process) {
			case "NDCG": processNDCGEvaluation(trec_topics, index_path, qrel_path); break;
			case "MMR": processMMRScoring(trec_topics, index_path, qrel_path); break;
			default: System.out.println(
					String.format(
							"\n===No process found with name %s. You should specify one of this: [NDCG, ]===", process));
		}
//		int i = 0;
//		
//		Map<Integer, Double> ndcgValues = new HashMap<>();
//		ndcgValues.put(1, 0.0);
//		ndcgValues.put(5, 0.0);
//		ndcgValues.put(10, 0.0);
//		ndcgValues.put(20, 0.0);
//		ndcgValues.put(30, 0.0);
//		ndcgValues.put(40, 0.0);
//		ndcgValues.put(50, 0.0);
//		while(trec_topics.hasNext())
//		{
////			String query = trec_topics.next();
//			HashMap<String, Double> scores = scorer.buildResultSet(i+"", trec_topics.next());
//			Integer queryID = Integer.valueOf(trec_topics.getQueryId());
//			
//			List<Result> retrievedDocs = new ArrayList<>();
//			List<Qrel> queryQrels = MyUtils.filterQrelList(queryID, 0, qrels);
//			int rank = 0;
//			for (Map.Entry<String, Double> entry : scores.entrySet()) {
//				
//				//Qrel judgedDoc = findQrel(queryID, entry, qrels);
//				//retrievedDocs.add( new Result(entry.getKey(), judgedDoc.getJudgment(), entry.getValue()) );
//				retrievedDocs.add( new Result(entry.getKey(), rank++, entry.getValue()) );
////				System.out.println(i+"\t"+entry.getKey()+"\t"+entry.getValue());
//			}
//			System.out.println(String.format("NDCG for topic: %s, nqrels: %d, ndocs: %d", queryID, queryQrels.size(), retrievedDocs.size() ));
//			if(retrievedDocs.size() > 0) {
//				for(int k: ndcgValues.keySet()) {
//					ndcgValues.put(k, ndcgValues.get(k) + NDCG.compute(retrievedDocs, queryQrels, k));
//				}
//			}
//			i++;
//		}
//		
//		for(int k: ndcgValues.keySet()) { //calculate the mean NDCG across queries
//			ndcgValues.put(k, ndcgValues.get(k) / i);
//		}
//		System.out.println("====Final Results: " + ndcgValues.toString());
		
		
	}
	
	public static void processNDCGEvaluation(TRECQuery trec_topics, String index_path, String qrel_path) {
		
		ArrayList <Qrel> qrels = Utils.loadQrels(qrel_path, QREL.ADHOC);
		// Initialize the scorer
		SimpleScoring scorer = new SimpleScoring(index_path, "terrier_clueweb_index", "BM25", 50);
				
		int i = 0;
		
		Map<Integer, Double> ndcgValues = new HashMap<>();
		ndcgValues.put(1, 0.0);
		ndcgValues.put(5, 0.0);
		ndcgValues.put(10, 0.0);
		ndcgValues.put(20, 0.0);
		ndcgValues.put(30, 0.0);
		ndcgValues.put(40, 0.0);
		ndcgValues.put(50, 0.0);
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
		MyUtils.saveOutputFile("bm25_ndcg.txt", "bm25\n", "K\t|\tNDCG@K\n", ndcgValues);
		System.out.println("====Final Results: " + ndcgValues.toString());
		
	}
	
	public static void processMMRScoring(TRECQuery trec_topics, String index_path, String qrel_path) {
		
		//ArrayList <Qrel> qrels = Utils.loadQrels(qrel_path, QREL.ADHOC);
		
		long start = System.currentTimeMillis();
		MMRScoring scorer = new MMRScoring(index_path, "terrier_clueweb_index", "BM25", 100);
		
		Boolean first = true;
		int i = 0;
		while(trec_topics.hasNext()) {
			long queryStart = System.currentTimeMillis();
			HashMap<String, Double> scores = scorer.buildResultSet(i+"", trec_topics.next(), 0.25);
			MyUtils.saveTRECOutputFile("mmr0.25.txt", trec_topics.getQueryId(), "Q0", "MMRl0.25", scores, !first);
			System.out.println("Elapsed time for query" + trec_topics.getQueryId() + ": " 
					+ (System.currentTimeMillis() - queryStart)/1000 + " seconds");
			if(first) first = false;
		}
		System.out.println("MMR scoring processed in: " + (System.currentTimeMillis()-start)/1000 + "seconds");
		
		
	}
	
//	public static Qrel findQrel(final String queryID, final Map.Entry<String, Double> entry, List<Qrel> qrels) {
//		return IterableUtils.find(qrels,new Predicate<Qrel>() {
//			@Override
//			public boolean evaluate(Qrel arg0) {
//				return arg0.getTopic_no() == Integer.valueOf(queryID) 
//						&& arg0.getDocument_id().equals(entry.getKey());
//			}
//			
//		});
//	}
}
