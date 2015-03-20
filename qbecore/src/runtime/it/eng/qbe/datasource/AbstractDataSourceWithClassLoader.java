/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.datasource;

import it.eng.qbe.datasource.configuration.IDataSourceConfiguration;
import it.eng.qbe.model.accessmodality.IModelAccessModality;
import it.eng.qbe.model.properties.IModelProperties;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.query.Query;
import it.eng.qbe.statement.IStatement;

import java.util.Locale;
/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 * 
 * This Data source wraps a IDataSource and it's class loader..
 * I'ts useful when you have different models working in the same moment
 * 
 * It calls all the methods of the wrapped data source but before call them
 * it sets the myClassLoader as thread class loader
 */

public abstract class AbstractDataSourceWithClassLoader implements IDataSource{
	
	protected static ClassLoader defoutlClassLoader;
	
	protected ClassLoader myClassLoader;

	//The wrapped data source
	protected IDataSource wrappedDataSource;
	
	public AbstractDataSourceWithClassLoader(IDataSource wrappedDataSource){
		if(defoutlClassLoader==null){
			defoutlClassLoader = Thread.currentThread().getContextClassLoader();
		}else{
			Thread.currentThread().setContextClassLoader(defoutlClassLoader);
		}
		myClassLoader = defoutlClassLoader;
		if(wrappedDataSource instanceof AbstractDataSourceWithClassLoader){
			this.wrappedDataSource = ((AbstractDataSourceWithClassLoader) wrappedDataSource).getWrappedDataSource();
		}
		this.wrappedDataSource = wrappedDataSource;
	}
	
	public String getName() {
		return wrappedDataSource.getName();
	}

	public IDataSourceConfiguration getConfiguration() {
		return wrappedDataSource.getConfiguration();
	}

	public IModelStructure getModelStructure() {
		Thread.currentThread().setContextClassLoader(myClassLoader);
		return wrappedDataSource.getModelStructure();
	}

	public IModelAccessModality getModelAccessModality() {
		return wrappedDataSource.getModelAccessModality();
	}

	public void setDataMartModelAccessModality(
			IModelAccessModality modelAccessModality) {
		wrappedDataSource.setDataMartModelAccessModality(modelAccessModality);
		
	}

	public IModelProperties getModelI18NProperties(Locale locale) {
		return wrappedDataSource.getModelI18NProperties(locale);
	}

	public boolean isOpen() {
		return wrappedDataSource.isOpen();
	}

	public void close() {
		wrappedDataSource.close();
	}

	public IStatement createStatement(Query query) {
		Thread.currentThread().setContextClassLoader(myClassLoader);
		return wrappedDataSource.createStatement(query);
	}

	public IDataSource getWrappedDataSource() {
		return wrappedDataSource;
	}

	public void setWrappedDataSource(IDataSource wrappedDataSource) {
		this.wrappedDataSource = wrappedDataSource;
	}
		
	
	
}