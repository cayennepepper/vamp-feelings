//This is the experimental java class - working to resolve only specific tokens for anaphora resolution!

import java.io.*;
import java.util.*;

import edu.stanford.nlp.io.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.*;
import edu.stanford.nlp.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.*;
import java.nio.charset.StandardCharsets;



public class getNames {
	//Reads in file to a string
	static String readFile(String path, Charset encoding) throws IOException {
  		byte[] encoded = Files.readAllBytes(Paths.get(path));
  		return new String(encoded, encoding);
	}

	public static void main(String[] args) throws IOException {
		System.out.println("shits printing");


		// creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
	    // Properties props = new Properties();
	    // props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
	    // StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

	    System.out.println("boogie");

	    //Import file...
	    String filePath = "./text_snippets/dracula_2_page.txt";
		String content = readFile(filePath, StandardCharsets.UTF_8);

		System.out.println("boogie2");





	    // String filePath = "./text_snippets/dracula_2_page.txt";
	    // String text = readUsingScanner(filePath);
	    // System.out.println(text);
	    // String text = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
	    
	 //    // create an empty Annotation just with the given text
	 //    Annotation document = new Annotation(text);
	    
	 //    // run all Annotators on this text
	 //    pipeline.annotate(document);
	    
	 //    // these are all the sentences in this document
	 //    // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
	 //    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	    
	 //    for(CoreMap sentence: sentences) {
	 //      // traversing the words in the current sentence
	 //      // a CoreLabel is a CoreMap with additional token-specific methods
	 //      for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
	 //        // this is the text of the token
	 //        String word = token.get(TextAnnotation.class);
	 //        System.out.println(word);
	 //        // this is the POS tag of the token
	 //        String pos = token.get(PartOfSpeechAnnotation.class);
	 //        // this is the NER label of the token
	 //        String ne = token.get(NamedEntityTagAnnotation.class);       
	 //      }

	 //      // this is the parse tree of the current sentence
	 //      Tree tree = sentence.get(TreeAnnotation.class);

	 //      // this is the Stanford dependency graph of the current sentence
	 //      SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
	 //    }

	 //    // This is the coreference link graph
	 //    // Each chain stores a set of mentions that link to each other,
	 //    // along with a method for getting the most representative mention
	 //    // Both sentence and token offsets start at 1!
	 //    Map<Integer, CorefChain> graph = 
	 //      document.get(CorefChainAnnotation.class);


		}


}
