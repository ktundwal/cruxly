package com.cruxly.api.model;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="document")
public class Document {
	
	public Document() {};	// JAXB needs this
	
	public Document(String text, String[] kip, boolean debug, String source,
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
	
	@XmlElement(nillable=false, required=true)
	public String text;
	
	@XmlElement(nillable=true, required=false)
	public String[] kip;
	
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
		return "Document [text=" + text + ", kip=" + Arrays.toString(kip)
				+ ", debug=" + debug + ", source=" + source + ", type=" + type
				+ ", id=" + id + ", intents=" + Arrays.toString(intents) + "]";
	}
}
