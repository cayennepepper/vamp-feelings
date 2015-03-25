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
	private HashMap<Integer,Integer> sentenceCounts;
	
	
	
	public EmotionLookup(HashMap<Integer,Integer> sCount){
		this.sentenceCounts = sCount;
	}
	
	/*
	 * Places lexicon into hashmap.
	 */
	public void hashNRCLex(){
		String filePath = "../../text_snippets/super_short_text.txt";
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		String line = br.readLine();
		StringBuilder sb = new StringBuilder();
		int index = 0; //Goes in 10s
		
		while(line != null){
			String[] split = line.split(" ");
			
			
			line = br.readLine();
			if(index == 9){
				index = 0;
			} else {
				index++;
			}
		}
		
		
		String content = new String();
		try {
			content = readFile(filePath, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	
	public static void main(String[] args) {
	}

}
