package com.aksifar.documentviewer.gdrive.controller;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.aksifar.documentviewer.gdrive.DTO.FileDTO;
import com.aksifar.documentviewer.gdrive.constants.Constants;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

@Controller
public class BaseController {

	private Credential credential;
	private Drive drive;
	private static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	private static JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	
	private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
	private static final String USER_IDENTIFIER_KEY ="MY_DUMMY_USER";
	
	@Value("${google.oauth.callback.uri}")
	private String CALLBACK_URI;
	
	@Value("${google.secret.key.path}")
	private Resource gdSecretKeys;
	
	@Value("${google.credentials.folder.path}")
	private Resource credentialsFolder;
	
	@Value("${google.project.name}")
	private String PPROJECT_NAME;
	
	private GoogleAuthorizationCodeFlow flow;
	
	@PostConstruct
	public void init() throws IOException
	{
		GoogleClientSecrets secrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(gdSecretKeys.getInputStream()));
		flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, secrets, SCOPES)
				.setDataStoreFactory(new FileDataStoreFactory(credentialsFolder.getFile())).build();
		
		credential = flow.loadCredential(USER_IDENTIFIER_KEY);
		drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(PPROJECT_NAME).build();
	}
	
	@GetMapping("/swagger")
    public String home()
    {
        return "redirect:swagger-ui.html";
    }
	
	
	@GetMapping("/")
	public String showHomePage() throws IOException
	{
		boolean isUserAuthenticated = false;
		Credential credential = flow.loadCredential(USER_IDENTIFIER_KEY);
		if(credential !=null )
		{
			boolean tokenValid = credential.refreshToken();
			if(tokenValid){
				isUserAuthenticated = true;
			}
		}
		return isUserAuthenticated? "dashboard.html" : "index.html" ;
	}
	
	@GetMapping("/googlesignin")
	public void googleSignIn(HttpServletResponse response) throws IOException{
		GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl();
		String redirectURL = url.setRedirectUri(CALLBACK_URI).setAccessType("offline").build();
		response.sendRedirect(redirectURL);
	}
	
	@GetMapping("/oauth")
	public void saveAuthorizationCode(HttpServletRequest request) throws IOException
	{
		String code = request.getParameter("code");
		if(null != code){
			saveToken(code);
		}
	}

	@PostMapping("/files")
	public ResponseEntity<Object> uploadFile(@Valid @RequestBody FileDTO fileDTO) throws IOException
	{
		if(null != fileDTO)
		{
			File file = new File();
			file.setName(fileDTO.getName());
			FileContent content = new FileContent(fileDTO.getContentType(), new java.io.File(fileDTO.getPath()));
			File uploadedFile = drive.files().create(file,content).setFields("id").execute();
			
			FileDTO uploaded = new FileDTO();
			uploaded.setName(uploadedFile.getName());
			uploaded.setId(uploadedFile.getId());
			
			return new ResponseEntity<>(uploaded, HttpStatus.CREATED);
		}
		return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
	}
	
	@GetMapping("/files")
	public ResponseEntity<List<FileDTO>> getFiles() throws IOException{
		List<FileDTO> response = new ArrayList<>();
		FileList fileList = getDriveFiles();
		for(File file: fileList.getFiles())
		{
			//skipping folders
			if(!file.getMimeType().equalsIgnoreCase(Constants.FOLDER_MIME))
			{
				response.add(mapGoogleFileToFileDTO(file));
			}
		}
		return new ResponseEntity<>(response,HttpStatus.OK);
	}
	
	
	@PostMapping("/folders/{folderId}/files")
	public ResponseEntity<Object> uploadFileInFolder(@Valid @RequestBody FileDTO fileDTO, @PathVariable("folderId") String folderId) throws IOException
	{
		if(null != fileDTO && folderId != null)
		{
			File file = new File();
			file.setName(fileDTO.getName());
			List<String> parents = new ArrayList<>();
			parents.add(folderId);
			file.setParents(parents);
			FileContent content = new FileContent(fileDTO.getContentType(), new java.io.File(fileDTO.getPath()));
			File uploadedFile = drive.files().create(file,content).setFields("id").execute();
			
			FileDTO uploaded = new FileDTO();
			uploaded.setName(uploadedFile.getName());
			uploaded.setId(uploadedFile.getId());
			
			return new ResponseEntity<>(uploaded, HttpStatus.CREATED);
		}
		return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
	}
	
	@PostMapping("/folders")
	public ResponseEntity<Object> createFolder(@Valid @RequestBody FileDTO fileDTO) throws IOException
	{
		if(null != fileDTO)
		{
			File file = new File();
			file.setName(fileDTO.getName());
			file.setMimeType(Constants.FOLDER_MIME);
			File uploadedFile = drive.files().create(file).execute();
			
			FileDTO createdFolder = new FileDTO();
			createdFolder.setName(uploadedFile.getName());
			createdFolder.setId(uploadedFile.getId());
			
			return new ResponseEntity<>(createdFolder, HttpStatus.CREATED);
		}
		return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
	}
	
	@GetMapping("/folders")
	public ResponseEntity<List<FileDTO>> getAllFolders() throws IOException{
		List<FileDTO> response = new ArrayList<>();
		FileList fileList = getDriveFiles();
		for(File file: fileList.getFiles())
		{
			//adding only folders
			if(file.getMimeType().equalsIgnoreCase(Constants.FOLDER_MIME))
			{
				response.add(mapGoogleFileToFileDTO(file));
			}
		}
		return new ResponseEntity<>(response,HttpStatus.OK);
	}
	
	private void saveToken(String code) throws IOException {
		GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(CALLBACK_URI).execute();
		flow.createAndStoreCredential(response, USER_IDENTIFIER_KEY);
	}
	
	private FileDTO mapGoogleFileToFileDTO(File file)
	{
		FileDTO newFile = new FileDTO();
		newFile.setName(file.getName());
		newFile.setId(file.getId());
		newFile.setContentType(file.getMimeType());
		return newFile;
	}
	
	private FileList getDriveFiles() throws IOException
	{
		return drive.files().list().setFields("files(id, name, mimeType)").execute();
	}
}
