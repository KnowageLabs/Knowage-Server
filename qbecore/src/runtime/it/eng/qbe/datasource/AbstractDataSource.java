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
package it.eng.qbe.datasource;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import it.eng.qbe.classloader.ClassLoaderManager;
import it.eng.qbe.datasource.configuration.IDataSourceConfiguration;
import it.eng.qbe.model.accessmodality.IModelAccessModality;
import it.eng.qbe.model.properties.IModelProperties;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.query.Query;
import it.eng.qbe.statement.IStatement;
import it.eng.qbe.statement.StatementFactory;
import it.eng.spagobi.commons.bo.UserProfile;

/**
 * @author Andrea Gioia
 */
public abstract class AbstractDataSource implements IDataSource {
	public static final String ALL_FIELDS_ACCESSIBLE = "_ALL_FIELD_ACCESSIBLE_";

	protected String name;
	protected IDataSourceConfiguration configuration;

	protected IModelAccessModality dataMartModelAccessModality;
	protected IModelStructure dataMartModelStructure;

	protected Map<String, IModelProperties> modelPropertiesCache;

	private static transient Logger logger = Logger.getLogger(AbstractDataSource.class);

	@Override
	public IDataSourceConfiguration getConfiguration() {
		return configuration;
	}

	@Override
	public IStatement createStatement(Query query) {
		return StatementFactory.createStatement(this, query);
	}

	@Override
	public IModelAccessModality getModelAccessModality() {
		return dataMartModelAccessModality;
	}

	@Override
	public void setDataMartModelAccessModality(IModelAccessModality dataMartModelAccessModality) {
		this.dataMartModelAccessModality = dataMartModelAccessModality;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public IModelProperties getModelI18NProperties(Locale locale) {
		IModelProperties properties;

		if (modelPropertiesCache == null) {
			modelPropertiesCache = new HashMap<String, IModelProperties>();
		}

		String key = name + ":" + "labels";
		if (locale != null) {
			key += "_" + locale.getLanguage();
		}

		properties = modelPropertiesCache.get(key);

		if (properties == null) {
			properties = getConfiguration().loadModelI18NProperties(locale);
			modelPropertiesCache.put(key, properties);
		} else {
			logger.debug("i18n properties loaded form cache");
		}

		return properties;
	}

	protected static void updateCurrentClassLoader(File jarFile) {
		ClassLoaderManager.updateCurrentWebClassLoader(jarFile);
	}

	public abstract it.eng.spagobi.tools.datasource.bo.IDataSource getToolsDataSource();

	@Override
	public IModelStructure getModelStructure(UserProfile profile) {
		return getModelStructure();
	}

	@Override
	public IModelStructure getModelStructure(boolean getFullModel) {
		return getModelStructure();
	}

}
