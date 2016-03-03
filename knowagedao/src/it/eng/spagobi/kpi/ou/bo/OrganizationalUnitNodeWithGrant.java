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

import java.util.List;


/**
 * This class represents the a node into a OU hierarchy with its grants
 * 
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class OrganizationalUnitNodeWithGrant {


    // Fields    

     private List<OrganizationalUnitGrantNode> grants;
     private OrganizationalUnitNode node;

    // Constructors

    /** default constructor */
    public OrganizationalUnitNodeWithGrant() {
    }
    
    /** full constructor */
    public OrganizationalUnitNodeWithGrant(OrganizationalUnitNode node, List<OrganizationalUnitGrantNode> grants) {
        this.node = node;
        this.grants = grants;
    }

	public List<OrganizationalUnitGrantNode> getGrants() {
		return grants;
	}

	public void setGrants(List<OrganizationalUnitGrantNode> grants) {
		this.grants = grants;
	}

	public OrganizationalUnitNode getNode() {
		return node;
	}

	public void setNode(OrganizationalUnitNode node) {
		this.node = node;
	}

	@Override
	public String toString() {
		return "OrganizationalUnitNodeWithGrant [grants=" + grants + ", node="
				+ node + "]";
	}
	
}
