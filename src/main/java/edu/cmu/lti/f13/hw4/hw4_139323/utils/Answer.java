package edu.cmu.lti.f13.hw4.hw4_139323.utils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * This class contains all the data required to score
 * an answer given from a retrieval
 * 
 * @author Salvador Medina
 *
 */
public class Answer implements Comparable<Answer>{
	
	public double 	Score;				// Score assigned
	public int 		QID;				// Query ID to which it belongs
	public int		Relevance;			// 1: correct 0: Incorrect
	public String 	Text;				// Raw String
	public Map<String, Integer> Vector;	// Tokens and their frequency
	
	
	public Answer() {
		QID 		= 0;
		Relevance	= 0;
		Score		= 0.0;
		Text		= "";
		Vector		= new HashMap<String, Integer>();
	}
	
	/**
	 * CompareTo is configured for a DESCENDING order
	 */
	@Override
	public int compareTo(Answer o) {
		if (Score < o.Score)
			return 1;
		if (Score > o.Score)
			return -1;
		return 0;
	}

}
