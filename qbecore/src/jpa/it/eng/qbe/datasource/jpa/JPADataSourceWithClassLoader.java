/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.datasource.jpa;

import java.io.File;

import it.eng.qbe.classloader.ClassLoaderManager;
import it.eng.qbe.datasource.configuration.FileDataSourceConfiguration;
import it.eng.qbe.datasource.configuration.IDataSourceConfiguration;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.query.Query;
import it.eng.qbe.statement.IStatement;

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */

public class JPADataSourceWithClassLoader extends JPADataSource{
	
	protected static ClassLoader defoutlClassLoader;
	
	protected ClassLoader myClassLoader;

	public JPADataSourceWithClassLoader(String dataSourceName, IDataSourceConfiguration configuration){
		super(dataSourceName, configuration);
		if(defoutlClassLoader==null){
			defoutlClassLoader = Thread.currentThread().getContextClassLoader();
		}else{
			Thread.currentThread().setContextClassLoader(defoutlClassLoader);
		}
		myClassLoader = defoutlClassLoader;
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

	@Override
	public void open() {
		File jarFile = null;
		
		FileDataSourceConfiguration configuration = getFileDataSourceConfiguration();
		
		jarFile = configuration.getFile();
		if(jarFile == null) return;
		
		myClassLoader = ClassLoaderManager.updateCurrentClassLoader(jarFile);
		
		super.initEntityManagerFactory( getConfiguration().getModelName() );
		
	}


		
	
	
}
