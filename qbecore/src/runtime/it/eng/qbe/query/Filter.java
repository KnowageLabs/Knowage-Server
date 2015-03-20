/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.query;

import it.eng.spagobi.utilities.StringUtils;

import java.io.IOException;
import java.util.Set;

/**
 * The Class Filter.
 * 
 * @author Andrea Gioia
 */
public class Filter {
	
	/** The entity name. */
	String entityName;
	
	/** The filter condition. */
	String filterCondition;
	
	/** The parameters. */
	Set parameters;
	
	/** The fields. */
	Set fields;
	
	/**
	 * Instantiates a new filter.
	 * 
	 * @param entityName the entity name
	 * @param filterCondition the filter condition
	 */
	public Filter(String entityName, String filterCondition) {
		this.entityName = entityName;
		this.filterCondition = filterCondition;
		try {
			this.fields = StringUtils.getParameters(filterCondition, "F");
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			this.parameters = StringUtils.getParameters(filterCondition, "P");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the entity name.
	 * 
	 * @return the entity name
	 */
	public String getEntityName() {
		return entityName;
	}

	/**
	 * Sets the entity name.
	 * 
	 * @param entityName the new entity name
	 */
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	/**
	 * Gets the filter condition.
	 * 
	 * @return the filter condition
	 */
	public String getFilterCondition() {
		return filterCondition;
	}

	/**
	 * Sets the filter condition.
	 * 
	 * @param filterCondition the new filter condition
	 */
	public void setFilterCondition(String filterCondition) {
		this.filterCondition = filterCondition;
	}

	/**
	 * Gets the fields.
	 * 
	 * @return the fields
	 */
	public Set getFields() {
		return fields;
	}

	/**
	 * Gets the parameters.
	 * 
	 * @return the parameters
	 */
	public Set getParameters() {
		return parameters;
	}
}
