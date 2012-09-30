package com.cruxly.api.client;

import java.net.URI;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.cruxly.lib.model.Document;
import com.cruxly.lib.model.Kip;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

public class TestIntentDetectorAPI {
	public static void main(String[] args) {

		ClientConfig config = new DefaultClientConfig();
		config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
		Client client = Client.create(config);
		WebResource service = client.resource(getBaseURI());
		
		System.out.println("Starting post test");
		handleResponse(testPost(service));
	}

	private static void handleResponse(ClientResponse clientResponse) {
		if (clientResponse.getStatus() != 200) {
			throw new RuntimeException("Failed : " + clientResponse);
		}

		Document[] documents = clientResponse.getEntity(Document[].class);

		System.out.println("Output from Server .... \n");
		for (Document document : documents) {
			System.out.println(document);
		}
	}
	
	private static ClientResponse testPost(WebResource service) {
		// Get JSON for application
		Document[] documents = new Document[1];
		documents[0] = new Document("I love coffee", 
				new Kip(new String[]{"starbucks"}, new String[]{"latte", "mocha", "coffee"}, null), 
				true, "twitter", "tweet", "001", null);
		WebResource webResource = service.path("v1").path("analyze");
		return webResource.type(MediaType.APPLICATION_JSON)
						  .accept(MediaType.APPLICATION_JSON)
						  .post(ClientResponse.class, documents);
	}

	private static URI getBaseURI() {
		return UriBuilder.fromUri("http://localhost:8080/api").build();
	}
}
