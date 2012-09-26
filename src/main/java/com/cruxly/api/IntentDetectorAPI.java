package com.cruxly.api;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.cruxly.api.model.Document;
import com.cruxly.lib.analytics.IntentDetector;
import com.cruxly.lib.analytics.IntentDetectorException;

@Path("/json/intents")
public class IntentDetectorAPI {
	
private static final Logger logger = Logger.getLogger(IntentDetectorAPI.class.getName());
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Document[] detect(Document[] documents) throws IntentDetectorException {
		
		for (Document document : documents) {
			
			IntentDetector detector = new IntentDetector();
			document.intents = detector.detect(document.text, document.kip);
		}
		
		logger.info("Returning response");
		return documents;
	}
}
