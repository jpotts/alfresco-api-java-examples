package com.alfresco.api.example;

import java.io.File;
import java.io.IOException;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;

/**
 * Shows how to use CMIS to create a document in the Alfresco Cloud.
 * Also uses the REST API to like a folder and comment on a document.
 * 
 * @author jpotts
 *
 */
public class CmisCreateDocumentExample extends BaseOnPremExample {

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
			ccde.doExample();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doExample() {
		try {
			// Get the home network
			String homeNetwork = getHomeNetwork(ALFRESCO_API_URL, getRequestFactory());
			
			// Get a CMIS session
			Session cmisSession = getCmisSession();
			
			// Find the root folder of our target site
			String rootFolderId = getRootFolderId(ALFRESCO_API_URL, getRequestFactory(), homeNetwork, SITE);
			
			// Create a new folder in the root folder
			Folder subFolder = createFolder(cmisSession, rootFolderId, FOLDER_NAME);
			
			// Like the folder
			like(ALFRESCO_API_URL, getRequestFactory(), homeNetwork, subFolder.getId());
			
			// Create a test document in the subFolder
			Document document = createDocument(cmisSession, subFolder, FILE, FILE_TYPE, null);
			
			// Create a comment on the test document
			// NOTE: When dealing with documents, the REST API wants a versionSeriesID! 
			comment(ALFRESCO_API_URL, getRequestFactory(), homeNetwork, document.getVersionSeriesId(), "Here is a comment!");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

}
