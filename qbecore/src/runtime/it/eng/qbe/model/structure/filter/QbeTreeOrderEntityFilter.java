/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.model.structure.filter;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.model.structure.IModelEntity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The Class QbeTreeAccessModalityFieldFilter.
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class QbeTreeOrderEntityFilter extends ComposableQbeTreeEntityFilter {
	
	
	/** The parent filter. */
	private IQbeTreeFieldFilter parentFilter;
	
	/**
	 * Instantiates a new qbe tree access modality field filter.
	 */
	public QbeTreeOrderEntityFilter() {
		parentFilter = null;
	}
	
	/**
	 * Instantiates a new qbe tree access modality field filter.
	 * 
	 * @param parentFilter the parent filter
	 */
	public QbeTreeOrderEntityFilter(IQbeTreeEntityFilter parentFilter) {
		setParentFilter(parentFilter);
	}
	
	
	public List filter(IDataSource dataSource, List fields) {
		
		Collections.sort(fields, new Comparator() {
		    public int compare(Object o1, Object o2) {
		    	IModelEntity f1, f2;
		    	String p1, p2;
		    	int i1, i2;
		    	
		    	f1 = (IModelEntity)o1;
		    	f2 = (IModelEntity)o2;
		    	/*
		    	p1 = properties.getProperty(f1, "position");
		    	p2 = properties.getProperty(f2, "position");
		    	
		    	try {
		    		i1 = Integer.parseInt(p1);
		    	} catch(Throwable t) {
		    		i1 = Integer.MAX_VALUE;
		    	}
		    	
		    	try {
		    		i2 = Integer.parseInt(p2);
		    	} catch(Throwable t) {
		    		i2 = Integer.MAX_VALUE;
		    	}
		    	*/
		    	//Assert.assertTrue(i1 == f1.getPropertyAsInt("position"), "Position attribute of entity [" + f1.getName() + "]");
		    	//Assert.assertTrue(i2 == f2.getPropertyAsInt("position"), "Position attribute of entity [" + f2.getName() + "]");
		    	
		    	i1 = f1.getPropertyAsInt("position");
		    	i2 = f2.getPropertyAsInt("position");
		        
		    	return (i1 < i2 ? -1 :
		                (i1 == i2 ? 0 : 1));
		    }
		});
		
		return fields;
	}
}
