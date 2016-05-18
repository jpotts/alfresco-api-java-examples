package com.alfresco.cmis.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;

/**
 * Knows how to provide the values specific to Alfresco on-premise, versions 4.2c and earlier.
 * Extend this class to load files into Alfresco running on your own server.
 * @author jpotts
 */
public class LocalExampleBase implements ExampleBaseIfc {

    // Change these to match your on-premise Alfresco server setup

    /**
     * Host domain and port with a trailing slash
     */
    public static final String ALFRESCO_API_URL = "http://localhost:8080/";

    /**
     * Username of a user with write access to the FOLDER_PATH
     */
    public static final String USER_NAME = "admin";

    /**
     * Password of a user with write access to the FOLDER_PATH
     */
    public static final String PASSWORD = "admin";

    /**
     * Folder path
     */
    public static final String FOLDER_PATH = "/blend";

    /**
     * The content type that should be used for the uploaded objects. The default below
     * Assumes you've deployed the Alfresco model included with the
     * CMIS & Apache Chemistry in Action book from Manning, see
     * https://github.com/fmui/ApacheChemistryInAction/tree/master/repositories/alfresco
     */
    public static final String CONTENT_TYPE = "D:cmisbook:image";

    // Probably do not need to change any constants below this

    //public static final String ATOMPUB_URL = ALFRESCO_API_URL + "alfresco/cmisatom"; // 4.0 - 4.2c
    public static final String ATOMPUB_URL = ALFRESCO_API_URL + "alfresco/api/-default-/public/cmis/versions/1.0/atom"; // 4.2d

    /**
     * Gets a CMIS Session by connecting to the Alfresco Cloud.
     *
     * @param accessToken
     * @return Session
     */
    public Session getCmisSession() {
        // default factory implementation
        SessionFactory factory = SessionFactoryImpl.newInstance();
        Map<String, String> parameter = new HashMap<String, String>();

        // connection settings
        parameter.put(SessionParameter.ATOMPUB_URL, ATOMPUB_URL);
        parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
        parameter.put(SessionParameter.AUTH_HTTP_BASIC, "true");
        parameter.put(SessionParameter.USER, USER_NAME);
        parameter.put(SessionParameter.PASSWORD, PASSWORD);
        parameter.put(SessionParameter.OBJECT_FACTORY_CLASS, "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");

        List<Repository> repositories = factory.getRepositories(parameter);

        return repositories.get(0).createSession();
    }

    public Folder getParentFolder(Session cmisSession) {
        Folder folder = (Folder) cmisSession.getObjectByPath(FOLDER_PATH);
        return folder;
    }

    public String getObjectTypeId() {
        return CONTENT_TYPE;
    }
}
