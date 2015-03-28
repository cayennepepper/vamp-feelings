import java.io.*;
import java.util.*;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.io.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.*;
import edu.stanford.nlp.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

//This class takes each word we suspect is referring to a vampire, and checks to see if it is in 10-word proximity
//to the word 'vampire.' Note that this is a very conservative approach and will throw out a lot of data,
//but it is a first pass.

public class VampireChecker {
	private HashMap<String, ArrayList<Tuple<Integer, Integer>>> indexMap;
	private HashMap<String, Integer> indexToSentenceNum;
	private Annotation doc;
	private HashMap<Integer, Integer> sentenceNumToCount;

	/**
	 * 
	 * @param annotatedDoc
	 * @param nameToIndices
	 * @param indToSentence - arrayList of index tuple as string to sentence number
	 */
	public VampireChecker(Annotation annotatedDoc, 
			HashMap<String, ArrayList<Tuple<Integer, Integer>>> nameToIndices,
			HashMap<String, Integer> indToSentence){
		this.doc = annotatedDoc;
		this.indexMap = nameToIndices;
		this.indexToSentenceNum = indToSentence;
		this.sentenceNumToCount = new HashMap<Integer,Integer>();
	}
	
	/*
	 * This weeds out everything in nameToIndices that isn't near "vampire". It simultaneously
	 * constructs a hashmap of Integer,Integer that will give the sentence numbers and the number
	 * of times those words need to be evaluated.
	 */
//	public HashMap<String, ArrayList<Tuple<Integer,Integer>>> getNearVampires(){
	public Tuple<HashMap<String, ArrayList<Tuple<Integer,Integer>>>,
	HashMap<Integer,Integer>> getNearVampiresSCount(){

		//Checking if indexToSentenceNum has anything that's null in it...
		String tempTup = (new Tuple<Integer,Integer>(new Integer(47232), new Integer(47238)).toString());
		if(indexToSentenceNum.containsKey(tempTup))
		{
			System.out.println("ALOHA! indexToSentenceNum contains the key in question :)");
			System.out.println("This is the sentence number its at: " + indexToSentenceNum.get(tempTup));
		}
		
		
		//DEBUGGING
//		Iterator blah = indexToSentenceNum.entrySet().iterator();
//		while (blah.hasNext()){
//			Map.Entry<String, Integer> p = (Map.Entry)blah.next();
//			System.out.println("Index: " + p.getKey() + " and sentence number: " + p.getValue());
//		}
		
		
	    Iterator it = indexMap.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String, ArrayList<Tuple<Integer,Integer>>> pair = (Map.Entry)it.next();
	        String mentionName = pair.getKey();
	        ArrayList<Tuple<Integer,Integer>> indexMentions = pair.getValue(); 
	        
	        //Iterate over every mention and check to see if near vampire. Also construct set
	        //to keep track of sentence numbers for a particular vampire mention (+ anaphoras)
	        Set<Integer> uniqueSentenceNumbers = new HashSet<Integer>();
	        
	        
	        Iterator indMenIt = indexMentions.iterator();
	        System.out.println("Size of indexMentions in VampireChecker: " + indexMentions.size());
	        int tempCount = 0;
	        while (indMenIt.hasNext()){
	        	tempCount++;
//		        System.out.println("TempCount is: " + tempCount);
	        	Tuple<Integer,Integer> toCheck = (Tuple<Integer,Integer>)(indMenIt.next());
	        	
	        	//Get the sentence number. If not near vampire, remove index both from NER hashmap
	        	//and from sentence number hashmap.
	        	System.out.println("toCheck to string is: " + toCheck.toString());
	        	Integer sentenceIndex = indexToSentenceNum.get(toCheck.toString());
	        	if(!isNearVampire(sentenceIndex)){
	        		indMenIt.remove();
//	        		indexToSentenceNum.remove(toCheck.toString());
	        		continue;
	        	}
	        	uniqueSentenceNumbers.add(sentenceIndex);
	        }
	        
	        //Update the counts for each sentence in the sentenceNumToCount hashmap
	        for(Integer i: uniqueSentenceNumbers){
		        if(!sentenceNumToCount.containsKey(i)){
		        	sentenceNumToCount.put(i, new Integer(1));
		        } else {
		        	Integer oldCount = sentenceNumToCount.get(i);
		        	sentenceNumToCount.put(i, oldCount+1);
		        }

	        }
	    }
//	    return indexMap;
	    return new Tuple(indexMap, sentenceNumToCount);
	}
	
	
	/*
	 * This takes indexMap and indexToSentenceNum. It goes 
	 */
	public HashMap<Integer,Integer> getSentenceCounts(){
		return null;
	}
	
	//This does the checking to see whether a word is within sentence containing "vampire"
	//May want to make it check the surrounding two sentences also
	public boolean isNearVampire(Integer sentenceIndex){
		//get sentence tokens and iterate
//		System.out.println("1");
		List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
		System.out.println("Sentence Index: " + sentenceIndex);
		CoreMap sentence = sentences.get(sentenceIndex);
//		System.out.println("Successfully got sentence index from sentences");
//		System.out.println("3");
		for (CoreLabel token: sentence.get(TokensAnnotation.class)){
			String tokString = token.value().toLowerCase();
			if(tokString.equalsIgnoreCase("vampire")){
				return true;
			}
		}
		return false;
	}
	
//	public boolean isNearVampire(List<CoreLabel> tokAnn){
//		for (CoreLabel token : tokAnn){
//			String tokString = token.value().toLowerCase();
//			if(tokString.equalsIgnoreCase("vampire")){
//				return true;
//			}
//		}
//		return false;
//	}
	
	//		List<CoreLabel>tks = doc.get(SentencesAnnotation.class).get(m.sentNum-1).get(TokensAnnotation.class)

}
