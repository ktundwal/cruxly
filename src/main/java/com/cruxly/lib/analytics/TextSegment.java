/* Copyright (c) 2010 Cruxly Inc.
*/

package com.cruxly.lib.analytics;

import java.util.Vector;

public class TextSegment {
	

	public final String text;
	public final int posStart;
	public final int posEnd;
	private final String toStringCached;
	public final String rule;
	
	public TextSegment (String _text, int _start, int _end) {
		text = _text;
		posStart = _start;
		posEnd = _end;
		toStringCached = text.substring(posStart, posEnd);
		rule = "";
	}
	
	public TextSegment (String _text, int _start, int _end, String rule) {
		text = _text;
		posStart = _start;
		posEnd = _end;
		toStringCached = text.substring(posStart, posEnd);
		this.rule = rule;
	}
	
	@Override
	public String toString() {
		String s;
		if (NewlineSegment.class.isInstance(this)
				|| SentenceStartSegment.class.isInstance(this)
				|| PhraseStartSegment.class.isInstance(this)
				|| SentenceEndSegment.class.isInstance(this)) {
			s = this.toString();
		}
		else {
			s = toStringCached;
		}
		return s;
		//return "TextSegment [text=" + text + ", posStart=" + posStart
		//		+ ", posEnd=" + posEnd + ", toStringCached=" + s
		//		+ ", rule=" + rule + "]";
	}
	
	public boolean equals(Object o) {
		return this.toString().equals(o.toString());
	}
	
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	public boolean overlaps(TextSegment s) {
		return !((s.posEnd < this.posStart) || (s.posStart > this.posEnd));
	}
	
	public static TextSegment[] excludeOverlapping (TextSegment [] arr1, TextSegment[] arr2) {
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
		
		TextSegment [] result = new TextSegment[v.size()];
		v.copyInto(result);
		return result;
	}
	
	public static abstract class SpecialSymbolSegment extends TextSegment {
		protected String symbol;
		
		public SpecialSymbolSegment(String _text, int _start, int _end) {
			super(_text, _start, _end);
			// TODO Auto-generated constructor stub
		}

		public String getSymbol() {
			return symbol;
		}

		public String toString() {
			throw new RuntimeException ("Cannot call toString() on a SpecialSymbolSegment");
		}
	}
	
	public static class NewlineSegment extends SpecialSymbolSegment {
		public NewlineSegment (String _text, int _start) {
			super(_text, _start, _start);
			this.symbol = "~NEWLINE";
		}
		
		public String toString() {
			return this.symbol;
		}
	}
	
	public static class SentenceStartSegment extends SpecialSymbolSegment {
		public SentenceStartSegment (String _text, int _start) {
			super(_text, _start, _start);
			this.symbol = "~SENT_START";
		}
		
		public String toString() {
			return this.symbol;
		}
	}
	
	public static class PhraseStartSegment extends SpecialSymbolSegment {
		public PhraseStartSegment (String _text, int _start) {
			super(_text, _start, _start);
			this.symbol = "~PHRASE_START";
		}
		
		public String toString() {
			return this.symbol;
		}
	}

	public static class SentenceEndSegment extends SpecialSymbolSegment {
		public SentenceEndSegment (String _text, int _start) {
			super(_text, _start, _start);
			this.symbol = "~SENT_END";
		}
		
		public String toString() {
			return this.symbol;
		}
	}
	
}
