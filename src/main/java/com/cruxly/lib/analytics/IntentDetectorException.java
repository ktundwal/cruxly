package com.cruxly.lib.analytics;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.cruxly.api.model.ErrorBean;

public class IntentDetectorException extends WebApplicationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Throwable cause = null;
	
	public IntentDetectorException() {
		super(Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON).build());
	}

	public IntentDetectorException(String message) {
		super(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
				.entity(new ErrorBean(message)).type(MediaType.APPLICATION_JSON).build());
	}

	public IntentDetectorException(String message, Throwable cause) {
		super(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
				.entity(new ErrorBean(message + cause)).type(MediaType.APPLICATION_JSON).build());
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
