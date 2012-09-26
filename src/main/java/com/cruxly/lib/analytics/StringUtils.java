/* Copyright (c) 2010 Cruxly Inc.
*/

package com.cruxly.lib.analytics;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;

import com.cruxly.lib.model.EmailMessage;
import com.cruxly.lib.model.TextSegmentEx;


public class StringUtils {
	protected static Set emailHeaders;
	protected static char[] sentBoundaryPunctuation = {'.', '!', '?', ';', ':', '-'};
	protected static boolean [] arrIsSentBoundaryPuncutation = null;
	
	protected static Set abbreviations;

	protected static char[] punctuation = {'.', '!', '?', ';', ':', '-', ',', '(', ')', '"', '[', ']', '{', '}', '<', '>'};
	protected static boolean [] arrIsPunctuation = null;
	
	private static final Logger log =
        Logger.getLogger(StringUtils.class.getName());

	static {
		emailHeaders = new Set();
		String [] e = {"From", "To", "Subject", "Date", "Sent", "Cc"};
		emailHeaders.addAll(e);
		
		String [] abbr = {"u.s.", "u.s.a.", "e.g.", "i.e.",  "vs.", "a.m.", "p.m.", "ph.d.", "mr.", "ms.", "mrs.", "dr.", "jr.", ".doc", ".pdf", ".jpg", ".jpeg", ".exe", ".zip"};
		abbreviations = new Set();
		abbreviations.addAll(abbr);
		
		arrIsSentBoundaryPuncutation = new boolean[255];
		arrIsPunctuation = new boolean[255];
		for (int i=0; i<arrIsSentBoundaryPuncutation.length; i++) {
			arrIsSentBoundaryPuncutation[i] = false;
			arrIsPunctuation[i] = false;
		}
		for (int i=0; i<sentBoundaryPunctuation.length; i++)
			arrIsSentBoundaryPuncutation[(int)sentBoundaryPunctuation[i]] = true;
		for (int i=0; i<punctuation.length; i++)
			arrIsPunctuation[(int)punctuation[i]] = true;
	}
	
	public static TextSegment[] splitIntoTextSegments(String text, boolean lowercase, boolean retainPunc) {
		Vector tokens = new Vector();
		StringBuffer buff = new StringBuffer();
		
		//text = addCommaAfterTwitterHandlesAndRemoveHashes(text);

		String textLC = (lowercase?text.toLowerCase():text) + " ";
		int textLen = textLC.length();

		boolean isInsideSentence = false;
		boolean isInsidePhrase = false;
		boolean isInsideNumberedListItem = false;

		for (int i=0; i<textLen; i++) {
			
			char thisChar = textLC.charAt(i);
			char nextChar = (i<(textLen-1))?textLC.charAt(i+1):'\0';
			char prevChar = (i>0)?textLC.charAt(i-1):'\0';

			// Scan forward to the next whitespace to see if it's a token that may contain punctuation,
			// such URL, email, common abbreviation. If so, add it as one whole.
			int startIdx = i;
			int endIdx = i+1;
			while ((endIdx < textLC.length()) && (!isWhitespace(textLC.charAt(endIdx))))
				endIdx++;
			String nextString = textLC.substring(startIdx, endIdx);
			
			// Treat items in a numbered lists as separate sentences
			if (StringUtils.isNumber(thisChar) && (nextChar == ')')) {
				isInsideNumberedListItem = true;
				if (prevChar == '\n') {
					tokens.addElement(new TextSegment.SentenceEndSegment(textLC, i));
					tokens.addElement(new TextSegment.SentenceStartSegment(textLC, i));
					isInsideSentence = true;
				}
			}
			
			if (StringUtils.isAlphanum(thisChar) ||
				(((thisChar == '-') || (thisChar == '\'')) && StringUtils.isAlphanum(prevChar) && StringUtils.isAlphanum(nextChar)) ||
				((thisChar == '"') && StringUtils.isAlphanum(nextChar)) ||
				((thisChar == '.') && (abbreviations.contains(nextString)))) {
			
				
				boolean nextTokenSpecial = false;
				boolean retainTrailingPunc = false;
				if (abbreviations.contains(nextString)) {
					nextTokenSpecial = true;
					retainTrailingPunc = false;
				} else if (TokenCategories.isURL(nextString) || TokenCategories.isEmail(nextString) || TokenCategories.isTime(nextString)) {
					nextTokenSpecial = true;
					retainTrailingPunc = true;
				} 
				
				if (nextTokenSpecial) {
					if (retainTrailingPunc) {
						while (isPunc(textLC.charAt(endIdx-1)))
							endIdx--;
					}

					tokens.addElement(new TextSegment(textLC, startIdx, endIdx));
					i=endIdx-1;
					continue;
				}
					
				
				buff.append(thisChar);
				if (isInsideSentence == false) {
					tokens.addElement(new TextSegment.SentenceStartSegment(textLC, i));
					isInsideSentence = true;
				}
				
				if (isInsidePhrase == false) {
					tokens.addElement(new TextSegment.PhraseStartSegment(textLC, i));
					isInsidePhrase = true;
				}
			} else {
				if (buff.length() > 0) {
					tokens.addElement(new TextSegment(textLC, i-buff.length(), i));
					buff.setLength(0);
				}
				
				if (StringUtils.isPunc(thisChar)) {
					if (retainPunc) {
						tokens.addElement(new TextSegment(textLC, i, i+1));
						if (arrIsSentBoundaryPuncutation[thisChar] == true) {
							if (!isPunc(nextChar)) {
								tokens.addElement(new TextSegment.SentenceEndSegment(textLC, i+1));
								isInsideSentence = false;
								isInsidePhrase = false;
							}
						} 
						
						if (arrIsPunctuation[thisChar] == true) {
								isInsidePhrase = false;
						} 
					}
				} else if (thisChar=='\n') {
					if (isInsideNumberedListItem == true) {
						isInsideNumberedListItem = false;
						tokens.addElement(new TextSegment.SentenceEndSegment(textLC, i));
						isInsideSentence = false;
						isInsidePhrase = false;
					} else if ((isInsideSentence) && (tokens.size() > 0) && (tokens.lastElement() instanceof TextSegment.NewlineSegment)) {
						int lastPos = ((TextSegment.NewlineSegment)tokens.lastElement()).posStart;
						tokens.insertElementAt(new TextSegment.SentenceEndSegment(textLC, lastPos), tokens.size()-1);
						isInsideSentence = false;
						isInsidePhrase = false;
					}
					
					tokens.addElement(new TextSegment.NewlineSegment(textLC, i));
				}
			}
		}
		
		// Add a newline token at the end of message
		if (retainPunc) {
			tokens.addElement(new TextSegment.SentenceEndSegment(textLC, textLC.length()-1));
			tokens.addElement(new TextSegment.NewlineSegment(textLC, textLC.length()-1));
		}

		TextSegment[] segs = new TextSegment[tokens.size()];
		tokens.copyInto(segs);
		return segs;
	}
	
	private static String addCommaAfterTwitterHandlesAndRemoveHashes(String text) {
		try {
			String[] words = text.split("\\s+");
			for (int i = 0; i < words.length; i++) {
			    // You may want to check for a non-word character before blindly
			    // performing a replacement
			    // It may also be necessary to adjust the character class
				String currentWord = words[i];
				if (currentWord.startsWith("@")) {
					words[i] = currentWord.concat(",");
				}
				if (currentWord.startsWith("#")) {
					words[i] =  words[i].replaceAll("#", "");
				}
			}
			return StringUtils.join(words, ' ');
		} catch (Exception e) {
			// something went wrong, send what we were given with
			log.severe("Exception fixing twitter handles and hash tags: " + e.getMessage());
			return text;
		}
	}
	
	public static String[] splitIntoTokens(String text, boolean lowercase, boolean retainPunc) {
		TextSegment [] segs = splitIntoTextSegments(text, lowercase, retainPunc);
		
		Vector resultVec = new Vector();
		for (int i=0; i<segs.length; i++) {
			if (!(segs[i] instanceof TextSegment.SpecialSymbolSegment)) {
				resultVec.addElement(segs[i].toString());
			}
		}
		String [] result = new String[resultVec.size()];
		resultVec.copyInto(result);
		return result;
	}
	
	public static String join(String[] parts, char token) {
		StringBuffer buff = new StringBuffer();
		for (int i=0; i<parts.length; i++) {
			buff.append(parts[i]);
			if (i < (parts.length-1))
				buff.append(token);
		}
		return buff.toString();
	}
	
	
	public static boolean isPunc (char c) {
		if (((int)c) >= 255)
			return false;
		return arrIsPunctuation[c];
	}
	
	public static boolean isAlphanum(char c) {
		return ( ((c >= 'a') && (c <= 'z')) ||
				 ((c >= 'A') && (c <= 'Z')) ||
				 ((c >= '0') && (c <= '9')) );
	}
	
	public static boolean isAlpha(char c) {
		return ( ((c >= 'a') && (c <= 'z')) ||
				 ((c >= 'A') && (c <= 'Z')) );
	}

	public static boolean isUpperCase(char c) {
		return ((c >= 'A') && (c <= 'Z'));
	}

	public static boolean isWhitespace (char c) {
		return ((c== ' ') || (c=='\n') || (c == '\t') || (c == '\r'));
	}
	
	public static boolean isNumber (char c) {
		return ((c >= '0') && (c <= '9'));
	}
	
	public static class PreviewText {
		public String text;
		public TextSegment[] highlights;
	}
	
	public static PreviewText generatePreviewText (String text, TextSegment[] annotations, int windowSize) {
		// Generate the preview: Find the window that contains the most high-scoring words
		float values [] = new float[text.length()];
		
		for (int i=0; i<values.length; i++)
			values[i] = 0.0f;
		
		for (int i=0; i<annotations.length; i++) {
			float v = 1.0f / annotations[i].text.length();
			for (int j=annotations[i].posStart; j<annotations[i].posEnd; j++)
				values[j] += v;
		}
		
		
		float bestVal = 0.0f;
		int bestStartIdx = 0;
		int bestEndIdx = 0;
		float curVal = 0.0f;
		for (int i=0; i<values.length; i++) {
			curVal += values[i];
			if (i >= windowSize)
				curVal -= values[i-windowSize];
			
			if ((curVal > bestVal) || 
			    ((curVal == bestVal) && (i<windowSize))) {	
				bestVal = curVal;
				bestStartIdx = (i >= windowSize)?(i-windowSize):0;
				bestEndIdx = i;
			}
		}
		

		// Expand the window to make sure we're not splitting words
		while ((bestStartIdx > 0) && (values[bestStartIdx] > 0.0f))
			bestStartIdx--;
		while ((bestEndIdx < values.length) && (values[bestEndIdx] > 0.0f))
			bestEndIdx++;
		
		// Generate the preview string	
		PreviewText result = new PreviewText();
		result.text = text.substring(bestStartIdx, bestEndIdx); 
		
		Vector highlights = new Vector();
		int start = -1;
		for (int i=bestStartIdx; i<bestEndIdx; i++) {
			if (values[i] > 0.0f) {
				if (start == -1) {
					start = i;
				}
			} else {
				if (start > -1) {
					TextSegment ts = new TextSegment(result.text, start-bestStartIdx, i-bestStartIdx);
					highlights.addElement(ts);
					start = -1;
				}
			}
		}
		
		if (start > -1) {
			highlights.addElement(new TextSegment(result.text, start-bestStartIdx, result.text.length()));
		}
		
		
		result.highlights = new TextSegment[highlights.size()];
		highlights.copyInto(result.highlights);
		
		return result;		
	}

	
	public static PreviewText[] generatePreviewTextAll (String text, TextSegment[] annotations, int windowSize) {
		// Generate the preview: Find the window that contains the most high-scoring words
		boolean values [] = new boolean[text.length()];
		
		for (int i=0; i<values.length; i++)
			values[i] = false;
		
		for (int i=0; i<annotations.length; i++) {
			float v = 1.0f / annotations[i].text.length();
			for (int j=annotations[i].posStart; j<annotations[i].posEnd; j++)
				values[j] = true;
		}
		
		Vector previews = new Vector();
		
		int startPreview = 0;
		int startHighlight = 0;
		PreviewText currentPreview = null;
		Vector currentHighlights = new Vector();
		
		for (int i=0; i<=values.length; i++) {
			if ((i < values.length) && ((values[i] == true) && ((i == 0) || (values[i-1] == false)))) {
				if ((currentPreview == null) || ((i-startPreview) > windowSize)) {
					if (currentPreview != null) {
						currentPreview.highlights = new TextSegment[currentHighlights.size()];
						currentHighlights.copyInto(currentPreview.highlights);
						currentHighlights = new Vector();
					}
					
					currentPreview = new PreviewText();
					currentPreview.text = text.substring(i, ((i+windowSize) < text.length())?(i+windowSize):text.length());
					previews.addElement(currentPreview);
					startPreview = i;
				}
				startHighlight = i;
				
			} else if ((i==values.length) || 
					   ((values[i] == false) && (i > 0) && (values[i-1] == true))) {
				if ((i-startPreview) < windowSize) {
					TextSegment seg = new TextSegment (currentPreview.text, 
							startHighlight-startPreview, 
							i-startPreview);
					currentHighlights.addElement(seg); 
					//System.out.println (seg);
				} else {
					if (currentHighlights.size() > 0) {
						currentPreview.highlights = new TextSegment[currentHighlights.size()];
						currentHighlights.copyInto(currentPreview.highlights);
					}
					currentHighlights = new Vector();
					
					currentPreview = new PreviewText();
					currentPreview.text = text.substring(startHighlight, 
												((startHighlight+windowSize) < text.length())?(startHighlight+windowSize):text.length());
					previews.addElement(currentPreview);
					startPreview = startHighlight;
					
					currentHighlights = new Vector();
					currentHighlights.addElement(new TextSegment (currentPreview.text, 
																	startHighlight-startPreview, 
																	i-startPreview));
					
				}
			}
		}
					
		PreviewText[] result = new PreviewText[previews.size()];
		previews.copyInto(result);
		
		return result;		
	}

	/**
	 * Split a text into lines by newline-characters
	 * @param text - text to be split 
	 * @return - array of strings 
	 */
	public static String[] splitByNewlines (String text) {
		Vector lines = new Vector();
		int idx = -1;
		int fromIdx = -1;
		while ((idx = text.indexOf('\n', fromIdx+1)) >= 0) {
			lines.addElement(text.substring(fromIdx+1, idx));
			fromIdx = idx;
		}
		lines.addElement(text.substring(fromIdx+1));
		
		String [] result = new String[lines.size()];
		lines.copyInto(result);
		return result;
	}
	
	/**
	 * Extract only the main text from the email message text,
	 * i.e. exclude previous messages which this message replies to
	 * 
	 * @param email - message body, in text format (don't handle HTML yet) 
	 * @return - main text 
	 */
	public static String extractMessageText(String email) {
		String[] lines = splitByNewlines(email);
		
		int endIdx = -1;
		
		for (int i=0; i<lines.length; i++) {
			String line = lines[i];
			
			
/*			if (line.endsWith("wrote:") ||
				(line.startsWith("Quoting") && line.endsWith(":")) ||
				(line.startsWith("---") && line.indexOf("Original Message") > -1)) {
				endIdx = i;
				break;
			}*/
			

			
			
			if ((i+1)<lines.length) {

				// Get the next line (unless it is blank, then get the following)
				String nextLine = lines[i+1];
				int j = 1;
				while ((lines[i+j].length() == 0) && ((i+j+1)<lines.length)) {
					j++;
					nextLine = lines[i+j];
				}
				
				
/*				if (
					(lines[i].startsWith(">") && nextLine.startsWith(">")) 
					||
				    (
				      (lines[i].indexOf(':') > -1) && (nextLine.indexOf(':') > -1)  &&
				      emailHeaders.contains(lines[i].substring(0, lines[i].indexOf(':'))) && 
				      emailHeaders.contains(nextLine.substring(0, nextLine.indexOf(':')))
				    )
				   ) {
					endIdx = i;
					break;
				}*/
			}
		}
		
		if (endIdx == -1)
			return email;
		
		StringBuffer out = new StringBuffer();
		for (int i=0; i<endIdx; i++) {
			out.append (lines[i]);
			if (i < (endIdx-1))
				out.append('\n');
		}

		return out.toString();
	}
	
	/**
	 * Extract the subject (if any) and body (excluding replied-to messages) from email
	 * 
	 * @param email - message  
	 * @return - the content
	 */
	public static String getEmailContent (EmailMessage email) {
		StringBuffer buff = new StringBuffer();

		// Append a period and newline after subject, if it exists
		String subject = (email.subject != null)?email.subject.trim():"";
		if (subject.length() > 0) {
			buff.append(subject);
			
			if (isAlphanum(subject.charAt(subject.length()-1)))
				buff.append('.');
			
			buff.append('\n');
		} 
		
		if (email.text != null)
			buff.append(extractMessageText(email.text));
		
		// Scrub some strange characters
		String ret = buff.toString();
		ret = ret.replace('\r', ' ');
		ret = ret.replace((char)160, ' ');
		ret = ret.replace((char)173, '-');
		ret = ret.replace((char)175, '-');
		
		return ret;
		
	}
	
	public static String[] splitBySeparator (String text, char sep) {
		Vector elements = new Vector();
		int idx = 0;
		int lastIdx = -1;
		while ((idx=text.indexOf(sep,lastIdx+1)) > -1) {
			String subs = text.substring(lastIdx+1, idx);
			if (!subs.equals(" "))
				elements.addElement(text.substring(lastIdx+1, idx));
			lastIdx = idx;
		}
		elements.addElement(text.substring(lastIdx+1));
		
		String [] results = new String[elements.size()];
		elements.copyInto(results);
		return results;
	}
	
	public static String[] extractEmailAddresses(String text) {
		Vector result = new Vector();
		String [] tokens = StringUtils.splitBySeparator(text, ' ');
		for (int i=0; i<tokens.length; i++) {
			String tok = tokens[i];
			if (tok.indexOf('@') > 0) {
				if (tok.startsWith("<") && tok.endsWith(">")) {
					tok = tok.substring (1,tok.length()-1);
				}
				result.addElement(tok);
			}
		}
		String [] _result = new String[result.size()];
		result.copyInto(_result);
		return _result;
	}
	
	public static int min(int a, int b) {
		if (a < b)
			return a;
		return b;
		
	}
	
	public static class TextHtmlConvert {
		private static final String COLOR_PLACEHOLDER = "Yellow";
		protected static Hashtable ampChars;
		static {
			ampChars = new Hashtable();
			ampChars.put("amp;", "&");
			ampChars.put("gt;", "<");
			ampChars.put("lt;", ">");
			ampChars.put("quot;", "\"");
			ampChars.put("nbsp;", " ");
		}
		protected static String START_TAG="<span id=\"cruxly_annotation\" style=\"background: " + COLOR_PLACEHOLDER + "\">";
		protected static String END_TAG="</span>";
		protected String subject;
		protected String html;
		protected String text;
		protected int[] text2htmlOffset;
		
		public TextHtmlConvert(String _subject, String _html) {
			int startIdx = _html.toLowerCase().indexOf("<body");
			
			// Embed the subject into HTML
			if (startIdx == -1) {
				startIdx = 0;
				_html = "<b>"+_subject+"</b>" + _html;
			} else {
				startIdx = _html.toLowerCase().indexOf('>', startIdx+1);
				_html = _html.substring(0, startIdx)+"<b>"+_subject+"</b>"+_html.substring(startIdx+1);
			}
			
			
			StringBuffer textBuff = new StringBuffer();
			this.subject = _subject;
			this.html = _html;
			text2htmlOffset = new int[html.length()-startIdx+1];
			boolean inTag=false;
			int lastTagIdx = -1;
			
			int textIdx = 0;
			for (int i=startIdx; i<html.length(); i++) {
				char c = html.charAt(i);
				
				if (c == '<') {
					inTag = true;
					lastTagIdx = i;
					continue;
				} else if (c == '>') {
					inTag = false;
					String tag = html.substring(lastTagIdx+1, i).toLowerCase();
					int spaceIdx = tag.indexOf(' ');
					if (spaceIdx > -1)
						tag=tag.substring(0, spaceIdx);
					if (tag.equals("br") || tag.equals("p") || tag.equals("/p")|| tag.equals("td") || 
							tag.equals("/h1") || tag.equals("/h2") || tag.equals("/h3") || tag.equals("/h4") || tag.equals("/h5") || tag.equals("/h6")) {
						text2htmlOffset[textIdx++] = lastTagIdx;
						text2htmlOffset[textIdx++] = lastTagIdx;
						textBuff.append("\n\n");
					} 
					continue;
				} else if (c == '&') {
					int semicolonIdx = html.indexOf(';', i+1);
					if (semicolonIdx > -1) {
						String expr = html.substring(i+1, semicolonIdx+1);
						String repChar = (String)ampChars.get(expr);
						if (repChar != null) {
							text2htmlOffset[textIdx++] = i;
							textBuff.append(repChar);
							i = semicolonIdx;
						}
					}
					continue;
				} else if (c == '\r') {
					continue;
				}
				
				if (!inTag) {
					text2htmlOffset[textIdx++] = i;
					textBuff.append(c);
				}
			}
			
			text2htmlOffset[textIdx++] = html.length();
			
			text = textBuff.toString();
			text = extractMessageText(text);
		}
		
		public String getEmailText() {
			return text;
		}
		
		public String getHtml() {
			return html;
		}
		
		public TextSegment mapAnnotationToHtml(TextSegment src) {
			TextSegment segNew = new TextSegment (html, 
													text2htmlOffset[src.posStart], 
													text2htmlOffset[src.posEnd]);
			return segNew;
		}
		
		public String annotateHtml(TextSegment[] annotations) {
			return annotateHtml(annotations, COLOR_PLACEHOLDER);
		}
		
		public String annotateHtml(TextSegment[] annotations, String colorName) {
			String annotatedHtml = html;

			String startTag = START_TAG.replace(COLOR_PLACEHOLDER, colorName);
			int tagLength = startTag.length() + END_TAG.length();
			
			for (int j=0; j<annotations.length; j++) {
				TextSegment annotationHtml = mapAnnotationToHtml(annotations[j]);
				
				annotatedHtml = annotatedHtml.substring(0, annotationHtml.posStart + j*tagLength) + 
				startTag + 
				annotatedHtml.substring(annotationHtml.posStart + j*tagLength, 
						annotationHtml.posEnd + j*tagLength) + 
				END_TAG + 
				annotatedHtml.substring(annotationHtml.posEnd + j*tagLength);
	
			}
			
			return annotatedHtml;
		}
		
		public String annotateHtml(TextSegmentEx[] annotations) {
			String annotatedHtml = html;
			if (annotations.length == 0)
				return html;
			
			int numCharsAdded = 0;
			
			for (int j=0; j<annotations.length; j++) {
				String startTag = START_TAG;
				if (annotations[j].type.equals(Application.INSTANCE.analyzers.COMMITMENT))
					startTag = START_TAG.replace(COLOR_PLACEHOLDER, "LightGreen");
				else if (annotations[j].type.equals(Application.INSTANCE.analyzers.SPEECHACT))
					startTag = START_TAG.replace(COLOR_PLACEHOLDER, "Yellow");
				
				TextSegment annotationHtml = mapAnnotationToHtml(annotations[j].segment);
				
				annotatedHtml = annotatedHtml.substring(0, annotationHtml.posStart + numCharsAdded) + 
				startTag + 
				annotatedHtml.substring(annotationHtml.posStart + numCharsAdded, 
						annotationHtml.posEnd + numCharsAdded) + 
				END_TAG + 
				annotatedHtml.substring(annotationHtml.posEnd + numCharsAdded);
	
				numCharsAdded += startTag.length() + END_TAG.length();
			}
			
			return annotatedHtml;
		}
		
		public String annotateHtmlEx(TextSegmentEx[] annotations) {
			String annotatedHtml = html;
			if (annotations.length == 0)
				return html;
			
			int numCharsAdded = 0;
			
			for (int j=0; j<annotations.length; j++) {
				log.info("annotation: " + annotations[j].type + " "
						+ annotations[j].segment.text.substring(annotations[j].segment.posStart, 
								annotations[j].segment.posEnd));
				String startTag = START_TAG;
				if (annotations[j].type == Application.INSTANCE.analyzers.COMMITMENT)
					startTag = START_TAG.replace(COLOR_PLACEHOLDER, "LightGreen");
				else if (annotations[j].type == Application.INSTANCE.analyzers.SPEECHACT)
					startTag = START_TAG.replace(COLOR_PLACEHOLDER, "Yellow");
				
				TextSegment annotationHtml = null;
				try {
					annotationHtml = mapAnnotationToHtml(annotations[j].segment);
					
					annotatedHtml = annotatedHtml.substring(0, annotationHtml.posStart + numCharsAdded) + 
						startTag 
						+ annotatedHtml.substring(annotationHtml.posStart + numCharsAdded, annotationHtml.posEnd + numCharsAdded) 
						+ END_TAG 
						+ annotatedHtml.substring(annotationHtml.posEnd + numCharsAdded);
		
					numCharsAdded += startTag.length() + END_TAG.length();
				} catch (Exception e) {
					e.printStackTrace();
					log.info("Exception: " + e.getMessage() 
							+ " at mapAnnotationToHtml for " 
							+ annotations[j].segment.toString()
							+ " " + annotations[j].segment.posStart
							+ " " + annotations[j].segment.posEnd);
				}
			}
			
			return annotatedHtml;
		}
	}
}