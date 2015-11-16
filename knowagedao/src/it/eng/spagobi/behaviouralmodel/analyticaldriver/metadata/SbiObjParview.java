/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiObjParview extends SbiHibernateModel {


	private SbiObjParviewId id;
	private Integer prog;
	private String viewLabel;
	
	
	// Constructors

	/**
	 * default constructor.
	 */
	public SbiObjParview() {
	}

	/**
	 * constructor with id.
	 * 
	 * @param id the id
	 */
	public SbiObjParview(SbiObjParviewId id) {
		this.id = id;
	}
	
	public SbiObjParviewId getId() {
		return id;
	}
	public void setId(SbiObjParviewId id) {
		this.id = id;
	}
	public Integer getProg() {
		return prog;
	}
	public void setProg(Integer prog) {
		this.prog = prog;
	}
	public String getViewLabel() {
		return viewLabel;
	}
	public void setViewLabel(String viewLabel) {
		this.viewLabel = viewLabel;
	}

	


}
