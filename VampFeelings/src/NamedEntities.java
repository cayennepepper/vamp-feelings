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
	
	public NamedEntities(Annotation annotatedDoc) {
		this.doc = annotatedDoc;
	}
	
	//Takes in an ANNOTATED DOCUMENT. ALREADY HAS TO GO THROUGH PIPELINE
	public Tuple<
				Tuple<HashMap<String, ArrayList<Tuple<Integer, Integer>>>, HashMap<String, String>>, 
				HashMap<String,Integer>>
	getNamedEntities(){
		int sentenceNum = 0;
		//NER
		// A CoreMap is essentially a Map that uses class objects as keys and has values with custom types
		List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
	    for(CoreMap sentence: sentences) {
	    	// Traversing the words in the current sentence
	    	// A CoreLabel is a CoreMap with additional token-specific methods
	    	for (CoreLabel token: sentence.get(TokensAnnotation.class)) { 
	    		//Check if this is a person. If so, place the appearance in the map.
	    		
	    		String ne = token.get(NamedEntityTagAnnotation.class);
	    		if (ne.equals("PERSON")){
	    			String person = token.get(TextAnnotation.class);
	    			System.out.println("Found person: " + person);
	    			Integer beginIndex = token.get(CharacterOffsetBeginAnnotation.class);
	    			Integer endIndex = token.get(CharacterOffsetEndAnnotation.class);
	        	 
	    			//Insert into name->index hashmap
	    			if(nameIndex.containsKey(person)) {
	    				ArrayList<Tuple<Integer, Integer>> tempList = nameIndex.get(person);
	    				tempList.add(new Tuple(beginIndex, endIndex));
	    				nameIndex.put(person, tempList);
	    			} else {
	    				ArrayList<Tuple<Integer, Integer>> tempList = new ArrayList<Tuple<Integer, Integer>>();
	    				tempList.add(new Tuple(beginIndex, endIndex));
	    				nameIndex.put(person, tempList);
	    			}
	    			
	    			//Also insert into index->name hashmap
	    			String newTupleName = (new Tuple(beginIndex, endIndex)).toString();
	    			indexToName.put(newTupleName, person);
	    			
	    			//Also insert string-sentence number into hashmap
	    			nameToSentence.put(newTupleName, new Integer(sentenceNum));
	    			
	    		}
	       }
	    }
	    
	    //Print out everything.
	    System.out.println("THIS IS THE ORIGINAL NAME SET...");
	    for (String personName : nameIndex.keySet()){
	    	//Get the list of mentions
	    	ArrayList<Tuple<Integer, Integer>> mentionList = nameIndex.get(personName);
	    	String val = mentionList.toString();
	    	System.out.println("Person: " + personName + " Mention List: " + val);
	    }
	    
	    return new Tuple((new Tuple(nameIndex, indexToName)), nameToSentence);
	
	}
}



