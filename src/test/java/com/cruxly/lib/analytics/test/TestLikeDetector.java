package com.cruxly.lib.analytics.test;

import java.util.List;
import java.util.logging.Level;

import org.junit.Test;

import com.cruxly.lib.model.Kip;

public class TestLikeDetector extends TestDetector {

	@Test
	public void testFarAwayKindleFire() {
		String content = "I like it but it actually is very far away KindleFire that is";
		Kip kip = KINDLEFIRE;
		List<String> expectedIntentRules = LIKE;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void testILikeStarbucks() {
		String content = "I like starbucks";
		Kip kip = STARBUCKS;
		List<String> expectedIntentRules = LIKE;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void testIMissMyMountainBike() {
		String content = "I miss my mountain bike. ???? # FuckAThief";
		Kip kip = NO_KIP;
		List<String> expectedIntentRules = LIKE;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void testUmSoTheGuyAtStarbucks() {
		String content = "Um so the guy at starbucks is amazing, he just gave me my 5$ drink for " +
				"2$ because I didn't have enough cash on me I'M IN LOVE";
		Kip kip = STARBUCKS;
		List<String> expectedIntentRules = LIKE;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void testILikeFooMno() {
		String content = "abc def I like foo mno pqr";
		Kip kip = new Kip(new String[]{"foo"});
		List<String> expectedIntentRules = LIKE;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void testILikeFarAwayFoo() {
		String content = "abc def I like mno pqr vbh jui foo";
		Kip kip = new Kip(new String[]{"foo"});
		List<String> expectedIntentRules = NOINTENT;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void testILikeYou() {
		String content = "I like you";
		Kip kip = NO_KIP;
		List<String> expectedIntentRules = LIKE;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void testStarbucksILikeYou() {
		String content = "starbucks I like you";
		Kip kip = STARBUCKS;
		List<String> expectedIntentRules = LIKE;
		// following 
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void testRTLike() {
		String content = "RT @macyymeoww: I like seriously need Starbucks.";
		Kip kip = NO_KIP;
		List<String> expectedIntentRules = LIKE;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void testIDefinitelyLikeKindleFire() {
		String content = "I definitely like KindleFire";
		Kip kip = NO_KIP;
		List<String> expectedIntentRules = LIKE;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void testKindleFireILoveYou() {
		String content = "KindleFire, I love you.";
		Kip kip = NO_KIP;
		List<String> expectedIntentRules = LIKE;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void testCastingMyVoteFor() {
		String content = "Casting my vote for starbucks";
		Kip kip = STARBUCKS;
		List<String> expectedIntentRules = LIKE_COMMITMENT;
		check(content, kip, expectedIntentRules);
	}
	
	@Test
	public void test_12_Starbucks_I_Like_You_STARBUCKS() {
		String[] rules = {
				":IWe=i|we|i'm|im|we're|i'll|we'll|i'm|we're",
				":NiceAdverbs=very|most|much|too|so|mucho|truly|indeed|genuinely|honestly|actually|seriously|definitely|really|truly|truely|just|verily|certainly",
				":LikeVerbs=miss|acclaim|advocate|admire|adore|applaud|appreciate|approve|attached to|attest|attracted to|awe of|back|believe in|benefit from|bless|brag|care for|cast my vote for|celebrate with|cheer|cheers to|cherish|clamor for|committed to|covet|crazy about|crazy for|delight|delight in|delighted with|desiderate|desire|devoted to|die|dig|dip into|disposed to|dote|drawn towards|dream|embrace|enamored with|endorse|enjoy|fancy|fantasize|fascinated with|favor|favour|fawn|feast|feel like a|feel a need for|fix on|fond of|freak out on|frolic with|glorify|go ape over|gravitate towards|hail|hanker|have a ball with|have a yen for|hooked on|hunger|impatient for|indulge|jump for|like|liked|lobby|long|look forward to|love|lust|luv|luxuriate|marvel|nominate|obsess with|obsess over|opt for|partial to|perk|picture myself in|pine|pleased with|plunge into|prefer|prescribe|prize|rally for|rally around|rave|regard for|rejoice|rejuvenate with|relish|revel|revere|rock with|rubber stamp|salivate|sanction|savor|side with|sing the praises of|stoke|take joy in|thirst|thrive|tout|treasure|tuck into|unwind with|value|venerate|vote for|vouch|wild for|wild about|wish for|worship|yearn",
				"+~PHRASE_START _KIP _IWe? _NiceAdverbs* _LikeVerbs",
				};
				
		String content = "starbucks I like you";
		Kip kip = STARBUCKS;
		List<String> expectedIntentRules = LIKE;
		check(rules, content, kip, expectedIntentRules);
	}
}
