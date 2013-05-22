package com.alfresco.cloud.example;

import java.io.File;
import java.io.IOException;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;

/**
 * Shows how to use CMIS to create a document in the Alfresco Cloud.
 * Also uses the REST API to like a folder and comment on a document.
 * 
 * @author jpotts
 *
 */
public class CmisCreateDocumentExample extends BaseJavaExample {

	public static final String FOLDER_NAME = "test folder";
	//public static final File FILE = new File("/users/jpotts/Documents/sample/sample-a.doc");
	//public static final String FILE_TYPE = "application/msword";
	public static final File FILE = new File("/users/jpotts/Documents/sample/sample-a.pdf");
	public static final String FILE_TYPE = "application/pdf";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CmisCreateDocumentExample ccde = new CmisCreateDocumentExample();
		try {
			ccde.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doExample(HttpRequestFactory requestFactory, Credential credential)
			throws IOException {
		
		// Get the home network
		String homeNetwork = getHomeNetwork(requestFactory, credential);
		
		// Get the accessToken
		String accessToken = credential.getAccessToken();

		// Get a CMIS session
		Session cmisSession = getCmisSession(accessToken);
		
		// Find the root folder of our target site
		String rootFolderId = getRootFolderId(requestFactory, homeNetwork, SITE);
		
		// Create a new folder in the root folder
		Folder subFolder = createFolder(cmisSession, rootFolderId, FOLDER_NAME);
		
		// Like the folder
		like(requestFactory, homeNetwork, subFolder.getId());
		
		// Create a test document in the subFolder
		Document document = createDocument(cmisSession, subFolder, FILE, FILE_TYPE, null);
		
		// Create a comment on the test document
		// NOTE: When dealing with documents, the REST API wants a versionSeriesID! 
		comment(requestFactory, homeNetwork, document.getVersionSeriesId(), "Here is a comment!");
		
	}

	/**
	 * Use the REST API to "like" an object
	 * 
	 * @param requestFactory
	 * @param homeNetwork
	 * @param objectId
	 * @throws IOException
	 */
	public void like(HttpRequestFactory requestFactory, String homeNetwork, String objectId) throws IOException {
        GenericUrl likeUrl = new GenericUrl(ALFRESCO_API_URL + 
        					 homeNetwork +
        					 NODES_URL + 
        					 objectId +
        					 "/ratings");
        HttpContent body = new ByteArrayContent("application/json", "{\"id\": \"likes\", \"myRating\": true}".getBytes());
        HttpRequest request = requestFactory.buildPostRequest(likeUrl, body);
        request.execute();
        System.out.println("You liked: " + objectId);
	}

	/**
	 * Use the REST API to comment on an object
	 * 
	 * @param requestFactory
	 * @param homeNetwork
	 * @param objectId
	 * @param comment
	 * @throws IOException
	 */
	public void comment(HttpRequestFactory requestFactory, String homeNetwork, String objectId, String comment) throws IOException {
        GenericUrl commentUrl = new GenericUrl(ALFRESCO_API_URL + 
        					 homeNetwork +
        					 NODES_URL + 
        					 objectId +
        					 "/comments");
        HttpContent body = new ByteArrayContent("application/json",
        										("{\"content\": \"" + comment + "\"}").getBytes());
        HttpRequest request = requestFactory.buildPostRequest(commentUrl, body);
        request.execute();
        System.out.println("You commented on: " + objectId);
	}

}
