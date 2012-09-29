package com.cruxly.lib.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@SuppressWarnings("serial")
@XmlRootElement(name="kip")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Kip implements Serializable {
	
	@XmlElement(nillable=false, required=true, name="keyterms")
	public String[] keyTerms = null;

	@XmlElement(nillable=false, required=true, name="genericterms")
	public String[] genericTerms = null;
	
	@XmlElement(nillable=false, required=true, name="competingterms")
	public String[] competingTerms = null;

	public Kip(String[] keyterms) {
		super();
		this.keyTerms = keyterms;
	}

	public Kip(String[] keyTerms, String[] industryTerms) {
		super();
		this.keyTerms = keyTerms;
		this.genericTerms = industryTerms;
	}

	public Kip(String[] keyTerms, String[] industryTerms, String[] competingTerms) {
		super();
		this.keyTerms = keyTerms;
		if (this.keyTerms != null) {
			this.keyTerms = convertToLowerCase(this.keyTerms);
		}
		
		this.genericTerms = industryTerms;
		if (this.genericTerms != null) {
			this.genericTerms = convertToLowerCase(this.genericTerms);
		}
		this.competingTerms = competingTerms;
		if (this.competingTerms != null) {
			this.competingTerms = convertToLowerCase(this.competingTerms);
		}
	}

	public Kip() {}

	public String getKey() {
		StringBuffer buffer = new StringBuffer();
		if (this.keyTerms != null) {
			List<String> products = Arrays.asList(this.keyTerms);
			Collections.sort(products);
			for (String product : products) {
				buffer.append(product + "_");
			}
		}
		
		if (this.genericTerms != null) {
			List<String> industryTerms = Arrays.asList(this.genericTerms);
			Collections.sort(industryTerms);
			for (String industryTerm : industryTerms) {
				buffer.append(industryTerm + "_");
			}
		}
		
		if (this.competingTerms != null) {
			List<String> competitors = Arrays.asList(this.competingTerms);
			Collections.sort(competitors);
			for (String competitor : competitors) {
				buffer.append(competitor + "_");
			}
		}
		
		return buffer.toString();
	}
	
	private String[] convertToLowerCase(String[] words) {
		for (int i = 0; i < words.length; i++) {
			words[i] = words[i].toLowerCase();
		}
		return words;
	}
	
	@Override
	public String toString() {
		return "Kip [keyterms=" + keyTerms + ", industryterms="
				+ Arrays.toString(genericTerms) + ", competingterms="
				+ Arrays.toString(competingTerms) + "]";
	}

	public String[] all() {
		List<String> all = new ArrayList<String>();
		if (this.keyTerms != null) {
			all.addAll(Arrays.asList(this.keyTerms));
		}
		if (this.genericTerms != null) {
			all.addAll(Arrays.asList(this.genericTerms));
		}
		if (this.competingTerms != null) {
			all.addAll(Arrays.asList(this.competingTerms));
		}
		
		return all.toArray(new String[all.size()]);
	}
}