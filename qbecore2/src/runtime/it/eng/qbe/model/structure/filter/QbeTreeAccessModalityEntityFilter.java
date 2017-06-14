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
