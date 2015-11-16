/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.events.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

/**
 * @author Gioia
 *
 */
public class SbiEvents extends SbiHibernateModel {
	private Integer id;
	private String user;
	
	/**
	 * Instantiates a new sbi events.
	 */
	public SbiEvents() {}
	
	/**
	 * Instantiates a new sbi events.
	 * 
	 * @param id the id
	 * @param user the user
	 */
	public SbiEvents(Integer id, String user) {
		this.id = id;
		this.user = user;
	}
	
	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}
	
	/**
	 * Sets the id.
	 * 
	 * @param id the new id
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	
	/**
	 * Gets the user.
	 * 
	 * @return the user
	 */
	public String getUser() {
		return user;
	}
	
	/**
	 * Sets the user.
	 * 
	 * @param user the new user
	 */
	public void setUser(String user) {
		this.user = user;
	}	   
}
