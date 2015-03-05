//Add all the imports
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
		//We take a file
		String filePath = "../../text_snippets/dracula_2_page.txt";
		String content = new String();
		try {
			content = readFile(filePath, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Then we want to grab all the mentions of named entities in the text.

		
		
	}

}
