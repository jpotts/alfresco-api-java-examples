package com.alfresco.cmis.example.model;

import com.google.api.client.util.Key;

/**
 * @author jpotts
 */
public class Network {
    @Key
    public String id;

    @Key
    public boolean homeNetwork;

    //@Key
    //DateTime createdAt;

    @Key
    public boolean paidNetwork;

    @Key
    public boolean isEnabled;

    @Key
    public String subscriptionLevel;
}
