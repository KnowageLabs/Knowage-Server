/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.document.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

/**
 * @author Gioia
 *
 */
public class SbiSubreports extends SbiHibernateModel {

	private SbiSubreportsId id;
	
	/**
	 * default constructor.
	 */
    public SbiSubreports() {}
    
    /**
     * constructor with id.
     * 
     * @param id the id
     */
    public SbiSubreports(SbiSubreportsId id) {
        this.id = id;
    }
    
	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public SbiSubreportsId getId() {
		return id;
	}

	/**
	 * Sets the id.
	 * 
	 * @param id the new id
	 */
	public void setId(SbiSubreportsId id) {
		this.id = id;
	}
    
}
