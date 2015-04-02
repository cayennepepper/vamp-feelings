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
	
	//Constants....
	HashMap<String,String> namer = new HashMap<String,String>(){{
		put("0", "anger");
		put("1", "anticipation");
		put("2", "disgust");
		put("3", "fear");
		put("4", "joy");
		put("5", "negative");
		put("6", "positive");
		put("7", "sadness");
		put("8", "surprise");
		put("9", "trust");
	}};
	
	
	
	//Reads in file to a string
	//Use this for texts to analyze
	static String readFile(String path, Charset encoding) throws IOException {
  		byte[] encoded = Files.readAllBytes(Paths.get(path));
  		return new String(encoded, encoding);
	}
	
	public static void main(String[] args) {
		
		//FILE READIN
		String filePath = "../text_snippets/Int_txts/Salem/Salem_14.txt";
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
	     props.setProperty("dcoref.maxdist", "2");
	     StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	     
	     
	     //ANNOTATION
	     Annotation document = new Annotation(content);
		 pipeline.annotate(document);

	     
		 //Get a NamedEntity Parser and grab the dictionary of name-> mention list
		 NamedEntities nameGrabber = new NamedEntities(document);
		 Tuple<
		 	Tuple<
		 		HashMap<String, ArrayList<Tuple<Integer, Integer>>>,
		 		HashMap<String, String>>,
		 		HashMap<String, Integer>>namedEntitiesTuple = nameGrabber.getNamedEntities();
		 HashMap<String, ArrayList<Tuple<Integer, Integer>>> nameIndex = namedEntitiesTuple.x.x;
		 HashMap<String, String> indexToName = namedEntitiesTuple.x.y;
		 HashMap<String, Integer> sentenceToIndex = namedEntitiesTuple.y;
		 Tuple<HashMap<Integer,Integer>, HashMap<Integer,Integer>> begEndAndEndBeg = 
				 nameGrabber.getPartialIndexMaps();//These are the partial indices map!
		 

		 
		 
		 
		 //Get an Anaphora Resolver and update the named entity thing
		 AnaphoraResolution resolver = 
				 new AnaphoraResolution(document, nameIndex, indexToName, sentenceToIndex, begEndAndEndBeg);
		 HashMap<Integer,Integer> sentenceCounts = resolver.getSentenceCounts();
		 
		 //Make an emotion lookup. This is what will give us the final emotions associated with vampires,
		 //in a strictly word emotion association way.
		 EmotionLookup eLookup = new EmotionLookup(sentenceCounts, document);
		 try {
			eLookup.hashNRCLex();
		} catch (Exception e) {
			System.out.println("NRC Lexicon creation error!");
			System.out.println(e.toString());
		}
		 
		 //Get the emotionmap
		 HashMap<String,Integer> emotionMap = eLookup.checkSentences();
		 System.out.println("Anger: " + emotionMap.get("0"));
		 System.out.println("Anticipation: " + emotionMap.get("1"));
		 System.out.println("Disgust: " + emotionMap.get("2"));
		 System.out.println("Fear: " + emotionMap.get("3"));
		 System.out.println("Joy: " + emotionMap.get("4"));
		 System.out.println("Negative: " + emotionMap.get("5"));
		 System.out.println("Positive: " + emotionMap.get("6"));
		 System.out.println("Sadness: " + emotionMap.get("7"));
		 System.out.println("Surprise: " + emotionMap.get("8"));
		 System.out.println("Trust: " + emotionMap.get("9"));
		 
		 //Place all the results into a file
		 try(PrintWriter out = new PrintWriter(new BufferedWriter
				 (new FileWriter("../text_snippets/Int_txts/Salem/Salem_results.txt", true)))) {
			 	 out.println("Anger: " + emotionMap.get("0"));
				 out.println("Anticipation: " + emotionMap.get("1"));
				 out.println("Disgust: " + emotionMap.get("2"));
				 out.println("Fear: " + emotionMap.get("3"));
				 out.println("Joy: " + emotionMap.get("4"));
				 out.println("Negative: " + emotionMap.get("5"));
				 out.println("Positive: " + emotionMap.get("6"));
				 out.println("Sadness: " + emotionMap.get("7"));
				 out.println("Surprise: " + emotionMap.get("8"));
				 out.println("Trust: " + emotionMap.get("9"));			
			 }catch (IOException e) {
				 System.out.println("Error trying to write tallies to file: " + e.toString());
			 }

		
		
	}
}
