package com.alfresco.api.example;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;

/**
 * This class contains only the logic that is specific to using the Public API
 * against an Alfresco repository running on-premise (4.2.d or later).
 * 
 * @author jpotts
 */
public class BaseOnPremExample extends BasePublicAPIExample {

	/**
	 * Change these to match your environment
	 */
	public static final String ALFRESCO_API_URL = "http://localhost:8080/alfresco/api/";
	public static final String USER_NAME = "admin";
	public static final String PASSWORD = "admin";

	public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	public static final JsonFactory JSON_FACTORY = new JacksonFactory();
	
	private HttpRequestFactory requestFactory;

	public String getAtomPubURL(HttpRequestFactory requestFactory) {
		String atomPubURL = null;
	
		try {
			atomPubURL = ALFRESCO_API_URL + getHomeNetwork(ALFRESCO_API_URL, requestFactory) + "/public/cmis/versions/1.0/atom";
		} catch (IOException ioe) {
			System.out.println("Warning: Couldn't determine home network, defaulting to -default-");
			atomPubURL = ALFRESCO_API_URL + "-default-" + "/public/cmis/versions/1.0/atom";
		}
		
		return atomPubURL;
	}

	/**
	 * Gets a CMIS Session by connecting to the local Alfresco server.
	 * 
	 * @return Session
	 */
	public Session getCmisSession() {
		// default factory implementation
		SessionFactory factory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();

		// connection settings
		parameter.put(SessionParameter.ATOMPUB_URL, getAtomPubURL(getRequestFactory()));
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
		parameter.put(SessionParameter.AUTH_HTTP_BASIC, "true");
		parameter.put(SessionParameter.USER, USER_NAME);
		parameter.put(SessionParameter.PASSWORD, PASSWORD);
		parameter.put(SessionParameter.OBJECT_FACTORY_CLASS, "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");

		List<Repository> repositories = factory.getRepositories(parameter);

		return repositories.get(0).createSession();
	}

	/**
	 * Uses basic authentication to create an HTTP request factory.
	 * 
	 * @return HttpRequestFactory
	 */
	public HttpRequestFactory getRequestFactory() {
		if (this.requestFactory == null) {
    		this.requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
    			@Override
    			public void initialize(HttpRequest request) throws IOException {
    				request.setParser(new JsonObjectParser(new JacksonFactory()));
    				request.getHeaders().setBasicAuthentication(USER_NAME, PASSWORD);
    			}
    		});
		}
		return this.requestFactory;	
	}
}
