package com.cruxly.api.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.cruxly.lib.model.Document;

@XmlRootElement
public class Documents {
	public List<Document> documents;
}
