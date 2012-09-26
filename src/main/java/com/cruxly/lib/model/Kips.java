package com.cruxly.lib.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="intent")
public class Kips  {
	
	@XmlElement(nillable=true, required=false)
	public List<String> kips;

	public Kips(List<String> kips) {
		this.kips = kips;
	}

	public Kips() {
		this.kips = new ArrayList<String>();
	}

	public String getKey() {
		StringBuffer buffer = new StringBuffer();
		Collections.sort(kips);
		for (String value : kips) {
			buffer.append(value + "_");
		}
		return buffer.toString();
	}

	public boolean isEmpty() {
		return kips.isEmpty();
	}
}