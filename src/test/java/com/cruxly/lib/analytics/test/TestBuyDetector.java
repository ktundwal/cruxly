package com.cruxly.lib.analytics.test;


import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.cruxly.lib.model.Kip;

public class TestBuyDetector extends TestDetector {
	
	@Test
	public void test1NeedAmericanApparelDiscoPants() {
		String content = "Need American Apparel disco pants : ((((";
		Kip kip = new Kip(new String[]{"American Apparel"});
		List<String> expectedIntentRules = BUY;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void test2RTINeedStarbucks() {
		String content = "RT @macyymeoww: I need Starbucks";
		Kip kip = STARBUCKS;
		List<String> expectedIntentRules = NOINTENT;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void test3INeedStarbucks() {
		String content = "@macyymeoww: I need Starbucks";
		Kip kip = STARBUCKS;
		List<String> expectedIntentRules = BUY;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void test4IWantStarbucks() {
		String content = "I want Starbucks";
		Kip kip = STARBUCKS;
		List<String> expectedIntentRules = BUY;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void test5INeedBlahBlahBlahStarbucks() {
		String content = "@macyymeoww: I need blah blah blah Starbucks";
		Kip kip = STARBUCKS;
		List<String> expectedIntentRules = NOINTENT;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void test6StandingInLineForALatte() {
		String content = "standing in line for a latte.";
		Kip kip = NO_KIP;
		List<String> expectedIntentRules = Arrays.asList(new String[]{"buy", "commitment"});
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void test7IWishIHadAMacbook() {
		String content = "I wish I had a macbook";
		Kip kip = NO_KIP;
		List<String> expectedIntentRules = Arrays.asList(new String[]{"like", "buy"});
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void test8IWishIHadAMacbookWithKIP() {
		String content = "Hey there, I wish I had a kindlefire That would be so cool";
		Kip kip = KINDLEFIRE;
		List<String> expectedIntentRules = BUY_LIKE;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void test_9_Can_You_Get_Me_A_Latte_Question() {
		String content = "can you get me a latte?";
		Kip kip = NO_KIP;
		List<String> expectedIntentRules = BUY;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void test10BuildingUpOnMyAppleStock() {
		String content = "Building up on my apple stock.";
		Kip kip = NO_KIP;
		List<String> expectedIntentRules = Arrays.asList(new String[]{"buy", "commitment"});
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void test_11_I_Want_A_Hash_Surface_NOKIP() {
		String content = "I want a # Surface";
		Kip kip = NO_KIP;
		List<String> expectedIntentRules = BUY;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void test_12_I_Want_I_Really_Do_I_Want_Better_Tablets_Question_NOKIP() {
		String content = "Don't get wrong .. I want a # Surface .. I really do .. " +
				"but before I decide I want to see what Samsung, KindleFire & Asus deliver.. better tablets?";
		Kip kip = NO_KIP;
		List<String> expectedIntentRules = Arrays.asList(new String[]{"buy", "commitment", "question"});
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void test_13_I_Need_Starbucks_STARBUCKS() {
		String content = "I need starbucks";
		Kip kip = STARBUCKS;
		List<String> expectedIntentRules = BUY;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void test_15_I_Will_Buy_Starbucks_STARBUCKS_MOCHA_LATTE() {
		String content = "I will buy starbucks";
		Kip kip = STARBUCKS_MOCHA_LATTE;
		List<String> expectedIntentRules = BUY_COMMITMENT;
		check(content, kip, expectedIntentRules);
	}

	@Test
	public void test_16_We_Are_In_Desperate_Need_Of_Starbucks_STARBUCKS_MOCHA_LATTE() {
		String content = "we are in desperate need of starbucks";
		Kip kip = STARBUCKS_MOCHA_LATTE;
		List<String> expectedIntentRules = BUY;
		check(content, kip, expectedIntentRules);
	}

	@Test
	public void test_17_I_Am_Buying_Starbucks_STARBUCKS_MOCHA_LATTE() {
		String content = "I am buying starbucks";
		Kip kip = STARBUCKS_MOCHA_LATTE;
		List<String> expectedIntentRules = BUY_COMMITMENT;
		check(content, kip, expectedIntentRules);
	}
	

	
	@Test
	public void test18IWillJustBuyAnotherStarbucks() {
		String content = "I'll just buy another starbucks";
		Kip kip = STARBUCKS_MOCHA_LATTE;
		List<String> expectedIntentRules = BUY_COMMITMENT;
		check(content, kip, expectedIntentRules);
	}

	@Test
	public void test_19_I_Might_Have_To_Get_Starbucks_STARBUCKS_MOCHA_LATTE() {
		String content = "I might have to get starbucks";
		Kip kip = STARBUCKS_MOCHA_LATTE;
		List<String> expectedIntentRules = BUY;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void test_20_I_Might_Have_To_Get_Starbucks_STARBUCKS() {
		String content = "I might have to get starbucks";
		Kip kip = STARBUCKS;
		List<String> expectedIntentRules = BUY;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void test_21_I_Might_Have_To_Get_Starbucks_NOKIP() {
		String content = "I might have to get starbucks";
		Kip kip = NO_KIP;
		List<String> expectedIntentRules = BUY;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void test_22_I_About_To_Buy_Starbucks_STARBUCKS_MOCHA_LATTE() {
		String content = "I am about to buy starbucks";
		Kip kip = STARBUCKS_MOCHA_LATTE;
		List<String> expectedIntentRules = BUY;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void test_23_Its_Been_Way_To_Long_Since_I_Had_Starbucks_STARBUCKS_MOCHA_LATTE() {
		String content = "Okay, it's been way too long since I've had starbucks.";
		Kip kip = STARBUCKS_MOCHA_LATTE;
		List<String> expectedIntentRules = BUY;
		// following rule should fire
		// +~PHRASE_START _PreVerbs* _Adverbs* _IWe? _AmAre? _AboutTo _PreVerbs* _Adverbs* _BIVerbs _Karticles* _KIP
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void test_24_Its_Been_Long_Time_Since_I_Had_Starbucks_STARBUCKS_MOCHA_LATTE() {
		String content = "I its been long time since I had starbucks";
		Kip kip = STARBUCKS_MOCHA_LATTE;
		List<String> expectedIntentRules = BUY;
		// following rule should fire
		// +_LongTimeSince _IWe? _IvWv? had? _Karticles* _KIP
		check(content, kip, expectedIntentRules);
	}
}
