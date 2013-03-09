package com.alfresco.cloud.example;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;

import com.alfresco.cloud.example.model.ContainerEntry;
import com.alfresco.cloud.example.model.ContainerList;
import com.alfresco.cloud.example.oauth.LocalServerReceiver;
import com.alfresco.cloud.example.oauth.OAuth2ClientCredentials;
import com.alfresco.cloud.example.oauth.VerificationCodeReceiver;
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
 * Base example class from which all other examples inherit.
 * 
 * @author jpotts
 *
 */
public abstract class BaseJavaExample {

	public static final String HOME_NETWORK = "alfresco.com";
	public static final String SITE = "alfresco-api-demo";
	public static final String SCOPE = "public_api";
	public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	public static final JsonFactory JSON_FACTORY = new JacksonFactory();
	
	public static final String ALFRESCO_API_URL = "https://api.alfresco.com/";
	public static final String TOKEN_SERVER_URL = ALFRESCO_API_URL + "auth/oauth/versions/2/token";
	public static final String AUTHORIZATION_SERVER_URL = ALFRESCO_API_URL + "auth/oauth/versions/2/authorize";
	public static final String SITES_URL = "/public/alfresco/versions/1/sites";
	public static final String NODES_URL = "/public/alfresco/versions/1/nodes/";
	public static final String ATOMPUB_URL = ALFRESCO_API_URL + "cmis/versions/1.0/atom";

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
	
	public void run() throws Exception {
	    // authorization
	    VerificationCodeReceiver receiver = new LocalServerReceiver();
	    try {
	    	String redirectUri = receiver.getRedirectUri();
	    	launchInBrowser("google-chrome", redirectUri, OAuth2ClientCredentials.CLIENT_ID, SCOPE);
	    	final Credential credential = authorize(receiver, redirectUri);
	    	
	    	HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
	            @Override
	            public void initialize(HttpRequest request) throws IOException {
	              credential.initialize(request);
	              request.setParser(new JsonObjectParser(new JacksonFactory()));
	            }
	    	});
	    	
	    	System.out.println("Access token:" + credential.getAccessToken());
	    	
	    	doExample(requestFactory, credential);
	        
	    } catch (Exception e) {
	    	e.printStackTrace();
	    } finally {
	        receiver.stop();
	    }
	}

	public void doExample(HttpRequestFactory requestFactory, Credential credential)
		throws IOException {
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
		            OAuth2ClientCredentials.CLIENT_ID, OAuth2ClientCredentials.CLIENT_SECRET),
		        OAuth2ClientCredentials.CLIENT_ID,
		        AUTHORIZATION_SERVER_URL).setScopes(SCOPE).build();
		
		TokenResponse response = codeFlow.newTokenRequest(code)
		        .setRedirectUri(redirectUri).setScopes(SCOPE).execute();
		
		return codeFlow.createAndStoreCredential(response, null);

	  }
	
	/**
	 * Gets a CMIS Session by connecting to the Alfresco Cloud.
	 * 
	 * @param accessToken
	 * @return Session
	 */
	public Session getCmisSession(String accessToken) {
		// default factory implementation
		SessionFactory factory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();

		// connection settings
		parameter.put(SessionParameter.ATOMPUB_URL, ATOMPUB_URL);
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
		parameter.put(SessionParameter.AUTH_HTTP_BASIC, "false");
		parameter.put(SessionParameter.HEADER + ".0", "Authorization: Bearer " + accessToken);
		parameter.put(SessionParameter.OBJECT_FACTORY_CLASS, "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");

		List<Repository> repositories = factory.getRepositories(parameter);

		return repositories.get(0).createSession();
	}
	
	/**
	 * Use the CMIS API to create a document in a folder
	 * 
	 * @param cmisSession
	 * @param parentFolder
	 * @param file
	 * @param fileType
	 * @param props
	 * @return
	 * @throws FileNotFoundException
	 * 
	 * @author jpotts
	 * 
	 */
	public Document createDocument(Session cmisSession,
								   Folder parentFolder,
								   File file,
								   String fileType,
								   Map<String, Object> props)
			throws FileNotFoundException {

		String fileName = file.getName();

		// create a map of properties if one wasn't passed in
		if (props == null) {
			props = new HashMap<String, Object>();
		}

		// Add the object type ID if it wasn't already
		if (props.get("cmis:objectTypeId") == null) {
			props.put("cmis:objectTypeId",  "cmis:document");
		}
		
		// Add the name if it wasn't already
		if (props.get("cmis:name") == null) {
			props.put("cmis:name", fileName);
		}

		ContentStream contentStream = cmisSession.getObjectFactory().
				  createContentStream(
					fileName,
					file.length(),
					fileType,
					new FileInputStream(file)
				  );

		Document document = null;
		try {
			document = parentFolder.createDocument(props, contentStream, null);		
			System.out.println("Created new document: " + document.getId());
		} catch (CmisContentAlreadyExistsException ccaee) {
			document = (Document) cmisSession.getObjectByPath(parentFolder.getPath() + "/" + fileName);
			System.out.println("Document already exists: " + fileName);
		}
		
		return document;		
	}

	/**
	 * Use the CMIS API to get a handle to the root folder of the
     * target site, then create a new folder, then create
     * a new document in the new folder
     * 
	 * @param cmisSession
	 * @param parentFolderId
	 * @param folderName
	 * @return Folder
	 * 
	 * @author jpotts
	 * 
	 */
	public Folder createFolder(Session cmisSession, String parentFolderId, String folderName) {
		        
        Folder rootFolder = (Folder) cmisSession.getObject(parentFolderId);
        
        Folder subFolder = null;
        try {
        	// Making an assumption here that you probably wouldn't normally do
        	subFolder = (Folder) cmisSession.getObjectByPath(rootFolder.getPath() + "/" + folderName);
        	System.out.println("Folder already existed!");
        } catch (CmisObjectNotFoundException onfe) {
            Map<String, Object> props = new HashMap<String, Object>();
            props.put("cmis:objectTypeId",  "cmis:folder");
            props.put("cmis:name", folderName);
            subFolder = rootFolder.createFolder(props);
            String subFolderId = subFolder.getId();
            System.out.println("Created new folder: " + subFolderId);        	
        }        
        
        return subFolder;
	}

	/**
	 * Use the REST API to find the documentLibrary folder for
     * the target site
	 * @return String
	 * 
	 * @author jpotts
	 * 
	 */
	public String getRootFolderId(HttpRequestFactory requestFactory, String homeNetwork, String site) throws IOException {
        GenericUrl containersUrl = new GenericUrl(ALFRESCO_API_URL +
                                             homeNetwork +
        		                             "/public/alfresco/versions/1/sites/" +
                                             site +
                                             "/containers");

        HttpRequest request = requestFactory.buildGetRequest(containersUrl);
        ContainerList containerList = request.execute().parseAs(ContainerList.class);
        String rootFolderId = null;
        for (ContainerEntry containerEntry : containerList.list.entries) {
        	if (containerEntry.entry.folderId.equals("documentLibrary")) {
        		rootFolderId = containerEntry.entry.id;
        		break;
        	}
        }
        return rootFolderId;
	}
}