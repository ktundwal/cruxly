package com.cruxly.lib.analytics;

import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;

import com.cruxly.lib.model.Kip;


public class Analyzers {
	
	private static final Logger logger = Logger.getLogger(Analyzers.class.getName());
	
	public final String COMMITMENT_ANALYSIS_GRAMMER_TXT 	= "commitment_surface_analysis_grammar.txt";
	public final String BUY_ANALYSIS_GRAMMER_TXT 			= "buy_intent_analysis_grammar.txt";
	public final String BUY_ANALYSIS_KIP_GRAMMER_TXT 		= "buy_intent_analysis_kip_grammar.txt";
	public final String LIKE_ANALYSIS_GRAMMER_TXT 			= "like_intent_analysis_grammar.txt";
	public final String LIKE_ANALYSIS_KIP_GRAMMER_TXT 		= "like_intent_analysis_kip_grammar.txt";
	public final String TRY_ANALYSIS_GRAMMER_TXT 			= "try_intent_analysis_grammar.txt";
	public final String TRY_ANALYSIS_KIP_GRAMMER_TXT 		= "try_intent_analysis_kip_grammar.txt";
	public final String RECOMMENDATION_ANALYSIS_GRAMMER_TXT = "recommendations_intent_analysis_grammar.txt";
	public final String QUESTION_ANALYSIS_GRAMMER_TXT 		= "questions_intent_analysis_grammar.txt";
	public final String DISLIKE_ANALYSIS_GRAMMER_TXT 		= "dislike_intent_analysis_grammar.txt";
	public final String DISLIKE_ANALYSIS_KIP_GRAMMER_TXT 	= "dislike_intent_analysis_kip_grammar.txt";
	
	public final String SPEECHACT 		= "speechact";
	public final String COMMITMENT 		= "commitment";
	public final String PURCHASE 		= "purchase";
	public final String IGNORE 			= "ignore";
	public final String BUY 			= "buy";
	public final String LIKE 			= "like";
	public final String TRY 			= "try";
	public final String RECOMMENDATION 	= "recommendation";
	public final String QUESTION 		= "question";
	public final String DISLIKE 		= "dislike";
	
	private Hashtable<String, SurfaceAnalysis> _table;
	
	public Analyzers() {
		super();
		_table = new Hashtable<String, SurfaceAnalysis>();
	}
	
	@SuppressWarnings("serial")
	private final Hashtable<String, String> grammars = new Hashtable<String, String> () {{
		put (BUY, 				BUY_ANALYSIS_GRAMMER_TXT);
		put (LIKE, 				LIKE_ANALYSIS_GRAMMER_TXT);
		put (TRY, 				TRY_ANALYSIS_GRAMMER_TXT);
		put (RECOMMENDATION, 	RECOMMENDATION_ANALYSIS_GRAMMER_TXT);
		put (QUESTION, 			QUESTION_ANALYSIS_GRAMMER_TXT);
		put (DISLIKE, 			DISLIKE_ANALYSIS_GRAMMER_TXT);
		put (COMMITMENT, 		COMMITMENT_ANALYSIS_GRAMMER_TXT);
	}};
	
	
	@SuppressWarnings("serial")
	private final Hashtable<String, String> kipAwareGrammars = new Hashtable<String, String> () {{
		put (BUY, 				BUY_ANALYSIS_KIP_GRAMMER_TXT);
		put (LIKE, 				LIKE_ANALYSIS_KIP_GRAMMER_TXT);
		put (TRY, 				TRY_ANALYSIS_KIP_GRAMMER_TXT);
		put (DISLIKE, 			DISLIKE_ANALYSIS_KIP_GRAMMER_TXT);
	}};
	
	public SurfaceAnalysis getAnalyzer(String type, Kip kip) {
		SurfaceAnalysis sa;
		
		String key = generateKey(type, kip);
		
		if (this.hasAnalyzer(type, kip)) {
			sa = _table.get(key);
			logger.info(String.format("Found Analyzer [%s] in cache. [%d] total analyzers",
					key, _table.size()));
		} else {
			// this is the first time we are asked for this. create one.
			String grammarFileName;
			if (kipAwareGrammars.containsKey(type)) {
				grammarFileName = kip == null ? grammars.get(type) : kipAwareGrammars.get(type);
			} else {
				grammarFileName = grammars.get(type);
			}
			long startTime = System.currentTimeMillis();
			sa = SurfaceAnalysis.create(type, grammarFileName, kip);
			_table.put(key, sa);
			long endTime = System.currentTimeMillis();
			
			logger.warning(String.format("Instantiated Analyzer for [%s] in %d ms. Total analyzers = %d",
					key, endTime - startTime, _table.size()));
		}
		return sa;
	}
	
	public SurfaceAnalysis getAnalyzer(String key) {
		return this.getAnalyzer(key, null);
	}
	
	public boolean hasAnalyzer(String type) {
		return hasAnalyzer(type, null);
	}
	
	public boolean hasAnalyzer(String type, Kip kips) {
		return _table.containsKey(generateKey(type, kips));
	}
	
	private String generateKey(String type, Kip kip) {
		String key;
		if (kip != null && kipAwareGrammars.containsKey(type)) {
			key = String.format("%s_%s", type, kip.getKey());
		} else {
			key = String.format("%s_%s", type, "nokip");
		}
		return key;
	}
}