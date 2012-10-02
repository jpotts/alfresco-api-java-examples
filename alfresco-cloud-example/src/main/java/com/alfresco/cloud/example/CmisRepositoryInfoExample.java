package com.alfresco.cloud.example;

import java.io.IOException;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpRequestFactory;

/**
 * Simple example that shows how to connect to the Alfresco Cloud
 * and retrieve repository information.
 * 
 * @author jpotts
 *
 */
public class CmisRepositoryInfoExample extends BaseJavaExample {

	public static void main(String[] args) {
		CmisRepositoryInfoExample crie = new CmisRepositoryInfoExample();
		try {
			crie.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doExample(HttpRequestFactory requestFactory, Credential credential)
		throws IOException {
		
		// Get the accessToken
		String accessToken = credential.getAccessToken();

		// Get a CMIS session
		Session cmisSession = getCmisSession(accessToken);
		
		// Get the repository info
		RepositoryInfo repositoryInfo = cmisSession.getRepositoryInfo();
		
		System.out.println("    Name: " + repositoryInfo.getName());
		System.out.println("  Vendor: " + repositoryInfo.getVendorName());
		System.out.println(" Version: " + repositoryInfo.getProductVersion());

	}
}
