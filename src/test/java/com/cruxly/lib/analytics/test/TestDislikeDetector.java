package com.cruxly.lib.analytics.test;

import java.util.List;

import org.junit.Test;

import com.cruxly.lib.model.Kip;

public class TestDislikeDetector extends TestDetector {

	@Test
	public void testIHateFarAwayRomney() {
		String content = "I hate knats them bitches love black people more than Romney";
		Kip kip = new Kip(new String[]{"Romney"});
		List<String> expectedIntentRules = NOINTENT;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void testKindleFireIsStupidAs() {
		String content = "KindleFire is stupid ass.";
		Kip kip = KINDLEFIRE;
		List<String> expectedIntentRules = DISLIKE;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void testIHateStarbucksKIP() {
		String content = "I hate starbucks";
		Kip kip = STARBUCKS;
		List<String> expectedIntentRules = DISLIKE;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void testIHateStarbucks() {
		String content = "I hate starbucks";
		Kip kip = NO_KIP;
		List<String> expectedIntentRules = DISLIKE;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void test_5_Kindlefire_Keeps_Screwing_Us_Over_KINDLEFIRE() {
		String content = "Kindlefire keeps screwing us over";
		Kip kip = KINDLEFIRE;
		List<String> expectedIntentRules = DISLIKE;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void test_6_Kindlefire_Keeps_Screwing_Us_Over() {
		String content = "Kindlefire keeps screwing us over";
		Kip kip = NO_KIP;
		List<String> expectedIntentRules = DISLIKE;
		check(content, kip, expectedIntentRules);
	}
}
