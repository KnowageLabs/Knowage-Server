/*
 * SpagoBI, the Open Source Business Intelligence suite
 * © 2005-2015 Engineering Group
 *
 * This file is part of SpagoBI. SpagoBI is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 2.1 of the License, or any later version. 
 * SpagoBI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received
 * a copy of the GNU Lesser General Public License along with SpagoBI. If not, see: http://www.gnu.org/licenses/.
 * The complete text of SpagoBI license is included in the COPYING.LESSER file. 
 */
package it.eng.spagobi.engines.talend.client;



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
