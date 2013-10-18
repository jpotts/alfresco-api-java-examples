These are simple examples showing how to exercise the Alfresco Public API. With
the release of Alfresco 4.2.d Community Edition, the Public API is now
available on both Alfresco in the cloud and Alfresco on-premise. The difference
is how authentication works and the base part of the URLs.

You can run these examples against either Alfresco in the cloud or your own
Alfresco server, it is completely up to you.

To run a given example against Alfresco in the cloud, change the example class to
extend from BaseCloudExample.

To run a given example against Alfresco on-premise, change the example class to extend
from BaseOnPremExample.

Before running against either cloud or on-premise
=================================================

Edit config.properties. Set site to a test site ID in which you can
create and delete folders and documents.

If you are running CmisCreateDocumentExample or CmisGeographicAspectExample, set the
folder_name, local_file_path, and local_file_type properties.

Before running against Alfresco in the cloud
============================================

Before running these examples against Alfresco in the cloud you must first register at:
https://developer.alfresco.com

Add your API key and secret to config.properties.

In your application profile on developer.alfresco.com, you must set
your callback URL to http://127.0.0.1:8080/Callback.

If you want to use a different host, port, or URL for your callback,
you must make the appropriate change to LocalServerReceiver.java.

Before running against Alfresco on-premise
==========================================

Edit host, username, and password.