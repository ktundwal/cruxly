package com.cruxly.nlp;

import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

public enum OpenNLPUtils {
     INSTANCE;
    
     private SentenceDetectorME _sentenceDetectorME = null;

     private OpenNLPUtils() {
    	 _sentenceDetectorME = initOpenNLPSentenceDetector();
     }
     
     public String[] detectSentences(String text) {
    	 return _sentenceDetectorME.sentDetect(text);
     }
	
	/*
	 * The OpenNLP Sentence Detector can detect that a punctuation character marks 
	 * the end of a sentence or not. In this sense a sentence is defined as the 
	 * longest white space trimmed character sequence between two punctuation marks. 
	 * The first and last sentence make an exception to this rule. 
	 * The first non whitespace character is assumed to be the begin of a sentence, 
	 * and the last non whitespace character is assumed to be a sentence end.
	 */
     private SentenceDetectorME initOpenNLPSentenceDetector() {
		SentenceDetectorME sentenceDetector = null;
		InputStream modelIn = null;
		try {
			// Loading sentence detection model
			// modelIn = this.getClass().getClassLoader().getClass()
			// .getResourceAsStream("/en-sent.bin");
			modelIn = OpenNLPUtils.class.getClassLoader().getResourceAsStream("en-sent.bin");
			final SentenceModel sentenceModel = new SentenceModel(modelIn);
			modelIn.close();

			sentenceDetector = new SentenceDetectorME(sentenceModel);

		} catch (final IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				} catch (final IOException e) {
				} // oh well!
			}
		}

		return sentenceDetector;
	}

	/*
	 * The OpenNLP Tokenizers segment an input character sequence into tokens. 
	 * Tokens are usually words, punctuation, numbers, etc.
	 */
	public static TokenizerME initOpenNLPTokenizer() {
		TokenizerME _tokenizer = null;

		InputStream modelIn = null;
		try {
			// Loading tokenizer model
			// modelIn = getClass().getResourceAsStream("/en-token.bin");
			modelIn = OpenNLPUtils.class.getClassLoader().getResourceAsStream("en-sent.bin");
			final TokenizerModel tokenModel = new TokenizerModel(modelIn);
			modelIn.close();

			_tokenizer = new TokenizerME(tokenModel);

		} catch (final IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				} catch (final IOException e) {
				} // oh well!
			}
		}
		return _tokenizer;
	}
}
