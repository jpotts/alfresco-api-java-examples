# alfresco-api-java-examples

This project contains two projects with sample code related to hitting the public Alfresco API.

The first project is alfresco-api-examples. Code in this project can do things like find the user's home network, find the sites the user can see, create folders and documents, and rate and comment on folders and documents. Starting with Alfresco 4.2.d Community Edition, this code can run against either Alfresco on-premise or Alfresco in the cloud.

The second project is file-loader. It shows how to upload a directory of images from your local machine to either Alfresco in the cloud or your own on-premise Alfresco repository. The point of the example is to show that the code is mostly the same. The parts that are different (how you obtain a CMIS session and how you determine the folder where the files should be uploaded) reside in different ancestor classes. Change the class LoadFiles.java extends depending on whether you want to upload to cloud or on-premise.

This second example is kind of obsolete now that the Public API is in both on-premise and the cloud.

## Requirements

To run the cloud examples in either project, you'll need an API key and a secret. To get those you must be a registered developer. Sign up for free at http://developer.alfresco.com.

You will also need an Alfresco Cloud account. Sign up for an Alfresco Cloud account at http://cloud.alfresco.com.
Dependencies

This project uses Maven to manage dependencies. It uses Google's OAuth2 client for Java for authentication. It also depends on Apache Chemistry OpenCMIS 0.9. The CmisAspectExample? relies on Apache Tika to extract information from JPEG files. All of these will be pulled in by Maven.

If you are running the file-loader project against an on-premise repository, the code is set up to expect that you've deployed the CMIS & Apache Chemistry in Action content model to your local Alfresco repository. If that's not the case for you, simply change the content type constant to something other than "cmisbook:image". 
