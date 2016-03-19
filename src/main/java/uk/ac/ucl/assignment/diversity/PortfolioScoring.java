package uk.ac.ucl.assignment.diversity;

import java.util.ArrayList;
import java.util.HashMap;

import org.terrier.structures.DocumentIndex;
import org.terrier.structures.Index;
import org.terrier.structures.Lexicon;

public class PortfolioScoring {

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


	/* *
	Computes correlation between term vectors of 2 documents .
	 * */
	public double getCorrelation (int document_id, String query, int document_id2)
	{
		return 0;
	}


	/* * Computes Variance * */
	public double computeVariance ( int document_id, ArrayList <Integer> result_list ) 
	{
		return 0;
	}


	/* * Computes Mean * */
	public double computeMean (int document_id, ArrayList < Integer > result_list ) 
	{
		return 0;
	}
	/* * Computes score of document * */
	public double scoreDocument (int document_id , String query )
	{

		return 0;
	}


	/* * Iteratively build the result list .
	The input parameters are search query and
	discount factor ( e.g. 1/r ) that yields rank
	specific w i.e. wj and the
	parameter b responsible for adjusting risk .
	 * */
	public HashMap <String, Double> buildResultSet (String query , double discount, double b)
	{

		HashMap<String, Double> scores  = null;
		return scores;
	}

}
