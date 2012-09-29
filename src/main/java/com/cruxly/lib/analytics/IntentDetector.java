package com.cruxly.lib.analytics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.cruxly.lib.model.Document;
import com.cruxly.lib.model.IntentRule;
import com.cruxly.lib.model.Kip;
import com.cruxly.lib.model.TextSegmentEx;
import com.cruxly.nlp.OpenNLPUtils;

public class IntentDetector {
	
	private static final Logger LOGGER = Logger.getLogger(IntentDetector.class.getName());
	
	public TextSegmentEx[] detectSpecificIntentTextSegments(String content, Kip kip, 
			SurfaceAnalysis analyzer, String intentType) {
		
		if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, String.format("INIT INTENT DETECTION: text(%s), kip(%s), intentType(%s)", 
            		content, kip, intentType));
		}
		
		List<TextSegmentEx> intent_list = new ArrayList<TextSegmentEx>();
		TextSegment[] arrTokens = StringUtils.splitIntoTextSegments(content, true, true);
		analyzer.insertIntent(content, arrTokens, kip, intent_list);
		
		return intent_list.toArray(new TextSegmentEx[intent_list.size()]);
		
	}
	
	public IntentRule[] detect(Document document) throws IntentDetectorException {
		
		/* tests
		 * http://localhost:8888/test.html?data=[{'content':'It\'s%20like%20that?%20%20Its%20ok.%20I%20need%20starbucks'},]
		 */
		List<IntentRule> intents_json = new ArrayList<IntentRule>();
		try {			
			String[] detectedSentences = OpenNLPUtils.INSTANCE.detectSentences(document.text);

			Hashtable<String, Set<IntentRule>> sentence_intents = new Hashtable<String, Set<IntentRule>>(); 
			
			if (LOGGER.isLoggable(Level.FINE)) {
	            LOGGER.log(Level.FINE, String.format("\n\nINIT DETECTINTENT: KIP(%s) CONTENT(%s)", 
	            		document.kip, document.text));
			}
	        for (String detectedSentence : detectedSentences) {
	        	if (LOGGER.isLoggable(Level.FINE)) {
	                LOGGER.log(Level.FINE, String.format("    SENTENCE(%s)", detectedSentence));
	        	}
				TextSegmentEx[] segments = detectIntentTextSegments(detectedSentence, document.kip);
				for (TextSegmentEx segmentEx : segments) {
					String intent = segmentEx.type;
					String rule = document.debug ? segmentEx.segment.rule : "";
					String sentence = segmentEx.segment.toString();
					
					if (LOGGER.isLoggable(Level.FINE)) {
			            LOGGER.log(Level.FINE, String.format("        INTENT(%s), RULE(%s), SEGMENT(%s)", 
			            		intent, rule, sentence));
					}
					
					if (sentence_intents.containsKey(sentence)) {
						Set<IntentRule> intents = sentence_intents.get(sentence);
						if (!intents.contains(intent)) {
							intents.add(new IntentRule(intent, rule));
						}
						sentence_intents.put(sentence, intents);
					} else {
						Set<IntentRule> intents = new HashSet<IntentRule>();
						intents.add(new IntentRule(intent, rule));
						sentence_intents.put(sentence, intents);
					}
				}
			}
			
			HashSet<IntentRule> unique_intents = new HashSet<IntentRule>();
			/* RULE: Since a sentence ending in '?' will always be detected as "question", 
			 * no need to even run "want" detection on it, since we are going to ignore any way.
			 */
			Iterator<Entry<String, Set<IntentRule>>> it = sentence_intents.entrySet().iterator();
			
			while (it.hasNext()) {
				Entry<String, Set<IntentRule>> entry = it.next();
				String sentence = entry.getKey();
				Set<IntentRule> intents = entry.getValue();
				IntentRule wantWithNoRule = IntentRule.getWantInstanceWithNoRule();
				if (sentence.endsWith("?") && intents.contains(wantWithNoRule)) {
					intents.remove(wantWithNoRule);
				}
				
				Iterator<IntentRule> itr = intents.iterator();
				while(itr.hasNext()) {
					unique_intents.add((IntentRule) itr.next());
				}
				
			}
			
			Iterator<IntentRule> itr = unique_intents.iterator();
			while(itr.hasNext()) {
				IntentRule intentRule = (IntentRule) itr.next();
				if (isIntentAllowed(document.text, intentRule.intent)) {
					intents_json.add(intentRule);
				}
			}
			
			return intents_json.toArray(new IntentRule[intents_json.size()]);
			
		} catch (Exception e) {
			if (LOGGER.isLoggable(Level.SEVERE)) {
	            LOGGER.log(Level.SEVERE, "Exception analyzing text: " + e);
			}
			throw new IntentDetectorException("Exception analyzing text: " + e, e);
		}
	}

	private Boolean isIntentAllowed(String content, String intent) {
		/* the final list (doing this incrementally) for neg rule for http:// removal
		We may change this later if we miss a lot
		
		- Buy
		- Like
		- Question
		- Commit
		- Dislike
		*/
		Boolean httpInTweetsWithInvalidIntents = content.contains("http:") 
				&& Arrays.asList(new String[]{"buy", "question", "like", "dislike", "commitment"}).contains(intent);
		
		// only like and dislike are allowed to be reported for RT
		Boolean tweetsStartsWithRTAndHasInvalidIntents = content.startsWith("RT") 
				&& Arrays.asList(new String[]{"buy", "try", "recommendation", "commitment", "question"}).contains(intent);
		Boolean allowedIntent = !httpInTweetsWithInvalidIntents && !tweetsStartsWithRTAndHasInvalidIntents;
		return allowedIntent;
	}
	
	private TextSegmentEx[] detectIntentTextSegments(String text, Kip kip) {

		if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, String.format("INIT INTENT DETECTION: text(%s), kip(%s)", text, kip));
		}

		List<TextSegmentEx> intent_list = new ArrayList<TextSegmentEx>();
		
		TextSegment[] arrTokens = StringUtils.splitIntoTextSegments(text, true, true);
		
		StringBuilder b = new StringBuilder();
		
		if (Application.DETECT_COMMITMENT) {
			String type = Application.INSTANCE.analyzers.COMMITMENT;
			detectSpecificIntentTextSegments(type, text, arrTokens, kip, intent_list, b);
		}
		
		if (Application.DETECT_BUY) {
			String type = Application.INSTANCE.analyzers.BUY;
			detectSpecificIntentTextSegments(type, text, arrTokens, kip, intent_list, b);
		}
		
		if (Application.DETECT_TRY) {
			String type = Application.INSTANCE.analyzers.TRY;
			detectSpecificIntentTextSegments(type, text, arrTokens, kip, intent_list, b);
		}
		
		if (Application.DETECT_RECOMMENDATION) {
			String type = Application.INSTANCE.analyzers.RECOMMENDATION;
			detectSpecificIntentTextSegments(type, text, arrTokens, kip, intent_list, b);
		}
		
		if (Application.DETECT_QUESTION) {
			String type = Application.INSTANCE.analyzers.QUESTION;
			detectSpecificIntentTextSegments(type, text, arrTokens, kip, intent_list, b);
		}
		
		if (Application.DETECT_LIKE) {
			String type = Application.INSTANCE.analyzers.LIKE;
			detectSpecificIntentTextSegments(type, text, arrTokens, kip, intent_list, b);
		}
		
		if (Application.DETECT_DISLIKE) {
			String type = Application.INSTANCE.analyzers.DISLIKE;
			detectSpecificIntentTextSegments(type, text, arrTokens, kip, intent_list, b);
		}

		if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, String.format("ANALYSIS RESULTS: %s to process TEXT[%s] KIP[%s] TOKENS[%s]", 
            		b.toString(),  text, kip, arrTokens));
		}
		return intent_list.toArray(new TextSegmentEx[intent_list.size()]);
	}

	private void detectSpecificIntentTextSegments(String type, String text, TextSegment[] arrTokens,
			Kip kip, List<TextSegmentEx> intent_list, StringBuilder logger) {
		try {
			long startTime = System.currentTimeMillis();
			boolean found = Application.INSTANCE.analyzers.getAnalyzer(type, kip).insertIntent(text, arrTokens, kip, intent_list);
			long endTime = System.currentTimeMillis();
			logger.append(String.format("[%s=%b, %dms], ", type, found, endTime - startTime));
		} catch (Exception e) {
			if (LOGGER.isLoggable(Level.SEVERE)) {
	            LOGGER.log(Level.SEVERE, String.format("Exception analyzing [%s] for [%s] with [%s]. %s", 
	            		type, text, kip, e.getMessage()));
			}
		}
	}
}
