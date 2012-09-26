package com.cruxly.lib.model;

import java.util.Vector;

import com.cruxly.lib.analytics.TextSegment;

public class TextSegments {
	Vector _textSegmentVector = new Vector();

	public void add(com.cruxly.lib.analytics.TextSegment[] textSegments) {
		for (int i = 0; i < textSegments.length; i++) {
			_textSegmentVector.addElement(textSegments[i]);
		}
	}

	public boolean isAvailable() {
		return _textSegmentVector.size() > 0 ? true : false;
	}

	public int size() {
		return _textSegmentVector.size();
	}

	public void copyInto(TextSegment[] annotations) {
		this.copyInto(annotations);
	}

	public TextSegment elementAt(int i) {
		return this.elementAt(i);
	}
}
