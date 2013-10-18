package com.alfresco.api.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
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
 * Shows how to add an aspect to a document. In this case, we're
 * using cm:geographic to store the latitude and longitude that is
 * set on some jpeg images in a folder.
 * 
 * @author jpotts
 *
 */
public class CmisGeographicAspectExample extends BaseOnPremExample {

	public static final String FOLDER_NAME = "images";
	public static final String FILE_PATH = "/users/jpotts/Documents/sample/photos/Berlin";
	public static final String FILE_TYPE = "image/jpeg";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CmisGeographicAspectExample ccde = new CmisGeographicAspectExample();
		try {
			ccde.doExample();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doExample() {
		try {
	
			// Get a CMIS session
			Session cmisSession = getCmisSession();
			
			// Find the root folder of our target site
			String rootFolderId = getRootFolderId(getSite());
			
			// Create a new folder in the root folder
			Folder subFolder = createFolder(cmisSession, rootFolderId, FOLDER_NAME);
			
			File dir = new File(FILE_PATH);
			if (!dir.exists() || !dir.isDirectory()) {
				System.out.println("Bad path specified: " + dir.getPath());
				return;
			}
	
			File[] fileList = dir.listFiles();
	
			for (File file : fileList) {
				// set up the properties map
		        Map<String, Object> props = getProperties(file);
	
		        // if we couldn't get the props for some reason, just
		        // move on to the next one
		        if (props.isEmpty()) {
		        	continue;
		        }
	
		        // create the document in the repo
		        createDocument(subFolder, file, FILE_TYPE, props);
			        
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * Use the CMIS API to get a handle to the root folder of the
     * target site, then create a new folder, then create
     * a new document in the new folder
	 * @param cmisSession
	 * @param parentFolderId
	 * @param folderName
	 * @return Folder
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
	 * Use Apache Tika to read the latitude and longitude from the
	 * jpegs.
	 * 
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public Map<String, Object> getProperties(File file)
			throws FileNotFoundException, IOException {

		Map<String, Object> props = new HashMap<String, Object>();

		//Tika tika = new Tika();
		
		String fileName = file.getName();
		System.out.println("File: " + fileName);
		InputStream stream = new FileInputStream(file);
        try {
	        Metadata metadata = new Metadata();
	        ContentHandler handler = new DefaultHandler();
	        Parser parser = new JpegParser();
	        ParseContext context = new ParseContext();
	 
	        //String mimeType = tika.detect(stream); // broken for my jpegs
	        String mimeType = "image/jpeg";
	        metadata.set(Metadata.CONTENT_TYPE, mimeType);
	 
	        parser.parse(stream, handler, metadata, context);
	        String lat = metadata.get("geo:lat");
	        String lon = metadata.get("geo:long");
	        stream.close();

	        // create a map of properties
			props.put("cmis:objectTypeId",  "cmis:document,P:cm:geographic");
			props.put("cmis:name", fileName);
			if (lat != null && lon != null) {
		        System.out.println("LAT:" + lat);
		        System.out.println("LON:" + lon);
				props.put("cm:latitude", lat);
				props.put("cm:longitude", lon);
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

}
