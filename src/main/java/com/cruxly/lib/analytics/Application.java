package com.cruxly.lib.analytics;

public enum Application {
	INSTANCE;
	
	public static final boolean DEBUG = System.getProperty("environment") == "development";
	
	public static final boolean DETECT_COMMITMENT = true;
	public static final boolean DETECT_BUY = true;
	public static final boolean DETECT_LIKE = true;
	public static final boolean DETECT_TRY = true;
	public static final boolean DETECT_QUESTION = true;
	public static final boolean DETECT_RECOMMENDATION = true;
	public static final boolean DETECT_DISLIKE = true;
	
	public Analyzers analyzers = null;
	
	private Application() {
		analyzers = new Analyzers();
	}
	
}
