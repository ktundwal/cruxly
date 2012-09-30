


package com.cruxly.nlp;


/*import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;*/

/*
 * http://nlp.stanford.edu/software/corenlp.shtml#Download
 * http://stackoverflow.com/questions/2521895/singleton-pattern-in-java-lazy-intialization?lq=1
 */
public enum CoreNLP {
     INSTANCE;
    
     /*private StanfordCoreNLP _nlp;

     private CoreNLP() {
          // creates a StanfordCoreNLP object, with POS tagging, lemmatization,
          // NER, parsing, and coreference resolution
          Properties props = new Properties();
          props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
          _nlp = new StanfordCoreNLP(props);
     }
    
     public String[] getSentences(String text) {
         
          // create an empty Annotation just with the given text
          Annotation document = new Annotation(text);

          // run all Annotators on this text
          _nlp.annotate(document);

          // these are all the sentences in this document
          // a CoreMap is essentially a Map that uses class objects as keys and
          // has values with custom types
          List<CoreMap> coremap_list = document.get(SentencesAnnotation.class);
         
          String[] sentences = new String[coremap_list.size()];
          int i = 0;
          for (CoreMap sentence : coremap_list) {
              
               sentences[i] = sentence.toString();
               i++;
              
               // traversing the words in the current sentence
               // a CoreLabel is a CoreMap with additional token-specific methods
               for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                    // this is the text of the token
                    String word = token.get(TextAnnotation.class);
                    // this is the POS tag of the token
                    String pos = token.get(PartOfSpeechAnnotation.class);
                    // this is the NER label of the token
                    String ne = token.get(NamedEntityTagAnnotation.class);
               }

               // this is the parse tree of the current sentence
               Tree tree = sentence.get(TreeAnnotation.class);

               // this is the Stanford dependency graph of the current sentence
               SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
          }

          // This is the coreference link graph
          // Each chain stores a set of mentions that link to each other,
          // along with a method for getting the most representative mention
          // Both sentence and token offsets start at 1!
          Map<Integer, CorefChain> graph = document.get(CorefChainAnnotation.class);    
         
          return sentences;
     }*/
}
