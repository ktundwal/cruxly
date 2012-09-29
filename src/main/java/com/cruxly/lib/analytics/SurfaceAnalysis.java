/* Copyright (c) 2010 Cruxly Inc.
 */

package com.cruxly.lib.analytics;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections.ListUtils;

import com.cruxly.lib.analytics.FiniteStateMachine.FSMResult;
import com.cruxly.lib.analytics.TextSegment.SentenceEndSegment;
import com.cruxly.lib.analytics.TextSegment.SentenceStartSegment;
import com.cruxly.lib.model.EmailMessage;
import com.cruxly.lib.model.Kip;
import com.cruxly.lib.model.TextSegmentEx;
import com.cruxly.lib.utils.Utils;

public class SurfaceAnalysis {
	private static final String KIP_ALIASKEY = "KIP";
	protected FiniteStateMachine FSM = null;
	protected Set punctuation;
	protected RepeatedTextDetector rtd = null;
	public boolean doExpandAnnotationsToSentences = false;
	private String[] grammarSplitAtLineBreaks = null;
	private SurfaceModel _surfaceModel = null;
	
	public final String _type;
	public final String _grammarFileName;
	
	private Boolean _kipAware;
	private static final List<String> UNKNOWN_KIP = Arrays.asList(("unknown"));
	
	private static final Logger LOGGER = Logger.getLogger(SurfaceAnalysis.class.getName());

	public void setRules(String[] rules) {
		this.grammarSplitAtLineBreaks = rules;
	}

	public SurfaceAnalysis(String type, Boolean kipAware, String grammarFileName) {
		punctuation = new Set();
		_type = type;
		_kipAware = kipAware;
		_grammarFileName = grammarFileName;
	}

	public boolean isInitialized() {
		return (FSM != null);
	}
	
	public static SurfaceAnalysis create(String type, String grammarFileName, Kip kips) {
		SurfaceAnalysis sa = null;
		try {
			String[] rules = Utils.getRulesFromGrammarFile(grammarFileName);
			
			boolean kipAware = kips != null;
			
			sa = new SurfaceAnalysis(type, kipAware, grammarFileName);
			sa.setUserName("Peter Doe");
			sa.setRules(rules);
			if (kipAware) {
				sa.setKIP(kips);
			}
			sa.init();
			
		} catch (Exception e) {
			if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, String.format("Exception initiating analytics TYPE(%s) GRAMMAR(%s)", type, grammarFileName), e);
            }
		}
		return sa;
	}
	
	public boolean insertIntent(String text, TextSegment[] arrTokens,
			Kip kip, List<TextSegmentEx> intent_list) {
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.log(Level.FINE, "STARTING " + _type + " detection");
		}
		boolean found = false;
		DocumentSurfaceScore score;
		try {			
			if (_surfaceModel == null) {
				EmailMessage[] emptyArray = new EmailMessage[0];
				_surfaceModel  = trainModel(emptyArray, emptyArray);
			}
			
			if (kip != null) {
				setKIP(kip);
			}
			doExpandAnnotationsToSentences = false;
			
			EmailMessage emailMessage = Utils.getEmailMessage(text);
			
			score = rateDocumentImportance(emailMessage, arrTokens,
					StringUtils.getEmailContent(emailMessage), _surfaceModel, 
					null /* , TextSegment [] ignore*/);
			
			TextSegmentEx[] detectedSegments = TextSegmentEx.GetTextSegmentsEx(score.annotations, _type);
			found = detectedSegments.length > 0;
			intent_list.addAll(Arrays.asList(detectedSegments));
			if (LOGGER.isLoggable(Level.FINE)) {
				for (TextSegmentEx detectedSegment : detectedSegments) {
					LOGGER.log(Level.FINE, String.format("\n          RESULT : detectedSegments(%s)", detectedSegment));
				}
				LOGGER.log(Level.FINE, "          DONE");
			}
		} catch (Exception e) {
			if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, String.format("Exception analyzing TYPE(%s) TEXT(%s) KIP(%s) ", _type, text, kip), e);
			}
		}
		return found;
	}

	public void init() {
		if (grammarSplitAtLineBreaks == null) {
			throw new RuntimeException("grammar not loaded");
		}
		
		String [] rules = grammarSplitAtLineBreaks;
		
		if (FSM == null)
			FSM = new FiniteStateMachine();
	
		for (int i=0; i<rules.length; i++) {
			if (rules[i].length() == 0)
				continue;

			if (rules[i].charAt(0) == ':') {
				String rule = rules[i].substring(1).trim();
				String[] kv = StringUtils.splitBySeparator (rule, '=');
				if (kv.length != 2)
					throw new RuntimeException ("Invalid clause: "+rules[i]);
				String[] vals = StringUtils.splitBySeparator (kv[1], '|');
				FSM.addAlias(kv[0], vals);
			}
		}

		for (int i=0; i<rules.length; i++) {
			if (rules[i].length() == 0)
				continue;

			if ((rules[i].charAt(0) == '+') || (rules[i].charAt(0) == '-')) {
				String rule = rules[i].substring(1).trim();
				boolean sentStartRule = false;

				String[] vals = StringUtils.splitBySeparator (rule, ' ');
				if (vals[0].equals("*"))
					throw new RuntimeException ("Rule starting with \"*\" : "+rules[i]);

				FSM.addPath(vals, (rules[i].charAt(0) == '+'));
			}
		}
	}

	public void setUserName (String userName) {
		String[] vals = StringUtils.splitBySeparator(userName.toLowerCase(), ' ');

		if (FSM == null)
			FSM = new FiniteStateMachine();

		FSM.addAlias("UserName", vals);
	}
	
	public void setKIP (Kip kip) {
		if (!_kipAware) return;
		
		List<String> currentKIP = null;
		try {
			currentKIP = getKIP();
		} catch (Exception e) {}
		
 
	    Boolean needToReinit = true;
	    String[] allTerms = kip.all();
	    
		if (currentKIP != null) {
	    	List diff = ListUtils.subtract(currentKIP, Arrays.asList(allTerms));
	    	needToReinit = diff.size() > 0;
	    }

	    if (needToReinit) {
	    	FSM = new FiniteStateMachine();

			FSM.addAlias(KIP_ALIASKEY, allTerms);
			init();
			if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, _type + " SurfaceAnalysis reinitated with request kip " + kip);
			}
	    } else{
	    	if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, _type + " SurfaceAnalysis already has kip " + kip + ". Skipping setKIP() call.");
	    	}
	    }
	}
	
	public List<String> getKIP() {
		if (!_kipAware) {
			return null;
		} else {
			return Arrays.asList(FSM.resolveAlias(KIP_ALIASKEY));
		}
	}

	public void addAliasToGrammar (String name, String value) {
		if (FSM == null)
			FSM = new FiniteStateMachine();


		String[] vals = StringUtils.splitBySeparator (value, '|');

		FSM.addAlias (name, vals);
	}

	public SurfaceModel trainModel (EmailMessage [] inbox, EmailMessage[] outbox) {
		if (!isInitialized())
			throw new RuntimeException ("Surface Analysis has not been initialized");

		SurfaceModel model = new SurfaceModel();


		boolean doTrainSurface = false;

		if (doTrainSurface) {
			/*
			 * Now compute mean/stdev
			 */
			if (inbox.length > 5) {
				float [] scores = new float[inbox.length];
				for (int i=0; i<inbox.length; i++) {
					String text = StringUtils.getEmailContent(inbox[i]);
					DocumentSurfaceScore dss = rateDocumentImportance (inbox[i],
							StringUtils.splitIntoTextSegments(text, true, true),
							text,
							model,
							null);
					scores[i] = dss.documentScore;
				}

				model.distribution = new StatsUtils.Distribution(StatsUtils.mean(scores), StatsUtils.standardDeviation(scores));
				if (model.distribution.stdev == 0.0f)
					model.distribution = null;
			}
		}

		return model;
	}

	protected Vector findPatterns (TextSegment [] segments, int targetState) {
		Vector annotations = new Vector();

	
		FSMResult result = null;

		boolean inside = false;
		for (int idx=0; idx<segments.length; idx++) {
			if (segments[idx] instanceof TextSegment.SentenceStartSegment)
				inside = true;
			else if (segments[idx] instanceof TextSegment.SentenceEndSegment)
				inside = false;
			
			if (!inside)
				continue;
			
			result = FSM.processSequence(segments, idx, targetState);
			if (result.result == targetState)
				//annotations.addElement(new TwoInts(idx, idx+result.length-1, result.matchedPath));
				annotations.addElement(new TwoInts(idx, result.lastIdx, result.matchedPath)); //KT, why add result.length-1? fails testINeedStarbucks test case

			// Advance the cursor
			if ( (result.result == FiniteStateMachine.ACCEPT) ||
					(result.result == FiniteStateMachine.REJECT) )  {
				idx = result.lastIdx;
			}
		}

		return annotations;
	}

	public DocumentSurfaceScore rateDocumentImportance (EmailMessage document,
			TextSegment[] arrTokens, 
			String text,
			SurfaceModel model,
			TextSegment [] ignore) {
		if (!isInitialized())
			throw new RuntimeException ("Surface Analysis has not been initialized");

		Vector ignoreRegions = new Vector();

		if (ignore != null) {
			for (int i=0; i<ignore.length; i++)
				ignoreRegions.addElement(ignore[i]);
		}
		
		// Ignore subjects that start with "re:" (add the tokens from subject to ignoreRegions)
		if ((document.subject != null) && (document.subject.trim().toLowerCase().startsWith("re:"))) {
			// Create a dummy message with just the subject
			// then replace words in the current message's subject with blanks

			EmailMessage dummy = new EmailMessage(document.messageId, document.from,
					null, new Date(), document.subject, "", null);

			TextSegment [] tokensDummy = StringUtils.splitIntoTextSegments(StringUtils.getEmailContent(dummy), true, true);
			if (tokensDummy.length > 0) {
				int maxPosEnd = 0;
				for (int i=0; i<tokensDummy.length; i++) {
					if ((!(tokensDummy[i] instanceof TextSegment.SpecialSymbolSegment)) && (tokensDummy[i].posEnd > maxPosEnd)) 
						maxPosEnd = tokensDummy[i].posEnd;
				}

				ignoreRegions.addElement(new TextSegment(text, arrTokens[0].posStart, maxPosEnd-1));
			}
			
		}
		
		if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, String.format("    DOCUMENT: %s", document.text));
			StringBuffer buffer = new StringBuffer();
			int i = 0;
			for (TextSegment token : arrTokens) {
				buffer.append(String.format("[%d](%d)%s(%d)", i, token.posStart, token, token.posEnd));
				buffer.append("___");
				i++;
			}
			LOGGER.log(Level.FINE, "    TOKENS: " + buffer.toString());
		}
		
		TwoInts[] annotations0 = new TwoInts[0];
		
		// Find matching REJECT rules
		Vector<TwoInts> annotationsVecNeg = findPatterns(arrTokens, FiniteStateMachine.REJECT);
		//TwoInts[] a2 = new TwoInts[annotationsVecNeg.size()];
		//annotationsVecNeg.copyInto(a2);
		
		if (LOGGER.isLoggable(Level.FINE)) {
			Iterator<TwoInts> annotationsVecNegItr = annotationsVecNeg.iterator();
			while(annotationsVecNegItr.hasNext()) {
				TwoInts rule = (TwoInts)annotationsVecNegItr.next();
				LOGGER.log(Level.FINE, String.format("          Recognized REJECT rule: location(%d, %d) path(%s)", 
						rule.a, rule.b, rule.path));
			}
		}
		
		// No REJECT rules found. Now lets try ACCEPT rules
		if (annotationsVecNeg.size() == 0) {
			Vector<TwoInts> annotationsVec = findPatterns(arrTokens, FiniteStateMachine.ACCEPT);
			TwoInts[] a1 = new TwoInts[annotationsVec.size()];
			annotationsVec.copyInto(a1);
			
			if (LOGGER.isLoggable(Level.FINE)) {
				Iterator<TwoInts> annotationsVecItr = annotationsVec.iterator();
				while(annotationsVecItr.hasNext()) {
					TwoInts rule = (TwoInts)annotationsVecItr.next();
					LOGGER.log(Level.FINE, String.format("          Recognized ACCEPT rule: location(%d, %d) path(%s)", 
							rule.a, rule.b, rule.path));
				}
			}
			
			// ASKKIRIL: why overlapping was done. assumption is once you find reject rule bail out. For now just fake REJECT rules 
			TwoInts[] a2 = new TwoInts[0];
			annotations0 = TwoInts.excludeOverlapping(a1, a2);
			
			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.log(Level.FINE, String.format("          POST excludeOverlapping: num rules = %d", annotations0.length));
				for (int i=0; i<annotations0.length; i++) {
					LOGGER.log(Level.FINE, String.format("          POST excludeOverlapping: location(%d, %d) path(%s)", 
							annotations0[i].a, annotations0[i].b, annotations0[i].path));
				}
			}
		}
		
		TextSegment [] annotations = null;
		if (doExpandAnnotationsToSentences) {
			annotations = expandToSentenceBoundaries (annotations0, arrTokens);
		} else {
			annotations = new TextSegment[annotations0.length];
			for (int i=0; i<annotations.length; i++)
				annotations[i] = new TextSegment(text, 
									arrTokens[annotations0[i].a].posStart, 
									arrTokens[annotations0[i].b].posEnd,
									annotations0[i].path);
		}

		if (ignoreRegions.size() > 0) {
			TextSegment[] ignoreArr = new TextSegment[ignoreRegions.size()];
			ignoreRegions.copyInto(ignoreArr);
			annotations = TextSegment.excludeOverlapping(annotations, ignoreArr);
		}

		int count = annotations.length;

		DocumentSurfaceScore dss = new DocumentSurfaceScore();

		int Z = StatsUtils.poorMansLog(arrTokens.length);
		float freq = (((float)count)) / (Z);
		if (freq > 1.0f)
			freq = 1.0f;

		dss.documentScore = freq;
		dss.annotations = annotations;
		
		return dss;
	}

	protected TextSegment[] expandToSentenceBoundaries(TwoInts[] annotations, TextSegment[] tokens) {
		
		StringBuffer buffer = new StringBuffer();
		
		if (annotations == null)
			return null;
		
		Vector result = new Vector();
		
		boolean []isTokenAnnotated = new boolean[tokens.length+1];
		for (int i=0; i<isTokenAnnotated.length;  i++)
			isTokenAnnotated[i] = false;
			
		
		
		//TextSegment[] result = new TextSegment[annotations.length];
		for (int i=0; i<annotations.length; i++) {
			int start = annotations[i].a;
			int end = annotations[i].b;
			
			while ((start > 0) && (!(tokens[start] instanceof SentenceStartSegment))) 
				start --;

			while ((end < (tokens.length-1)) && (!(tokens[end] instanceof SentenceEndSegment))) 
				end++;
			
			
			for (int j=start; j<end; j++)
				isTokenAnnotated[j] = true;
			
			buffer.append("[" + annotations[i].path + "] ");
		}
		
		
		int start = -1;
		for (int i=0; i<tokens.length; i++) {
			if ((isTokenAnnotated[i]) && (start == -1)) {
				start = i;
				continue;
			}
			
			if ((!isTokenAnnotated[i]) && (start > -1)) {
				result.addElement(new TextSegment(tokens[0].text, tokens[start].posStart, tokens[i-1].posEnd, buffer.toString()));
				start = -1;
			}
		}
		
		TextSegment []_result = new TextSegment[result.size()];
		result.copyInto(_result);
		return _result;
		
	}

	public static class SurfaceModel {
		private StatsUtils.Distribution distribution = null;
		private final int serializedVersion = 1;

		protected SurfaceModel() {

		}

		public SurfaceModel (java.io.DataInputStream in) throws java.io.IOException {
			int version = in.readInt();
			if (version != serializedVersion)
				throw new java.io.IOException ("Incorrect version "+version+", expectiong "+serializedVersion);

			boolean hasDist = in.readBoolean();
			if (hasDist) {
				float _mean = in.readFloat();
				float _sd = in.readFloat();
				distribution = new StatsUtils.Distribution (_mean, _sd);
			}
		}

		public void serialize(java.io.DataOutputStream out) throws java.io.IOException {
			out.writeInt (serializedVersion);

			if (distribution != null) {
				out.writeBoolean(true);
				out.writeFloat (distribution.mean);
				out.writeFloat (distribution.stdev);
			} else {
				out.writeBoolean(false);
			}
		}
	}

	public static class DocumentSurfaceScore extends DocumentScore {
	}
	
	public static class TwoInts {
		public int a;
		public int b;
		public String path;
		
		public TwoInts (int _a, int _b, String _path) {
			a = _a;
			b = _b;
			path = _path;
		}
	
		public static TwoInts[] excludeOverlapping (TwoInts [] arr1, TwoInts[] arr2) {
			Vector v = new Vector();
			
			for (int i=0; i<arr1.length; i++) {
				boolean overlaps = false;
				
				for (int j=0; j<arr2.length; j++) {
					if (arr1[i].overlaps(arr2[j])) {
						overlaps = true;
						break;
					}
				}
				
				if (!overlaps)
					v.addElement(arr1[i]);
			}
			
			TwoInts [] result = new TwoInts[v.size()];
			v.copyInto(result);
			return result;
		}
		public boolean isSubsetOf(TwoInts s) {
			return ((s.a <= this.a) && (s.b >= this.b));
		}

		public boolean overlaps(TwoInts s) {
 			return !((s.b < this.a) || (s.a > this.b));
 		}


	}
		
	
}