/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.model.bo;

import it.eng.spagobi.tools.udp.bo.UdpValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Model implements Serializable{
	
	private Integer id = null;// id of ModelNode (KPI_MODEL_ID)
	private String guiId = null;// id of the rendered node
	private Integer parentId = null; //the parent id of the tree made of ModelNodes, representing the model
	private List childrenNodes = null;//List of Model children
	private List modelAttributes = null;//List of all related ModelAttributes
	private String name = null;//name of the complete model (like CMMI/GQM)
	private String description = null;//description of the complete model
	private String code = null;//code of the complete model
	private Integer typeId = null;//id of the type of the model 
	private String typeCd = null;//name of the type of the model (GENERIC_ROOT/GQM_ROOT)
	private String typeName = null;//name of the type of the model (GENERIC_ROOT/GQM_ROOT)
	private String typeDescription = null;//description of the type of the model
	private Integer kpiId = null;// id of Kpi
	private String label = null;// unique label of the model
	
	List udpValues = new ArrayList<UdpValue>();
	
	public List getUdpValues() {
		return udpValues;
	}
	public void setUdpValues(List udpValues) {
		this.udpValues = udpValues;
	}
	
	public Integer getKpiId() {
		return kpiId;
	}

	public void setKpiId(Integer kpiId) {
		this.kpiId = kpiId;
	}

	public Integer getId(){
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getTypeId() {
		return typeId;
	}



	public String getGuiId() {
		return guiId;
	}

	public void setGuiId(String guiId) {
		this.guiId = guiId;
	}

	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
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

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	public String getTypeName() {
		return typeName;
	}
	
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	
	public String getTypeDescription() {
		return typeDescription;
	}
	
	public void setTypeDescription(String typeDescription) {
		this.typeDescription = typeDescription;
	}
	
	public Model() {
		super();
		this.childrenNodes= new ArrayList();
		this.modelAttributes = new ArrayList();
	}
	
	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	public List getChildrenNodes() {
		return childrenNodes;
	}

	public void setChildrenNodes(List childrenNodes) {
		this.childrenNodes = childrenNodes;
	}
	
	public List getModelAttributes(){
		return modelAttributes;
	}

	public void setModelAttributes(List modelAttributes) {
		this.modelAttributes = modelAttributes;
	}

	public String getTypeCd() {
		return typeCd;
	}

	public void setTypeCd(String typeCd) {
		this.typeCd = typeCd;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	
	
}
