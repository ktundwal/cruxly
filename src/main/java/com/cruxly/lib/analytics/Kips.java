package com.cruxly.lib.analytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Kips  {
	
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