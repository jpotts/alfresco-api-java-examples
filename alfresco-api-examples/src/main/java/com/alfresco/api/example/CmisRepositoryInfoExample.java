package com.alfresco.api.example;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.RepositoryCapabilities;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;

/**
 * Simple example that shows how to use the Alfresco Public API to
 * retrieve repository information.
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
		RepositoryInfo repoInfo = cmisSession.getRepositoryInfo();
		
		System.out.println("Product name: " + repoInfo.getProductName());
		System.out.println("Product version: " + repoInfo.getProductVersion());
		System.out.println("Product vendor: " + repoInfo.getVendorName());
		System.out.println("CMIS version supported: " + repoInfo.getCmisVersionSupported());
		
		RepositoryCapabilities caps = repoInfo.getCapabilities();
		
		System.out.println("Partial list of capabilities...");
		System.out.println("Joins? " + caps.getJoinCapability());
		System.out.println("ACLs? " + caps.getAclCapability());
		System.out.println("Changes? " + caps.getChangesCapability());
		System.out.println("Queries? " + caps.getQueryCapability());
		System.out.println("Content stream updates? " + caps.getContentStreamUpdatesCapability());
		System.out.println("Renditions? " + caps.getRenditionsCapability());
		System.out.println("Multifiling? " + caps.isMultifilingSupported());
		System.out.println("Version-specific filing? " + caps.isVersionSpecificFilingSupported());		

	}
}
