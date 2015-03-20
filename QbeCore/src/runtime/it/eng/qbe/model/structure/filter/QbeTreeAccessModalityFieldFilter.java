/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.model.structure.filter;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.model.structure.IModelField;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class QbeTreeAccessModalityFieldFilter.
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class QbeTreeAccessModalityFieldFilter extends ComposableQbeTreeFieldFilter {
	
	/** The parent filter. */
	private IQbeTreeFieldFilter parentFilter;
	
	/**
	 * Instantiates a new qbe tree access modality field filter.
	 */
	public QbeTreeAccessModalityFieldFilter() {
		parentFilter = null;
	}
	
	/**
	 * Instantiates a new qbe tree access modality field filter.
	 * 
	 * @param parentFilter the parent filter
	 */
	public QbeTreeAccessModalityFieldFilter(IQbeTreeFieldFilter parentFilter) {
		setParentFilter(parentFilter);
	}
	
	
	public List filter(IDataSource dataSource, List fields) {
		List list;
		IModelField field;
		
		list = new ArrayList();
		
		for(int i = 0; i < fields.size(); i++) {
			field = (IModelField)fields.get(i);
			if( isFieldVisible(dataSource, field)) {
				list.add(field);
			}
		}
		
		return list;
	}
	
	/**
	 * Checks if is field visible.
	 * 
	 * @param datamartModel the datamart model
	 * @param field the field
	 * 
	 * @return true, if is field visible
	 */
	private boolean isFieldVisible(IDataSource dataSource, IModelField field) {
		//DatamartProperties qbeProperties = dataSource.getDataMartProperties();
		
		if( !field.getPropertyAsBoolean("visible") ) return false;
		if( !dataSource.getModelAccessModality().isFieldAccessible( field ) )return false;
		return true;
	}
}
