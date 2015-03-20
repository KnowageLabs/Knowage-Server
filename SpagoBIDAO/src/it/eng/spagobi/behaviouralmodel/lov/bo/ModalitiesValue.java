/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.behaviouralmodel.lov.bo;

import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;

import java.io.Serializable;


/**
 * Defines a Value object for the Predefined LOV 
 * 
 * @author sulis
 *
 */



public class ModalitiesValue implements Serializable  {
	
	private Integer id;
	private String name ="";
	private String description ="";
	private String lovProvider ="";
	private String iTypeCd ="";
	private String iTypeId = "";
	private SbiDataSet dataset;
	private Integer datasetId;
	private String label ="";
	private String selectionType ="";
	private boolean multivalue = true;



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
	 * Gets the id.
	 * 
	 * @return Returns the id.
	 */
	public Integer  getId() {
		return id;
	}
	
	/**
	 * Sets the id.
	 * 
	 * @param id The id to set.
	 */
	public void setId(Integer  id) {
		this.id = id;
	}
	
	/**
	 * Gets the i type cd.
	 * 
	 * @return Returns the iTypeCd.
	 */
	public String getITypeCd() {
		return iTypeCd;
	}
	
	/**
	 * Sets the i type cd.
	 * 
	 * @param typeCd The iTypeCd to set.
	 */
	public void setITypeCd(String typeCd) {
		iTypeCd = typeCd;
	}
	
	/**
	 * Gets the lov provider.
	 * 
	 * @return Returns the lovProvider.
	 */
	public String getLovProvider() {
		return lovProvider;
	}
	
	/**
	 * Sets the lov provider.
	 * 
	 * @param lovProvider The lovProvider to set.
	 */
	public void setLovProvider(String lovProvider) {
		this.lovProvider = lovProvider;
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
	 * Gets the i type id.
	 * 
	 * @return Returns the iTypeId.
	 */
	public String getITypeId() {
		return iTypeId;
	}
	
	/**
	 * Sets the i type id.
	 * 
	 * @param typeId The iTypeId to set.
	 */
	public void setITypeId(String typeId) {
		iTypeId = typeId;
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
	 * Gets the selection type.
	 * 
	 * @return the selection type
	 */
	public String getSelectionType() {
		return selectionType;
	}
	
	/**
	 * Sets the selection type.
	 * 
	 * @param selectionType the new selection type
	 */
	public void setSelectionType(String selectionType) {
		this.selectionType = selectionType;
	}
	
	/**
	 * Checks if is multivalue.
	 * 
	 * @return true, if is multivalue
	 */
	private boolean isMultivalue() {
		return multivalue;
	}
	
	/**
	 * Sets the multivalue.
	 * 
	 * @param multivalue the new multivalue
	 */
	public void setMultivalue(boolean multivalue) {
		this.multivalue = multivalue;
	}

	/**
	 * @return the datasetID
	 */
	public Integer getDatasetID() {
		this.datasetId = new Integer(dataset.getId().getDsId());		
		return datasetId;
	}

	/**
	 * @param datasetId the datasetID to set
	 */
	public void setDatasetID(Integer datasetId) {
		this.datasetId = datasetId;
	}

	/**
	 * @return the dataset
	 */
	public SbiDataSet getDataset() {
		return dataset;
	}

	/**
	 * @param dataset the dataset to set
	 */
	public void setDataset(SbiDataSet dataset) {
		this.dataset = dataset;
	}
}
