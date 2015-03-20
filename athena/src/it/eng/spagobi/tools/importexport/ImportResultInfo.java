/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.importexport;

import java.util.HashMap;
import java.util.Map;

public class ImportResultInfo {

	String folderName = "";
	String logFileName = "";
	String associationsFileName = "";
	Map manualTasks = new HashMap();
	
	/**
	 * Gets the manual tasks.
	 * 
	 * @return the manual tasks
	 */
	public Map getManualTasks() {
		return manualTasks;
	}
	
	/**
	 * Sets the manual tasks.
	 * 
	 * @param manualTasks the new manual tasks
	 */
	public void setManualTasks(Map manualTasks) {
		this.manualTasks = manualTasks;
	}
	
	/**
	 * Gets the Name of log file.
	 * 
	 * @return the Name log file
	 */
	public String getLogFileName() {
		return logFileName;
	}
	
	/**
	 * Sets the Name of log file.
	 * 
	 * @param logFileName the Name of log file.
	 */
	public void setLogFileName(String logFileName) {
		this.logFileName = logFileName;
	}
	
	/**
	 * Gets the Name of associations file.
	 * 
	 * @return the Name of associations file
	 */
	public String getAssociationsFileName() {
		return associationsFileName;
	}
	
	/**
	 * Sets the Name of associations file.
	 * 
	 * @param associationsFileName the Name of associations file.
	 */
	public void setAssociationsFileName(String associationsFileName) {
		this.associationsFileName = associationsFileName;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}
	
}
