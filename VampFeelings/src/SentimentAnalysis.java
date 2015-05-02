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
//		FileInputStream inputStream = new FileInputStream("foo.txt");
//	    try {
//	        String everything = IOUtils.toString(inputStream);
//	    } finally {
//	        inputStream.close();
//	    }
  		return new String(encoded, encoding);
	}
	
	public static void main(String[] args) {
		
		int fileNumber = 1;
		String filePath = "../text_snippets/Int_txts/Varney/Varney_" + fileNumber + ".txt";
		String toWritePath = "../text_snippets/fixed_tallies/Varney/Varney_results.txt";
		//Overall emotion count
		ArrayList<Integer> allEmotionCount = new ArrayList<Integer>();
		
		for (int i = 0; i < 10; i++){
			allEmotionCount.add(0);
		}
		
		System.out.println(filePath);
		File bah = new File(filePath);

		while(new File(filePath).isFile()){
			System.out.println("Iteration: " + fileNumber);
			
			//FILE READIN
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
			 
			 for(int i = 0; i < 10; i++){
				 if(i==0){
					 System.out.println("Anger counttt" + emotionMap.get("0"));
				 }
				 Integer newRes = allEmotionCount.get(i) + emotionMap.get(Integer.toString(i));
				 allEmotionCount.set(i, newRes);
			 }
			 
			fileNumber++;
			filePath = "../text_snippets/Int_txts/Varney/Varney_" + fileNumber + ".txt";

		}
		
		 //Place all the results into a file
		 try(PrintWriter out = new PrintWriter(new BufferedWriter
				 (new FileWriter(toWritePath, true)))) {
			 	 out.println("Anger: " + allEmotionCount.get(0));
				 out.println("Anticipation: " + allEmotionCount.get(1));
				 out.println("Disgust: " + allEmotionCount.get(2));
				 out.println("Fear: " + allEmotionCount.get(3));
				 out.println("Joy: " + allEmotionCount.get(4));
				 out.println("Negative: " + allEmotionCount.get(5));
				 out.println("Positive: " + allEmotionCount.get(6));
				 out.println("Sadness: " + allEmotionCount.get(7));
				 out.println("Surprise: " + allEmotionCount.get(8));
				 out.println("Trust: " + allEmotionCount.get(9));	
				 out.close();
			 }catch (IOException e) {
				 System.out.println("Error trying to write tallies to file: " + e.toString());
			 }
	}
}
