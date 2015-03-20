/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.datasource;

import it.eng.qbe.classloader.ClassLoaderManager;
import it.eng.qbe.datasource.configuration.IDataSourceConfiguration;
import it.eng.qbe.model.accessmodality.IModelAccessModality;
import it.eng.qbe.model.properties.IModelProperties;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.query.Query;
import it.eng.qbe.statement.IStatement;
import it.eng.qbe.statement.StatementFactory;
import it.eng.spagobi.tools.datasource.bo.DataSource;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia
 */
public abstract class AbstractDataSource implements IDataSource {
	
	protected String name;
	protected IDataSourceConfiguration configuration;
	
	protected IModelAccessModality dataMartModelAccessModality;
	protected IModelStructure dataMartModelStructure;

	protected Map<String, IModelProperties> modelPropertiesCache;		
	
	private static transient Logger logger = Logger.getLogger(AbstractDataSource.class);
	
	public IDataSourceConfiguration getConfiguration() {
		return configuration;
	}

	
	public IStatement createStatement(Query query) {
		return StatementFactory.createStatement(this, query);
	}
	
	public IModelAccessModality getModelAccessModality() {
		return dataMartModelAccessModality;
	}

	public void setDataMartModelAccessModality(
			IModelAccessModality dataMartModelAccessModality) {
		this.dataMartModelAccessModality = dataMartModelAccessModality;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public IModelProperties getModelI18NProperties(Locale locale) {
		IModelProperties properties;
		
		if(modelPropertiesCache == null) {
			modelPropertiesCache = new HashMap<String, IModelProperties>();
		}
		
		String key = name + ":" + "labels";
		if(locale != null) {
			key += "_" + locale.getLanguage();
		}
		
		properties = modelPropertiesCache.get(key);
		
		if(properties == null) {			
			properties = getConfiguration().loadModelI18NProperties(locale);
			modelPropertiesCache.put(key, properties);
		} else {
			logger.debug("i18n properties loaded form cache");
		}
		
		return properties;
	}
	
	protected static void updateCurrentClassLoader(File jarFile){
		ClassLoaderManager.updateCurrentWebClassLoader(jarFile);
	}
	
	public abstract it.eng.spagobi.tools.datasource.bo.IDataSource getToolsDataSource();
	
}
