package com.cruxly.api;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.cruxly.api.model.Document;
import com.cruxly.api.model.IntentRule;

@Path("/json/documents")
public class IntentDetectorAPI {
	
private static final Logger logger = Logger.getLogger(IntentDetectorAPI.class.getName());
	
	@POST
	@Path("/post")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Document[] detect(Document[] documents) {
		
		for (Document document : documents) {
			document.intents = new IntentRule[1];
			IntentRule intent = new IntentRule("placeholder", "buy");
			document.intents[0] = intent;
		}
		
		logger.info("Returning response");
		return documents;
	}
}
