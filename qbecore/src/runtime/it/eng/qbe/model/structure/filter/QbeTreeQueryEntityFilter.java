/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.model.structure.filter;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.query.Query;
import it.eng.qbe.statement.IStatement;

import java.util.List;
import java.util.Set;

// TODO: Auto-generated Javadoc
/**
 * The Class QbeTreeOrderEntityFilter.
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class QbeTreeQueryEntityFilter extends QbeTreeWhiteListEntityFilter {

	private Query query;
	
	/**
	 * Instantiates a new qbe tree order entity filter.
	 */
	public QbeTreeQueryEntityFilter() {
		super();
	}
	
	public QbeTreeQueryEntityFilter(IQbeTreeEntityFilter parentFilter, Query query) {
		super(parentFilter, null);
		this.setQuery( query );
	}
	
	public List filter(IDataSource dataSource, List entities) {
		List list = null;
		
		IStatement statement = dataSource.createStatement(getQuery());
		Set whiteList = statement.getSelectedEntities();
		setWhiteList(whiteList);
		list = super.filter(dataSource, entities);
		return list;
	}
	
	public Query getQuery() {
		return query;
	}

	public void setQuery(Query query) {
		this.query = query;
	}

	
	
}
