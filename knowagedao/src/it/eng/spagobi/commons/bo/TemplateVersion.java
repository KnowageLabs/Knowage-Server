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
