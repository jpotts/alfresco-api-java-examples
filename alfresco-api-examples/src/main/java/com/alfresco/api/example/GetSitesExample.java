package com.alfresco.api.example;

import java.io.IOException;

import com.alfresco.api.example.model.SiteEntry;
import com.alfresco.api.example.model.SiteList;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;

/** 
 * Simple example that shows how to hit the Alfresco Cloud with
 * the REST API to find the user's home network and to list up to
 * 10 sites visible to the user.
 * 
 * @author jpotts
 */
public class GetSitesExample extends BaseCloudExample {
	
	public static void main(String[] args) {
		GetSitesExample gse = new GetSitesExample();
		gse.doExample();
	}

	public void doExample() {
		try {
			// Find the user's home network
			String homeNetwork = getHomeNetwork(ALFRESCO_API_URL, getRequestFactory());
	        
	        // List some of the sites the user can see
	        GenericUrl sitesUrl = new GenericUrl(ALFRESCO_API_URL +
	        									 homeNetwork +
	        									 SITES_URL + "?maxItems=10");
	        System.out.println(sitesUrl);
	        HttpRequest request = getRequestFactory().buildGetRequest(sitesUrl);
	        SiteList siteList = request.execute().parseAs(SiteList.class);
	        System.out.println("Up to 10 sites you can see are:");
	        for (SiteEntry siteEntry : siteList.list.entries) {
	        		System.out.println(siteEntry.entry.id);
	        }
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		System.out.println("Done!");
	}
}
