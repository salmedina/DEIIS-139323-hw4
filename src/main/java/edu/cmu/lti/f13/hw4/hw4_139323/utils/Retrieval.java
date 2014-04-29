package edu.cmu.lti.f13.hw4.hw4_139323.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 * This class works as a data structure to keep together
 * the data required to analyze a retrieval
 * 
 * @author Salvador Medina
 *
 */
public class Retrieval {
	public Map<String,Integer>	QueryVector;
	public String				QueryText;
	public Integer				QID;
	public int					Rank;
	public ArrayList<Answer> 	Answers;
	
	
	public Retrieval(Integer qid, String queryText, Map<String,Integer> query) {
		QID			= qid;
		Rank		= 0;
		QueryText 	= queryText;
		QueryVector		= query;
		Answers 	= new ArrayList<Answer>();
	}
	
	public int computeRank() {
		int rankPos = 0;
		SortAnswers();
		for(rankPos=0; rankPos<Answers.size(); ++rankPos) {
			if( ((Answer)Answers.get(rankPos)).Relevance ==1 ) {
				rankPos += 1;
				break;
			}
		}
		Rank = rankPos;
		return rankPos;
	}
	
	private void SortAnswers() {
		Collections.sort(Answers);
	}
}
