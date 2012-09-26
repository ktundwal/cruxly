package com.cruxly.lib.analytics;

public class IntentDetectorException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Throwable cause = null;

	public IntentDetectorException() {
		super();
	}

	public IntentDetectorException(String message) {
		super(message);
	}

	public IntentDetectorException(String message, Throwable cause) {
		super(message);
		this.cause = cause;
	}

	public Throwable getCause() {
		return cause;
	}

	public void printStackTrace() {
		super.printStackTrace();
		if (cause != null) {
			System.err.println("Caused by:");
			cause.printStackTrace();
		}
	}

	public void printStackTrace(java.io.PrintStream ps) {
		super.printStackTrace(ps);
		if (cause != null) {
			ps.println("Caused by:");
			cause.printStackTrace(ps);
		}
	}

	public void printStackTrace(java.io.PrintWriter pw) {
		super.printStackTrace(pw);
		if (cause != null) {
			pw.println("Caused by:");
			cause.printStackTrace(pw);
		}
	}

}
