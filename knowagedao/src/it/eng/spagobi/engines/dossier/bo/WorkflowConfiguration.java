/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.dossier.bo;

import java.io.Serializable;

public class WorkflowConfiguration implements Serializable{

	private String nameWorkflowPackage = "";
	private String nameWorkflowProcess = "";
	
	
	/**
	 * Gets the name workflow package.
	 * 
	 * @return the name workflow package
	 */
	public String getNameWorkflowPackage() {
		return nameWorkflowPackage;
	}
	
	/**
	 * Sets the name workflow package.
	 * 
	 * @param nameWorkflowPackage the new name workflow package
	 */
	public void setNameWorkflowPackage(String nameWorkflowPackage) {
		this.nameWorkflowPackage = nameWorkflowPackage;
	}
	
	/**
	 * Gets the name workflow process.
	 * 
	 * @return the name workflow process
	 */
	public String getNameWorkflowProcess() {
		return nameWorkflowProcess;
	}
	
	/**
	 * Sets the name workflow process.
	 * 
	 * @param nameWorkflowProcess the new name workflow process
	 */
	public void setNameWorkflowProcess(String nameWorkflowProcess) {
		this.nameWorkflowProcess = nameWorkflowProcess;
	}
	
}
