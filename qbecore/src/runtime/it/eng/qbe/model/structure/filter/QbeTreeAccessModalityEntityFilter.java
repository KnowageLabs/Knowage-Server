/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.model.structure.filter;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.model.structure.IModelEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class QbeTreeAccessModalityEntityFilter.
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class QbeTreeAccessModalityEntityFilter extends ComposableQbeTreeEntityFilter{

	/**
	 * Instantiates a new qbe tree access modality entity filter.
	 */
	public QbeTreeAccessModalityEntityFilter() {
		super();
	}
	
	/**
	 * Instantiates a new qbe tree access modality entity filter.
	 * 
	 * @param parentFilter the parent filter
	 */
	public QbeTreeAccessModalityEntityFilter(IQbeTreeEntityFilter parentFilter) {
		super(parentFilter);
	}
	
	public List filter(IDataSource dataSource, List entities) {
		List list = null;
		IModelEntity entity;
		
		list = new ArrayList();
		
		for(int i = 0; i < entities.size(); i++) {
			entity = (IModelEntity)entities.get(i);
			if( isEntityVisible(dataSource, entity)) {
				list.add(entity);
			}
		}
		
		return list;
	}
	
	/**
	 * Checks if is entity visible.
	 * 
	 * @param datamartModel the datamart model
	 * @param entity the entity
	 * 
	 * @return true, if is entity visible
	 */
	private boolean isEntityVisible(IDataSource dataSource, IModelEntity entity) {
		//DatamartProperties qbeProperties = dataSource.getDataMartProperties();
		
		if( !entity.getPropertyAsBoolean("visible") ) return false;
		if( !dataSource.getModelAccessModality().isEntityAccessible( entity ) ) return false;
		return true;
	}	
}
