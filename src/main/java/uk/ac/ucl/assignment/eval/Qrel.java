package uk.ac.ucl.assignment.eval;

/**
 * Bean that represent a Qrel judgement
 * @author santteegt
 *
 */
public class Qrel {
	
	String document_id ;
	int topic_no ;
	int subtopic_no ;
	int judgment ;
	
	/**
	 * Initialize the Qrel object with query, document and 
	 * its relevance grade.
	 * @param topic
	 * @param doc_id
	 * @param rel
	 */
	public Qrel(String topic, String doc_id, String rel) {
		// TODO Auto-generated constructor stub
		this.topic_no = Integer.valueOf(topic);
		this.document_id = doc_id;
		this.judgment = Integer.valueOf(rel);
		
	}

	/**
	 * Initialize the Qrel object with query, subtopic, document and 
	 * its relevance grade with respect to query subtopic.
	 * @param topic
	 * @param subtopic
	 * @param doc_id
	 * @param rel
	 */
	public Qrel(String topic, String subtopic,  String doc_id, String rel) {
		// TODO Auto-generated constructor stub
		this.topic_no = Integer.valueOf(topic);
		this.subtopic_no = Integer.valueOf(subtopic);
		this.document_id = doc_id;
		this.judgment = Integer.valueOf(rel);
		
	}

	public String getDocument_id() {
		return document_id;
	}

	public void setDocument_id(String document_id) {
		this.document_id = document_id;
	}

	public int getTopic_no() {
		return topic_no;
	}

	public void setTopic_no(int topic_no) {
		this.topic_no = topic_no;
	}

	public int getSubtopic_no() {
		return subtopic_no;
	}

	public void setSubtopic_no(int subtopic_no) {
		this.subtopic_no = subtopic_no;
	}

	public int getJudgment() {
		return judgment;
	}

	public void setJudgment(int judgment) {
		this.judgment = judgment;
	}
	
	
	
	

}
