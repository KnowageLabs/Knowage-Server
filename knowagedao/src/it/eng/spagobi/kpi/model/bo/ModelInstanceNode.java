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
