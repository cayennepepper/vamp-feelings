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

/*
 * This class does the emotion lookup for vampires! The prototyping is done with the NRC emotion lexicon. 
 * Other lexicons may or may not be used. First we grab the whole NRC file. We stick it into a hashmap.
 * This hashmap hashes from word->bits, where the bits are e.g. 00101000 for anger, anticipation, disgust, 
 * fear, joy, negative, positive, sadness, 	surprise, trust. We also take in a list of sentences whose words
 * need to be checked in the emotion lexicon. Yay.
 */

public class EmotionLookup {
	private Annotation doc;
	private HashMap<Integer,Integer> sentenceCounts;
	private HashMap<String,String> nrcLex;
	
	
	
	public EmotionLookup(HashMap<Integer,Integer> sCount, Annotation d){
		this.sentenceCounts = sCount;
		this.nrcLex = new HashMap<String,String>();
		this.doc = d;
	}
	
	public EmotionLookup(){
		this.nrcLex = new HashMap<String,String>();
	}
	
	/*
	 * Places lexicon into hashmap.
	 */
	public void hashNRCLex() throws Exception{
		String filePath = "../NRC-Emotion-Lexicon-v0.92/NRC-emotion-lexicon-wordlevel-alphabetized-v0.92.txt";
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filePath));
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		String line = "";
		line = br.readLine();
		StringBuilder sb = new StringBuilder();
		int index = 0; //Goes in 10s
		
		while(line != null){
			String[] split = line.split("\t");
			String wd = split[0];
			if(wd.contains("vampire")){
				System.out.println("Oops. Dict contains vampire");
			}
			String emotPres = split[2];
			sb.append(emotPres);
			
			line = br.readLine();
			if(index == 9){
				String fullEmotions = sb.toString();
				nrcLex.put(wd, fullEmotions);
				sb.setLength(0);
				index = 0;
			} else {
				index++;
			}
		}
		
	}
	
	public HashMap<String,Integer> HashTopicLists(ArrayList<ArrayList<String>> topicLists){
		HashMap<String,Integer> emotionMap = new HashMap<String,Integer>(){{
			put("0", 0);
			put("1", 0);
			put("2", 0);
			put("3", 0);
			put("4", 0);
			put("5", 0);
			put("6", 0);
			put("7", 0);
			put("8", 0);
			put("9", 0);
		}};
		
		
		for(ArrayList<String> topicList: topicLists){
			for(String wd : topicList){
				if(nrcLex.containsKey(wd)){
	            	String emotionBlock = nrcLex.get(wd);
	            	for(int j = 0; j<10; j++){
	            		String mapIndex = "" + j;
	            		Integer prevCount = emotionMap.get(mapIndex);
	            		Integer plusCount = Character.getNumericValue(emotionBlock.charAt(j));
	            		emotionMap.put(mapIndex, prevCount+plusCount);
	            	}
				}
			}
		}
		return emotionMap;
	}
	
	public HashMap<String,Integer> checkSentences(){
		List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
//		HashMap<String,Integer> emotionMap = new HashMap<String,Integer>(){{
//			put("anger", 0);
//			put("anticipation", 0);
//			put("disgust", 0);
//			put("fear", 0);
//			put("joy", 0);
//			put("negative", 0);
//			put("positive", 0);
//			put("sadness", 0);
//			put("surprise", 0);
//			put("trust", 0);
//		}};
		HashMap<String,Integer> emotionMap = new HashMap<String,Integer>(){{
			put("0", 0);
			put("1", 0);
			put("2", 0);
			put("3", 0);
			put("4", 0);
			put("5", 0);
			put("6", 0);
			put("7", 0);
			put("8", 0);
			put("9", 0);
		}};
		
		
		
		Integer tally = 0;
		Iterator it = sentenceCounts.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<Integer,Integer> pair = (Map.Entry)it.next();
	        Integer sIndex = pair.getKey();
	        Integer sCount = pair.getValue();
	        CoreMap sentence = sentences.get(sIndex);
	        for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
	            String word = (token.get(LemmaAnnotation.class));
	            if(nrcLex.containsKey(word)){
	            	tally++;
	            	String emotionBlock = nrcLex.get(word);
	            	for(int j = 0; j<10; j++){
	            		String mapIndex = "" + j;
	            		Integer prevCount = emotionMap.get(mapIndex);
	            		Integer plusCount = Character.getNumericValue(emotionBlock.charAt(j));
	            		emotionMap.put(mapIndex, prevCount+plusCount);
	            	}
	            }
	                        
	        }
	        
	    }//endwhile

	    System.out.println("Total number of words looked up: " + tally);
	    return emotionMap;
	    
	}

	
	
	public static void main(String[] args) {
	}

}
