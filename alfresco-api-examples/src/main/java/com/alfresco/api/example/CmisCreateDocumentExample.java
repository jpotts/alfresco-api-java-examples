package com.alfresco.api.example;

import java.io.IOException;

import org.apache.chemistry.opencmis.client.api.Folder;

/**
 * Shows how to use CMIS to create a document using the Alfresco Public API.
 *
 * @author jpotts
 *
 */
public class CmisCreateDocumentExample extends BaseOnPremExample {

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
            // Find the root folder of our target site
            String rootFolderId = getRootFolderId(getSite());

            // Create a new folder in the root folder
            Folder subFolder = createFolder(rootFolderId, getFolderName());

            // Create a test document in the subFolder
            createDocument(subFolder, getLocalFile(), getLocalFileType());

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}
