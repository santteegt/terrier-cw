package uk.ac.ucl.assignment.diversity;

import java.util.ArrayList;
import java.util.HashMap;


import org.terrier.structures.DocumentIndex;
import org.terrier.structures.Index;
import org.terrier.structures.Lexicon;

public class MMRScoring {

	/* Terrier Index */
	Index index;
	
	/* Index structures*/
	/* list of terms in the index */
	Lexicon<String> term_lexicon = null;
	/* list of documents in the index */
	DocumentIndex doi = null;
	
	/* Collection statistics */
	long total_tokens;
	long total_documents;
	
	
	/* Initialize MMR model with index. Use 
	 * @param index_path : initialize index 
	 * @param prefix : language prefix for index (default = 'en')
	 * with location of index created using bash script.
	 */
	public MMRScoring(String index_path, String prefix) {
		
	}
	
	
	public double getSimilarity(int document_id1, int document_id2) {
		return 0;
		
	}
	
	
	public double scoreDocumentWrtList(int document_id, String query, ArrayList <Integer> result_list)
	{
		return 0;
	}
	
	public double scoreDocument(int document_id, String query, double lambda)
	{
		return 0;
	}
	public HashMap <String, Double> buildResultSet(String query, double lambda)
	{
		HashMap<String, Double> scores  = null;
		return scores;
		
	}
}
