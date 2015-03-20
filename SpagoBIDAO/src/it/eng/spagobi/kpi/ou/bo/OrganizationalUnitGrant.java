/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.ou.bo;

import it.eng.spagobi.kpi.model.bo.ModelInstance;

import java.util.Date;


/**
 * This class represents the grant to an Organizational Unit hierarchy for a KPI model instance
 * 
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class OrganizationalUnitGrant {


    // Fields    

     private Integer id;
     private ModelInstance modelInstance; // the root node of the KPI model instance
     private OrganizationalUnitHierarchy hierarchy; 
     private Date startDate;
     private Date endDate;
     private String label;
     private String name;
     private String description;
     private Boolean isAvailable;
    // Constructors

    /** default constructor */
    public OrganizationalUnitGrant() {
    }
    
    /** full constructor */
    public OrganizationalUnitGrant(Integer id,Boolean isAvailable, ModelInstance modelInstance, OrganizationalUnitHierarchy hierarchy, Date startDate, Date endDate, String label, String name, String description) {
        this.id = id;
        this.modelInstance = modelInstance;
        this.hierarchy = hierarchy;
        this.startDate = startDate;
        this.endDate = endDate;
        this.label = label;
        this.name = name;
        this.description = description;
        this.isAvailable = isAvailable;
    }

	public Boolean getIsAvailable() {
		return isAvailable;
	}

	public void setIsAvailable(Boolean isAvailable) {
		this.isAvailable = isAvailable;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public ModelInstance getModelInstance() {
		return modelInstance;
	}

	public void setModelInstance(ModelInstance modelInstance) {
		this.modelInstance = modelInstance;
	}

	public OrganizationalUnitHierarchy getHierarchy() {
		return hierarchy;
	}

	public void setHierarchy(OrganizationalUnitHierarchy hierarchy) {
		this.hierarchy = hierarchy;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

    @Override
	public String toString() {
		return "OrganizationalUnitGrant [id=" + id + ", modelInstance="
				+ modelInstance + ", hierarchy=" + hierarchy + ", startDate="
				+ startDate + ", endDate=" + endDate + ", label=" + label
				+ ", name=" + name + ", description=" + description + "]";
	}
	
}
