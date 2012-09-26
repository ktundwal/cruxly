package com.cruxly.lib.analytics.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.cruxly.lib.analytics.Kips;

public class TestQuestionDetector extends TestDetector {
	@Test
	public void testWhoWantsKindleFire() {
		String content = "Who else wants a Kindle Fire? Follow us if you do RT @TheWhipNovel if you REALLY want it.. # FollowTheFire";
		Kips kip = KINDLEFIRE;
		List<String> expectedIntentRules = QUESTION;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void testCanYouGetMeALatte() {
		String content = "can you get me a latte";
		Kips kip = NO_KIP;
		List<String> expectedIntentRules = QUESTION;
		check(content, kip, expectedIntentRules);
	}
}
