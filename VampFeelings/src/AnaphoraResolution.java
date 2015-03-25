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

	public AnaphoraResolution(Annotation annotatedDoc, 
			HashMap<String, ArrayList<Tuple<Integer, Integer>>> inputNameIndex,
			HashMap<String, String> inputIndexToName,
			HashMap<String, Integer> sentence2Index) {
		this.doc = annotatedDoc;
		this.nameIndex = inputNameIndex;
		this.indexToName = inputIndexToName;
		this.indexToSentenceNum = sentence2Index;
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
            
            CorefMention cm = c.getRepresentativeMention();
//            System.out.println("Here is a CorefMention: " + cm);
            Integer repMentionBeginInd = 0;
            Integer repMentionEndInd = 0;
            List<CoreLabel> tks = doc.get(SentencesAnnotation.class).get(cm.sentNum-1).get(TokensAnnotation.class);
//            for(int i = cm.startIndex-1; i < cm.endIndex-1; i++) {
//            	Integer beginIndex = tks.get(i).get(CharacterOffsetBeginAnnotation.class);
//				Integer endIndex = tks.get(i).get(CharacterOffsetEndAnnotation.class);
//				if (i == cm.startIndex-1){
//					repMentionBeginInd = beginIndex;
//				}
//				if (i == cm.endIndex-2){
//					repMentionEndInd = endIndex;
//				}
//            }
//            //Add repMention's indices to the indexSet
//            indexSet.add(new Tuple(repMentionBeginInd, repMentionEndInd));
            

            for(CorefMention m : c.getMentionsInTextualOrder()){
//                System.out.println(" And here is another mention: "+ m);
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
    					//Add to the index -> sentence number
    					String tupToString = newTup.toString();
    					
    				}
                }
            }
            //Now we check everything against the mentions. This involves seeing if the index in question
            //appears in the index->name hashmap. If it does, we grab the name from the name->index hashmap 
            //and update its value with the entire set of indices and then we break :)
            
            //TEMP: printing everything in indexSet and indexToName
//            System.out.println("Stuff in indexSet: ");
//            for (Tuple<Tuple<Integer,Integer>, Integer> it : indexSet){
//            	System.out.println(it);
//            }
//            System.out.println("Done with stuff in indexSet. Stuff in indexName: ");
//            for (Tuple<Tuple<Integer,Integer>, Integer> name: example.keySet()){
//
//                String key =name.toString();
//                String value = example.get(name).toString();  
//                System.out.println(key + " " + value);  
//            } 
            
            
            for(Tuple<Tuple<Integer,Integer>, Integer> ind : indexSet){
            	Tuple<Integer,Integer> pruneTup = ind.x;
            	            	
            	if(!indexToName.containsKey(pruneTup.toString())){ //We hash on strings!
            		continue;
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
            
        }
        
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
