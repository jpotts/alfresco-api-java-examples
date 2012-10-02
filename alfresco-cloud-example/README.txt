Before running these examples you must first register at:
https://developer.alfresco.com

Add your auth key and secret to OAuth2ClientCredentials.java.

In your application profile on developer.alfresco.com, you must set
your callback URL to http://127.0.0.1:8080/Callback.

If you want to use a different host, port, or URL for your callback,
you must make the appropriate change to LocalServerReceiver.java.

BaseJavaExample.java

Set HOME_NETWORK to your Alfresco Cloud home network.

Set SITE to a test site ID in your home network in which you can create
and delete folders and documents.

CmisCreateDocumentExample.java

Before running this example, make sure the SITE has a folder named FOLDER_NAME.

CmisAspectExample.java

Before running this example, make sure the SITE has a folder named FOLDER_NAME.

