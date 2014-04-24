package edu.cmu.lti.f13.hw4.hw4_139323.annotators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.IntegerArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.FSCollectionFactory;

import edu.cmu.lti.f13.hw4.hw4_139323.typesystems.Document;
import edu.cmu.lti.f13.hw4.hw4_139323.typesystems.Token;

public class DocumentVectorAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {

		FSIterator<Annotation> iter = jcas.getAnnotationIndex().iterator();
		if (iter.isValid()) {
			iter.moveToNext();
			Document doc = (Document) iter.get();
			createTermFreqVector(jcas, doc);
		}

	}
	/**
	 * 
	 * @param jcas
	 * @param doc
	 */

	private void createTermFreqVector(JCas jcas, Document doc) {

		String docText = doc.getText();
		
		//Tokenize the text
		String[]	words = docText.split(" ");
		//Convert to lower case 
		for(int idx=0; idx<words.length; ++idx) {
			words[idx] = words[idx].toLowerCase();
		}
		//Eliminate repeated through the use of a set
		Set<String> wordSet = new HashSet<String>(Arrays.asList(words));
		Iterator setIter = wordSet.iterator();
		//Create the token list
		List<Token> tokens = new ArrayList<Token>();
		while(setIter.hasNext()) {
			//Get the frequency of each token in the set
			String tokenText = (String)setIter.next();
			int tokenFreq = 0;
			for(String word : words) {
				tokenFreq += tokenText.equals(word)? 1: 0;
			}
			//Add the token data into the index and the tokenlist
			Token newToken = new Token(jcas);
			newToken.setText(tokenText);
			newToken.setFrequency(tokenFreq);
			newToken.addToIndexes();
			tokens.add(newToken);
		}
		//Set tokenlist to the document
		doc.setTokenList(FSCollectionFactory.createFSList(jcas, tokens));
	}

}
