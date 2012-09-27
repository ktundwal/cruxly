package com.cruxly.lib.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@SuppressWarnings("serial")
@XmlRootElement(name="kip")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Kip implements Serializable {
	
	@XmlElement(nillable=false, required=true, name="product")
	public String product = null;

	@XmlElement(nillable=false, required=true, name="industryterms")
	public String[] industryTerms = null;
	
	@XmlElement(nillable=false, required=true, name="competitors")
	public String[] competitors = null;

	public Kip(String product) {
		super();
		this.product = product;
	}

	public Kip(String product, String[] industryTerms) {
		super();
		this.product = product;
		this.industryTerms = industryTerms;
	}

	public Kip(String product, String[] industryTerms, String[] competitors) {
		super();
		this.product = product;
		this.industryTerms = industryTerms;
		if (this.industryTerms != null) {
			this.industryTerms = convertToLowerCase(this.industryTerms);
		}
		this.competitors = competitors;
		if (this.competitors != null) {
			this.competitors = convertToLowerCase(this.competitors);
		}
	}

	public Kip() {}

	public String getKey() {
		StringBuffer buffer = new StringBuffer();
		if (product != null && !product.isEmpty()) {
			buffer.append(product + "_");
		}
		
		if (this.industryTerms != null) {
			List<String> industryTerms = Arrays.asList(this.industryTerms);
			Collections.sort(industryTerms);
			for (String industryTerm : industryTerms) {
				buffer.append(industryTerm + "_");
			}
		}
		
		if (this.competitors != null) {
			List<String> competitors = Arrays.asList(this.competitors);
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
		return "Kip [product=" + product + ", industryTerms="
				+ Arrays.toString(industryTerms) + ", competitors="
				+ Arrays.toString(competitors) + "]";
	}

	public String[] all() {
		List<String> all = new ArrayList<String>();
		all.add(product);
		if (this.industryTerms != null) {
			all.addAll(Arrays.asList(this.industryTerms));
		}
		if (this.competitors != null) {
			all.addAll(Arrays.asList(this.competitors));
		}
		
		return all.toArray(new String[all.size()]);
	}
}