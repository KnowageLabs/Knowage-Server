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
import java.util.Set;

// TODO: Auto-generated Javadoc
/**
 * The Class QbeTreeOrderEntityFilter.
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class QbeTreeWhiteListEntityFilter extends ComposableQbeTreeEntityFilter{

	private Set whiteList;
	
	public QbeTreeWhiteListEntityFilter() {
		super();
	}
	
	public QbeTreeWhiteListEntityFilter(IQbeTreeEntityFilter parentFilter, Set whiteList) {
		super(parentFilter);
		this.setWhiteList( whiteList );
	}
	
	public List filter(IDataSource dataSource, List entities) {
		List list = null;
		IModelEntity entity;

		list = new ArrayList();
		for(int i = 0; i < entities.size(); i++) {
			entity = (IModelEntity)entities.get(i);
			if(entity.getParent() == null) {
				if(getWhiteList().contains(entity)) {
					list.add(entity);
				}
			} else {
				list.add(entity);
			}
			
		}
		
		return list;
	}
	
	public Set getWhiteList() {
		return whiteList;
	}

	public void setWhiteList(Set whiteList) {
		this.whiteList = whiteList;
	}

	
}
