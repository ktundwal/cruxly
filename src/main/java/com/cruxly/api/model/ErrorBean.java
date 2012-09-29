package com.cruxly.api.model;

import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "error")
public class ErrorBean {
    public ErrorBean(String message, int code) {
		super();
		this.errorMsg = message;
		this.errorCode = code;
	}
    public ErrorBean(String message) {
		super();
		this.errorMsg = message;
		this.errorCode = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
	}
	private String errorMsg;
    private int errorCode;
}