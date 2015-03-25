/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.scheduler.dispatcher;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.tools.massiveExport.bo.ProgressThread;
import it.eng.spagobi.tools.massiveExport.dao.IProgressThreadDAO;
import it.eng.spagobi.tools.scheduler.to.DispatchContext;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class FileSystemDocumentDispatcher implements IDocumentDispatchChannel {
	
	private DispatchContext dispatchContext;
	List<File> filesToZip;
	String fileName;
	String zipFileName;
	boolean zipFileDocument=false;
	Map<String, String> randomNamesToName;
	ProgressThread progressThread;
	Integer progressThreadId;
	IProgressThreadDAO progressThreadDAO;
	String fileExtension;

	
	// logger component
	private static Logger logger = Logger.getLogger(FileSystemDocumentDispatcher.class); 
	
	public FileSystemDocumentDispatcher(DispatchContext dispatchContext) {
		this.dispatchContext = dispatchContext;
		this.filesToZip = new ArrayList<File>();
	//	this.zipFileName = generateZipFileName();
		this.randomNamesToName = new HashMap<String, String>();
	}
	
	public boolean canDispatch(BIObject document)  {
		return true;
	}
	
	public boolean dispatch(BIObject document, byte[] executionOutput) {
		File exportFile;
		
		fileExtension = dispatchContext.getFileExtension();
		
		try {
			
			fileName = dispatchContext.getFileName() != null && !dispatchContext.getFileName().equals("")?
					dispatchContext.getFileName() : document.getName();
			fileName = fileName+dispatchContext.getNameSuffix();
			fileName = fileName.replace(':', '-');
					
		    zipFileName = dispatchContext.getZipFileName() != null && !dispatchContext.getZipFileName().equals("")?
							dispatchContext.getZipFileName() : document.getName();
			zipFileName = zipFileName+dispatchContext.getNameSuffix();
			zipFileName = zipFileName.replace(':', '-');
			
			zipFileDocument = dispatchContext.isZipFileDocument();
			
			if(dispatchContext.isProcessMonitoringEnabled()) {
				logger.debug("Monitoring of dispatch process is enabled");
				
				progressThreadDAO = DAOFactory.getProgressThreadDAO();
		
				if(progressThread == null) {
					progressThread = new ProgressThread(
							dispatchContext.getOwner(), 
							dispatchContext.getTotalNumberOfDocumentsToDispatch(), 
							dispatchContext.getFunctionalityTreeFolderLabel(), null, zipFileName, ProgressThread.TYPE_MASSIVE_SCHEDULE);
					
					progressThreadId = progressThreadDAO.insertProgressThread(progressThread);
					progressThreadDAO.setStartedProgressThread(progressThreadId);
				}
			} else {
				logger.debug("Monitoring of dispatch process is disabled");
			}
			
		
			if(executionOutput == null) {
				logger.error("execution proxy returned null document for BiObjectDocumetn: " + document.getLabel());
				exportFile = createErrorFile(document, null , randomNamesToName);
			} else{
				String checkError = new String(executionOutput);
				if(checkError.startsWith("error") || checkError.startsWith("{\"errors\":")){
					logger.error("Error found in execution, make txt file");
					String fileNameError = "Error " + document.getLabel() + "-" + document.getName();
					exportFile = File.createTempFile(fileNameError, ".txt");
					randomNamesToName.put(exportFile.getName(), fileNameError+".txt");
				} else{
					logger.info("Export ok for biObj with label "+document.getLabel());
					//String fileName = document.getLabel() + "-" + document.getName();
					//fileName = fileName + dispatchContext.getDescriptionSuffix();
					fileName = fileName.replace(' ', '_');
					exportFile = File.createTempFile(fileName, fileExtension); 
					randomNamesToName.put(exportFile.getName(), fileName + fileExtension);
				}

				FileOutputStream stream = new FileOutputStream(exportFile);
				stream.write(executionOutput);

				logger.debug("create an export file named " + exportFile.getName());

				filesToZip.add(exportFile);
				if(dispatchContext.isProcessMonitoringEnabled()) {
					progressThreadDAO.incrementProgressThread(progressThreadId);
				}
			}
		} catch (Exception e) {
			throw new SpagoBIServiceException("Exception in  writeing export file for BiObject with label "+document.getLabel()+" delete DB row", e);
		}
		
		return true;
	}

	public void close() {
		
		byte[] buffer = new byte[1024]; 
		ZipOutputStream out = null;
		FileOutputStream simpleOut = null;
		FileInputStream in = null;
		
		logger.debug("IN");
		
		try {
			if(zipFileDocument){
			logger.debug("Zip file");
			File destinationFolder = getDestinationFolder();
			
			File zipFile = new File(destinationFolder, zipFileName + ".zip");
	
			logger.debug("zip file written "+zipFile.getAbsolutePath());
	
			out = new ZipOutputStream(new FileOutputStream(zipFile)); 
			for (Iterator iterator = filesToZip.iterator(); iterator.hasNext();) {
				File file = (File) iterator.next();
				in = new FileInputStream(file); 
				String fileName = file.getName();
				String realName = randomNamesToName.get(fileName);
				ZipEntry zipEntry=new ZipEntry(realName);
				out.putNextEntry(zipEntry);

				int len; 
				while ((len = in.read(buffer)) > 0) 
				{ 
					out.write(buffer, 0, len); 
				} 

				out.closeEntry(); 
				in.close(); 
			}
			out.flush();
			out.close();
			}
			else{
				logger.debug("DO not zip file, create plain file");
				File destinationFolder = getDestinationFolder();
				File resultFile = new File(destinationFolder, fileName+fileExtension);
				simpleOut = new FileOutputStream(resultFile); 
				for (Iterator iterator = filesToZip.iterator(); iterator.hasNext();) {
					File file = (File) iterator.next();
					in = new FileInputStream(file); 

					int len; 
					while ((len = in.read(buffer)) > 0) 
					{ 
						simpleOut.write(buffer, 0, len); 
					} 

					in.close(); 
				}
				simpleOut.flush();
				simpleOut.close();
			}
			
			
			
			if(dispatchContext.isProcessMonitoringEnabled()) {
				progressThreadDAO.setDownloadProgressThread(progressThreadId);
			}
		} catch(Throwable t) {
			throw new DispatchException("An unexpected error occured while closing dipatcher", t);
		} finally{
			if(in != null)
			try {
				in.close();
			} catch (IOException e) {
				throw new DispatchException("An unexpected error occured while closing input stream");
			}
			if(out != null)
			try {
				out.close();
			} catch (IOException e) {
				throw new DispatchException("An unexpected error occured while closing output stream");
			}
			if(simpleOut != null)
				try {
					simpleOut.close();
				} catch (IOException e) {
					throw new DispatchException("An unexpected error occured while closing output stream");
				}			
			logger.debug("OUT");
		}
	}
	
	private File getDestinationFolder() {
		File destinationFolder;
		
		logger.debug("IN");
		
		destinationFolder = null;
	
		try {
				
			if (dispatchContext.getDestinationFolder() == null) {
				throw new SpagoBIRuntimeException("Variable destination folder is not set into dispatch context");
			}
			
			if(dispatchContext.isDestinationFolderRelativeToResourceFolder()) {
				String resourceFolderPath = null;
				String jndiVariableName = SingletonConfig.getInstance().getConfigValue("SPAGOBI.RESOURCE_PATH_JNDI_NAME");
				if(jndiVariableName == null){
					throw new SpagoBIRuntimeException("Could not find configuration variable [SPAGOBI.RESOURCE_PATH_JNDI_NAME]");
				}
				
				resourceFolderPath = SpagoBIUtilities.readJndiResource(jndiVariableName);
				if(resourceFolderPath == null) {
					throw new SpagoBIRuntimeException("Could not find resolve jndi variable [" + jndiVariableName + "]");
				}
				
				File resourceFolder = new File(resourceFolderPath);
				if(!resourceFolder.exists()){
					throw new SpagoBIRuntimeException("Could not find resource directory ["+resourceFolderPath + "]");
				}
				
				destinationFolder = new File(resourceFolder, dispatchContext.getDestinationFolder());
			} else {
				destinationFolder = new File(dispatchContext.getDestinationFolder());
			}
		
			if(!destinationFolder.exists()) {
				destinationFolder.mkdirs();
			}
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while retrieving destination folder", t);
		} finally {
			logger.debug("OUT");
		}

		return destinationFolder;
	}
	
//	private String generateZipFileName(){
//		DateFormat formatter = new SimpleDateFormat("dd-MMM-yy hh:mm:ss.SSS");
//		String randomName = formatter.format(new Date());			
//		randomName=randomName.replaceAll(" ", "_");
//		randomName=randomName.replaceAll(":", "-");
//		return randomName;
//
//	}

	
	


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
	
	public void setDispatchContext(DispatchContext dispatchContext) {
		this.dispatchContext = dispatchContext;
	}

	
}
