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

	public AnaphoraResolution(Annotation annotatedDoc, 
			HashMap<String, ArrayList<Tuple<Integer, Integer>>> inputNameIndex,
			HashMap<String, String> inputIndexToName) {
		this.doc = annotatedDoc;
		this.nameIndex = inputNameIndex;
		this.indexToName = inputIndexToName;
		
	}
	
	public HashMap<String, ArrayList<Tuple<Integer, Integer>>> getAnaphoraNameList(){
		//Get the coreference-resolved material from the nlp library
        Map<Integer, CorefChain> coref = doc.get(CorefChainAnnotation.class);
        
        
        for(Map.Entry<Integer, CorefChain> entry : coref.entrySet()) {
            CorefChain c = entry.getValue();

            //Avoid those that are only self-references
            if(c.getMentionsInTextualOrder() .size() <= 1)
                continue;

            //Take representative mention and build up the phrase that marks it. Store the begin index
            //and the very last end index. Check for a match in nameIndex to see if that pair of 
            //begin and end index is present in the hashmap. If so, add the index pair to the hashmap's
            //list and build up the mentions that way. I create a set of indices for the representative
            //mention and the other mentions to see if any of them match on any named entities.

            Set<Tuple<Integer,Integer>> indexSet = new HashSet<Tuple<Integer,Integer>>();
            
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
//                System.out.print(" And here is another mention: "+ m);
            	Integer bIndex = 0;
            	Integer eIndex = 0;
                tks = doc.get(SentencesAnnotation.class).get(m.sentNum-1).get(TokensAnnotation.class);
                for(int i = m.startIndex-1; i < m.endIndex-1; i++){
                	Integer beginIndex = tks.get(i).get(CharacterOffsetBeginAnnotation.class);
    				Integer endIndex = tks.get(i).get(CharacterOffsetEndAnnotation.class);
    				if (i == m.startIndex-1){
    					bIndex = beginIndex;
    				}
    				if (i == m.endIndex-2){
    					eIndex = endIndex;
    					indexSet.add(new Tuple(bIndex,eIndex)); //Can add after we know end index.
    				}
                }
//                System.out.println(" whose indices are: " + bIndex + "," + eIndex);
            }
            //Now we check everything against the mentions. This involves seeing if the index in question
            //appears in the index->name hashmap. If it does, we grab the name from the name->index hashmap 
            //and update its value with the entire set of indices and then we break :)
            
            for(Tuple ind : indexSet){
            	
            	if(!indexToName.containsKey(ind.toString())){ //We hash on strings!
            		continue;
            	}
        		String person = indexToName.get(ind.toString());
        		indexSet.remove(ind); //Get rid of repeat mention
				ArrayList<Tuple<Integer, Integer>> tempList = nameIndex.get(person); //get already existing
				ArrayList<Tuple<Integer,Integer>> setAsList = new ArrayList<Tuple<Integer,Integer>>(indexSet);
				tempList.addAll(setAsList);
				nameIndex.put(person, tempList);
				break;
            }
            
        }

        //Let's print all the crap!
        System.out.println("\n\n\n\n\n NOW WE HAVE ANAPHORIZED..... \n\n\n\n\n");
	    for (String personName : nameIndex.keySet()){
	    	//Get the list of mentions
	    	ArrayList<Tuple<Integer, Integer>> mentionList = nameIndex.get(personName);
	    	String val = mentionList.toString();
	    	System.out.println("Person: " + personName + " Mention List: " + val);
	    }
		
		return nameIndex;
	}
}
