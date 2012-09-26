package com.cruxly.lib.analytics.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.cruxly.lib.model.Kips;

public class TestRecommendationDetector extends TestDetector {
	@Test
	public void testLetsAddSomeFlash() {
		String content = "KindleFire speeds EqualLogic arrays: Stuff spindles, let's add some flash http://t.co/TgrwkFBC via @regvulture";
		Kips kip = NO_KIP;
		List<String> expectedIntentRules = RECOMMENDATION;
		check(content, kip, expectedIntentRules);
	}
}
