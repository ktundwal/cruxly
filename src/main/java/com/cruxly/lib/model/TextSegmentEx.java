package com.cruxly.lib.model;

import java.util.Vector;

import com.cruxly.lib.analytics.TextSegment;

public class TextSegmentEx {	
	@Override
	public String toString() {
		return "TextSegmentEx [type=" + type + ", segment=" + segment + "]";
	}

	public String type;
	public TextSegment segment;

	public TextSegmentEx(String segmentType, TextSegment textSegment) {
		this.type = segmentType;
		this.segment = textSegment;
	}
	
	public static TextSegmentEx[] GetTextSegmentsEx(TextSegment[] segments, String segmentType)
	{
		TextSegmentEx[] segmentsEx = new TextSegmentEx[segments.length];

		for (int i = 0; i < segments.length; i++)
		{
			segmentsEx[i] = new TextSegmentEx(segmentType, segments[i]);
		}
		return segmentsEx;
	}

	public static TextSegmentEx[] CombineSegments(Vector textSegmentExs)
	{
		Vector list = new Vector();
		int totalSize = 0; // every element in the set
	    for (int i = 0; i < textSegmentExs.size(); i++) {
	    	TextSegmentEx[] textSegmentExArray = (TextSegmentEx[]) textSegmentExs.elementAt(i);
	        totalSize += textSegmentExArray.length;
	        list.addElement(arrayToVector(textSegmentExArray));
	    }
	    
		Vector result = new Vector();
		Vector lowest;
		
		while (result.size() < totalSize) { // while we still have something to add
            lowest = null;

            for (int i = 0; i < textSegmentExs.size(); i++) {
            	Vector curVector = (Vector) list.elementAt(i);
                    if (curVector.size() != 0) {
                            if (lowest == null) {
                                    lowest = curVector;
                            }
                            else if (((TextSegmentEx)curVector.elementAt(0)).segment.posStart 
                            		<= ((TextSegmentEx)lowest.elementAt(0)).segment.posStart) {
                                    lowest = curVector;
                            }
                    }
            }
            result.addElement(lowest.elementAt(0));
            lowest.removeElementAt(0);
		}
		
		return vectorToArray(result);
	}

	private static Vector arrayToVector(TextSegmentEx[] textSegmentExArray) {
		Vector result = new Vector();
		for (int i = 0; i < textSegmentExArray.length; i++) {
	    	result.addElement(textSegmentExArray[i]);
	    }
		return result;
	}
	
	private static TextSegmentEx[] vectorToArray(Vector textSegmentExVector) {
		TextSegmentEx[] result = new TextSegmentEx[textSegmentExVector.size()];
		for (int i = 0; i < textSegmentExVector.size(); i++) {
	    	result[i] = (TextSegmentEx) textSegmentExVector.elementAt(i);
	    }
		return result;
	}
}
