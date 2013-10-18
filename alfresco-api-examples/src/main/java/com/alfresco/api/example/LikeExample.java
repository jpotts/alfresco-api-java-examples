package com.alfresco.api.example;

import java.io.IOException;

import org.apache.chemistry.opencmis.client.api.Folder;

/**
 * Shows how to use CMIS to create a document using the Alfresco Public API.
 * Also uses the REST API to like a folder and comment on a document.
 * 
 * @author jpotts
 *
 */
public class LikeExample extends BaseOnPremExample {

	public static void main(String[] args) {
		LikeExample le = new LikeExample();
		try {
			le.doExample();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doExample() {
		try {
			// Find the root folder of our target site
			String rootFolderId = getRootFolderId(getSite());
			
			// Create a new folder in the root folder
			Folder subFolder = createFolder(rootFolderId, getFolderName());
			
			// Like the folder
			like(subFolder.getId());
			
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

}
