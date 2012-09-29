package com.cruxly.lib.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.validation.constraints.NotNull;


@SuppressWarnings("serial")
@XmlRootElement(name="document")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Document implements Serializable {
	
	public Document() {};	// JAXB needs this
	
	public Document(String text, Kip kip, boolean debug, String source,
			String type, String id, IntentRule[] intents) {
		super();
		this.text = text;
		this.kip = kip;
		this.debug = debug;
		this.source = source;
		this.type = type;
		this.id = id;
		this.intents = intents;
	}
	
	public Document(String text, Kip kip) {
		super();
		this.text = text;
		this.kip = kip;
		this.debug = false;
		this.source = "";
		this.type = "";
		this.id = "";
		this.intents = null;
	}

	@NotNull
	@XmlElement(nillable=false, required=true)
	public String text;
	
	@XmlElement(nillable=true, required=false, name="kip")
	public Kip kip;
	
	@XmlElement(nillable=true, required=false)
	public boolean debug;
	
	@XmlElement(nillable=true, required=false)
	public String source;
	
	@XmlElement(nillable=true, required=false)
	public String type;
	
	@XmlElement(nillable=true, required=false)
	public String id;
	
	@XmlElement(nillable=true, required=false)
	public IntentRule[] intents;

	@Override
	public String toString() {
		return "Document [text=" + text + ", kip=" + kip
				+ ", debug=" + debug + ", source=" + source + ", type=" + type
				+ ", id=" + id + ", intents=" + Arrays.toString(intents) + "]";
	}
}
