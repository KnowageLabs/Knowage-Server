/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.talend.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.dom4j.DocumentException;
import org.xml.sax.InputSource;

import sun.misc.BASE64Encoder;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.talend.TalendEngine;
import it.eng.spagobi.engines.talend.TalendEngineConfig;
import it.eng.spagobi.engines.talend.runtime.Job;
import it.eng.spagobi.engines.talend.runtime.JobDeploymentDescriptor;
import it.eng.spagobi.engines.talend.runtime.RuntimeRepository;
import it.eng.spagobi.engines.talend.utils.ZipUtils;
import it.eng.spagobi.services.proxy.ContentServiceProxy;
import it.eng.spagobi.utilities.engines.AbstractEngineStartServlet;
import it.eng.spagobi.utilities.engines.EngineStartServletIOManager;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;

/**
 * @author Andrea Gioia
 *
 */
public class JobUploadService extends AbstractEngineStartServlet {
	
	private static final String USER = "biadmin";
	private static final String PASSWORD = "biadmin";
	
	private static final long serialVersionUID = 1L;
	
	private static transient Logger logger = Logger.getLogger(JobUploadService.class);
	
	public void doService( EngineStartServletIOManager servletIOManager ) throws SpagoBIEngineException { 
		
		boolean isMultipart;
		FileItemFactory factory;
		ServletFileUpload upload;
		JobDeploymentDescriptor jobDeploymentDescriptor;
		
		logger.debug("IN");
		
		try {		
			
			auditServiceStartEvent();
				
					
			//  Check that we have a file upload request
			isMultipart = ServletFileUpload.isMultipartContent( servletIOManager.getRequest() );
			
			// Create a factory for disk-based file items
			factory = new DiskFileItemFactory();

			// Create a new file upload handler
			upload = new ServletFileUpload(factory);

			// Parse the request
			List items = null;
			try {
				items = upload.parseRequest(servletIOManager.getRequest());
			} catch (FileUploadException e) {
				throw new SpagoBIEngineException("Impossible to upload file", "impossible.to.upload.file", e );
			}
			
			jobDeploymentDescriptor = getJobsDeploymetDescriptor(items);
			
			// Process the uploaded items
			Iterator iter = items.iterator();
			while (iter.hasNext()) {
			    FileItem item = (FileItem) iter.next();
			    if (item.isFormField()) {
			        processFormField(item);
			    } else {
			        String[] jobNames = processUploadedFile(item, jobDeploymentDescriptor);
			        if(TalendEngine.getConfig().isAutoPublishActive()) {
			        	if(jobNames == null) continue;
				        for(int i = 0; i < jobNames.length; i++) {
				        	publishOnSpagoBI(servletIOManager, jobDeploymentDescriptor.getLanguage(), jobDeploymentDescriptor.getProject(), jobNames[i]);		 
				        }
			        }
			    }
			}
			
			servletIOManager.tryToWriteBackToClient( "OK" );			
			
		} catch (Exception e){
			throw new SpagoBIEngineException("An error occurred while executing [JobUploadService]", "an.unpredicted.error.occured", e);
		} finally {
			logger.debug("OUT");
		}		
	}
	
	public void auditServiceStartEvent() {
		logger.info("EXECUTION_STARTED: " + new Date(System.currentTimeMillis()));
	}

	public void auditServiceErrorEvent(String msg) {
		logger.info("EXECUTION_FAILED: " + new Date(System.currentTimeMillis()));
	}

	public void auditServiceEndEvent() {
		logger.info("EXECUTION_PERFORMED: " + new Date(System.currentTimeMillis()));	
	}
	
	public String getUserIdentifier() {    	
    	return USER;
    }
	
	

	private JobDeploymentDescriptor getJobsDeploymetDescriptor(List items) {
		JobDeploymentDescriptor jobDeploymentDescriptor = null;
		
		Iterator iter = items.iterator();
		while (iter.hasNext()) {
		    FileItem item = (FileItem) iter.next();
		    if (!item.isFormField()) {
		    	String fieldName = item.getFieldName();
		    	if(fieldName.equalsIgnoreCase("deploymentDescriptor")) {
		    		jobDeploymentDescriptor = new JobDeploymentDescriptor();
		    		try {
		    			jobDeploymentDescriptor.load(item.getInputStream());
					} catch (DocumentException e) {
						e.printStackTrace();
						return null;
					} catch (IOException e) {
						e.printStackTrace();
						return null;
					}
		    	}
		    }
		}
		return jobDeploymentDescriptor;
	}
	
	
	private void processFormField(FileItem item) {
		// TODO Auto-generated method stub
		
	}

	private String[] processUploadedFile(FileItem item, JobDeploymentDescriptor jobDeploymentDescriptor) throws ZipException, IOException, SpagoBIEngineException {
		String fieldName = item.getFieldName();
	    String fileName = item.getName();
	    String contentType = item.getContentType();
	    boolean isInMemory = item.isInMemory();
	    long sizeInBytes = item.getSize();
	    
	    if(fieldName.equalsIgnoreCase("deploymentDescriptor")) return null;
	    
	    RuntimeRepository runtimeRepository = TalendEngine.getRuntimeRepository();
	    File jobsDir = new File(runtimeRepository.getRootDir(), jobDeploymentDescriptor.getLanguage().toLowerCase());
		File projectDir = new File(jobsDir, jobDeploymentDescriptor.getProject());
		File tmpDir = new File(projectDir, "tmp");
		if(!tmpDir.exists()) tmpDir.mkdirs();	   
	     File uploadedFile = new File(tmpDir, fileName);
	    
	    try {
			item.write(uploadedFile);
		} catch (Exception e) {
			e.printStackTrace();
		}	
		
		String[] dirNames = ZipUtils.getDirectoryNameByLevel(new ZipFile(uploadedFile), 2);
		List dirNameList = new ArrayList();
		for(int i = 0; i < dirNames.length; i++) {
			if(!dirNames[i].equalsIgnoreCase("lib")) dirNameList.add(dirNames[i]);
		}
		String[] jobNames = (String[])dirNameList.toArray(new String[0]);
		
	    runtimeRepository.deployJob(jobDeploymentDescriptor, new ZipFile(uploadedFile));
	    uploadedFile.delete();	
	    tmpDir.delete();
	    
	    return jobNames;
	}
	
	
	/**
	 * Checks if is valid template.
	 * 
	 * @param templateFile the template file
	 * 
	 * @return true, if is valid template
	 */
	public boolean isValidTemplate(File templateFile) {
		try {
		   	SourceBean template = SourceBean.fromXMLStream( new InputSource(new FileInputStream(templateFile)) ); 
			Job job = new Job( template );
			if(job.getLanguage() == null || job.getProject() == null || job.getName() == null) return false;
		} catch (Exception e) {
			 return false;
		}
		
		return true;
	}
	
	private String getTemplate(String language, String projectName, String jobName) throws IOException, SpagoBIEngineException {
		String template = null;
		
		RuntimeRepository runtimeRepository = TalendEngine.getRuntimeRepository();
		File jobsDir = new File(runtimeRepository.getRootDir(), language.toLowerCase());
		File projectDir = new File(jobsDir, projectName);
		File jobDir = new File(projectDir, jobName);
		File templateFile = new File(jobDir, "spagobi.xml");
		if(templateFile.exists() && isValidTemplate(templateFile) ) {
			BufferedReader reader = new BufferedReader(new FileReader(templateFile));
			String line = null;
			template = "";
			while((line = reader.readLine()) != null) {
				template += line + "\n";
			}
		} else {
			String contextName = "Default";
			template = "";
			template += "<etl>\n";
			template += "\t<job project=\"" + projectName + "\" ";
			template += "jobName=\"" + jobName + "\" ";
			template += "context=\"" + contextName + "\" ";
			template += "language=\"" + language + "\" />\n";
			template += "</etl>";
		}
		
		return template;
	}
	
	
	/*
	 * TODO: implementare questa funzione tramite una API  WEB Service
	 */
	private void publishOnSpagoBI(EngineStartServletIOManager servletIOManager, String language, String projectName, String jobName) throws IOException, SpagoBIEngineException {
		RuntimeRepository runtimeRepository = TalendEngine.getRuntimeRepository();
		
		String template = getTemplate(language, projectName, jobName);
		
		BASE64Encoder encoder = new BASE64Encoder();
		String templateBase64Coded = encoder.encode(template.getBytes());		
		
		TalendEngineConfig config = TalendEngineConfig.getInstance();
		
		String user = USER;
		String password = PASSWORD;
		Date now = new Date();
		String date = new Long (now.getTime()).toString();
		String label = jobName.toUpperCase();
		if (label.length() > 20) label = label.substring(0,19);
		String name = jobName ;
		String description = language + " job defined in " + projectName  + " project";
		String encrypt = "false";
		String visible = "true";
		String functionalitiyCode = config.getSpagobiTargetFunctionalityLabel();		
		String type = "ETL";
		String state = "DEV";
		
		 HashMap<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("TEMPLATE", templateBase64Coded);
		attributes.put("LABEL", label);
		attributes.put("NAME", name);
		attributes.put("DESCRIPTION", description);
		attributes.put("ENCRYPTED", encrypt);
		attributes.put("VISIBLE", visible);
		attributes.put("TYPE", type);
		attributes.put("FUNCTIONALITYCODE", functionalitiyCode);	
		attributes.put("STATE", state);
		attributes.put("USER", user);
		
		try {
			ContentServiceProxy contentProxy=new ContentServiceProxy(user, servletIOManager.getHttpSession());
		    contentProxy.publishTemplate(attributes);
			//servletIOManager.getContentServiceProxy().publishTemplate(attributes);
			/*
			String spagobiurl = config.getSpagobiUrl();
		    session.setAttribute("BACK_END_SPAGOBI_CONTEXT", spagobiurl);
		    ContentServiceProxy contentProxy=new ContentServiceProxy(user,session);
		    contentProxy.publishTemplate(attributes);
		    */
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * TODO: implementare questa funzione tramite una API  WEB Service
	 */
	private void publishOnSpagoBI(FileItem item, JobDeploymentDescriptor jobDeploymentDescriptor) throws ZipException, IOException, SpagoBIEngineException {
		String fieldName = item.getFieldName();
	    String fileName = item.getName();
	    String contentType = item.getContentType();
	    boolean isInMemory = item.isInMemory();
	    long sizeInBytes = item.getSize();
	    
	    if(fieldName.equalsIgnoreCase("deploymentDescriptor")) return;
	    
	    
	    
	    RuntimeRepository runtimeRepository = TalendEngine.getRuntimeRepository();
	    
	    String projectName = jobDeploymentDescriptor.getProject();
	    String projectLanguage = jobDeploymentDescriptor.getLanguage().toLowerCase();
	    String jobName = "JOB_NAME";
	    String contextName = "Default";
	    String template = "";
	    template += "<etl>\n";
	    template += "\t<job project=\"" + projectName + "\" ";
	    template += "jobName=\"" + projectName + "\" ";
	    template += "context=\"" + projectName + "\" ";
	    template += "language=\"" + contextName + "\" />\n";
	    template += "</etl>";
	    
	    BASE64Encoder encoder = new BASE64Encoder();
	    String templateBase64Coded = encoder.encode(template.getBytes());		
	    
	    TalendEngineConfig config = TalendEngineConfig.getInstance();
	    
	    String user = "biadmin";
		String password = "biadmin";
		String label = "ETL_JOB";
		String name = "EtlJob";
		String description = "Etl Job";
		boolean encrypt = false;
		boolean visible = true;
		String functionalitiyCode = config.getSpagobiTargetFunctionalityLabel();	
		String type = "ETL";
		String state = "DEV";
	    
	    try {

	    	//PublishAccessUtils.publish(spagobiurl, user, password, label, name, description, encrypt, visible, type, state, functionalitiyCode, templateBase64Coded);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	}

}

