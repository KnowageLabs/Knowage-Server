/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.model.structure.filter;

import it.eng.qbe.datasource.IDataSource;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class ComposableQbeTreeEntityFilter.
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public abstract class ComposableQbeTreeEntityFilter implements IQbeTreeEntityFilter {
	
	/** The parent filter. */
	private IQbeTreeEntityFilter parentFilter;
	
	/**
	 * Instantiates a new composable qbe tree entity filter.
	 */
	public ComposableQbeTreeEntityFilter() {
		parentFilter = null;
	}
	
	/**
	 * Instantiates a new composable qbe tree entity filter.
	 * 
	 * @param parentFilter the parent filter
	 */
	public ComposableQbeTreeEntityFilter(IQbeTreeEntityFilter parentFilter) {
		setParentFilter(parentFilter);
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.qbe.tree.filter.IQbeTreeEntityFilter#filterEntities(it.eng.qbe.model.IDataMartModel, java.util.List)
	 */
	public List filterEntities(IDataSource dataSource, List entities) {
		
		if( getParentFilter() != null) {
			entities = getParentFilter().filterEntities(dataSource, entities);
		}
		
		return filter( dataSource, entities );
	}
	
	/**
	 * Filter.
	 * 
	 * @param datamartModel the datamart model
	 * @param fields the fields
	 * 
	 * @return the list
	 */
	public abstract List filter(IDataSource dataSource, List fields);

	/**
	 * Gets the parent filter.
	 * 
	 * @return the parent filter
	 */
	protected IQbeTreeEntityFilter getParentFilter() {
		return parentFilter;
	}

	/**
	 * Sets the parent filter.
	 * 
	 * @param parentFilter the new parent filter
	 */
	protected void setParentFilter(IQbeTreeEntityFilter parentFilter) {
		this.parentFilter = parentFilter;
	}

}
