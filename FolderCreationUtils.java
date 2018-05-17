package com.bct.dms.util;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

public class FolderCreationUtils {
	private static final Logger logger = (Logger) LogManager.getLogger(FolderCreationUtils.class.getName());

	public boolean CheckAndCreateYearFolder(String rootFolder,String year,Session session)
	{
		ThreadContext.put("filename", "AlfrescoLogs");
		boolean result=false;
		Folder subFolder=null;
		
		try{
			subFolder = (Folder) session.getObjectByPath("/"+rootFolder+"/"+year);
			System.out.println("Already Folder existed!!!");
			logger.info("Already Folder existed!!!");
			result=true;
		}catch(CmisObjectNotFoundException confe)
		{
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
			properties.put(PropertyIds.NAME, year);
			Folder root=(Folder) session.getObjectByPath("/"+rootFolder+"/");
			Folder newFolder = root.createFolder(properties);
			System.out.println(newFolder.getName()+" Folder created successfully");
			result=true;
		}
		catch(CmisConstraintException cce)
		{
			System.out.println(""+cce.getLocalizedMessage());
		}
		return result;

	}

	public boolean CheckAndCreateMonthFolder(String rootFolder,String Year,String month,Session session)
	{
		
		boolean result=false;
		Folder subFolder=null;
		try{
			subFolder = (Folder) session.getObjectByPath("/"+rootFolder+"/"+Year+"/"+month);
			System.out.println("Folder existed!!!");
			result=true;
		}catch(CmisObjectNotFoundException confe)
		{
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
			properties.put(PropertyIds.NAME, month);
			Folder root=(Folder) session.getObjectByPath("/"+rootFolder+"/"+Year+"/");
			Folder newFolder = root.createFolder(properties);
			System.out.println(newFolder.getName()+" Folder created successfully");
			result=true;
		}
		catch(CmisConstraintException cce)
		{
			System.out.println(""+cce.getErrorContent());
		}

		return result;
	}

	public boolean CheckAndCreateDayFolder(String rootFolder,String Year,String month,String day,Session session)
	{
	
		boolean result=false;
		Folder subFolder=null;
		try{
			subFolder = (Folder) session.getObjectByPath("/"+rootFolder+"/"+Year+"/"+month+"/"+day+"/");
			System.out.println("Folder existed!!!");
			result=true;
		}catch(CmisObjectNotFoundException confe)
		{
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
			properties.put(PropertyIds.NAME, day);
			Folder root=(Folder) session.getObjectByPath("/"+rootFolder+"/"+Year+"/"+month+"/");
			Folder newFolder = root.createFolder(properties);
			System.out.println(newFolder.getName()+" Folder created successfully");
			//logger.info(newFolder.getName()+" Folder created successfully"+"/"+rootFolder+"/"+Year+"/"+month+"/");
			result=true;
		}catch(CmisConstraintException cce)
		{
			System.out.println(""+cce.getErrorContent());
		}
		return result;

	}

	public boolean CheckAndCreateHourFolder(String rootFolder,String Year,String month,String day,String hour,Session session)
	{
		
		boolean result=false;
		Folder subFolder=null;
		result=true;
		try{
			subFolder = (Folder) session.getObjectByPath("/"+rootFolder+"/"+Year+"/"+month+"/"+day+"/"+hour+"/");
			//System.out.println("Folder existed!!!");
			System.out.println("Folder existed!!! Path is "+"/"+rootFolder+"/"+Year+"/"+month+"/"+day+"/"+hour+"/");
		}catch(CmisObjectNotFoundException confe)
		{
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
			properties.put(PropertyIds.NAME, hour);
			Folder root=(Folder) session.getObjectByPath("/"+rootFolder+"/"+Year+"/"+month+"/"+day+"/");
			Folder newFolder = root.createFolder(properties);
			//System.out.println(newFolder.getName()+" Folder created successfully");
			System.out.println(newFolder.getName()+" Folder created successfully.Path is "+"/"+rootFolder+"/"+Year+"/"+month+"/"+day+"/");
			result=true;
		}catch(CmisConstraintException cce)
		{
			System.out.println(""+cce.getErrorContent());
		}
		return result;

	}

	public String CheckAndCreateTreeFolder(Session session,String rootFolder,String year,String month,String day,String hour)
	{
		
		String result = "";
		String searchForThis = "/";
		try{
		boolean year_res,mon_res,day_res,hr_res;
		year_res=mon_res=day_res=hr_res=false;
		if(rootFolder.contains(searchForThis)==false && year.contains(searchForThis)==false && month.contains(searchForThis)==false && day.contains(searchForThis)==false && hour.contains(searchForThis)==false)
		{
			
				FolderCreationUtils FolderUtil=new FolderCreationUtils();
				year_res=FolderUtil.CheckAndCreateYearFolder(rootFolder,year,session);;
				mon_res=FolderUtil.CheckAndCreateMonthFolder(rootFolder,year,month,session);
				day_res=FolderUtil.CheckAndCreateDayFolder(rootFolder,year,month,day,session);
				hr_res=FolderUtil.CheckAndCreateHourFolder(rootFolder,year,month,day,hour,session);
			if(year_res=true && mon_res==true && day_res==true && hr_res==true )
			{
				result="Folders creation or checking successfully\n"+"Folder structure is "+"/"+rootFolder+"/"+year+"/"+month+"/"+day+"/"+hour+"/";
				System.out.println("Tree Folder Structure has been created successfully!"+"Folder structure is "+"/"+rootFolder+"/"+year+"/"+month+"/"+day+"/"+hour+"/");
			}
			else
			{
				result="Error in folder creation";
				System.out.println("Error in Tree folder creation");
			}
		}

		else
		{
			result="Folder name should not contain Special characters ";
			System.out.println("Folder name should not contain Special characters" );
		}
		}catch(CmisConstraintException cce)
		{
			result="Error in folder creation";
			System.out.println(""+cce.getErrorContent());
		}
		return result;


	}

}