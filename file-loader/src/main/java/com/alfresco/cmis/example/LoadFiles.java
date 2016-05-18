package com.alfresco.cmis.example;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.jpeg.JpegParser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Loads the images in a local folder into a CMIS repo. Extend CloudExampleBase
 * to load into Alfresco in the cloud. Extend LocalExampleBase to load into a local repo.
 *
 * @author jpotts
 *
 */
public class LoadFiles extends CloudExampleBase {

    // Change these constants to fit your set up

    /**
     * Local directory containing JPG files
     */
    public static final String FILE_PATH = "/users/jpotts/Documents/sample/photos/Berlin";

    /**
     * Code assumes that every file is of the type below
     */
    public static final String FILE_TYPE = "image/jpeg";

    /**
     * Files will be uploaded to this folder, which resides in the folder returned
     * by super.getParentFolder()
     */
    public static final String FOLDER_NAME = "Art";

    /**
     * @param args
     */
    public static void main(String[] args) {
        LoadFiles lf = new LoadFiles();
        try {
            lf.doExample();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Uploads all files in a local directory to the CMIS server.
     * @throws IOException
     */
    public void doExample()
            throws IOException {

        // Get a CMIS session
        Session cmisSession;
        Folder folder;
        try {
            cmisSession = getCmisSession();
            Folder parentFolder = getParentFolder(cmisSession);
            folder = createFolder(cmisSession, parentFolder, FOLDER_NAME);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }

        File dir = new File(FILE_PATH);
        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("Bad path specified: " + dir.getPath());
            return;
        }

        File[] fileList = dir.listFiles();

        for (File file : fileList) {
            // set up the properties map
            Map<String, Object> props = getProperties(getObjectTypeId(), file);

            // if we couldn't get the props for some reason, just
            // move on to the next one
            if (props.isEmpty()) {
                continue;
            }

            // create the document in the repo
            createDocument(cmisSession, folder, file, FILE_TYPE, props);

        }

    }

    /**
     * Gets or creates a folder named folderName in the parentFolder.
     * @param cmisSession
     * @param parentFolder
     * @param folderName
     * @return
     */
    public Folder createFolder(Session cmisSession, Folder parentFolder, String folderName) {

        Folder subFolder = null;
        try {
            // Making an assumption here that you probably wouldn't normally do
            subFolder = (Folder) cmisSession.getObjectByPath(parentFolder.getPath() + "/" + folderName);
            System.out.println("Folder already existed!");
        } catch (CmisObjectNotFoundException onfe) {
            Map<String, Object> props = new HashMap<String, Object>();
            props.put("cmis:objectTypeId",  "cmis:folder");
            props.put("cmis:name", folderName);
            subFolder = parentFolder.createFolder(props);
            String subFolderId = subFolder.getId();
            System.out.println("Created new folder: " + subFolderId);
        }

        return subFolder;
    }

    /**
     * Returns the properties that need to be set on an object for a given file.
     *
     * @param file
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public Map<String, Object> getProperties(String objectTypeId, File file)
            throws FileNotFoundException, IOException {

        Map<String, Object> props = new HashMap<String, Object>();

        //Tika tika = new Tika();

        String fileName = file.getName();
        System.out.println("File: " + fileName);
        InputStream stream = new FileInputStream(file);
        try {
            // if the target type is the CMIS Book image type, let's extract some metadata from the file
            // and return them as properties to be set on the object
            if (objectTypeId.equals("D:cmisbook:image")) {

                Metadata metadata = new Metadata();
                ContentHandler handler = new DefaultHandler();
                Parser parser = new JpegParser();
                ParseContext context = new ParseContext();

                //String mimeType = tika.detect(stream); // broken for my jpegs
                String mimeType = FILE_TYPE;
                metadata.set(Metadata.CONTENT_TYPE, mimeType);

                parser.parse(stream, handler, metadata, context);
                String lat = metadata.get("geo:lat");
                String lon = metadata.get("geo:long");
                stream.close();

                // create a map of properties

                props.put("cmis:objectTypeId",  objectTypeId);
                props.put("cmis:name", fileName);
                if (lat != null && lon != null) {
                    System.out.println("LAT:" + lat);
                    System.out.println("LON:" + lon);
                    props.put("cmisbook:gpsLatitude", BigDecimal.valueOf(Float.parseFloat(lat)));
                    props.put("cmisbook:gpsLongitude", BigDecimal.valueOf(Float.parseFloat(lon)));
                }
            } else {
                // otherwise, just set the object type and name and be done
                props.put("cmis:objectTypeId",  objectTypeId);
                props.put("cmis:name", fileName);
            }
        } catch (TikaException te) {
            System.out.println("Caught tika exception, skipping");
        } catch (SAXException se) {
            System.out.println("Caught SAXException, skipping");
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        return props;
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

}
