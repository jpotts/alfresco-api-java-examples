package com.alfresco.api.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;

import com.alfresco.api.example.model.ContainerEntry;
import com.alfresco.api.example.model.ContainerList;
import com.alfresco.api.example.model.NetworkEntry;
import com.alfresco.api.example.model.NetworkList;
import com.alfresco.api.example.util.Config;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;

/**
 * This class contains constants and methods that are common across
 * the Alfresco Public API regardless of where the target repository is
 * hosted.
 *
 * @author jpotts
 *
 */
abstract public class BasePublicAPIExample {
    public static final String SITES_URL = "/public/alfresco/versions/1/sites/";
    public static final String NODES_URL = "/public/alfresco/versions/1/nodes/";

    private String homeNetwork;

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
    public Folder createFolder(String parentFolderId, String folderName) {
        Session cmisSession = getCmisSession();
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

    public String getHomeNetwork() throws IOException {
        if (this.homeNetwork == null) {
            GenericUrl url = new GenericUrl(getAlfrescoAPIUrl());

            HttpRequest request = getRequestFactory().buildGetRequest(url);

            NetworkList networkList = request.execute().parseAs(NetworkList.class);
            System.out.println("Found " + networkList.list.pagination.totalItems + " networks.");
            for (NetworkEntry networkEntry : networkList.list.entries) {
                if (networkEntry.entry.homeNetwork) {
                    this.homeNetwork = networkEntry.entry.id;
                }
            }

            if (this.homeNetwork == null) {
                this.homeNetwork = "-default-";
            }

            System.out.println("Your home network appears to be: " + homeNetwork);
        }
        return this.homeNetwork;
    }

    public Document createDocument(Folder parentFolder,
                                   File file,
                                   String fileType)
            throws FileNotFoundException {
        return createDocument(parentFolder, file, fileType, null);
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
    public Document createDocument(Folder parentFolder,
                                   File file,
                                   String fileType,
                                   Map<String, Object> props)
            throws FileNotFoundException {

        Session cmisSession = getCmisSession();

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
     * Use the REST API to find the documentLibrary folder for
     * the target site
     * @return String
     *
     * @author jpotts
     *
     */
    public String getRootFolderId(String site) throws IOException {

        GenericUrl containersUrl = new GenericUrl(getAlfrescoAPIUrl() +
                                             getHomeNetwork() +
                                             SITES_URL +
                                             site +
                                             "/containers");
        System.out.println(containersUrl);
        HttpRequest request = getRequestFactory().buildGetRequest(containersUrl);
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

    /**
     * Use the REST API to "like" an object
     *
     * @param requestFactory
     * @param homeNetwork
     * @param objectId
     * @throws IOException
     */
    public void like(String objectId) throws IOException {
        GenericUrl likeUrl = new GenericUrl(getAlfrescoAPIUrl() +
                             getHomeNetwork() +
                             NODES_URL +
                             objectId +
                             "/ratings");
        HttpContent body = new ByteArrayContent("application/json", "{\"id\": \"likes\", \"myRating\": true}".getBytes());
        HttpRequest request = getRequestFactory().buildPostRequest(likeUrl, body);
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
    public void comment(String objectId, String comment) throws IOException {
        GenericUrl commentUrl = new GenericUrl(getAlfrescoAPIUrl() +
                             getHomeNetwork() +
                             NODES_URL +
                             objectId +
                             "/comments");
        HttpContent body = new ByteArrayContent("application/json",
                                                ("{\"content\": \"" + comment + "\"}").getBytes());
        HttpRequest request = getRequestFactory().buildPostRequest(commentUrl, body);
        request.execute();
        System.out.println("You commented on: " + objectId);
    }

    public String getSite() {
        return Config.getConfig().getProperty("site");
    }

    public String getFolderName() {
        return Config.getConfig().getProperty("folder_name");
    }

    public File getLocalFile() {
        String filePath = Config.getConfig().getProperty("local_file_path");
        return new File(filePath);
    }

    public String getLocalFileType() {
        return Config.getConfig().getProperty("local_file_type");
    }

    abstract public String getAlfrescoAPIUrl();
    abstract public Session getCmisSession();
    abstract public HttpRequestFactory getRequestFactory();
}
