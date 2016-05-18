package com.alfresco.cmis.example;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
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

import com.alfresco.cmis.example.model.ContainerEntry;
import com.alfresco.cmis.example.model.ContainerList;
import com.alfresco.cmis.example.oauth.LocalServerReceiver;
import com.alfresco.cmis.example.oauth.OAuth2ClientCredentials;
import com.alfresco.cmis.example.oauth.VerificationCodeReceiver;
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
 * Knows how to provide the values specific to Alfresco in the cloud. Extend this
 * class to load files into an existing site you've created in the cloud.
 */
public class CloudExampleBase implements ExampleBaseIfc {

    // Change these to match your network, site, and folder in Alfresco in the Cloud
    /**
     * Specify the cloud user's home network. In real life you'd probably make an API call to determine this.
     */
    public static final String HOME_NETWORK = "alfresco.com";

    /**
     * Specify the short name of the Alfresco cloud site where the files should be uploaded.
     */
    public static final String SITE = "alfresco-api-demo";

    // Probably do not need to change any constants below this
    public static final String ALFRESCO_API_URL = "https://api.alfresco.com/";
    public static final String ATOMPUB_URL = ALFRESCO_API_URL + "cmis/versions/1.0/atom";
    public static final String SCOPE = "public_api";
    public static final String CONTENT_TYPE = "cmis:document";

    public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    public static final JsonFactory JSON_FACTORY = new JacksonFactory();

    public static final String TOKEN_SERVER_URL = ALFRESCO_API_URL + "auth/oauth/versions/2/token";
    public static final String AUTHORIZATION_SERVER_URL = ALFRESCO_API_URL + "auth/oauth/versions/2/authorize";
    public static final String SITES_URL = "/public/alfresco/versions/1/sites";

    public HttpRequestFactory requestFactory;
    public Session cmisSession;

    /**
     * Gets a CMIS Session by connecting to the Alfresco Cloud.
     *
     * @param accessToken
     * @return Session
     */
    public Session getCmisSession() throws Exception {
        if (cmisSession == null) {
            // default factory implementation
            SessionFactory factory = SessionFactoryImpl.newInstance();
            Map<String, String> parameter = new HashMap<String, String>();

            // connection settings
            parameter.put(SessionParameter.ATOMPUB_URL, ATOMPUB_URL);
            parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
            parameter.put(SessionParameter.AUTH_HTTP_BASIC, "false");
            parameter.put(SessionParameter.HEADER + ".0", "Authorization: Bearer " + getAccessToken());
            parameter.put(SessionParameter.OBJECT_FACTORY_CLASS, "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");

            List<Repository> repositories = factory.getRepositories(parameter);

            this.cmisSession = repositories.get(0).createSession();
        }

        return this.cmisSession;
    }

    /**
     * Get the Folder object where the demo folder is to be created.
     */
    public Folder getParentFolder(Session cmisSession) throws Exception {

        String rootFolderId = getRootFolderId(this.requestFactory, HOME_NETWORK, SITE);

        Folder folder = (Folder) cmisSession.getObject(rootFolderId);

        return folder;

    }

    /**
     * Return the object type ID of the objects we want to create
     */
    public String getObjectTypeId() {
        return CONTENT_TYPE;
    }

    /**
     * Get the OAuth2 access token.
     * @return
     * @throws Exception
     */
    public String getAccessToken() throws Exception {
        String accessToken = "";
        // authorization
        VerificationCodeReceiver receiver = new LocalServerReceiver();
        try {
            String redirectUri = receiver.getRedirectUri();
            launchInBrowser("google-chrome", redirectUri, OAuth2ClientCredentials.CLIENT_ID, SCOPE);
            final Credential credential = authorize(receiver, redirectUri);

            this.requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) throws IOException {
                  credential.initialize(request);
                  request.setParser(new JsonObjectParser(JSON_FACTORY));
                }
            });

            accessToken = credential.getAccessToken();

            System.out.println("Access token:" + accessToken);

        } catch (Exception e) {
                e.printStackTrace();
        } finally {
            receiver.stop();
        }

        return accessToken;

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
     * Use the REST API to find the documentLibrary folder, then return its ID.
     *
     * @param requestFactory
     * @param homeNetwork
     * @param site
     * @return
     * @throws IOException
     */
    public String getRootFolderId(HttpRequestFactory requestFactory, String homeNetwork, String site) throws IOException {
        GenericUrl containersUrl = new GenericUrl(ALFRESCO_API_URL +
                                             homeNetwork +
                                             SITES_URL +
                                             "/" +
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
