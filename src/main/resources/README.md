Steps to retrieve client ID and secret file for Google drive authentication and basic application configuration 

	1. go to https://console.developers.google.com and login using google account.
	2. Create a new Project with name "DocumentViewer"
	3. Enable Google Drive API service for the project
	4. Create a credential
		a) Add project name as 'DocumentViewer'
		b) Add Authorized JS origin in my case ip of local host and port : http://192.168.1.2:8080
		c) Add authorized redirect :  http://192.168.1.2.nip.io:8080/oauth
		d) Download the secret file and rename it to client_secret_gd.json and paste it to 'src/main/resources/keys'
		e) update the path of 'client_secret_gd.json' in  the application.properties file under 'google.secret.key.path'
		
	5. Update other properties in 'application.properties' file. Example shown below
		
		google.project.name=DocumentViewer
		google.secret.key.path=classpath:keys/client_secret_gd.json
		google.oauth.callback.uri=http://<localhost-ip>:8080/oauth
		google.credentials.folder.path=file: <path on localhost to store credential>
		
 Start the spring boot application by running the class 'com.aksifar.documentviewer.gdrive.GdriveApplication.java'as a Java application.

- Once the application has started.
- Go to to browser and goto localhost:8080/ and click on 'SignIn' link.
- You will be navigated to google login page, login with into your google account and provide access to the google drive app.
- If all goes well then u will be re directed to a Welcome Page.

- You can go to localhost:8080/swagger to check the request response structures.

<b> After OAuth authentication is successfully completed. You need to use a REST client like POSTMAN to access the APIs <b>


APIs for following action has been implemented
==============================================
 1. Upload a file from localhost to the root folder in google drive (file path required)
 2. Upload a file from localhost to the desired folder in google drive(you need to know parent folder id)
 3. Get a List of all the files in the root folder
 4. Get a list of all the folders in the root folder
 5. Create a folder in the root folder. 
