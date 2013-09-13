package com.alfresco.api.example;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;

/**
 * Simple example that shows how to connect to the Alfresco Cloud
 * and retrieve repository information.
 * 
 * @author jpotts
 *
 */
public class CmisRepositoryInfoExample extends BaseOnPremExample {

	public static void main(String[] args) {
		CmisRepositoryInfoExample crie = new CmisRepositoryInfoExample();
		try {
			crie.doExample();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doExample() {
		
		// Get a CMIS session
		Session cmisSession = getCmisSession();
		
		// Get the repository info
		RepositoryInfo repositoryInfo = cmisSession.getRepositoryInfo();
		
		System.out.println("    Name: " + repositoryInfo.getName());
		System.out.println("  Vendor: " + repositoryInfo.getVendorName());
		System.out.println(" Version: " + repositoryInfo.getProductVersion());

	}
}
