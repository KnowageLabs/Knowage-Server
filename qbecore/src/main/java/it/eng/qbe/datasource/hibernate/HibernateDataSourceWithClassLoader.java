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