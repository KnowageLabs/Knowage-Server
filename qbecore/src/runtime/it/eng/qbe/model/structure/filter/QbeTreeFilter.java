/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.model.structure.filter;

import it.eng.qbe.datasource.IDataSource;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class QbeTreeFilter.
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class QbeTreeFilter {
	
	/** The entity filter. */
	private IQbeTreeEntityFilter entityFilter;
	
	/** The field filter. */
	private IQbeTreeFieldFilter fieldFilter;
	
	/**
	 * Instantiates a new qbe tree filter.
	 * 
	 * @param entityFilter the entity filter
	 * @param fieldFilter the field filter
	 */
	public QbeTreeFilter(IQbeTreeEntityFilter entityFilter, IQbeTreeFieldFilter fieldFilter) {
		setEntityFilter(entityFilter);
		setFieldFilter(fieldFilter);
	}
	
	/**
	 * Filter entities.
	 * 
	 * @param dataSource the datamart model
	 * @param entities the entities
	 * 
	 * @return the list
	 */
	public List filterEntities(IDataSource dataSource, List entities) {
		return getEntityFilter().filterEntities(dataSource, entities);
	}
	
	/**
	 * Filter fields.
	 * 
	 * @param datamartModel the datamart model
	 * @param fields the fields
	 * 
	 * @return the list
	 */
	public List filterFields(IDataSource dataSource, List fields) {
		return getFieldFilter().filterFields(dataSource, fields);
	}

	/**
	 * Gets the entity filter.
	 * 
	 * @return the entity filter
	 */
	protected IQbeTreeEntityFilter getEntityFilter() {
		return entityFilter;
	}

	/**
	 * Sets the entity filter.
	 * 
	 * @param entityFilter the new entity filter
	 */
	protected void setEntityFilter(IQbeTreeEntityFilter entityFilter) {
		this.entityFilter = entityFilter;
	}

	/**
	 * Gets the field filter.
	 * 
	 * @return the field filter
	 */
	protected IQbeTreeFieldFilter getFieldFilter() {
		return fieldFilter;
	}

	/**
	 * Sets the field filter.
	 * 
	 * @param fieldFilter the new field filter
	 */
	protected void setFieldFilter(IQbeTreeFieldFilter fieldFilter) {
		this.fieldFilter = fieldFilter;
	}
}
