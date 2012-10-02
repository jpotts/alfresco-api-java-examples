package com.alfresco.cloud.example;

import java.io.IOException;

import com.alfresco.cloud.example.model.NetworkList;
import com.alfresco.cloud.example.model.SiteEntry;
import com.alfresco.cloud.example.model.SiteList;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;

/** 
 * Simple example that shows how to hit the Alfresco Cloud with
 * the REST API to find the user's home network and to list up to
 * 10 sites visible to the user.
 * 
 * @author jpotts
 */
public class GetSitesExample extends BaseJavaExample {

	public static void main(String[] args) {
		GetSitesExample gse = new GetSitesExample();
		try {
			gse.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doExample(HttpRequestFactory requestFactory, Credential credential)
		throws IOException {

		// Find the user's home network
		GenericUrl url = new GenericUrl(ALFRESCO_API_URL);
        
		HttpRequest request = requestFactory.buildGetRequest(url);        
        NetworkList networkList = request.execute().parseAs(NetworkList.class);
        System.out.println("Found " + networkList.list.pagination.totalItems + " networks.");
        String homeNetwork = networkList.list.entries.get(0).entry.id; // Assuming first network for right now
        System.out.println("Your home network appears to be: " + homeNetwork);
        
        // List some of the sites the user can see
        GenericUrl sitesUrl = new GenericUrl(ALFRESCO_API_URL +
        									 homeNetwork +
        									 SITES_URL + "?maxItems=10");
        
        request = requestFactory.buildGetRequest(sitesUrl);
        SiteList siteList = request.execute().parseAs(SiteList.class);
        System.out.println("Up to 10 sites you can see are:");
        for (SiteEntry siteEntry : siteList.list.entries) {
        	System.out.println(siteEntry.entry.id);
        }

        System.out.println("Done!");
	}
}
