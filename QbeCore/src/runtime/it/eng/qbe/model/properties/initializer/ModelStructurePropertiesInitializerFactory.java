/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.model.properties.initializer;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.datasource.hibernate.IHibernateDataSource;
import it.eng.qbe.datasource.jpa.IJpaDataSource;
import it.eng.qbe.datasource.jpa.JPADataSource;


/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class ModelStructurePropertiesInitializerFactory { 
	
	public static IModelStructurePropertiesInitializer getDataMartStructurePropertiesInitializer(IDataSource dataSource) {
		IModelStructurePropertiesInitializer initializer;
		
		initializer = null;
		
		if(dataSource instanceof IHibernateDataSource) {
			initializer = new SimpleModelStructurePropertiesInitializer((IHibernateDataSource)dataSource);
		} else if (dataSource instanceof JPADataSource) {
			initializer = new SimpleModelStructurePropertiesInitializer((IJpaDataSource)dataSource);
		} else {
			throw new RuntimeException("Impossible to load datamart structure from a datasource of type [" + dataSource.getClass().getName() + "]");
		}
		
		
		return initializer;		
	}
}
