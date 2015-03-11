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
//import java.nio.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


public class SentimentAnalysis {
	
	//Reads in file to a string
	//Use this for texts to analyze
	static String readFile(String path, Charset encoding) throws IOException {
  		byte[] encoded = Files.readAllBytes(Paths.get(path));
  		return new String(encoded, encoding);
	}
	
	public static void main(String[] args) {
		//FILE READIN
		String filePath = "../../text_snippets/dracula_2_page.txt";
		String content = new String();
		try {
			content = readFile(filePath, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		//PIPELINE
		//Creates a StanfordCoreNLP object, with POS tagging, lemmatization, 
		//NER, parsing, and coreference resolution 
	     Properties props = new Properties();
	     props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
//	     props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
	     props.setProperty("dcoref.maxdist", "-1");
	     StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	     
	     
	     //ANNOTATION
	     Annotation document = new Annotation(content);
		 pipeline.annotate(document);

	     
		 //Get a NamedEntity Parser and grab the dictionary of name-> mention list
		 NamedEntities nameGrabber = new NamedEntities(document);
		 Tuple<HashMap<String, ArrayList<Tuple<Integer, Integer>>>,
		 		HashMap<String, String>> namedEntitiesTuple = nameGrabber.getNamedEntities();
		 HashMap<String, ArrayList<Tuple<Integer, Integer>>> nameIndex = namedEntitiesTuple.x;
		 HashMap<String, String> indexToName = namedEntitiesTuple.y;
		 
		 
		 //Get an Anaphora Resolver and update the named entity thing
		 AnaphoraResolution resolver = new AnaphoraResolution(document, nameIndex, indexToName);
		 HashMap<String, ArrayList<Tuple<Integer, Integer>>> anaphoraNameIndex = 
				 resolver.getAnaphoraNameList();
		
		
	}
}
