/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.knowage.meta.generator.jpamapping.wrappers;

import it.eng.knowage.meta.generator.jpamapping.wrappers.impl.JpaSubEntity;

import java.util.List;

/**
 * A unique reference to a table or view given its position in a 
 * relationship path
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public interface IJpaSubEntity {
	
	/**
	 * @return the unique name of the specified entity
	 */
	String getUniqueName();
	
	/**
	 * @return the value of the specified entity attribute
	 */
	public String getAttribute(String name);
	
	/**
	 * @return the columns objects of the sub entity
	 */
	public List<IJpaColumn> getColumns();
	
	/**
	 * @return the columns names of the sub entity
	 */
	public List<String> getColumnNames();
	
	/**
	 * @return the unique columns names of the sub entity
	 */
	public List<String> getColumnUniqueNames();
	
	public JpaSubEntity getParent();
	
}
