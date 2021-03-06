//Add all the imports
import java.io.*;
import java.util.*;

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


public class NamedEntities {
	private Annotation doc; //The already annotated document
	private HashMap<String, ArrayList<Tuple<Integer, Integer>>> nameIndex = 
			new  HashMap<String, ArrayList<Tuple<Integer, Integer>>>();//Maps from names to 
																	//Pairs of integers listing loc
	private HashMap<String, String> indexToName = 
			new HashMap<String, String>(); //Maps from location index pair to name
																		//Useful in the anaphora resolution phase
	
	private HashMap<String, Integer> nameToSentence =
			new HashMap<String, Integer>();//Will use to grab the sentence indices for each word.
	
	private HashMap<Integer,Integer> beginIndToEnd = 
			new HashMap<Integer, Integer>();//This is the begin index of a mention, hashing to the end index of a 
											//a mention. Used for finding 'partial key' in anaphora resolution
	
	private HashMap<Integer,Integer> endIndToBegin =
			new HashMap<Integer,Integer>();//TODO This combined with beginIndToEnd could be a Guava biMap.
											//This just hashes the opposite direction and is also used for finding
											//partial key in anaphora resolution.
	
	private boolean namedEntitiesGathered = false;
	
	public NamedEntities(Annotation annotatedDoc) {
		this.doc = annotatedDoc;
	}
	
	//Takes in an ANNOTATED DOCUMENT. ALREADY HAS TO GO THROUGH PIPELINE
	/*
	 * Returns nameToIndex, indexToName, and nameToSentence
	 */
	public Tuple<
				Tuple<HashMap<String, ArrayList<Tuple<Integer, Integer>>>, HashMap<String, String>>, 
				HashMap<String,Integer>>
	getNamedEntities(){
		namedEntitiesGathered = true;
		int sentenceNum = 0;
		//NER
		boolean isBuildingName = false;
		String nameInProgress = "";
		Integer beginIndex = 0;
		Integer endIndex = 0;
		// A CoreMap is essentially a Map that uses class objects as keys and has values with custom types
		List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
		System.out.println("The size of all the sentences: " + sentences.size());
	    for(CoreMap sentence: sentences) {
//	    	sentenceNum = sentence.
	    	// Traversing the words in the current sentence
	    	// A CoreLabel is a CoreMap with additional token-specific methods
	    	for (CoreLabel token: sentence.get(TokensAnnotation.class)) { 
	    		//Check if this is a person. If so, place the appearance in the map.
	    		String ne = token.get(NamedEntityTagAnnotation.class);
	    		if (ne.equals("PERSON")){
	    			String person = token.get(TextAnnotation.class);
	    			//Cases: we're building a name and will concat
	    			if(isBuildingName){
	    				nameInProgress = nameInProgress + " " + person;
	    			} else { //Case: we are starting a new name
	    				nameInProgress = person;
	    				beginIndex = token.get(CharacterOffsetBeginAnnotation.class);
	    				isBuildingName = true;
	    			}
	    		} else if(isBuildingName) { //and it's NOT a person
	    			endIndex = token.get(CharacterOffsetEndAnnotation.class);
	    			//Insert into name->index hashmap
	    			if(nameIndex.containsKey(nameInProgress)) {
	    				ArrayList<Tuple<Integer, Integer>> tempList = nameIndex.get(nameInProgress);
	    				tempList.add(new Tuple(beginIndex, endIndex));
	    				nameIndex.put(nameInProgress, tempList);
	    			} else {
	    				ArrayList<Tuple<Integer, Integer>> tempList = new ArrayList<Tuple<Integer, Integer>>();
	    				tempList.add(new Tuple(beginIndex, endIndex));
	    				nameIndex.put(nameInProgress, tempList);
	    			}
	    			
	    			//Also insert into index->name hashmap
	    			String newTupleName = (new Tuple(beginIndex, endIndex)).toString();
	    			indexToName.put(newTupleName, nameInProgress);
	    			
	    			//Also insert string-sentence number into hashmap
	    			nameToSentence.put(newTupleName, new Integer(sentenceNum));
	    			
	    			//Also insert into 'partial key' hashmaps - begin-> end, end->begin
	    			beginIndToEnd.put(beginIndex, endIndex);
	    			endIndToBegin.put(endIndex, beginIndex);
	    			
	    			//Reset isbuilding and string
//	    			System.out.println("New person: " + nameInProgress + " Indices: " + beginIndex + "," + endIndex);
	    			nameInProgress = "";
	    			isBuildingName = false;
	    		} else {
	    			//name is not in progress. Do nothing
	    		}
	    	}
	        sentenceNum++;
	    }
	    
	    return new Tuple((new Tuple(nameIndex, indexToName)), nameToSentence);
	
	}
	
	/*
	 * While this is bad coding practice, this was done in a time crunch. This method
	 * Should never be called prior to the previous one. I've put a temporary fix
	 * by having a boolean change.
	 */
	public Tuple<HashMap<Integer,Integer>, HashMap<Integer,Integer>> 
		getPartialIndexMaps() {
			if(namedEntitiesGathered){
				return new Tuple(beginIndToEnd, endIndToBegin);
			} else {
				System.out.println("ERROR. Need to call this method with the getNamedEntities method!");
				return null;
			}
		}
}



