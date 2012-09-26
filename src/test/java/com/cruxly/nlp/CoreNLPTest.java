package com.cruxly.nlp;

import static org.junit.Assert.*;

import org.junit.Test;

public class CoreNLPTest {

	@Test
    public void testSentenceDetection() {
         String tweet = "Don't get wrong .. I want a # Surface .. I really do .. " +
                   "but before I decide I want to see what Samsung, Dell & Asus deliver.. better tablets?";
         System.out.println(tweet);
         String[] sentences = CoreNLP.INSTANCE.getSentences(tweet);
         for (String sentence : sentences) {
        	 System.out.println("   - " + sentence);
         }
         assertEquals("First sentence in incorrect", "Don't get wrong .", sentences[0]);
    }

}
