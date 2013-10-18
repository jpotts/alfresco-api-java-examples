package com.alfresco.api.example;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;

import com.alfresco.api.example.oauth.LocalServerReceiver;
import com.alfresco.api.example.oauth.VerificationCodeReceiver;
import com.alfresco.api.example.util.Config;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;

/**
 * This class contains only the logic specific to using the Alfresco Public API
 * against Alfresco in the cloud.
 * 
 * @author jpotts
 */
public class BaseCloudExample extends BasePublicAPIExample {
	
	public static final String CMIS_URL = "cmis/versions/1.0/atom";
	public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	public static final JsonFactory JSON_FACTORY = new JacksonFactory();

	public static final String ALFRESCO_API_URL = "https://api.alfresco.com/";
	
	public static final String TOKEN_SERVER_URL = ALFRESCO_API_URL + "auth/oauth/versions/2/token";
	public static final String AUTHORIZATION_SERVER_URL = ALFRESCO_API_URL + "auth/oauth/versions/2/authorize";
	public static final String SCOPE = "public_api";
	public static final List<String> SCOPES = Arrays.asList(SCOPE);

	private HttpRequestFactory requestFactory;
	private Credential credential;
	private Session cmisSession;

	public String getAlfrescoAPIUrl() {
		return ALFRESCO_API_URL;
	}

	public String getAtomPubURL() {
		return ALFRESCO_API_URL + CMIS_URL;
	}

	public void launchInBrowser(
		      String browser, String redirectUrl, String clientId, String scope) throws IOException {
		
		String authorizationUrl = new AuthorizationCodeRequestUrl(
		        AUTHORIZATION_SERVER_URL, clientId).setRedirectUri(redirectUrl)
		        .setScopes(Arrays.asList(scope)).build();
		
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
		    if (desktop.isSupported(Action.BROWSE)) {
		    	desktop.browse(URI.create(authorizationUrl));
		        return;
		    }
		}
		
		if (browser != null) {
			Runtime.getRuntime().exec(new String[] {browser, authorizationUrl});
		} else {
			System.out.println("Open the following address in your favorite browser:");
			System.out.println("  " + authorizationUrl);
		}
	}
	
	/**
	 * Does the OAuth dance by starting up a local server to handle the
	 * redirect. The credential gets saved off because it needs to be used
	 * when/if a CMIS session is needed.
	 * 
	 * @return HttpRequestFactory
	 */
	public HttpRequestFactory getRequestFactory() {
		if (this.requestFactory == null) {
			VerificationCodeReceiver receiver = new LocalServerReceiver();
		    try {
		    	String redirectUri = receiver.getRedirectUri();
		    	launchInBrowser("google-chrome", redirectUri, BaseCloudExample.getAPIKey(), SCOPE);
		    	this.credential = authorize(receiver, redirectUri);
		    	
		    	this.requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
		            @Override
		            public void initialize(HttpRequest request) throws IOException {
		              credential.initialize(request);
		              request.setParser(new JsonObjectParser(new JacksonFactory()));
		            }
		    	});
		    	
		    	System.out.println("Access token:" + credential.getAccessToken());
		        
		    } catch (Exception e) {
		    	e.printStackTrace();
		    } finally {
		    	try {
		    		receiver.stop();
		    	} catch (Exception e) {}
		    }
		}
		return this.requestFactory;
	}

	public Credential authorize(VerificationCodeReceiver receiver, String redirectUri)
		      throws IOException {
			
			String code = receiver.waitForCode();
			
			AuthorizationCodeFlow codeFlow = new AuthorizationCodeFlow.Builder(
			        BearerToken.authorizationHeaderAccessMethod(),
			        HTTP_TRANSPORT,
			        JSON_FACTORY,
			        new GenericUrl(TOKEN_SERVER_URL),
			        new ClientParametersAuthentication(
			            BaseCloudExample.getAPIKey(), BaseCloudExample.getAPISecret()),
			        BaseCloudExample.getAPIKey(),
			        AUTHORIZATION_SERVER_URL).setScopes(SCOPES).build();
			
			TokenResponse response = codeFlow.newTokenRequest(code)
			        .setRedirectUri(redirectUri).setScopes(SCOPES).execute();
			
			return codeFlow.createAndStoreCredential(response, null);

		  }

	/**
	 * Gets a CMIS Session by connecting to the Alfresco Cloud.
	 * 
	 * @param accessToken
	 * @return Session
	 */
	public Session getCmisSession() {
		if (cmisSession == null) {
			String accessToken = getCredential().getAccessToken();
			System.out.println("Access token:" + accessToken);
			
			// default factory implementation
			SessionFactory factory = SessionFactoryImpl.newInstance();
			Map<String, String> parameter = new HashMap<String, String>();
	
			// connection settings
			parameter.put(SessionParameter.ATOMPUB_URL, this.getAtomPubURL());
			parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
			parameter.put(SessionParameter.AUTH_HTTP_BASIC, "false");
			parameter.put(SessionParameter.HEADER + ".0", "Authorization: Bearer " + accessToken);
			parameter.put(SessionParameter.OBJECT_FACTORY_CLASS, "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");
	
			List<Repository> repositories = factory.getRepositories(parameter);
	
			cmisSession = repositories.get(0).createSession();
		}
		return cmisSession;
	}

	public Credential getCredential() {
		if (this.credential == null) {
			getRequestFactory(); // Yuck, depending on a side-effect
		}
		return this.credential;
	}

	public static String getAPIKey() {
		return Config.getConfig().getProperty("api_key");
	}
	
	public static String getAPISecret() {
		return Config.getConfig().getProperty("api_secret");
	}
}
