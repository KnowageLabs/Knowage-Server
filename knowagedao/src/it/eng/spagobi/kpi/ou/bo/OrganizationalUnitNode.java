/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.ou.bo;

/**
 * This class represents a node of a hierarchy of Organizational Units
 * 
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class OrganizationalUnitNode {

    // Fields    

     private Integer nodeId;
     private OrganizationalUnit ou;
     private OrganizationalUnitHierarchy hierarchy;
     private Integer parentNodeId;
     private String path;
     private boolean leaf;


    // Constructors

    /** default constructor */
    public OrganizationalUnitNode() {
    }

    public OrganizationalUnitNode(Integer nodeId, OrganizationalUnit ou, OrganizationalUnitHierarchy hierarchy, String path, Integer parentNodeId) {
        this.nodeId = nodeId;
        this.ou = ou;
        this.hierarchy = hierarchy;
        this.path = path;
        this.parentNodeId = parentNodeId;
    }

	public Integer getNodeId() {
		return nodeId;
	}

	public void setNodeId(Integer nodeId) {
		this.nodeId = nodeId;
	}

	public OrganizationalUnit getOu() {
		return ou;
	}

	public void setOu(OrganizationalUnit ou) {
		this.ou = ou;
	}

	public OrganizationalUnitHierarchy getHierarchy() {
		return hierarchy;
	}

	public void setHierarchy(OrganizationalUnitHierarchy hierarchy) {
		this.hierarchy = hierarchy;
	}

	public Integer getParentNodeId() {
		return parentNodeId;
	}

	public void setParentNodeId(Integer parentNodeId) {
		this.parentNodeId = parentNodeId;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public boolean isLeaf() {
		return leaf;
	}

	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}

	@Override
	public String toString() {
		return "OrganizationalUnitNode [nodeId=" + nodeId + ", ou=" + ou
				+ ", hierarchy=" + hierarchy + ", parentNodeId=" + parentNodeId
				+ ", path=" + path + "]";
	}
	
}
