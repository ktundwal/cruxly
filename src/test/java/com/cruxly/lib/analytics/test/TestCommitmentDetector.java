package com.cruxly.lib.analytics.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.cruxly.lib.analytics.Kips;

public class TestCommitmentDetector extends TestDetector {
	@Test
	public void testAboutToMakeAAmericanApparelOrder() {
		String content = "about to make a american apparel order..... should i";
		Kips kip = NO_KIP;
		List<String> expectedIntentRules = COMMITMENT;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void testRallySoftwareIsLookingFor() {
		String content = "Rally Software is looking for: Software Engineer - Web Application.INSTANCE " +
				"... http://t.co/CnIWnVwM # job";
		Kips kip = new Kips(Arrays.asList("Rally Software"));
		List<String> expectedIntentRules = COMMITMENT;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void testMeanwhileAboutTo() {
		String content = "meanwhile about to download this detroit mixtape";
		Kips kip = NO_KIP;
		List<String> expectedIntentRules = COMMITMENT;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void testIWouldLikeToTryThatCar() {
		String content = "I would like to try that car.";
		Kips kip = NO_KIP;
		List<String> expectedIntentRules = COMMITMENT;
		check(content, kip, expectedIntentRules);
	}
	

	
	@Test
	public void testAboutToBuyThisKindleFire() {
		String content = "about to buy this KindleFire.";
		Kips kip = NO_KIP;
		List<String> expectedIntentRules = COMMITMENT;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void testICouldUseAnotherMocha() {
		String content = "I could use another Mocha Frapp� right now";
		Kips kip = new Kips(Arrays.asList("latte", "starbucks", "mocha", "coffee"));
		List<String> expectedIntentRules = BUY;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void test_14_Three_Redbulls_And_A_Large_Coffee_Later_STARBUCKS_MOCHA_LATTE() {
		String content = "Three Redbulls and a large coffee later... I'm crashing incredibly hard.";
		Kips kip = STARBUCKS_MOCHA_LATTE;
		List<String> expectedIntentRules = COMMITMENT;
		check(content, kip, expectedIntentRules);
	}
}
