package uk.ac.ucl.assignment.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.io.IOUtils;

import uk.ac.ucl.assignment.eval.Qrel;
import uk.ac.ucl.assignment.eval.Result;

/**
 * Utility functions for the assignment
 * @author santteegt
 *
 */
public class MyUtils {

	/**
	 * Find the judgement qrel related with the retrieved document
	 * @param entry
	 * @param qrels
	 * @return
	 */
	public static Qrel findQrel(final Result entry, List<Qrel> qrels) {
		return IterableUtils.find(qrels,new Predicate<Qrel>() {
			@Override
			public boolean evaluate(Qrel arg0) {
				return arg0.getDocument_id().equals(entry.getDocument_id());
			}
			
		});
	}
	
	/**
	 * filter related qrels by query ID
	 * @param queryID
	 * @param subTopicId
	 * @param qrels
	 * @return
	 */
	public static List<Qrel> filterQrelList(final Integer queryID, final Integer subTopicId, List<Qrel> qrels) {
		return IterableUtils.toList(IterableUtils.filteredIterable(qrels, new Predicate<Qrel>() {
			@Override
			public boolean evaluate(Qrel arg0) {
				return arg0.getTopic_no() == queryID; 
						//&& arg0.getSubtopic_no() == subTopicId;
			}
			
		}));
	}
	
	/**
	 * Save results on txt file
	 * @param filename
	 * @param title
	 * @param header
	 * @param data
	 */
	public static void saveOutputFile(String filename, String title, String header, Map<?, ?> data, Boolean append, String ... param) {
		try {
			OutputStream out = new FileOutputStream(filename, append);
			TreeMap<?, ?> sortedData = new TreeMap<>(data);
			if(!append) {
				IOUtils.write(title, out);
				IOUtils.write(header, out);
			}
			for(Object key: sortedData.keySet()) {
				if(param.length > 0) {
					IOUtils.write(
							String.format("%s\t|%s\t|%.3f\n", param[0], key.toString(), Double.valueOf(sortedData.get(key).toString()) ), out);					
				} else {
					IOUtils.write(
							String.format("%s\t|\t%.3f\n", key.toString(), Double.valueOf(sortedData.get(key).toString()) ), out);					
				}
			}
			out.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Save results as a TREC-formatted output file
	 * @param filename
	 * @param topicId
	 * @param subtopicId
	 * @param evalMethod
	 * @param data
	 * @param append
	 */
	public static void saveTRECOutputFile(String filename, String topicId, String subtopicId, String evalMethod, 
			Map<?, ?> data, Boolean append) {
		try {
			OutputStream out = new FileOutputStream(filename, append);
			//TreeMap<?, ?> sortedData = new TreeMap<>(data);
			int i = 0;
			for(Object key: data.keySet()) {
				IOUtils.write(
						String.format("%s %s %s %d %.3f %s\n", topicId, subtopicId, key.toString(), i++, 
								Double.valueOf(data.get(key).toString()), evalMethod ), out);
			}
			out.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
}
