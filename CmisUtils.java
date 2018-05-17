package com.bct.dms.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map;
import java.util.UUID;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class CmisUtils {
	public Session CMISSession(String RepositoryURL,String User,String Password)
	{
		Session session = null;
		System.out.println("Connecting to Alfresco...");
		SessionFactory sessionFactory=SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();
		parameter.put(SessionParameter.USER, User);
		parameter.put(SessionParameter.PASSWORD,Password);
		parameter.put(SessionParameter.ATOMPUB_URL,RepositoryURL); //Example :: "http://192.168.1.225:8080/alfresco/cmisatom"
		parameter.put(SessionParameter.BINDING_TYPE,BindingType.ATOMPUB.value());
		parameter.put(PropertyIds.NAME, "-default-");
		parameter.put(SessionParameter.OBJECT_FACTORY_CLASS,"org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");

		try
		{
			List<Repository> repositories = sessionFactory.getRepositories(parameter);
			System.out.println("Connection established to alfresco");
			session = repositories.get(0).createSession();

			//String repositoryId=repositories.get(0).getId();
			//parameter.put(SessionParameter.REPOSITORY_ID,repositoryId);
			//session=sessionFactory.createSession(parameter);
			System.out.println("Repository ID: "+ session.getRepositoryInfo().getId());
		}
		catch(CmisUnauthorizedException cae)
		{
			System.out.println("Given credentials are "+cae.getMessage());
			//System.out.println("Given credentials are "+cae.getMessage());
			System.out.println("Failed to connect Alfresco");
			//System.out.println("Failed to connect Alfresco");
		}

		return session;
	}

	public Document PushDocument(File file,Session session,String path,String rootFol) 
	{

		Document document = null;
		String year = "",month = "",day = "",hour = "",rootFolder = "";
		String parts[];
		try{
			if(path==""||path==null)
			{
				Date dNow = new Date();
				SimpleDateFormat Fomat_Folder = new SimpleDateFormat ("yyyy/MM/dd/HH/");
				String url_path=Fomat_Folder.format(dNow);	
				parts = url_path.split("/");
				rootFolder = rootFol;
				year = parts[0];
				month = parts[1];
				day= parts[2];
				hour= parts[3];
				path="/"+rootFolder+"/"+url_path;
			}
			else if(!path.equalsIgnoreCase(null)|| !path.equalsIgnoreCase(""))
			{
				parts = path.split("/");
				rootFolder = parts[1];
				year = parts[2];
				month = parts[3];
				day= parts[4];
				hour= parts[5];
			}

			FolderCreationUtils foldercreate=new FolderCreationUtils();
			foldercreate.CheckAndCreateTreeFolder(session,rootFolder, year, month, day, hour);
			//File file=new File("E:\\JP\\ALFRESCO\\cmis-article.pdf");
			String fileName = file.getName();
			Date date = new Date();
			SimpleDateFormat ft = new SimpleDateFormat ("yyyyMMddhhmmss");
			//String UniqID= ft.format(date);
			CmisObject cmisObject = session.getObjectByPath(path);
			Folder parent = (Folder)cmisObject;
			Map props = new HashMap();
			if (props == null) 
			{
				props = new HashMap<String, Object>();
			}
			if (props.get("cmis:objectTypeId") == null) 
			{
				props.put("cmis:objectTypeId",  "cmis:document");
			}
			if (props.get("cmis:name") == null) 
			{
				props.put("cmis:name",fileName);

			}
			ContentStream contentStream = session.getObjectFactory().
					createContentStream( fileName, file.length(),"application/octet-stream", new FileInputStream(file));

			try {

				document = parent.createDocument(props, contentStream, null);
				//System.out.println("Created new document: " + document.getVersionSeriesId());
				System.out.println("Created new document: " + document.getVersionSeriesId());
			} catch (CmisContentAlreadyExistsException ccaee) {

				props.put("cmis:name",fileName);
				ContentStream contentStream1 = session.getObjectFactory().
						createContentStream( fileName, file.length(),"application/octet-stream", new FileInputStream(file));
				document = parent.createDocument(props, contentStream1, null);
				//System.out.println("Created new document: " + document.getVersionSeriesId());
				System.out.println("Created new document: " + document.getVersionSeriesId());
			}
		}catch(CmisInvalidArgumentException e)
		{
			System.out.println(e.getLocalizedMessage());
		}catch(FileNotFoundException e)
		{
			System.out.println(e.getLocalizedMessage());
		}
		catch(CmisObjectNotFoundException e)
		{
			System.out.println(e.getLocalizedMessage());
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			System.out.println(e.fillInStackTrace()+" Path must be /rootFolder/year/month/day/hour/ in this format");
			System.out.println("ArrayIndexOutOfBoundsException :: "+e.getMessage());
		}
		return document;
	}	

	public static  String getGuid(){
		UUID uuid = UUID.randomUUID();
		String randomUUIDString = uuid.toString();
		return randomUUIDString ;
	}

	public Document BulkPushDocument(File file,Session session,String path,String rootFol) 
	{

		Document document = null;
		String year = "",month = "",day = "",hour = "",rootFolder = "",minute="",seconds="",ms="";
		String parts[];
		try{
			if(path==""||path==null)
			{
				Date dNow = new Date();
				SimpleDateFormat Fomat_Folder = new SimpleDateFormat ("yyyy/MM/dd/HH/");
				String url_path=Fomat_Folder.format(dNow);	
				parts = url_path.split("/");
				rootFolder = rootFol;
				year = parts[0];
				month = parts[1];
				day= parts[2];
				hour= parts[3];
				path="/"+rootFolder+"/"+url_path;
			}
			else if(!path.equalsIgnoreCase(null)|| !path.equalsIgnoreCase(""))
			{
				parts = path.split("/");
				rootFolder = parts[1];
				year = parts[2];
				month = parts[3];
				day= parts[4];
				hour= parts[5];
			}

			FolderCreationUtils foldercreate=new FolderCreationUtils();
			foldercreate.CheckAndCreateTreeFolder(session,rootFolder, year, month, day, hour);
			//File file=new File("E:\\JP\\ALFRESCO\\cmis-article.pdf");
			String fileName = file.getName();
			Date date = new Date();
			SimpleDateFormat ft = new SimpleDateFormat ("yyyy_MM_dd_hh_mm_ss_SS");
			String UniqID= getGuid();
			CmisObject cmisObject = session.getObjectByPath(path);
			Folder parent = (Folder)cmisObject;
			Map props = new HashMap();
			if (props == null) 
			{
				props = new HashMap<String, Object>();
			}
			if (props.get("cmis:objectTypeId") == null) 
			{
				props.put("cmis:objectTypeId",  "cmis:document");
			}
			if (props.get("cmis:name") == null) 
			{
				props.put("cmis:name",UniqID+fileName);

			}
			ContentStream contentStream = session.getObjectFactory().
					createContentStream( fileName, file.length(),"application/octet-stream", new FileInputStream(file));

			try {

				document = parent.createDocument(props, contentStream, null);
				//System.out.println("Created new document: " + document.getVersionSeriesId());
				System.out.println("Created new document: " + document.getVersionSeriesId());
			} catch (CmisContentAlreadyExistsException ccaee) {

				props.put("cmis:name",fileName);
				ContentStream contentStream1 = session.getObjectFactory().
						createContentStream( fileName, file.length(),"application/octet-stream", new FileInputStream(file));
				document = parent.createDocument(props, contentStream1, null);
				//System.out.println("Created new document: " + document.getVersionSeriesId());
				System.out.println("Created new document: " + document.getVersionSeriesId());
			}
		}catch(CmisInvalidArgumentException e)
		{
			System.out.println(e.getLocalizedMessage());
		}catch(FileNotFoundException e)
		{
			System.out.println(e.getLocalizedMessage());
		}
		catch(CmisObjectNotFoundException e)
		{
			System.out.println(e.getLocalizedMessage());
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			System.out.println(e.fillInStackTrace()+" Path must be /rootFolder/year/month/day/hour/ in this format");
			System.out.println("ArrayIndexOutOfBoundsException :: "+e.getMessage());
		}
		return document;
	}	

	public Document PMSPushDocument(File file,String OrgFileName,String UniqID,Session session,String path,String rootFol) 
	{

		Document document = null;
		String year = "",month = "",day = "",hour = "",rootFolder = "",minute="",seconds="",ms="";
		String parts[];
		try{
			if(path==""||path==null)
			{
				Date dNow = new Date();
				SimpleDateFormat Fomat_Folder = new SimpleDateFormat ("yyyy/MM/dd/HH/");
				String url_path=Fomat_Folder.format(dNow);	
				parts = url_path.split("/");
				rootFolder = rootFol;
				year = parts[0];
				month = parts[1];
				day= parts[2];
				hour= parts[3];
				path="/"+rootFolder+"/"+url_path;
			}
			else if(!path.equalsIgnoreCase(null)|| !path.equalsIgnoreCase(""))
			{
				parts = path.split("/");
				rootFolder = parts[1];
				year = parts[2];
				month = parts[3];
				day= parts[4];
				hour= parts[5];
			}

			FolderCreationUtils foldercreate=new FolderCreationUtils();
			foldercreate.CheckAndCreateTreeFolder(session,rootFolder, year, month, day, hour);
			//File file=new File("E:\\JP\\ALFRESCO\\cmis-article.pdf");
			//String fileName = file.getName();
			long fileLen=file.length();
			Date date = new Date();
			SimpleDateFormat ft = new SimpleDateFormat ("yyyy_MM_dd_hh_mm_ss_SS");
			//String UniqID= getGuid();
			CmisObject cmisObject = session.getObjectByPath(path);
			Folder parent = (Folder)cmisObject;
			Map props = new HashMap();
			if (props == null) 
			{
				props = new HashMap<String, Object>();
			}
			if (props.get("cmis:objectTypeId") == null) 
			{
				props.put("cmis:objectTypeId",  "cmis:document");
			}
			if (props.get("cmis:name") == null) 
			{
				props.put("cmis:name",UniqID+"_"+OrgFileName);

			}
			ContentStream contentStream = session.getObjectFactory().
					createContentStream( OrgFileName, fileLen,"application/octet-stream",new FileInputStream(file));

			try {

				document = parent.createDocument(props, contentStream, null);
				//System.out.println("Created new document: " + document.getVersionSeriesId());
				System.out.println("Created new document: " + document.getVersionSeriesId());
			} catch (CmisContentAlreadyExistsException ccaee) {

				props.put("cmis:name",OrgFileName);
				ContentStream contentStream1 = session.getObjectFactory().
						createContentStream( OrgFileName, fileLen,"application/octet-stream", new FileInputStream(file));
				document = parent.createDocument(props, contentStream1, null);
				//System.out.println("Created new document: " + document.getVersionSeriesId());
				System.out.println("Created new document: " + document.getVersionSeriesId());
			}
		}catch(CmisInvalidArgumentException e)
		{
			System.out.println(e.getLocalizedMessage());
		}catch(CmisObjectNotFoundException e)
		{
			System.out.println(e.getLocalizedMessage());
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			System.out.println(e.fillInStackTrace()+" Path must be /rootFolder/year/month/day/hour/ in this format");
			System.out.println("ArrayIndexOutOfBoundsException :: "+e.getMessage());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return document;
	}	


	public Document PMSPushDocument(InputStream inputstream,String OrgFileName,String UniqID,Session session,String path,String rootFol) 
	{

		Document document = null;
		String year = "",month = "",day = "",hour = "",rootFolder = "",minute="",seconds="",ms="";
		String parts[];
		String str = inputstream.toString();
		byte[] b3 = str.getBytes();
		long inputStreamLen=b3.length;
		try{
			if(path==""||path==null)
			{
				Date dNow = new Date();
				SimpleDateFormat Fomat_Folder = new SimpleDateFormat ("yyyy/MM/dd/HH/");
				String url_path=Fomat_Folder.format(dNow);	
				parts = url_path.split("/");
				rootFolder = rootFol;
				year = parts[0];
				month = parts[1];
				day= parts[2];
				hour= parts[3];
				path="/"+rootFolder+"/"+url_path;
			}
			else if(!path.equalsIgnoreCase(null)|| !path.equalsIgnoreCase(""))
			{
				parts = path.split("/");
				rootFolder = parts[1];
				year = parts[2];
				month = parts[3];
				day= parts[4];
				hour= parts[5];
			}

			FolderCreationUtils foldercreate=new FolderCreationUtils();
			foldercreate.CheckAndCreateTreeFolder(session,rootFolder, year, month, day, hour);
			//File file=new File("E:\\JP\\ALFRESCO\\cmis-article.pdf");
			//String fileName = file.getName();

			Date date = new Date();
			SimpleDateFormat ft = new SimpleDateFormat ("yyyy_MM_dd_hh_mm_ss_SS");
			//String UniqID= getGuid();
			CmisObject cmisObject = session.getObjectByPath(path);
			Folder parent = (Folder)cmisObject;
			Map props = new HashMap();
			if (props == null) 
			{
				props = new HashMap<String, Object>();
			}
			if (props.get("cmis:objectTypeId") == null) 
			{
				props.put("cmis:objectTypeId",  "cmis:document");
			}
			if (props.get("cmis:name") == null) 
			{
				props.put("cmis:name",UniqID+"_"+OrgFileName);

			}
			ContentStream contentStream = session.getObjectFactory().
					createContentStream( OrgFileName, inputStreamLen,"application/octet-stream",inputstream);

			try {

				document = parent.createDocument(props, contentStream, null);
				//System.out.println("Created new document: " + document.getVersionSeriesId());
				System.out.println("Created new document: " + document.getVersionSeriesId());
			} catch (CmisContentAlreadyExistsException ccaee) {

				props.put("cmis:name",OrgFileName);
				ContentStream contentStream1 = session.getObjectFactory().
						createContentStream( OrgFileName, inputStreamLen,"application/octet-stream", inputstream);
				document = parent.createDocument(props, contentStream1, null);
				//System.out.println("Created new document: " + document.getVersionSeriesId());
				System.out.println("Created new document: " + document.getVersionSeriesId());
			}
		}catch(CmisInvalidArgumentException e)
		{
			System.out.println(e.getLocalizedMessage());
		}catch(CmisObjectNotFoundException e)
		{
			System.out.println(e.getLocalizedMessage());
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			System.out.println(e.fillInStackTrace()+" Path must be /rootFolder/year/month/day/hour/ in this format");
			System.out.println("ArrayIndexOutOfBoundsException :: "+e.getMessage());
		}
		return document;
	}	




	public Document PushDocument(InputStream file,String fileName,long filelength,Session session,String path,String rootFol) 
	{

		Document document = null;
		String year = "",month = "",day = "",hour = "",rootFolder = "";
		String parts[];
		try{
			if(path==""||path==null)
			{
				Date dNow = new Date();
				SimpleDateFormat Fomat_Folder = new SimpleDateFormat ("yyyy/MM/dd/HH/");
				String url_path=Fomat_Folder.format(dNow);	
				parts = url_path.split("/");
				rootFolder = rootFol;
				year = parts[0];
				month = parts[1];
				day= parts[2];
				hour= parts[3];
				path="/"+rootFolder+"/"+url_path;
			}
			else if(!path.equalsIgnoreCase(null)|| !path.equalsIgnoreCase(""))
			{
				parts = path.split("/");
				rootFolder = parts[1];
				year = parts[2];
				month = parts[3];
				day= parts[4];
				hour= parts[5];
			}

			FolderCreationUtils foldercreate=new FolderCreationUtils();
			foldercreate.CheckAndCreateTreeFolder(session,rootFolder, year, month, day, hour);
			//File file=new File("E:\\JP\\ALFRESCO\\cmis-article.pdf");
			//String fileName = fileName;
			Date date = new Date();
			SimpleDateFormat ft = new SimpleDateFormat ("yyyyMMddhhmmss");
			//String UniqID= ft.format(date);
			CmisObject cmisObject = session.getObjectByPath(path);
			Folder parent = (Folder)cmisObject;
			Map props = new HashMap();
			if (props == null) 
			{
				props = new HashMap<String, Object>();
			}
			if (props.get("cmis:objectTypeId") == null) 
			{
				props.put("cmis:objectTypeId",  "cmis:document");
			}
			if (props.get("cmis:name") == null) 
			{
				props.put("cmis:name",fileName);

			}
			ContentStream contentStream = session.getObjectFactory().
					createContentStream( fileName, filelength,"application/octet-stream", file);

			try {

				document = parent.createDocument(props, contentStream, null);
				//System.out.println("Created new document: " + document.getVersionSeriesId());
				System.out.println("Created new document: " + document.getVersionSeriesId());
			} catch (CmisContentAlreadyExistsException ccaee) {

				props.put("cmis:name",fileName);
				ContentStream contentStream1 = session.getObjectFactory().
						createContentStream( fileName, filelength,"application/octet-stream", file);
				document = parent.createDocument(props, contentStream1, null);
				//System.out.println("Created new document: " + document.getVersionSeriesId());
				System.out.println("Created new document: " + document.getVersionSeriesId());
			}
		}catch(CmisInvalidArgumentException e)
		{
			System.out.println(e.getLocalizedMessage());
		}
		catch(CmisObjectNotFoundException e)
		{
			System.out.println(e.getLocalizedMessage());
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			System.out.println(e.fillInStackTrace()+" Path must be /rootFolder/year/month/day/hour/ in this format");
			System.out.println("ArrayIndexOutOfBoundsException :: "+e.getMessage());
		}
		return document;
	}	
	public String getTicket(String host,String user,String password,String port) throws IOException
	{
		CmisUtils util=new CmisUtils();
		//ThreadContext.put("filename", "AlfrescoLogs");
		String url="http://"+host+":"+port+"/alfresco/s/api/login?u="+user+"&pw="+password;
		int responseCode=0;
		URL obj;
		StringBuffer response = null;
		String responseString="";
		String ticket="";
		HttpURLConnection conn_Get = null;
		try {
			obj = new URL(url);
			conn_Get = (HttpURLConnection)obj.openConnection();
			conn_Get.setRequestMethod("GET");
			responseCode = conn_Get.getResponseCode();
			System.out.println(responseCode);

			if (responseCode == HttpURLConnection.HTTP_OK) {
				System.out.println("Http ok ");
				BufferedReader in = new BufferedReader(new InputStreamReader(conn_Get.getInputStream()));
				String inputLine;
				response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				responseString=response.toString();

				ticket=util.Parse(responseString);
				System.out.println("Ticket is:: "+ticket);
			} else {
				System.out.println("Ticket cannot able to retrieve");
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Exception in Getting Ticket"+e.getLocalizedMessage());
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Exception in Getting Ticket"+e.getLocalizedMessage());
		}
		return ticket;
	}


	public String getTicketByUrl(String alfUrl,String user,String password) throws IOException
	{
		CmisUtils util=new CmisUtils();
		//ThreadContext.put("filename", "AlfrescoLogs");
		String url=alfUrl+"/alfresco/s/api/login?u="+user+"&pw="+password;
		int responseCode=0;
		URL obj;
		StringBuffer response = null;
		String responseString="";
		String ticket="";
		HttpURLConnection conn_Get = null;
		try {
			obj = new URL(url);
			conn_Get = (HttpURLConnection)obj.openConnection();
			conn_Get.setRequestMethod("GET");
			responseCode = conn_Get.getResponseCode();
			System.out.println(responseCode);

			if (responseCode == HttpURLConnection.HTTP_OK) {
				System.out.println("Http ok ");
				BufferedReader in = new BufferedReader(new InputStreamReader(conn_Get.getInputStream()));
				String inputLine;
				response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				responseString=response.toString();

				ticket=util.Parse(responseString);
				System.out.println("Ticket is:: "+ticket);
			} else {
				System.out.println("Ticket cannot able to retrieve");
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Exception in Getting Ticket"+e.getLocalizedMessage());
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Exception in Getting Ticket"+e.getLocalizedMessage());
		}
		return ticket;
	}


	public String Parse(String xmlRecords) throws Exception
	{
		String id="";
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(xmlRecords));

		org.w3c.dom.Document doc = db.parse(is);
		NodeList nodes = doc.getElementsByTagName("ticket");
		Element line = (Element) nodes.item(0);
		id=getCharacterDataFromElement(line);

		return id;

	}

	public static String getCharacterDataFromElement(Element e) {
		Node child = e.getFirstChild();
		if (child instanceof CharacterData) {
			CharacterData cd = (CharacterData) child;
			return cd.getData();
		}
		return "";
	}


	public String viewDocument(Session session,String ID,String AlfIPAddress,String port,String user,String password)
	{
		//ThreadContext.put("filename", "AlfrescoLogs");
		String URLpath="";
		String docurl="";
		String ticket="";
		CmisUtils util=new CmisUtils();
		try{
			CmisObject cmis=session.getObject(ID);
			Document doc=(Document)cmis;
			docurl=doc.getPaths().get(0);
			//System.out.println("Path of this document:: "+doc.getPaths());
			ticket=util.getTicket(AlfIPAddress, user, password, port);
			URLpath="http://"+AlfIPAddress+":"+port+"/alfresco/webdav"+docurl+"?"+"ticket="+ticket;
			//System.out.println("You can view this document by this URL "+URLpath);
		}catch(CmisInvalidArgumentException e)
		{
			System.out.println(e.getLocalizedMessage());
		}
		catch(CmisObjectNotFoundException e)
		{
			System.out.println(e.getLocalizedMessage());
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return URLpath;
	}

	public String viewDocumentByPubUrl(Session session,String ID,String alfUrl,String user,String password)
	{
		//ThreadContext.put("filename", "AlfrescoLogs");
		String URLpath="";
		String docurl="";
		String ticket="";
		CmisUtils util=new CmisUtils();
		try{
			CmisObject cmis=session.getObject(ID);
			Document doc=(Document)cmis;
			docurl=doc.getPaths().get(0);
			//System.out.println("Path of this document:: "+doc.getPaths());
			ticket=util.getTicketByUrl(alfUrl, user, password);
			URLpath=alfUrl+"/alfresco/webdav"+docurl+"?"+"ticket="+ticket;
			//System.out.println("You can view this document by this URL "+URLpath);
		}catch(CmisInvalidArgumentException e)
		{
			System.out.println(e.getLocalizedMessage());
		}
		catch(CmisObjectNotFoundException e)
		{
			System.out.println(e.getLocalizedMessage());
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return URLpath;
	}



	public String viewDocumentByPath(Session session,String path,String AlfIPAddress,String port,String user,String password)
	{
		//ThreadContext.put("filename", "AlfrescoLogs");
		String URLpath="";
		String docurl="";
		String ticket="";
		CmisUtils util=new CmisUtils();
		try{
			//CmisObject cmis=session.getObject(ID);
			CmisObject cmis=session.getObjectByPath(path);
			Document doc=(Document)cmis;
			docurl=doc.getPaths().get(0);
			//System.out.println("Path of this document:: "+doc.getPaths());
			ticket=util.getTicket(AlfIPAddress, user, password, port);
			URLpath="http://"+AlfIPAddress+":"+port+"/alfresco/webdav"+docurl+"?"+"ticket="+ticket;
			//System.out.println("You can view this document by this URL "+URLpath);
		}catch(CmisInvalidArgumentException e)
		{
			System.out.println(e.getLocalizedMessage());
		}
		catch(CmisObjectNotFoundException e)
		{
			System.out.println(e.getLocalizedMessage());
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return URLpath;
	}


	public String DownloadDocument(Session session,String ID,String AlfIPAddress,String port,String user,String password)
	{
		CmisUtils util=new CmisUtils();
		String downlink="";
		String docName="";
		try 
		{
			String ticket=util.getTicket(AlfIPAddress, user,password, port);
			Document doc=util.GetDocumentUsingID(session, ID);
			docName=doc.getName();
			downlink="http://"+AlfIPAddress+":"+port+"/alfresco/d/a/workspace/SpacesStore/"+ID+"/"+docName+"?ticket="+ticket;
			//downlink=downlink.replaceAll("alfresco/d/d", "alfresco/d/a");
			//http://192.168.5.180:8080/alfresco/download/attach/workspace/SpacesStore/8af4ac5b-d658-4228-9063-dfb569759324/20151229121257_5688377.png?ticket=TICKET_a5ed05c9e8b15255166284982792e48eff616359

		} catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return downlink;
	}


	public String DownloadDocumentByPubUrl(Session session,String ID,String alfUrl,String user,String password)
	{
		CmisUtils util=new CmisUtils();
		String downlink="";
		String docName="";
		try 
		{
			String ticket=util.getTicketByUrl(alfUrl, user, password);
			Document doc=util.GetDocumentUsingID(session, ID);
			docName=doc.getName();
			downlink=alfUrl+"/alfresco/d/a/workspace/SpacesStore/"+ID+"/"+docName+"?ticket="+ticket;
			//downlink=downlink.replaceAll("alfresco/d/d", "alfresco/d/a");
			//http://192.168.5.180:8080/alfresco/download/attach/workspace/SpacesStore/8af4ac5b-d658-4228-9063-dfb569759324/20151229121257_5688377.png?ticket=TICKET_a5ed05c9e8b15255166284982792e48eff616359

		} catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return downlink;
	}


	public Document GetDocumentUsingID(Session session,String id)
	{
		//ThreadContext.put("filename", "AlfrescoLogs");
		Document document=null;
		try
		{
			CmisObject cmis=session.getObject(id);

			document=(Document) cmis;
			String name=document.getName();
			//System.out.println(name+" is a Document!!");
			System.out.println(name+" is a Document!!");
		}catch(CmisInvalidArgumentException e)
		{
			System.out.println(e.getLocalizedMessage());
		}
		catch(CmisObjectNotFoundException e)
		{
			System.out.println(e.getLocalizedMessage());
		}
		return document;
	}

	public Document GetDocumentUsingPath(Session session,String path)
	{
		//ThreadContext.put("filename", "AlfrescoLogs");
		Document document=null;
		try
		{
			CmisObject cmis=session.getObjectByPath(path);

			document=(Document) cmis;
			String name=document.getName();
			//System.out.println(name+" is a Document!!");
			System.out.println(name+" is a Document!!");
		}catch(CmisInvalidArgumentException e)
		{
			System.out.println(e.getLocalizedMessage());
		}
		catch(CmisObjectNotFoundException e)
		{
			System.out.println(e.getLocalizedMessage());
		}
		return document;
	}

	public String DeleteDocumentUsingId(Session session,String id)
	{

		String name="";
		String result="";
		try
		{
			CmisObject cmis=session.getObject(id);
			Document doc=(Document) cmis;
			doc.delete(true);
			name=doc.getName();
			//System.out.println("Document "+ name+" deleted Successfully!!");
			System.out.println("Document "+ name+" deleted Successfully!!");
			result="Document deleted successfully!!";
		}catch(CmisInvalidArgumentException ciae)
		{
			System.out.println("Error::: "+ciae.getLocalizedMessage());
			result="Error::: "+ciae.getLocalizedMessage();
		}
		catch(CmisObjectNotFoundException ce)
		{
			System.out.println(ce.getLocalizedMessage());
		}
		return result;
	}

	public String[] ListItem(Session session,String Path)
	{
		int maxItemsPerPage = 10;
		int skipCount = 0;
		String list[] = new String[40];

		CmisObject object = session.getObjectByPath(Path);
		Folder folder = (Folder) object;
		System.out.println("Folder Name::"+folder.getName());
		OperationContext operationContext = session.createOperationContext();
		operationContext.setMaxItemsPerPage(maxItemsPerPage);

		ItemIterable<CmisObject> children = folder.getChildren(operationContext);
		ItemIterable<CmisObject> page = children.skipTo(skipCount).getPage();
		int i=0;
		Iterator<CmisObject> pageItems = page.iterator();
		//System.out.println(pageItems.toString());
		while(pageItems.hasNext()) {
			CmisObject item = pageItems.next();
			System.out.println("Item Props:: "+item.getType().getLocalName());
			list[i]=item.getName();
			System.out.println("Content Name:: "+item.getName());
			// Do something with the item.
			i++;
		}
		System.out.println("Total Number of Contents:: "+i);
		return list;

	}


	public Document EMSPushDocument(File file,Session session,String path,String rootFol) 
	{

		Document document = null;
		String year = "",month = "",day = "",hour = "",rootFolder = "",minute="",seconds="",ms="";
		String parts[];
		try{
			if(path==""||path==null)
			{
				Date dNow = new Date();
				SimpleDateFormat Fomat_Folder = new SimpleDateFormat ("yyyy/MM/dd/HH/");
				String url_path=Fomat_Folder.format(dNow);	
				parts = url_path.split("/");
				rootFolder = rootFol;
				year = parts[0];
				month = parts[1];
				day= parts[2];
				hour= parts[3];
				path="/"+rootFolder+"/"+url_path;
			}
			else if(!path.equalsIgnoreCase(null)|| !path.equalsIgnoreCase(""))
			{
				parts = path.split("/");
				rootFolder = parts[1];
				year = parts[2];
				month = parts[3];
				day= parts[4];
				hour= parts[5];
			}

			FolderCreationUtils foldercreate=new FolderCreationUtils();
			foldercreate.CheckAndCreateTreeFolder(session,rootFolder, year, month, day, hour);
			//File file=new File("E:\\JP\\ALFRESCO\\cmis-article.pdf");
			String fileName = file.getName().replaceAll("[^a-zA-Z0-9.-]", "_");
			Date date = new Date();
			SimpleDateFormat ft = new SimpleDateFormat ("yyyy_MM_dd_hh_mm_ss_SS");
			String UniqID= getGuid();
			CmisObject cmisObject = session.getObjectByPath(path);
			Folder parent = (Folder)cmisObject;
			Map props = new HashMap();
			if (props == null) 
			{
				props = new HashMap<String, Object>();
			}
			if (props.get("cmis:objectTypeId") == null) 
			{
				props.put("cmis:objectTypeId",  "cmis:document");
			}
			if (props.get("cmis:name") == null) 
			{
				props.put("cmis:name",UniqID+"_"+fileName);

			}
			ContentStream contentStream = session.getObjectFactory().
					createContentStream( UniqID+"_"+fileName, file.length(),"application/octet-stream", new FileInputStream(file));

			try {

				document = parent.createDocument(props, contentStream, null);
				//System.out.println("Created new document: " + document.getVersionSeriesId());
				System.out.println("Created new document: " + document.getVersionSeriesId());
			} catch (CmisContentAlreadyExistsException ccaee) {

				props.put("cmis:name",UniqID+"_"+fileName);
				ContentStream contentStream1 = session.getObjectFactory().
						createContentStream( UniqID+"_"+fileName, file.length(),"application/octet-stream", new FileInputStream(file));
				document = parent.createDocument(props, contentStream1, null);
				//System.out.println("Created new document: " + document.getVersionSeriesId());
				System.out.println("Created new document: " + document.getVersionSeriesId());
			}
		}catch(CmisInvalidArgumentException e)
		{
			System.out.println(e.getLocalizedMessage());
		}catch(FileNotFoundException e)
		{
			System.out.println(e.getLocalizedMessage());
		}
		catch(CmisObjectNotFoundException e)
		{
			System.out.println(e.getLocalizedMessage());
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			System.out.println(e.fillInStackTrace()+" Path must be /rootFolder/year/month/day/hour/ in this format");
			System.out.println("ArrayIndexOutOfBoundsException :: "+e.getMessage());
		}
		return document;
	}	

	public Document EMSPushDocument(InputStream inputstream,String OrgFileName,Session session,String path,String rootFol) 
	{

		OrgFileName=OrgFileName.replaceAll("[^a-zA-Z0-9.-]", "_");
		Document document = null;
		String year = "",month = "",day = "",hour = "",rootFolder = "",minute="",seconds="",ms="";
		String parts[];
		String str = inputstream.toString();
		byte[] b3 = str.getBytes();
		long inputStreamLen=b3.length;
		try{
			String UniqID= getGuid();
			if(path==""||path==null)
			{
				Date dNow = new Date();
				SimpleDateFormat Fomat_Folder = new SimpleDateFormat ("yyyy/MM/dd/HH/");
				String url_path=Fomat_Folder.format(dNow);	
				parts = url_path.split("/");
				rootFolder = rootFol;
				year = parts[0];
				month = parts[1];
				day= parts[2];
				hour= parts[3];
				path="/"+rootFolder+"/"+url_path;
			}
			else if(!path.equalsIgnoreCase(null)|| !path.equalsIgnoreCase(""))
			{
				parts = path.split("/");
				rootFolder = parts[1];
				year = parts[2];
				month = parts[3];
				day= parts[4];
				hour= parts[5];
			}

			FolderCreationUtils foldercreate=new FolderCreationUtils();
			foldercreate.CheckAndCreateTreeFolder(session,rootFolder, year, month, day, hour);
			//File file=new File("E:\\JP\\ALFRESCO\\cmis-article.pdf");
			//String fileName = file.getName();

			Date date = new Date();
			SimpleDateFormat ft = new SimpleDateFormat ("yyyy_MM_dd_hh_mm_ss_SS");
			//String UniqID= getGuid();
			CmisObject cmisObject = session.getObjectByPath(path);
			Folder parent = (Folder)cmisObject;
			Map props = new HashMap();
			if (props == null) 
			{
				props = new HashMap<String, Object>();
			}
			if (props.get("cmis:objectTypeId") == null) 
			{
				props.put("cmis:objectTypeId",  "cmis:document");
			}
			if (props.get("cmis:name") == null) 
			{
				props.put("cmis:name",UniqID+"_"+OrgFileName);

			}
			ContentStream contentStream = session.getObjectFactory().
					createContentStream( UniqID+"_"+OrgFileName, inputStreamLen,"application/octet-stream",inputstream);

			try {

				document = parent.createDocument(props, contentStream, null);
				//System.out.println("Created new document: " + document.getVersionSeriesId());
				System.out.println("Created new document: " + document.getVersionSeriesId());
			} catch (CmisContentAlreadyExistsException ccaee) {

				props.put("cmis:name",UniqID+"_"+OrgFileName);
				ContentStream contentStream1 = session.getObjectFactory().
						createContentStream( UniqID+"_"+OrgFileName, inputStreamLen,"application/octet-stream", inputstream);
				document = parent.createDocument(props, contentStream1, null);
				//System.out.println("Created new document: " + document.getVersionSeriesId());
				System.out.println("Created new document: " + document.getVersionSeriesId());
			}
		}catch(CmisInvalidArgumentException e)
		{
			System.out.println(e.getLocalizedMessage());
		}catch(CmisObjectNotFoundException e)
		{
			System.out.println(e.getLocalizedMessage());
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			System.out.println(e.fillInStackTrace()+" Path must be /rootFolder/year/month/day/hour/ in this format");
			System.out.println("ArrayIndexOutOfBoundsException :: "+e.getMessage());
		}
		return document;
	}	


	public static void main(String args[]) throws FileNotFoundException
	{
		CmisUtils cs=new CmisUtils();
		Session session=cs.CMISSession("http://192.168.4.54:8080/alfresco/api/-default-/public/cmis/versions/1.0/atom","admin","admin123");
		//Session session=cs.CMISSession("http://192.168.2.221:8080/alfresco/api/-default-/public/cmis/versions/1.1/atom","admin","admin123");
		//Session session=cs.CMISSession("http://192.168.4.54:8080/alfresco/cmisatom","admin","efaxadmin");
		File file=new File("D:\\JP\\PDF\\ER.pdf");
		InputStream instr=new FileInputStream(file);
		Document doc=cs.EMSPushDocument(instr, "ER",session, "", "EMS");
		System.out.println(doc.getName());
		System.out.println(""+doc.getPaths().get(0));
		//http://192.168.1.225:8080/alfresco/api/-default-/public/cmis/versions/1.1/atom
		//System.out.println(session.getRootFolder().getName());
		//http://192.168.1.225:8080/alfresco/api/-default-/public/cmis/versions/1.1/atom
		/*File file=new File("E:/test2.jpg");
		Document doc=cs.PushDocument(file, session, "", "STARFAX");
		System.out.println(doc.getName());*/
		//String view=cs.ViewDocument(session, "0265ea44-7ed8-4f01-b3cb-3205235fc7ed", "192.168.4.54", "8080", "admin", "admin321");
		//System.out.println("view ::"+view);
		//String down=cs.DownloadDocument(session, "0265ea44-7ed8-4f01-b3cb-3205235fc7ed",  "192.168.4.54", "8080", "admin", "admin321");
		//System.out.println("down :: "+down);
		FolderCreationUtils folutil=new FolderCreationUtils();
		//folutil.CheckAndCreateTreeFolder(session, "STARFAX", "2015", "12", "01","01");
		/*Document doc=cs.GetDocumentUsingID(session, "0265ea44-7ed8-4f01-b3cb-3205235fc7ed");
		List<Property<?>> props = doc.getProperties();
		Property<?> someProperty = props.get(0);
		//System.out.println(someProperty);
		if (((DocumentType)(doc.getType())).isVersionable()) {
			System.out.println(doc.getName() + " is versionable");
			System.out.println(doc.getVersionLabel() + "Version");
			System.out.println(doc.getPaths() + " PATH");
		} else {
			System.out.println(doc.getName() + " is NOT versionable");
		}*/

		//String[] res= cs.ListItem(session, "/STARFAX/2015/12/31");
		//String[] res= cs.ListItem(session, "/Galaxy/2016/01/5/15");
		//String[] res= cs.ListItem(session, "/Galaxy/2016/01/4");
		//System.out.println("list[i] : is " +res);
		/*System.out.println(someProperty.getDisplayName() + " property on " + doc.getName()
		    + " (by getPropertValue()) is "
		    + doc.getPropertyValue(someProperty.getQueryName()));
		 */
	}
}