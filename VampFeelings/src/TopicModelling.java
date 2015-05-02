/*
 * This will use Mallet - experimentation with topic modelling! Woot
 * 
 */
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import cc.mallet.util.*;
import cc.mallet.types.*;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.topics.*;

public class TopicModelling {		
	
public void basicTopicModelling() throws Exception {
		// Begin by importing documents from text to feature sequences
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
        pipeList.add( new TokenSequenceRemoveStopwords(new File("../stoplists/en.txt"), "UTF-8", false, false, false) );
        pipeList.add( new TokenSequence2FeatureSequence() );

        InstanceList instances = new InstanceList (new SerialPipes(pipeList));
        
        Reader fileReader = new InputStreamReader(new FileInputStream(
        		new File("../../full_texts/Varney_the_Vampire.txt")), "UTF-8");
        instances.addThruPipe(new CsvIterator (fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
                                               3, 2, 1)); // data, label, name fields

     // Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
        //  Note that the first parameter is passed as the sum over topics, while
        //  the second is the parameter for a single dimension of the Dirichlet prior.
        int numTopics = 100;
        ParallelTopicModel model = new ParallelTopicModel(numTopics, 1.0, 0.01);

        model.addInstances(instances);

        // Use two parallel samplers, which each look at one half the corpus and combine
        //  statistics after every iteration.
        model.setNumThreads(2);

        // Run the model for 50 iterations and stop (this is for testing only, 
        //  for real applications, use 1000 to 2000 iterations)
        model.setNumIterations(2000);
        model.estimate();

        // Show the words and topics in the first instance

        // The data alphabet maps word IDs to strings
        Alphabet dataAlphabet = instances.getDataAlphabet();
        
        FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();
        LabelSequence topics = model.getData().get(0).topicSequence;

        Formatter out = new Formatter(new StringBuilder(), Locale.US);
        for (int position = 0; position < tokens.getLength(); position++) {
            out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));
        }
        System.out.println(out);
        
        
     // Estimate the topic distribution of the first instance, 
        //  given the current Gibbs state.
        double[] topicDistribution = model.getTopicProbabilities(0);

        // Get an array of sorted sets of word ID/count pairs
        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
        
        // Show top 5 words in topics with proportions for the first document
        for (int topic = 0; topic < numTopics; topic++) {
            Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
            
            out = new Formatter(new StringBuilder(), Locale.US);
            out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
            int rank = 0;
            while (iterator.hasNext() && rank < 5) {
                IDSorter idCountPair = iterator.next();
                out.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
                rank++;
            }
            System.out.println(out);
        }
	
	}

	public static Tuple<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> getTopTopics(int topicWordListLength) throws Exception{
		// Begin by importing documents from text to feature sequences
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
        pipeList.add( new TokenSequenceRemoveStopwords(new File("../stoplists/en.txt"), "UTF-8", false, false, false) );
        pipeList.add( new TokenSequence2FeatureSequence() );

        InstanceList instances = new InstanceList (new SerialPipes(pipeList));
        
        Reader fileReader = new InputStreamReader(new FileInputStream(
        		new File("../../full_texts/Varney_the_Vampire.txt")), "UTF-8");
        instances.addThruPipe(new CsvIterator (fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
                                               3, 2, 1)); // data, label, name fields

     // Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
        //  Note that the first parameter is passed as the sum over topics, while
        //  the second is the parameter for a single dimension of the Dirichlet prior.
        int numTopics = 100;
        ParallelTopicModel model = new ParallelTopicModel(numTopics, 1.0, 0.01);

        model.addInstances(instances);

        // Use two parallel samplers, which each look at one half the corpus and combine
        //  statistics after every iteration.
        model.setNumThreads(1);

        // Run the model for 50 iterations and stop (this is for testing only, 
        //  for real applications, use 1000 to 2000 iterations)
        model.setNumIterations(2000);
        model.estimate();

        // Show the words and topics in the first instance

        // The data alphabet maps word IDs to strings
        Alphabet dataAlphabet = instances.getDataAlphabet();
        
        FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();
        LabelSequence topics = model.getData().get(0).topicSequence;

        Formatter out = new Formatter(new StringBuilder(), Locale.US);
        for (int position = 0; position < tokens.getLength(); position++) {
            out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));
        }
        System.out.println(out);
        
        
     // Estimate the topic distribution of the first instance, 
        //  given the current Gibbs state.
        double[] topicDistribution = model.getTopicProbabilities(0);

        // Get an array of sorted sets of word ID/count pairs
        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
        
        
        //Arrays: holding the topics and top x words for each that have 'vampire' mentioned in them
        //And the other array holds all other topics and top x words.
        ArrayList<ArrayList<String>> vampireTopics = new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> nonVampireTopics = new ArrayList<ArrayList<String>>();

        
        // Show top x words in topics with proportions for the first document
        for (int topic = 0; topic < numTopics; topic++) {
            Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
            
            //The ArrayList<String> that holds the top x words for a topic
            ArrayList<String> topicWorldList = new ArrayList<String>();
            
            out = new Formatter(new StringBuilder(), Locale.US);
            out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
            int rank = 0;
            while (iterator.hasNext() && rank < topicWordListLength) {
                IDSorter idCountPair = iterator.next();
                out.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
                topicWorldList.add(dataAlphabet.lookupObject(idCountPair.getID()).toString());
                rank++;
            }
            
            if(topicWorldList.contains("vampire") ||topicWorldList.contains("vampyre") 
            	|| topicWorldList.contains("vampires") || topicWorldList.contains("vampyres") ){
            	vampireTopics.add(topicWorldList);
            	System.out.println("Added to vampire List");
            } else {
            	nonVampireTopics.add(topicWorldList);
            	System.out.println("Added to nonvampire list");
            }
//            System.out.println(out);
        }
        
        return new Tuple(vampireTopics, nonVampireTopics);
	}
	
	public static void getVampNonVampEmotions(ArrayList<ArrayList<String>> vampireTopics, 
			ArrayList<ArrayList<String>> nonVampireTopics){
		
		EmotionLookup eLookup = new EmotionLookup();
		 try {
			eLookup.hashNRCLex();
		} catch (Exception e) {
			System.out.println("NRC Lexicon creation error!");
			System.out.println(e.toString());
		}
		 
		 
		 HashMap<String,Integer> emotionMapVamp = 
				 eLookup.HashTopicLists(vampireTopics);
		 HashMap<String,Integer> emotionMapNonVamp = 
				 eLookup.HashTopicLists(nonVampireTopics);
		 
		 try(PrintWriter out = new PrintWriter(new BufferedWriter
				 (new FileWriter("../topic_model_results/topic_sentiments.txt", true)))) {
			 		 out.println("Book in question: Varney");
			 		
				 	 out.println("Vampire-including topics: ");
				 	 out.println("Anger: " + emotionMapVamp.get("0"));
					 out.println("Anticipation: " + emotionMapVamp.get("1"));
					 out.println("Disgust: " + emotionMapVamp.get("2"));
					 out.println("Fear: " + emotionMapVamp.get("3"));
					 out.println("Joy: " + emotionMapVamp.get("4"));
					 out.println("Negative: " + emotionMapVamp.get("5"));
					 out.println("Positive: " + emotionMapVamp.get("6"));
					 out.println("Sadness: " + emotionMapVamp.get("7"));
					 out.println("Surprise: " + emotionMapVamp.get("8"));
					 out.println("Trust: " + emotionMapVamp.get("9"));
					 
				 	 out.println("NON-Vampire-including topics: ");
				 	 out.println("Anger: " + emotionMapNonVamp.get("0"));
					 out.println("Anticipation: " + emotionMapNonVamp.get("1"));
					 out.println("Disgust: " + emotionMapNonVamp.get("2"));
					 out.println("Fear: " + emotionMapNonVamp.get("3"));
					 out.println("Joy: " + emotionMapNonVamp.get("4"));
					 out.println("Negative: " + emotionMapNonVamp.get("5"));
					 out.println("Positive: " + emotionMapNonVamp.get("6"));
					 out.println("Sadness: " + emotionMapNonVamp.get("7"));
					 out.println("Surprise: " + emotionMapNonVamp.get("8"));
					 out.println("Trust: " + emotionMapNonVamp.get("9"));	
					 out.close();			 
				 }catch (IOException e) {
				 System.out.println("Error trying to write sentiments for topics: " + e.toString());
			 }
		 

		
	}

	static String readFile(String path, Charset encoding) throws IOException {
  		byte[] encoded = Files.readAllBytes(Paths.get(path));
  		return new String(encoded, encoding);
	}
	
	public static void main (String[] args) throws Exception {
		String filePath = "../../full_texts/Varney_the_Vampire.txt";
		
		String content = new String();
		try {
			content = readFile(filePath, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("About to calculate...");
		Tuple<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> topicLists = getTopTopics(10);
		getVampNonVampEmotions(topicLists.x, 
				topicLists.y);
		System.out.println("Done calculating.");
    }
	
	
}
