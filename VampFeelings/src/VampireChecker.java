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
	HashMap<String, ArrayList<Tuple<Integer, Integer>>> indexMap;
	HashMap<String, Integer> indexToSentenceNum;

	Annotation doc;

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
	}
	
	//This weeds out everything in nameToIndices that isn't near "vampire"
	public HashMap<String, ArrayList<Tuple<Integer,Integer>>> getNearVampires(){
		//DEBUGGING
		Iterator blah = indexToSentenceNum.entrySet().iterator();
		while (blah.hasNext()){
			Map.Entry<String, Integer> p = (Map.Entry)blah.next();
			System.out.println("Index: " + p.getKey() + " and sentence number: " + p.getValue());
		}
		
		
	    Iterator it = indexMap.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String, ArrayList<Tuple<Integer,Integer>>> pair = (Map.Entry)it.next();
	        String mentionName = pair.getKey();
	        ArrayList<Tuple<Integer,Integer>> indexMentions = pair.getValue(); 
	        
	        //Iterate over every mention and check to see if near vampire
	        Iterator indMenIt = indexMentions.iterator();
	        while (indMenIt.hasNext()){
	        	//The way I do this now: check each mapped sentence to see if contains "vampire"
	        	//Might not be efficient
	        	Tuple<Integer,Integer> toCheck = (Tuple<Integer,Integer>)(indMenIt.next());
	        	System.out.println("Tuple to check: " + toCheck.toString());
	        		
	        	
	        	
	        	//TEMP: PRINTING OUT ALL OF INDEXTOSENTENCE NUM
//	        	System.out.println("Here is indexToSentenceNum: ");
//	        	for (String name: indexToSentenceNum.keySet()){
//	                String value = indexToSentenceNum.get(name).toString();  
//	                System.out.println(name + " " + value);  
//	        	} 
//	        	System.out.println("End indexToSentenceNum");
	        	
	        	
	        	
	        	//Get the sentence number. If not near vampire, remove index.
	        	Integer sentenceIndex = indexToSentenceNum.get(toCheck.toString());
	        	if(!isNearVampire(sentenceIndex)){
	        		indMenIt.remove();
	        	}
	        }
	    }
	    return indexMap;
	}
	
	//This does the checking to see whether a word is within sentence containing "vampire"
	//May want to make it check the surrounding two sentences also
	public boolean isNearVampire(Integer sentenceIndex){
		//get sentence tokens and iterate
//		System.out.println("1");
		List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
//		System.out.println("Sentence Index: " + sentenceIndex);
		CoreMap sentence = sentences.get(sentenceIndex);
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
