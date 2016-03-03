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
