package com.cruxly.api;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.cruxly.lib.analytics.IntentDetector;
import com.cruxly.lib.model.Document;
import com.cruxly.lib.model.Kip;

@Path("/v1/analyze")
public class IntentDetectorAPI {
	
private static final Logger LOGGER = Logger.getLogger(IntentDetectorAPI.class.getName());
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Document[] detect(Document[] documents) {
		
		/*
		 * [{"text":"I love coffee","kip":{"product":"starbucks","industryterms":["coffee","latte","mocha"]},
		 * "source":"twitter","type":"tweet","id":"001"}]
		 */
		long start = System.currentTimeMillis();
		for (Document document : documents) {
			
			IntentDetector detector = new IntentDetector();
			document.intents = detector.detect(document);
		}
		long end = System.currentTimeMillis();
		if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, String.format("Processed %d tweets in %s secs", 
            		documents.length, (end-start)/1000));
		}
		return documents;
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Document[] sample(
			@QueryParam("text") @DefaultValue("I love starbucks") final String text,
			@QueryParam("kip") @DefaultValue("") final String kip) 
					throws IntentDetectorException {
		
		Document[] documents = new Document[1];
		documents[0] = new Document(text, new Kip(new String[]{kip}), 
				false, "twitter", "tweet", "001", null);
		long start = System.currentTimeMillis();
		for (Document document : documents) {
			
			IntentDetector detector = new IntentDetector();
			document.intents = detector.detect(document);
		}
		long end = System.currentTimeMillis();
		if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, String.format("Processed %d tweets in %s secs", 
            		documents.length, (end-start)/1000));
		}
		return documents;
	}
}
