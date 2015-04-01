//Add all the imports
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


//This class will be passed in a document with annotations and the dictionary of names 
//and the list of their mentions.
public class AnaphoraResolution {
	private Annotation doc;
	private HashMap<String, ArrayList<Tuple<Integer, Integer>>> nameIndex;
	private HashMap<String, String> indexToName;
	private HashMap<String, Integer> indexToSentenceNum = new HashMap<String, Integer>();
	private HashMap<Integer,Integer> beginIndToEnd;//Partial index checking
	private HashMap<Integer,Integer> endIndToBegin;//Partial index checking
	
	private int vampCount = 0;

	public AnaphoraResolution(Annotation annotatedDoc, 
			HashMap<String, ArrayList<Tuple<Integer, Integer>>> inputNameIndex,
			HashMap<String, String> inputIndexToName,
			HashMap<String, Integer> sentence2Index,
			Tuple<HashMap<Integer,Integer>,HashMap<Integer,Integer>> begEndEndBeg) {
		this.doc = annotatedDoc;
		this.nameIndex = inputNameIndex;
		this.indexToName = inputIndexToName;
		this.indexToSentenceNum = sentence2Index;
		this.beginIndToEnd = begEndEndBeg.x;
		this.endIndToBegin = begEndEndBeg.y;
	}
	
//	public HashMap<String, ArrayList<Tuple<Integer, Integer>>> getAnaphoraNameList(){
	public HashMap<Integer,Integer> getSentenceCounts(){

		//Get the coreference-resolved material from the nlp library
        Map<Integer, CorefChain> coref = doc.get(CorefChainAnnotation.class);
        
        
        for(Map.Entry<Integer, CorefChain> entry : coref.entrySet()) {
            CorefChain c = entry.getValue();

            //Avoid those that are only self-references
            if(c.getMentionsInTextualOrder() .size() <= 1){
                continue;
            }

            //Take representative mention and build up the phrase that marks it. Store the begin index
            //and the very last end index. Check for a match in nameIndex to see if that pair of 
            //begin and end index is present in the hashmap. If so, add the index pair to the hashmap's
            //list and build up the mentions that way. I create a set of indices for the representative
            //mention and the other mentions to see if any of them match on any named entities.
            //The third Integer is the sentence number
            Set<Tuple<Tuple<Integer,Integer>, Integer>> indexSet = 
            		new HashSet<Tuple<Tuple<Integer,Integer>, Integer>>();
            
            //This set is for mentions containing "vampire". These automatically get put into nameIndex,
            //with "the vampire" as being the named entity. We force the introduction of "the vampire" 
            //into the nameIndex hashmap.
            Set<Tuple<Tuple<Integer,Integer>, Integer>> vampireSet = 
            		new HashSet<Tuple<Tuple<Integer,Integer>, Integer>>();
            
            CorefMention cm = c.getRepresentativeMention();
            List<CoreLabel> tks = doc.get(SentencesAnnotation.class).get(cm.sentNum-1).get(TokensAnnotation.class);
            

            boolean thereIsVampMention = false;
            for(CorefMention m : c.getMentionsInTextualOrder()){
            	if(
            			(
            				(
            					m.toString().contains(new String("the vampire")) ||
            					m.toString().contains(new String("the vampyre")) ||
            					m.toString().contains(new String("The vampire")) ||
            					m.toString().contains(new String("The vampyre"))
            				) && m.toString().split(" ").length == 5
            			)
            				||
            				(	
            						(
            						m.toString().contains(new String("vampires")) || 
            						m.toString().contains(new String("vampyres"))
            						) && m.toString().split(" ").length == 4
            				)
            		){
//            		System.out.println("mention m contains vampire: " + m.toString());
            		thereIsVampMention = true;
            	}//This sees if one of the forms of 'vampire' listed above is included
            	Integer bIndex = 0;
            	Integer eIndex = 0;
            	Integer sNum = m.sentNum-1;
                tks = doc.get(SentencesAnnotation.class).get(m.sentNum-1).get(TokensAnnotation.class);
                for(int i = m.startIndex-1; i < m.endIndex-1; i++){
                	Integer beginIndex = tks.get(i).get(CharacterOffsetBeginAnnotation.class);
    				Integer endIndex = tks.get(i).get(CharacterOffsetEndAnnotation.class);
    				if (i == m.startIndex-1){
    					bIndex = beginIndex;
    				}
    				if (i == m.endIndex-2){
    					eIndex = endIndex;
    					Tuple<Tuple<Integer,Integer>, Integer> newTup = 
    							new Tuple((new Tuple(bIndex, eIndex)), sNum);
    					indexSet.add(newTup); //Can add after we know end index.
    					
    				}
                }
            }
            if(thereIsVampMention){
            	vampireSet.addAll(indexSet);
            }
            
            //Now we check everything against the mentions. This involves seeing if the index in question
            //appears in the index->name hashmap. If it does, we grab the name from the name->index hashmap 
            //and update its value with the entire set of indices and then we break :)
            
            for(Tuple<Tuple<Integer,Integer>, Integer> ind : indexSet){
            	Tuple<Integer,Integer> pruneTup = ind.x;
            	            	
            	if(!indexToName.containsKey(pruneTup.toString())){ //We hash on strings!
            		//Just because the exact indices don't exist, doesn't mean there wasn't a mismatch between
            		//anaphora graphs and named entities! This is where partial indices come in. We check if
            		//there's a match on begin index OR end end index!
            		Integer beginLookup = pruneTup.x;
            		Integer endLookup = pruneTup.y;
            		if(beginIndToEnd.containsKey(beginLookup)){
//            			System.out.println("Have a word that matches on just begin index!");
            			Integer endIndex = beginIndToEnd.get(beginLookup);
            			pruneTup = new Tuple<Integer,Integer>(beginLookup,endIndex);
            		} else if(endIndToBegin.containsKey(endLookup)){
//            			System.out.println("Have a word that matches on just end index!");
            			Integer beginIndex = endIndToBegin.get(endLookup);
            			pruneTup = new Tuple<Integer,Integer>(beginIndex,endLookup);
            		} else {
            			continue;
            		}
               	}
        		String person = indexToName.get(pruneTup.toString());
        		indexSet.remove(ind); //Get rid of repeat mention
				ArrayList<Tuple<Integer, Integer>> tempList = nameIndex.get(person); //get already existing
				//Get the arraylist of tuple< tuple<beginIndex, endIndex>, sentenceNum and strip the indices.
				//Those indices get put into nameIndex.
				//Place the sentence numbers into indexToSentenceNum.
				ArrayList<Tuple<Tuple<Integer,Integer>, Integer>> setAsList = 
						new ArrayList<Tuple<Tuple<Integer,Integer>, Integer>>(indexSet);
				for(Tuple<Tuple<Integer,Integer>, Integer> dubTup : setAsList){
					Tuple<Integer,Integer> indexTup = dubTup.x;
					Integer sentNum = dubTup.y;
					tempList.add(indexTup);
					indexToSentenceNum.put(indexTup.toString(), sentNum);
				}
				nameIndex.put(person, tempList);
				break;
            }
            
            //Now we mass-place all mentions containing "vampire" into nameIndex. We force the named entity
            //'the vampire' to be a key in nameIndex if it's not already there. Also place sentence numbers
            //of each mention into indexToSentenceNum
            if(!nameIndex.containsKey("the vampire")){
            	nameIndex.put("the vampire", new ArrayList<Tuple<Integer,Integer>>());
            }

            ArrayList<Tuple<Integer,Integer>> vampMentionList = nameIndex.get("the vampire");
            for(Tuple<Tuple<Integer,Integer>, Integer> vampireEntry : vampireSet){
            	vampCount++;
            	Tuple<Integer,Integer> indexTup = vampireEntry.x;
            	Integer sentNum = vampireEntry.y;
            	vampMentionList.add(indexTup);
            	indexToSentenceNum.put(indexTup.toString(), sentNum);
            }
            
        }
        
//        System.out.println("Total number of 'vampire'-containing mentions: " + vampCount);
        
        //TODO is this a problem??
        //Prune out everything that isn't around "vampire"
        VampireChecker checker = new VampireChecker(this.doc, this.nameIndex, this.indexToSentenceNum);
        HashMap<Integer,Integer> sentCount = checker.getNearVampiresSCount().y;
        nameIndex = checker.getNearVampiresSCount().x;        

//        //Let's print all the crap!
//        System.out.println("\n\n NOW WE HAVE ANAPHORIZED..... \n\n");
//	    for (String personName : nameIndex.keySet()){//Get the list of mentions
//	    	ArrayList<Tuple<Integer, Integer>> mentionList = nameIndex.get(personName);
//	    	String val = mentionList.toString();
//	    	System.out.println("Person: " + personName + " Mention List: " + val);
//	    }
		
//		return new Tuple(nameIndex, sentCount);
        return sentCount;
	}
}
