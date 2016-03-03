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
