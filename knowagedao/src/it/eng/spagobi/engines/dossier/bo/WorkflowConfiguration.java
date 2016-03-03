/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
