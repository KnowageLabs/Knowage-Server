/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
