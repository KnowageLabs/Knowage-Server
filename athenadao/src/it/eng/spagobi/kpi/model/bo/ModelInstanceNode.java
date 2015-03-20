/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.model.bo;

import it.eng.spagobi.kpi.config.bo.KpiInstance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ModelInstanceNode implements Serializable{
	
	Boolean isRoot = null;
	String name = null;
	String descr = null;
	String modelCode = null;
	Integer modelNodeId = null;//the referenced ModelNodeID
	Integer modelInstanceNodeId = null;
	Integer fatherId = null;
	List childrenIds = null;// List of ModelNodesInstances children
	KpiInstance kpiInstanceAssociated = null;
	List resources = null ; //Resources to which this model has to be applied
	String modelInstaceReferenceLabel = null;
	
	public Boolean getIsRoot() {
		return isRoot;
	}

	public void setIsRoot(Boolean isRoot) {
		this.isRoot = isRoot;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}


	public Integer getModelReference() {
		return modelNodeId;
	}

	public void setModelReference(Integer modelNodeId) {
		this.modelNodeId = modelNodeId;
	}

	public KpiInstance getKpiInstanceAssociated() {
		return kpiInstanceAssociated;
	}

	public void setKpiInstanceAssociated(KpiInstance kpiInstanceAssociated) {
		this.kpiInstanceAssociated = kpiInstanceAssociated;
	}

	public List getResources() {
		return resources;
	}

	public void setResources(List resources) {
		this.resources = resources;
	}

	public ModelInstanceNode() {
		super();
		this.childrenIds = new ArrayList();	
		this.resources = new ArrayList();	
	}

	public Integer getModelInstanceNodeId() {
		return modelInstanceNodeId;
	}

	public void setModelInstanceNodeId(Integer modelInstanceNodeId) {
		this.modelInstanceNodeId = modelInstanceNodeId;
	}

	public List getChildrenIds() {
		return childrenIds;
	}

	public void setChildrenIds(List childrenIds) {
		this.childrenIds = childrenIds;
	}

	public Integer getFatherId() {
		return fatherId;
	}

	public void setFatherId(Integer fatherId) {
		this.fatherId = fatherId;
	}

	public String getModelCode() {
		return modelCode;
	}

	public void setModelCode(String modelCode) {
		this.modelCode = modelCode;
	}

	public Integer getModelNodeId() {
		return modelNodeId;
	}

	public void setModelNodeId(Integer modelNodeId) {
		this.modelNodeId = modelNodeId;
	}

	public String getModelInstaceReferenceLabel() {
		return modelInstaceReferenceLabel;
	}

	public void setModelInstaceReferenceLabel(String modelInstaceReferenceLabel) {
		this.modelInstaceReferenceLabel = modelInstaceReferenceLabel;
	}

}
