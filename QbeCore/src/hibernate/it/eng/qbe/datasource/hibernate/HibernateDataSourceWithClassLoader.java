/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.datasource.hibernate;

import it.eng.qbe.classloader.ClassLoaderManager;
import it.eng.qbe.datasource.configuration.FileDataSourceConfiguration;
import it.eng.qbe.datasource.configuration.IDataSourceConfiguration;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.query.Query;
import it.eng.qbe.statement.IStatement;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */

public class HibernateDataSourceWithClassLoader extends  HibernateDataSource{

	protected static ClassLoader defoutlClassLoader;
	
	protected ClassLoader myClassLoader;
	
	public HibernateDataSourceWithClassLoader(String dataSourceName, IDataSourceConfiguration configuration) {
		super(dataSourceName, configuration);
		if(defoutlClassLoader==null){
			defoutlClassLoader = Thread.currentThread().getContextClassLoader();
		}else{
			Thread.currentThread().setContextClassLoader(defoutlClassLoader);
		}
		myClassLoader = defoutlClassLoader;
	}


	@Override
	protected void addDatamart(FileDataSourceConfiguration configuration, boolean extendClassLoader) {
		Configuration cfg = null;	
		SessionFactory sf = null;
		if(configuration.getFile() == null) return;
		
		cfg = buildEmptyConfiguration();
		configurationMap.put(configuration.getModelName(), cfg);
		
		if (extendClassLoader){
			myClassLoader = ClassLoaderManager.updateCurrentClassLoader(configuration.getFile());
		}	
		
		cfg.addJar(configuration.getFile());
		
		try {
			compositeHibernateConfiguration.addJar(configuration.getFile());
		} catch (Throwable t) {
			throw new RuntimeException("Cannot add datamart", t);
		}
		
		sf = cfg.buildSessionFactory();
		sessionFactoryMap.put(configuration.getModelName(), sf);		
	}
	
	@Override
	public IModelStructure getModelStructure() {
		Thread.currentThread().setContextClassLoader(myClassLoader);
		return super.getModelStructure();
	}

	@Override
	public IStatement createStatement(Query query) {
		Thread.currentThread().setContextClassLoader(myClassLoader);
		return super.createStatement(query);
	}
	
}