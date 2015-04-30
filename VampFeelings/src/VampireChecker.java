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
	public Tuple<HashMap<String, ArrayList<Tuple<Integer,Integer>>>,
	HashMap<Integer,Integer>> getNearVampiresSCount(){

		
		
	    Iterator it = indexMap.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String, ArrayList<Tuple<Integer,Integer>>> pair = (Map.Entry)it.next();
	        String mentionName = pair.getKey();
	        ArrayList<Tuple<Integer,Integer>> indexMentions = pair.getValue(); 
	        
	        //Iterate over every mention and check to see if near vampire. Also construct set
	        //to keep track of sentence numbers for a particular vampire mention (+ anaphoras)
	        Set<Integer> uniqueSentenceNumbers = new HashSet<Integer>();
	        
	        //Here is a possible issue: we're going through every single anaphora and checking it
	        //separately to see if it's near 'vampire.' I think this is giving us better results
	        //than if we were just to find 'vampire' near one of the anaphora group and immediately 
	        //assign every mention to be a vampire character, but...
	        Iterator indMenIt = indexMentions.iterator();
	        while (indMenIt.hasNext()){
	        	Tuple<Integer,Integer> toCheck = (Tuple<Integer,Integer>)(indMenIt.next());
	        	
	        	//Get the sentence number. If not near vampire, remove index both from NER hashmap
	        	//and from sentence number hashmap.
	        	Integer sentenceIndex = indexToSentenceNum.get(toCheck.toString());
	        	System.out.print("Sentence we are checking for vampires...." + sentenceIndex);
	        	if(!isNearVampire(sentenceIndex)){
	        		indMenIt.remove();
	        		//TODO INVESTIGATE PROBLEMS WITH THIS
//	        		indexToSentenceNum.remove(toCheck.toString());
	        		continue;
	        	}
	        	
	        	//Ostensibly around a vampire:
//	    		List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
//	    		CoreMap sentence = sentences.get(sentenceIndex);
//	    		System.out.println("Sentence index:" + sentenceIndex);
//	    		System.out.println("Sentence itself: " + sentence);
//	        	System.out.println("Place to check" + toCheck);
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
//		System.out.println("In isNearVampire, sentence we are checking is: " + sentenceIndex);
		//get sentence tokens and iterate
		List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
//		System.out.println("Size of sentences in VampireChecker..." + sentences.size());
		CoreMap sentence = sentences.get(sentenceIndex);
		for (CoreLabel token: sentence.get(TokensAnnotation.class)){
			String tokString = token.value().toLowerCase();
			if(tokString.equalsIgnoreCase("vampire") ||
					tokString.equalsIgnoreCase("vampires") ||
					tokString.equalsIgnoreCase("vampyre") ||
					tokString.equalsIgnoreCase("vampyres")){
				System.out.println("and it includes vampires: " + sentence.toString());
				return true;
			}
		}
		return false;
	}
	
	

}
