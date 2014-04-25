package edu.cmu.lti.f13.hw4.hw4_139323.casconsumers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;
import org.uimafit.util.FSCollectionFactory;

import edu.cmu.lti.f13.hw4.hw4_139323.VectorSpaceRetrieval;
import edu.cmu.lti.f13.hw4.hw4_139323.typesystems.Document;
import edu.cmu.lti.f13.hw4.hw4_139323.typesystems.Token;
import edu.cmu.lti.f13.hw4.hw4_139323.utils.Answer;
import edu.cmu.lti.f13.hw4.hw4_139323.utils.Retrieval;


public class RetrievalEvaluator extends CasConsumer_ImplBase {

	/** query id number **/
	public ArrayList<Integer> qIdList;
	
	/** query and text relevant values **/
	public ArrayList<Integer> relList;
	
	/** document text **/
	public ArrayList<String> textList;
	
	/** document vector represented by Maps**/
	public ArrayList<Map<String, Integer>> documents;
	
	/** retrieval wrapper list**/
	public ArrayList<Retrieval> retrievals;
	
	/** all the relevant words for analysis**/
	public Set<String> globalWordSet;
	
	/** stopwords provided in a file**/
	public Set<String> stopwordSet;
		
	public void initialize() throws ResourceInitializationException {

		qIdList			= new ArrayList<Integer>();
		relList			= new ArrayList<Integer>();
		textList		= new ArrayList<String>();
		documents 		= new ArrayList<Map<String, Integer>>();
		retrievals		= new ArrayList<Retrieval>();
		globalWordSet	= new HashSet<String>();
		stopwordSet		= new HashSet<String>();
		LoadStopwords();
	}

	/**
	 * TODO: 
	 * 		1. construct the global word dictionary 
	 * 		2. keep the word frequency for each sentence
	 */
	@Override
	public void processCas(CAS aCas) throws ResourceProcessException {

		JCas jcas;
		try {
			jcas =aCas.getJCas();
		} catch (CASException e) {
			throw new ResourceProcessException(e);
		}
		
		//Traverse the index for all the documents found
		FSIterator it = jcas.getAnnotationIndex(Document.type).iterator();
		if (it.hasNext()) {
			Document doc = (Document) it.next();

			//Make sure that your previous annotators have populated this in CAS
			FSList fsTokenList = doc.getTokenList();
			//ArrayList<Token>tokenList=Utils.fromFSListToCollection(fsTokenList, Token.class);

			qIdList.add(doc.getQueryID());
			relList.add(doc.getRelevanceValue());
			
			//Do something useful here
			textList.add(doc.getText());
			Map<String, Integer> docVector = new HashMap<String, Integer>();
			for (Token token : 
				FSCollectionFactory.create(fsTokenList,Token.class)) {
				String tokenText = token.getText();
				//Only add word if it is not a stopword
				if ( !stopwordSet.contains(tokenText)) {
					globalWordSet.add(tokenText);
					docVector.put(tokenText, token.getFrequency());
				}
			}
			documents.add(docVector);
		}
	}

	/**
	 * TODO:
	 * 		1. Compute Cosine Similarity (OK)
	 * 		2. rank the retrieved sentences 
	 * 		3. Compute the MRR metric
	 */
	@Override
	public void collectionProcessComplete(ProcessTrace arg0)
			throws ResourceProcessException, IOException {

		super.collectionProcessComplete(arg0);

		parseRetrievals(retrievals);
		
		scoreRetrievals(retrievals);
		
		double metric_mrr = compute_mrr(retrievals);
		System.out.println(" (MRR) Mean Reciprocal Rank ::" + metric_mrr);
	}

	/**
	 * 
	 * @return cosine_similarity
	 */
	private double computeCosineSimilarity(Map<String, Integer> queryVector,
			Map<String, Integer> docVector) {
		double cosine_similarity=0.0;
		double dotProduct = computeDotProduct(queryVector, docVector);
		double queryMag = getMagnitude(queryVector);
		double docMag = getMagnitude(docVector);
		
		cosine_similarity =  dotProduct/ (queryMag * docMag);

		return cosine_similarity;
	}
	
	private double computeDotProduct(Map<String, Integer> queryVector,
			Map<String, Integer> docVector) {
		double dotProduct = 0.0;
		
		Iterator queryIter = queryVector.entrySet().iterator();
		while(queryIter.hasNext()) {
			Map.Entry queryEntry = (Map.Entry)queryIter.next();
			String queryKey = (String)queryEntry.getKey();
			Integer queryVal = (Integer)queryEntry.getValue();
			
			if( globalWordSet.contains(queryKey) &&
				docVector.containsKey(queryKey) ) {
				dotProduct += queryVal.doubleValue() * docVector.get(queryKey).doubleValue();
			}
		}
		
		return dotProduct;
	}
	
	/*
	 * This is calculated in L2
	 */
	private double getMagnitude(Map<String, Integer> vector) {
		double magnitude = 0.0;
		
		Iterator vectorIter = vector.entrySet().iterator();
		while(vectorIter.hasNext()) {
			Map.Entry vectorEntry = (Map.Entry)vectorIter.next();
			double entryVal = ((Integer)vectorEntry.getValue()).doubleValue(); 
			magnitude += entryVal * entryVal;
		}
		magnitude = Math.sqrt(magnitude);
		
		return magnitude;
	}

	/**
	 * 
	 * @return mrr
	 */
	private double compute_mrr(List<Retrieval> retList) {
		double metric_mrr=0.0;

		for(Retrieval retrieval : retList) {
			metric_mrr += 1.0/(double)retrieval.computeRank();
			printRetrieval(retrieval);
		}
		metric_mrr *= 1.0/retList.size();
		
		return metric_mrr;
	}
	
	/**
	 * Builds up a list of Retrieval objects from Document lists
	 */
	private void parseRetrievals(List<Retrieval> retList) {
		Retrieval curRetrieval = null;
		for(int i =0; i<qIdList.size(); ++i) {
			int qid = qIdList.get(i);
			int rel = relList.get(i);
			String text = textList.get(i);
			//Each retrieval starts with its query denoted
			//with a relevance = 99
			if( rel == 99) {
				curRetrieval = new Retrieval(qid, text, documents.get(i) );
				retList.add(curRetrieval);
			}
			else {
				//Add answer to current retrieval
				Answer curAnswer = new Answer();
				curAnswer.QID		= qid;
				curAnswer.Relevance	= rel;
				curAnswer.Text		= text;
				curAnswer.Vector	= documents.get(i);
				curRetrieval.Answers.add(curAnswer);
			}
		}
	}
	
	/**
	 * 
	 */
	private void scoreRetrievals(List<Retrieval> retList) {
		for(Retrieval curRetrieval:retrievals) {
			for(Answer curAnswer: curRetrieval.Answers) {
				curAnswer.Score = computeCosineSimilarity(curRetrieval.QueryVector, curAnswer.Vector);
			}
		}
	}
	
	/**
	 * Prints the retrieval information
	 */
	private void printRetrieval(Retrieval ret) {
		String output = "";
		output += "Query:\n";
		output += "   QID:  "+ ret.QID +"\n";
		output += "   Text: "+ ret.QueryText + "\n";
		output += "   Rank: " + ret.Rank +"\n";
		output += "Answers:\n";
		for(Answer curAnswer: ret.Answers) {
			output += "   Rel: "+curAnswer.Relevance;
			output += String.format("  Score: %.5f", curAnswer.Score);
			output += "  " + curAnswer.Text + "\n"; 
		}
		
		
		System.out.println(output);
	}
	
	/**
	 * Loads the stopwords provided in the original resources' file
	 * Each line of the file contains a unique stopword
	 */
	private void LoadStopwords() {
		try {
			
			URL docUrl = RetrievalEvaluator.class.getResource("/stopwords.txt");
		    if (docUrl == null) {
		       throw new IllegalArgumentException("Error opening stopwords.txt");
		    }
		    //Each line contains a stopword
		    String sLine;
			BufferedReader br = new BufferedReader(new InputStreamReader(docUrl.openStream()));
			while ((sLine = br.readLine()) != null)   {
				stopwordSet.add(sLine);
			}
			//Clean up
			br.close();
			br=null;
		} catch(IOException e) {
			System.err.println("Error while loading the stopwords");
		}
	}
}
