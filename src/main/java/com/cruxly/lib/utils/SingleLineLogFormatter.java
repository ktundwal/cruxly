package com.cruxly.lib.utils;

import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public final class SingleLineLogFormatter extends SimpleFormatter {
	
	@Override
	public String format(LogRecord record) {
        return record.getSourceClassName() + "." + record.getSourceMethodName() 
        		+ " " + record.getLevel() + " " + record.getMessage() + "\r\n";
        
	}
}