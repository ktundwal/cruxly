package com.cruxly.lib.analytics.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.junit.Before;

import com.cruxly.api.IntentDetectorException;
import com.cruxly.lib.analytics.FiniteStateMachine;
import com.cruxly.lib.analytics.IntentDetector;
import com.cruxly.lib.analytics.StringUtils;
import com.cruxly.lib.analytics.SurfaceAnalysis;
import com.cruxly.lib.analytics.TextSegment;
import com.cruxly.lib.model.Document;
import com.cruxly.lib.model.IntentRule;
import com.cruxly.lib.model.Kip;
import com.cruxly.lib.model.TextSegmentEx;
import com.cruxly.lib.utils.SingleLineLogFormatter;

public class TestDetector {

	protected static final List<String> LIKE = Arrays.asList("like");
	protected static final List<String> COMMITMENT = Arrays.asList("commitment");
	protected static final List<String> DISLIKE = Arrays.asList("dislike");
	protected static final List<String> BUY = Arrays.asList("buy");
	protected static final List<String> BUY_LIKE = Arrays.asList("buy", "like");
	protected static final List<String> BUY_COMMITMENT = Arrays.asList("buy", "commitment");
	protected static final List<String> LIKE_COMMITMENT = Arrays.asList("like", "commitment");
	@SuppressWarnings("unused")
	private static final List<String> TRY = Arrays.asList("try");
	protected static final List<String> QUESTION = Arrays.asList("question");
	protected static final List<String> RECOMMENDATION = Arrays.asList(("recommendation"));
	protected static final List<String> NOINTENT = new ArrayList<String>();
	
	protected static final Kip NO_KIP = new Kip();
	protected static final Kip STARBUCKS = new Kip(new String[]{"starbucks"});
	protected static final Kip STARBUCKS_MOCHA_LATTE = new Kip(new String[]{"starbucks"}, 
			new String[]{"latte", "mocha", "coffee"});
	protected static final Kip KINDLEFIRE = new Kip(new String[]{"KindleFire"}, new String[]{"kindle fire"});
	
	private static Logger LOGGER = Logger.getLogger(TestDetector.class.getName());
	
	private static IntentDetector _detector = new IntentDetector();
	private static Level logLevel = Level.FINE;

	public TestDetector() {
		super();
	}

	@Before
	public void setup() {
	    System.setProperty("runtime.environment", "development");
	    
	    Logger globalLogger = Logger.getLogger("");
	    setSingleLineLogFormatterForLogger(globalLogger);
	    setHandlerLevels(globalLogger, logLevel);
		
	    Logger surfaceAnalysisLogger = Logger.getLogger(SurfaceAnalysis.class.getName());
		surfaceAnalysisLogger.addHandler(new ConsoleHandler());
		setHandlerLevels(surfaceAnalysisLogger, logLevel);
		setSingleLineLogFormatterForLogger(surfaceAnalysisLogger);
		
		setHandlerLevels(LOGGER, logLevel);
		
		Logger fsmLogger = Logger.getLogger(FiniteStateMachine.class.getName());
		setHandlerLevels(fsmLogger, logLevel);

	}

	private void setSingleLineLogFormatterForLogger(Logger globalLogger) {
		Handler[] handlers = globalLogger.getHandlers();
	    for(Handler handler : handlers) {
	        handler.setFormatter(new SingleLineLogFormatter());
	    }
	}
	
	private String getLoggerName(Logger logger) {
        String loggerName = logger.getName();
        if (loggerName.isEmpty()) {
            return "[root logger]";
        }
        return loggerName;
    }

    private void listHandlerLevels(Logger logger) {
        for (Handler handler : logger.getHandlers()) {
            logger.info(getLoggerName(logger) + ": handler level = " + handler.getLevel());
        }
        Logger parentLogger = logger.getParent();
        if (null != parentLogger) {
            for (Handler handler : parentLogger.getHandlers()) {
                logger.info("parent logger handler (" + getLoggerName(parentLogger) + "): handler level = " + handler.getLevel());
            }
        }
    }

    private void setHandlerLevels(Logger logger, Level level) {
    	logger.setLevel(level);
        for (Handler handler : logger.getHandlers()) {
            handler.setLevel(level);
        }
        Logger parentLogger = logger.getParent();
        if (null != parentLogger) {
            for (Handler handler : parentLogger.getHandlers()) {
                handler.setLevel(level);
            }
        }
    }

	protected void check(String content, Kip kip, List<String> expectedIntentRules) {
		IntentRule[] intentRules = null;
		try {
			intentRules = _detector.detect(new Document(content, kip));
		} catch (IntentDetectorException e) {
			Assert.fail(e.getMessage());
		}
		//if (!kip.isEmpty()) assertEquals("Intended KIP wasnt used", kip, Application.INSTANCE.kipAwareBuyAnalyzer.getKIP());
		
		List<String> intents = new ArrayList<String>();
		for (IntentRule intentRule : intentRules) {
			intents.add(intentRule.intent);
		}
		assertEquals(String.format("(%s) %s", kip, content), expectedIntentRules, intents);
	}
	
	protected void check(String[] rules, String content, Kip kip,
			List<String> expectedIntentRules) {
		SurfaceAnalysis analyzer = createAnalyzer("unknown", rules, kip, kip.getKeyTerms() != null);
		List<TextSegmentEx> intent_list = new ArrayList<TextSegmentEx>();
		TextSegment[] arrTokens = StringUtils.splitIntoTextSegments(content, true, true);
		analyzer.insertIntent(content, arrTokens, kip, intent_list);
		StringBuffer b = new StringBuffer();
		b.append("DETECTED SEGMENTS: " + intent_list.size());
		for (TextSegmentEx intent : intent_list) {
			b.append("\n\t" + intent.segment);
		}
		LOGGER.info(b.toString());
		assertEquals(String.format("(%s) %s", kip, content), expectedIntentRules.size(), intent_list.size());
	}

	private SurfaceAnalysis createAnalyzer(String type, String[] rules,
			Kip kip, boolean kipAware) {		
		SurfaceAnalysis sa = new SurfaceAnalysis(type, kipAware, "unknown");
		sa.setUserName("Peter Doe");
		sa.setRules(rules);
		if (kipAware) {
			sa.setKIP(kip);
		}
		sa.init();
		
		return sa;
	}

}