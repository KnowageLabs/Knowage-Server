/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.tools.massiveExport.work;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.DocumentMetadataProperty;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.MetadataJSONSerializer;
import it.eng.spagobi.commons.utilities.ExecutionProxy;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.massiveExport.dao.IProgressThreadDAO;
import it.eng.spagobi.tools.massiveExport.utils.Utilities;
import it.eng.spagobi.tools.objmetadata.bo.ObjMetacontent;
import it.eng.spagobi.tools.objmetadata.bo.ObjMetadata;
import it.eng.spagobi.tools.objmetadata.dao.IObjMetacontentDAO;
import it.eng.spagobi.tools.objmetadata.dao.IObjMetadataDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONFailure;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

import commonj.work.Work;

/** 
 * Thread of massive export; cycle on documetns to be exported calling engine for export
 * , meanwhile keeps updated the record of the export, finally create the zip and store it in temporary table
 * 
 * @author gavardi
 *
 */

public class MassiveExportWork implements Work {

	private static transient Logger logger = Logger.getLogger(MassiveExportWork.class);

	public static final String PREPARED = "PREPARED";
	public static final String STARTED = "STARTED";
	public static final String DOWNLOAD = "DOWNLOAD";
	public static final String ERROR = "ERROR";
	
	public static final String OUTPUT_XLS = "application/vnd.ms-excel";
	public static final String OUTPUT_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	
	

	IEngUserProfile userProfile;
	List<BIObject> documents;
	LowFunctionality functionality;

	Integer progressThreadId;
	// key identifing the file
	String zipKey;

	List<File> filesToZip = null;

	boolean splittingFilter = false;
	String outputMIMEType;

	static byte[] buf = new byte[1024]; 

	private boolean completeWithoutError = false;
	IProgressThreadDAO progressThreadDAO;

	public MassiveExportWork(List<BIObject> documents, IEngUserProfile userProfile, LowFunctionality functionality, 
			Integer progressThreadId, String zipKey, boolean splittingFilter, String outputMIMEType) {
		super();
		this.documents = documents;
		this.userProfile = userProfile;
		this.functionality = functionality;
		this.progressThreadId = progressThreadId;
		this.zipKey = zipKey;
		this.splittingFilter = splittingFilter;
		this.outputMIMEType = outputMIMEType;
	}
	
	public void run() {
		try {
			this.setTenant();
			this.runInternal();
		} finally {
			TenantManager.unset();
		}
	}

	private void setTenant() {
		logger.debug("IN");
		UserProfile profile = (UserProfile) this.getProfile();
		String tenant = profile.getOrganization();
		LogMF.debug(logger, "Tenant : [{0}]", tenant);
		TenantManager.setTenant(new Tenant(tenant));
		logger.debug("OUT");
	}
	
	private void runInternal() {
		logger.debug("IN");

		progressThreadDAO = null;
		IObjMetadataDAO metaDAO = null;
		IObjMetacontentDAO contentDAO = null;

		String output = null;
		Thread thread = Thread.currentThread();
		Long threadId = thread.getId();

		logger.debug("Started thread Id "+threadId+" from user id: "+userProfile.getUserUniqueIdentifier());

		Integer totalDocs = documents.size();
		logger.debug("# of documents: "+totalDocs);


		try {
			progressThreadDAO = DAOFactory.getProgressThreadDAO();
			progressThreadDAO.setStartedProgressThread(progressThreadId);

			metaDAO = DAOFactory.getObjMetadataDAO();
			contentDAO = DAOFactory.getObjMetacontentDAO();


		} catch (Exception e) {
			logger.error("Error setting DAO");
			deleteDBRowInCaseOfError(progressThreadDAO, progressThreadId);
			throw new SpagoBIServiceException("Error setting DAO", e);
		}


		String fileExtension = null;
		if(outputMIMEType.equals(OUTPUT_XLS)){
			fileExtension = ".xls";
		}
		else if(outputMIMEType.equals(OUTPUT_XLSX)){
			fileExtension = ".xlsx";
		}
		else{
			logger.debug("output type nopt found, put default .xls");
			fileExtension = ".xls";	
		}
		logger.debug("Export File extension: "+fileExtension);
		
		filesToZip = new ArrayList<File>();

		// map used to recover real name to put inside zip
		Map<String, String> randomNamesToName = new HashMap<String, String>();

		for (BIObject document : documents) {
			
			File exportFile = null;

			ExecutionProxy proxy = new ExecutionProxy();
			byte[] returnByteArray = null;

			try{
				// get Obj Metadata
				List<DocumentMetadataProperty> listObjMetaContent = getMetaDataAndContent(metaDAO, contentDAO, document);
				document.setObjMetaDataAndContents(listObjMetaContent);

				proxy.setBiObject(document);
				proxy.setSplittingFilter(splittingFilter);
				proxy.setMimeType(outputMIMEType);
				UserProfile scheduler = UserProfile.createSchedulerUserProfile();
				returnByteArray = proxy.exec(userProfile, SpagoBIConstants.MASSIVE_EXPORT_MODALITY, output);
			}
			catch (Throwable e) {
				logger.error("Error while executing export for object with label "+document.getLabel(), e);
				returnByteArray = null;
			}

			try{
				if(returnByteArray == null){
					logger.error("execution proxy returned null document for BiObjectDocumetn: "+document.getLabel());
					exportFile = createErrorFile(document, null , randomNamesToName);
					// update progress table
					progressThreadDAO.incrementProgressThread(progressThreadId);
					logger.debug("progress Id incremented");
				}
				else{
					
					String cleanLabel = cleanFileName(document.getLabel());
					String cleanName = cleanFileName(document.getName());
					
					String checkerror = new String(returnByteArray);
					if(checkerror.startsWith("error") || checkerror.startsWith("{\"errors\":")){
						logger.error("Error found in execution, make txt file");
						String fileName = "Error "+cleanLabel+"-"+cleanName;
						exportFile = File.createTempFile(fileName, ".txt");
						randomNamesToName.put(exportFile.getName(), fileName+".txt");
					}
					else{
						logger.debug("Export ok for biObj with label "+document.getLabel());
						String fileName = cleanLabel+"-"+cleanName;
						exportFile = File.createTempFile(fileName, fileExtension); 
						randomNamesToName.put(exportFile.getName(), fileName+fileExtension);
					}

					FileOutputStream stream = new FileOutputStream(exportFile);
					stream.write(returnByteArray);

					logger.debug("create an export file named "+exportFile.getName());

					filesToZip.add(exportFile);

					// update progress table
					progressThreadDAO.incrementProgressThread(progressThreadId);
					logger.debug("progress Id incremented");

				}
			} catch (Exception e) {
				logger.error("Exception in  writeing export file for BiObject with label: "+document.getLabel()+": delete DB row",e);
				deleteDBRowInCaseOfError(progressThreadDAO, progressThreadId);
				throw new SpagoBIServiceException("Exception in  writeing export file for BiObject with label "+document.getLabel()+" delete DB row", e);
			}

		} // close For
		File zipFile = null;
		try{
			zipFile = createZipFile(filesToZip, randomNamesToName);
			logger.debug("zip created");
		}
		catch (Exception e) {
			logger.error("Error in writeing the zip file: DB row will be deleted to avoid cycling problems");
			deleteDBRowInCaseOfError(progressThreadDAO, progressThreadId);
			throw new SpagoBIServiceException("Error in writeing the zip file; DB row will be deleted to avoid cycling problems", e);
		}

		try{

			progressThreadDAO.setDownloadProgressThread(progressThreadId);
			logger.debug("Thread row in database set as download state");
		}
		catch (EMFUserError e) {
			logger.error("Error in closing database row relative to thread "+progressThreadId+" row will be deleted");
			deleteDBRowInCaseOfError(progressThreadDAO, progressThreadId);
			throw new SpagoBIServiceException("Error in closing database row relative to thread "+progressThreadId+" row will be deleted", e);
		}


		logger.debug("OUT");
	}

	/**
	 *  Zip file placed under resource_directory/massiveExport/functionalityCd
	 * @param filesToZip
	 * @param randomNamesToName
	 * @return
	 * @throws ZipException
	 * @throws IOException
	 */

	public File createZipFile(List<File> filesToZip, Map<String, String> randomNamesToName) throws ZipException, IOException{
		logger.debug("IN");
		File zipFile = Utilities.createMassiveExportZip(functionality.getCode(), zipKey);
		logger.debug("zip file written "+zipFile.getAbsolutePath());
		ZipOutputStream out = null;
		FileInputStream in = null;
		try{
			out = new ZipOutputStream(new FileOutputStream(zipFile)); 
			for (Iterator iterator = filesToZip.iterator(); iterator.hasNext();) {
				File file = (File) iterator.next();
				in = new FileInputStream(file); 
				String fileName = file.getName();
				String realName = randomNamesToName.get(fileName);
				ZipEntry zipEntry=new ZipEntry(realName);
				out.putNextEntry(zipEntry);

				int len; 
				while ((len = in.read(buf)) > 0) 
				{ 
					out.write(buf, 0, len); 
				} 

				out.closeEntry(); 
				in.close(); 
			}
			out.flush();
			out.close();
		}
		finally{
			if(in != null) in.close();
			if(out != null) out.close();
		}

		//filesToZip
		logger.debug("OUT");
		return zipFile;
	}


	private List<DocumentMetadataProperty> getMetaDataAndContent(IObjMetadataDAO metaDao, IObjMetacontentDAO metaContentDAO, BIObject obj) throws Exception{
		logger.debug("IN");
		List toReturn = null; 

		try{
			DocumentMetadataProperty objMetaDataAndContent = null;
			List<ObjMetadata> allMetas =metaDao.loadAllObjMetadata();
			Map<Integer, ObjMetacontent> values =  new HashMap<Integer, ObjMetacontent>();

			List list = metaContentDAO.loadObjOrSubObjMetacontents(obj.getId(), null);
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				ObjMetacontent content = (ObjMetacontent) iterator.next();
				Integer metaid = content.getObjmetaId();
				values.put(metaid, content);
			}

			for (Iterator iterator = allMetas.iterator(); iterator.hasNext();) {
				ObjMetadata meta = (ObjMetadata) iterator.next();
				objMetaDataAndContent = new DocumentMetadataProperty();
				objMetaDataAndContent.setMetadataPropertyDefinition(meta);
				objMetaDataAndContent.setMetadataPropertyValue(values.get(meta.getObjMetaId()));
				if(toReturn == null) toReturn = new ArrayList<DocumentMetadataProperty>();
				toReturn.add(objMetaDataAndContent);
			}

		}
		catch (Exception e) {
			logger.error("error in retrieving metadata and metacontent for biobj  id "+obj.getId(), e);	
			throw e;
		}
		logger.debug("OUT");
		return toReturn;
	}


	public File createErrorFile(BIObject biObj, Throwable error, Map randomNamesToName){
		logger.debug("IN");
		File toReturn = null;
		FileWriter fw = null;

		try{
			String fileName = "Error "+biObj.getLabel()+"-"+biObj.getName();
			toReturn = File.createTempFile(fileName, ".txt");
			randomNamesToName.put(toReturn.getName(), fileName+".txt");
			fw = new FileWriter(toReturn);
			fw.write("Error while executing biObject "+biObj.getLabel()+" - "+biObj.getName()+"\n");
			if(error != null){
				StackTraceElement[] errs = error.getStackTrace();
				for (int i = 0; i < errs.length; i++) {
					String err = errs[i].toString();
					fw.write(err+"\n");
				}
			}
			fw.flush();
		}
		catch (Exception e) {
			logger.error("Error in wirting error file for biObj "+biObj.getLabel());
			deleteDBRowInCaseOfError(progressThreadDAO, progressThreadId);
			throw new SpagoBIServiceException("Error in wirting error file for biObj "+biObj.getLabel(), e);			
		}
		finally{
			if(fw != null ) {
				try {
					fw.flush();
					fw.close();	
				} catch (IOException e) {}
			}
		}
		logger.debug("OUT");
		return toReturn;
	}



	public boolean isDaemon() {
		return false;
	}

	public void release() {
	}





	public List getBiObjects() {
		return documents;
	}





	public void setBiObjects(List biObjects) {
		this.documents = biObjects;
	}





	/**
	 * Checks if is complete without error.
	 * 
	 * @return true, if is complete without error
	 */
	public boolean isCompleteWithoutError() {
		return completeWithoutError;
	}





	public IEngUserProfile getProfile() {
		return userProfile;
	}





	public void setProfile(IEngUserProfile profile) {
		this.userProfile = profile;
	}


	public void deleteDBRowInCaseOfError(IProgressThreadDAO threadDAO, Integer progressThreadId){
		logger.debug("IN");
		try {
			threadDAO.deleteProgressThread(progressThreadId);
		} catch (EMFUserError e1) {
			logger.error("Error in deleting the row with the progress id "+progressThreadId);
		}
		logger.debug("OUT");

	}

	
	public String cleanFileName(String name){
		logger.debug("IN");
		char[] forbiddenCharList = {'/','?','!',';',':','.',',','*','#','@','\'','%','&','(',')'};
		
		for (int i = 0; i < forbiddenCharList.length; i++) {
			char f = forbiddenCharList[i];
			name = name.replace(f, '_');
		}
		logger.debug("OUT");		
		return name;
	}
	

}
