/* Copyright (c) 2010 Cruxly Inc.
*/

package com.cruxly.lib.analytics;

public class StatsUtils {
	public static float standardDeviation (float [] sample) {
		if (sample.length < 2)
			throw new RuntimeException ("Standard deviation of sample size < 2");
		float mean = mean (sample);
		
		float sum = 0.0f;
		for (int i=0; i<sample.length; i++) {
			float d =sample[i] - mean;
			sum += d*d;
		}
		
		return (float)Math.sqrt(1.0d/(sample.length-1)*sum);
		
	}
	
	public static float mean(float[] sample) {
		if ((sample == null) || (sample.length == 0))
			throw new RuntimeException ("Mean of sample size 0");
		
		float sum = 0.0f;
		for (int i=0; i<sample.length; i++)
			sum += sample[i];
		
		return sum / sample.length;
	}
	
	public static class Distribution {
		public final float mean;
		public final float stdev;
		
		public Distribution (float m, float s) {
			mean = m;
			stdev = s;
		}
	}
	
	public static int poorMansLog (int x) {
		String s = new String (""+x);
		return s.length();
	}
}
