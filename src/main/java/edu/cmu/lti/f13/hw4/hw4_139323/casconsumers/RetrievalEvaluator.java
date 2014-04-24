package edu.cmu.lti.f13.hw4.hw4_139323.casconsumers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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


public class RetrievalEvaluator extends CasConsumer_ImplBase {

	/** query id number **/
	public ArrayList<Integer> qIdList;
	
	/** query and text relevant values **/
	public ArrayList<Integer> relList;
	
	/** all the relevant words for analysis**/
	public Set<String> globalWordSet;
	
	/** stopwords provided in a file**/
	public Set<String> stopwordSet;
	
	/** document vector represented by Maps**/
	public ArrayList<Map<String, Integer>> documents;
		
	public void initialize() throws ResourceInitializationException {

		qIdList			= new ArrayList<Integer>();
		relList			= new ArrayList<Integer>();
		documents 		= new ArrayList<Map<String, Integer>>();
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
			Map<String, Integer> docVector = new HashMap<String, Integer>();
			for (Token token : 
				FSCollectionFactory.create(fsTokenList,Token.class)) {

				String tokenText = token.getText();
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

		// TODO :: compute the cosine similarity measure
		
		
		
		// TODO :: compute the rank of retrieved sentences
		
		
		
		// TODO :: compute the metric:: mean reciprocal rank
		double metric_mrr = compute_mrr();
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
	private double compute_mrr() {
		double metric_mrr=0.0;

		// TODO :: compute Mean Reciprocal Rank (MRR) of the text collection
		
		return metric_mrr;
	}
	
	/**
	 * 
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
