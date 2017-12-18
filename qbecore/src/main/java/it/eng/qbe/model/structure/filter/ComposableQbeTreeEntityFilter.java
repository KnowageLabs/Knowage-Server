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
