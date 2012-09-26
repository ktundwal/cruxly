package com.cruxly.lib.analytics;
import java.util.Hashtable;

public class TokenCategories {
	protected static String [] _months = {"january", "february", "march", "april", "may", "june", "july", "august", "september", "october", "november", "december", "jan", "feb", "mar", "apr", "jun", "jul", "aug", "sep", "sept", "oct", "nov", "dec"};
	protected static String [] _DOWs  = {"monday", "mon", "tuesday", "tue", "wednesday", "wed", "thursday", "thu", "friday", "fri", "saturday", "sat", "sunday", "sun"};
	protected static Object dummy = new Object();
	
	protected static Hashtable months = new Hashtable();
	protected static Hashtable DOWs = new Hashtable();

	static {
		for (int i=0; i<_months.length; i++)
			months.put (_months[i], dummy);

		for (int i=0; i<_DOWs.length; i++)
			DOWs.put (_DOWs[i], dummy);

	}
	
	protected static boolean isMonth(String s) {
		return months.containsKey(s.toLowerCase());
	}

	protected static boolean isDOW(String s) {
		return DOWs.containsKey(s.toLowerCase());
	}
	
	protected static boolean isAlphaNum(String s) {
		boolean hasAlpha = false;
		boolean hasNum = false;
		for (int i=0; i<s.length(); i++) 
			if (StringUtils.isNumber(s.charAt(i)))
				hasNum = true;
			else if (StringUtils.isAlpha(s.charAt(i)))
				hasAlpha = true;
	
		return (hasAlpha && hasNum);
	}
	
	protected static boolean isTime(String s) {
		boolean hasColon = false;
		boolean hasNum = false;
		for (int i=0; i<s.length(); i++) 
			if (StringUtils.isNumber(s.charAt(i)))
				hasNum = true;
			else if (':' == s.charAt(i))
				hasColon = true;
	
		return (hasColon && hasNum);
	}
	
	protected static boolean isNumber(String s) {
		for (int i=0; i<s.length(); i++) 
			if (!StringUtils.isNumber(s.charAt(i)))
				return false;
		
		return true;
	}
	
	protected static boolean isURL(String s) {
		int idxDot = s.indexOf('.');
		
		if ((idxDot == -1) || (idxDot == (s.length()-1)))
			return false; //no period, or period in last place
		
		char nextChar = s.charAt(idxDot+1);
		if (nextChar=='.')
			return false;  // probaby an ellipsis (...)
		
		
		if ((idxDot == (s.length()-2)) && 
			((nextChar == 'a') || (nextChar == 'i')))
			return false; //blah.a or blah.i could just mean lack of space after period

		return true;
	}

	protected static boolean isEmail(String s) {
		int idxAt = s.indexOf('@');
		return (idxAt > 0) && (idxAt < s.length()) && (s.indexOf('@', idxAt+1) == -1);
	}
	
	protected static boolean isUppercase(char a) {
		return ((a >= 'A') && (a<='Z'));
	}
}