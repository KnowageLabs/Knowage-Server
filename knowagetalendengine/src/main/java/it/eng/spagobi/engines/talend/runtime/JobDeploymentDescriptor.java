/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.talend.runtime;

import it.eng.spagobi.engines.talend.TalendEngine;
import it.eng.spagobi.engines.talend.TalendEngineConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

/**
 * @author Andrea Gioia
 *
 */
public class JobDeploymentDescriptor {
	String project;
	String language;
		
	/**
	 * Instantiates a new job deployment descriptor.
	 */
	public JobDeploymentDescriptor() {}
	
		
	/**
	 * Instantiates a new job deployment descriptor.
	 * 
	 * @param project the project
	 * @param language the language
	 */
	public JobDeploymentDescriptor(String project, String language) {
		this.project = project;
		this.language = language;
	}		
	
	/**
	 * Load.
	 * 
	 * @param file the file
	 * 
	 * @throws FileNotFoundException the file not found exception
	 * @throws DocumentException the document exception
	 */
	public void load(File file) throws FileNotFoundException, DocumentException {
		load(new FileInputStream(file));
	}
	
	/**
	 * Load.
	 * 
	 * @param is the is
	 * 
	 * @throws DocumentException the document exception
	 */
	public void load(InputStream is) throws DocumentException{
		SAXReader reader = new org.dom4j.io.SAXReader();
	    Document document = null;
	    
	   	document = reader.read(is);		
	    
	    Node job = document.selectSingleNode("//etl/job");
	    if (job != null) {
	    	this.project = job.valueOf("@project");
	    	this.language = job.valueOf("@language");
	    }
	}
	
	
	/**
	 * Checks if is perl job.
	 * 
	 * @return true, if is perl job
	 */
	public boolean isPerlJob() {
		return (language!= null && language.equalsIgnoreCase("perl"));
	}
	
	/**
	 * Checks if is java job.
	 * 
	 * @return true, if is java job
	 */
	public boolean isJavaJob() {
		return (language!= null && language.equalsIgnoreCase("java"));
	}
	
	
	
	/**
	 * Gets the project.
	 * 
	 * @return the project
	 */
	public String getProject() {
		return project;
	}
	
	/**
	 * Sets the project.
	 * 
	 * @param project the new project
	 */
	public void setProject(String project) {
		this.project = project;
	}
	
	/**
	 * Gets the language.
	 * 
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * Sets the language.
	 * 
	 * @param language the new language
	 */
	public void setLanguage(String language) {
		this.language = language;
	}
	
	/**
	 * To xml.
	 * 
	 * @return the string
	 */
	public String toXml() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<etl>");
		buffer.append("<job");
		if(project != null && !project.trim().equalsIgnoreCase("")) buffer.append(" project=\"" + project + "\"");
		if(language != null && !language.trim().equalsIgnoreCase("")) buffer.append(" language=\"" + language + "\"");
		buffer.append("/>");
		buffer.append("</etl>");
		
		return buffer.toString();
	}
	
	/**
	 * Load job deployment descriptor.
	 * 
	 * @param is the is
	 * 
	 * @return the job deployment descriptor
	 * 
	 * @throws DocumentException the document exception
	 */
	public static JobDeploymentDescriptor loadJobDeploymentDescriptor(InputStream is) throws DocumentException {
		JobDeploymentDescriptor job = new JobDeploymentDescriptor();
		job.load(is);
		return job;
	}
}
