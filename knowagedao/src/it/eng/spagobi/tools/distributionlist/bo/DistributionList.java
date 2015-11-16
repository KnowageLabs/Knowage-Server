/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.distributionlist.bo;

import java.io.Serializable;
import java.util.List;
/**
* @author Chiarelli Chiara (chiara.chiarelli@eng.it)
*/

public class DistributionList implements Serializable {
	
	private int id;
	private String name = null;
	private String descr = null;
	private List emails = null;
	private List documents = null;
	

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Sets the id.
	 * 
	 * @param id the new id
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name.
	 * 
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the descr.
	 * 
	 * @return the descr
	 */
	public String getDescr() {
		return descr;
	}
	
	/**
	 * Sets the descr.
	 * 
	 * @param descr the new descr
	 */
	public void setDescr(String descr) {
		this.descr = descr;
	}
	
	/**
	 * Gets the emails.
	 * 
	 * @return the emails
	 */
	public List getEmails() {
		return emails;
	}
	
	/**
	 * Sets the emails.
	 * 
	 * @param emails the new emails
	 */
	public void setEmails(List emails) {
		this.emails = emails;
	}
	
	/**
	 * Gets the documents.
	 * 
	 * @return the documents
	 */
	public List getDocuments() {
		return documents;
	}
	
	/**
	 * Sets the documents.
	 * 
	 * @param documents the new documents
	 */
	public void setDocuments(List documents) {
		this.documents = documents;
	}
	

}
