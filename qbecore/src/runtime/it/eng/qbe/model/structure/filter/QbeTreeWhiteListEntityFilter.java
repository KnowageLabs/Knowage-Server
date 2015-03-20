/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
