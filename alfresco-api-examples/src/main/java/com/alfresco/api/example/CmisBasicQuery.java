package com.alfresco.api.example;

import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.commons.data.PropertyData;

/**
 * An extremely basic CMIS query example. This is a port of the "Execute a
 * Query" example that ships with the Groovy console in the Workbench plus
 * an OperationContext to show how to limit the number of results returned.
 *
 * @author jpotts
 */
public class CmisBasicQuery extends BaseOnPremExample {
    public static void main(String[] args) {
        CmisBasicQuery sce = new CmisBasicQuery();
        sce.doExample();
    }

    public void doExample() {
        doQuery("SELECT cmis:objectId, cmis:name, cmis:contentStreamLength FROM cmis:document", 5);
    }

    public void doQuery(String cql, int maxItems) {
        Session cmisSession = getCmisSession();

        OperationContext oc = new OperationContextImpl();
        oc.setMaxItemsPerPage(maxItems);

        ItemIterable<QueryResult> results = cmisSession.query(cql, false, oc);

        for (QueryResult result : results) {
            for (PropertyData<?> prop : result.getProperties()) {
                System.out.println(prop.getQueryName() + ": " + prop.getFirstValue());
            }
            System.out.println("--------------------------------------");
        }

        System.out.println("--------------------------------------");
        System.out.println("Total number: " + results.getTotalNumItems());
        System.out.println("Has more: " + results.getHasMoreItems());
        System.out.println("--------------------------------------");
    }

}
