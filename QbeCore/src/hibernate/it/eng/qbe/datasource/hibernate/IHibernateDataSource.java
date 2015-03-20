/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.datasource.hibernate;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.datasource.configuration.IDataSourceConfiguration;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * The Interface IHibernateDataSource.
 */
public interface IHibernateDataSource extends IDataSource {
	
	List<IDataSourceConfiguration> getSubConfigurations();
	
	Configuration getHibernateConfiguration();	
	SessionFactory getHibernateSessionFactory();
	SessionFactory getHibernateSessionFactory(String dmName);	
}