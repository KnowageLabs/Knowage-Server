/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.bo;

import java.io.Serializable;

/**
 * Defines a <code>TemplateVersion</code> object.
 * 
 * @author sulis
 */
public class TemplateVersion implements Serializable {

	String versionName = null;
	String nameFileTemplate = null;
	String dataLoad = null;
	
	/**
	 * Gets the data load.
	 * 
	 * @return the template version data load
	 */
	public String getDataLoad() {
		return dataLoad;
	}
	
	/**
	 * Sets the data load.
	 * 
	 * @param dataLoad the template version data load to set
	 */
	public void setDataLoad(String dataLoad) {
		this.dataLoad = dataLoad;
	}
	
	/**
	 * Gets the name file template.
	 * 
	 * @return the template version name file
	 */
	public String getNameFileTemplate() {
		return nameFileTemplate;
	}
	
	/**
	 * Sets the name file template.
	 * 
	 * @param nameFileTemplate the template version name file to set
	 */
	public void setNameFileTemplate(String nameFileTemplate) {
		this.nameFileTemplate = nameFileTemplate;
	}
	
	/**
	 * Gets the version name.
	 * 
	 * @return the template version version name
	 */
	public String getVersionName() {
		return versionName;
	}
	
	/**
	 * Sets the version name.
	 * 
	 * @param versionName the template version version name to set
	 */
	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}
}
