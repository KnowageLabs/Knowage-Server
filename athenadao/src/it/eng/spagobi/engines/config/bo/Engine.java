/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.config.bo;

import java.io.Serializable;



/**
 * Defines an <code>engine</code> object
 * 
 * @author sulis
 *
 */

public class Engine implements Serializable {
	
	private Integer id;
	private Integer criptable; 
	private String name = "";
	private String description = "";
	private String url = "";
	private String secondaryUrl = "";
	private String dirUpload = "";
	private String dirUsable = "";
	private String driverName = "";	
	private String label = "";
	private String className = "";
	private Integer biobjTypeId;
	private Integer engineTypeId;
	private boolean useDataSource = false;
	private boolean useDataSet = false;

	/**
	 * Gets the criptable.
	 * 
	 * @return Returns the criptable.
	 */
	public Integer getCriptable() {
		return criptable;
	}
	
	/**
	 * Sets the criptable.
	 * 
	 * @param criptable The criptable to set.
	 */
	public void setCriptable(Integer criptable) {
		this.criptable = criptable;
	}
	
	/**
	 * Gets the description.
	 * 
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Sets the description.
	 * 
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Gets the dir upload.
	 * 
	 * @return Returns the dirUpload.
	 */
	public String getDirUpload() {
		return dirUpload;
	}
	
	/**
	 * Sets the dir upload.
	 * 
	 * @param dirUpload The dirUpload to set.
	 */
	public void setDirUpload(String dirUpload) {
		this.dirUpload = dirUpload;
	}
	
	/**
	 * Gets the dir usable.
	 * 
	 * @return Returns the dirUsable.
	 */
	public String getDirUsable() {
		return dirUsable;
	}
	
	/**
	 * Sets the dir usable.
	 * 
	 * @param dirUsable The dirUsable to set.
	 */
	public void setDirUsable(String dirUsable) {
		this.dirUsable = dirUsable;
	}
	
	/**
	 * Gets the driver name.
	 * 
	 * @return Returns the driverName.
	 */
	public String getDriverName() {
		return driverName;
	}
	
	/**
	 * Sets the driver name.
	 * 
	 * @param driverName The driverName to set.
	 */
	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}
	
	/**
	 * Gets the id.
	 * 
	 * @return Returns the id.
	 */
	public Integer getId() {
		return id;
	}
	
	/**
	 * Sets the id.
	 * 
	 * @param id The id to set.
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	
	/**
	 * Gets the name.
	 * 
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name.
	 * 
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the secondary url.
	 * 
	 * @return Returns the secondaryUrl.
	 */
	public String getSecondaryUrl() {
		return secondaryUrl;
	}
	
	/**
	 * Sets the secondary url.
	 * 
	 * @param secondaryUrl The secondaryUrl to set.
	 */
	public void setSecondaryUrl(String secondaryUrl) {
		this.secondaryUrl = secondaryUrl;
	}
	
	/**
	 * Gets the url.
	 * 
	 * @return Returns the url.
	 */
	public String getUrl() {
		return url;
	}
	
	/**
	 * Sets the url.
	 * 
	 * @param url The url to set.
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	
	/**
	 * Gets the label.
	 * 
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}
	
	/**
	 * Sets the label.
	 * 
	 * @param label the new label
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	
	/**
	 * Gets the engine type id.
	 * 
	 * @return the engine type id
	 */
	public Integer getEngineTypeId() {
		return engineTypeId;
	}
	
	/**
	 * Sets the engine type id.
	 * 
	 * @param engineTypeId the new engine type id
	 */
	public void setEngineTypeId(Integer engineTypeId) {
		this.engineTypeId = engineTypeId;
	}
	
	/**
	 * Gets the class name.
	 * 
	 * @return the class name
	 */
	public String getClassName() {
		return className;
	}
	
	/**
	 * Sets the class name.
	 * 
	 * @param className the new class name
	 */
	public void setClassName(String className) {
		this.className = className;
	}
	
	/**
	 * Gets the biobj type id.
	 * 
	 * @return the biobj type id
	 */
	public Integer getBiobjTypeId() {
		return biobjTypeId;
	}
	
	/**
	 * Sets the biobj type id.
	 * 
	 * @param biobjTypeId the new biobj type id
	 */
	public void setBiobjTypeId(Integer biobjTypeId) {
		this.biobjTypeId = biobjTypeId;
	}
	
	/**
	 * Gets the use data source.
	 * 
	 * @return the use data source
	 */
	public boolean getUseDataSource() {
		return useDataSource;
	}
	
	/**
	 * Sets the use data source.
	 * 
	 * @param useDataSource the new use data source
	 */
	public void setUseDataSource(boolean useDataSource) {
		this.useDataSource = useDataSource;
	}
	
	/**
	 * Gets the use data set.
	 * 
	 * @return the use data set
	 */
	public boolean getUseDataSet() {
		return useDataSet;
	}
	
	/**
	 * Sets the use data set.
	 * 
	 * @param useDataSet the new use data set
	 */
	public void setUseDataSet(boolean useDataSet) {
		this.useDataSet = useDataSet;
	}
	
	

}
