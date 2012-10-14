package com.cruxly.lib.analytics.test;

import java.util.List;
import java.util.logging.Level;

import org.junit.Test;

import com.cruxly.lib.model.Kip;

public class TestAdHocBuyGrammar extends TestDetector {
	
	@Test
	public void test_1_Its_Been_Long_Time_Since_I_Had_Starbucks_STARBUCKS_MOCHA_LATTE() {
		String[] rules = {
				":IWe=i|we|i'd|we'd|i'm|im|we're|i'll|we'll|i'm|ima|we're|i would|we would|i may|i might|we may|we might", 
				":IvWv=i've|ive|we have|i have|we've", 
				":Karticles=a|#|an|the|that|this|some|these|those|either|some|another|one|2|two|one of the|new|brand new|spanking new|my|my own|our own|our|either|is the|myself a", 
				":LongTimeSince=too long since|way too long since|its been ages since|it's been ages since|long time since|long time no", 
				//":LongTimeSince=ages", 
				"+~PHRASE_START _LongTimeSince _IWe? _IvWv? had? _Karticles* _KIP"}; 
		String content = "it's been ages since I had starbucks";
		Kip kip = STARBUCKS_MOCHA_LATTE;
		List<String> expectedIntentRules = BUY;
		// following rule should fire
		// +_LongTimeSince _IWe? _IvWv? had? _Karticles* _KIP
		check(rules, content, kip, expectedIntentRules);
	}
	
	@Test
	public void test_2_Its_Been_Long_Time_Since_I_Had_Starbucks_STARBUCKS_MOCHA_LATTE() {
		String[] rules = {
				":IWe=i|we would",
				":Foo=abc cde|x tyui v",
				"+~PHRASE_START _IWe need _Foo _KIP",
				}; 
				
		String content = "we would need x tyui v starbucks";
		Kip kip = STARBUCKS;
		List<String> expectedIntentRules = BUY;
		// following rule should fire
		// +_LongTimeSince _IWe? _IvWv? had? _Karticles* _KIP
		check(rules, content, kip, expectedIntentRules);
	}
}
