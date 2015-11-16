/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.ou.bo;

import it.eng.spagobi.kpi.model.bo.ModelInstanceNode;

/**
 * This class represents a grant for a particular Organizational Unit of a hierarchy for a node of a KPI model instance
 * 
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class OrganizationalUnitGrantNode {


    // Fields    

     private OrganizationalUnitNode ouNode;
     private ModelInstanceNode modelInstanceNode;
     private OrganizationalUnitGrant grant;


    // Constructors

    /** default constructor */
    public OrganizationalUnitGrantNode() {
    }

    
    /** full constructor */
    public OrganizationalUnitGrantNode(OrganizationalUnitNode ouNode, ModelInstanceNode modelInstanceNode, OrganizationalUnitGrant grant) {
        this.ouNode = ouNode;
        this.modelInstanceNode = modelInstanceNode;
        this.grant = grant;
    }


	public OrganizationalUnitNode getOuNode() {
		return ouNode;
	}


	public void setOuNode(OrganizationalUnitNode ouNode) {
		this.ouNode = ouNode;
	}


	public ModelInstanceNode getModelInstanceNode() {
		return modelInstanceNode;
	}


	public void setModelInstanceNode(ModelInstanceNode modelInstanceNode) {
		this.modelInstanceNode = modelInstanceNode;
	}


	public OrganizationalUnitGrant getGrant() {
		return grant;
	}


	public void setGrant(OrganizationalUnitGrant grant) {
		this.grant = grant;
	}


	@Override
	public String toString() {
		return "OrganizationalUnitGrantNode [ouNode=" + ouNode
				+ ", modelInstanceNode=" + modelInstanceNode + ", grant="
				+ grant + "]";
	}

}
