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
package it.eng.qbe.datasource.configuration;


import java.util.Locale;
import java.util.Properties;

import it.eng.qbe.model.properties.IModelProperties;
import it.eng.qbe.model.properties.SimpleModelProperties;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class DataSetDataSourceConfiguration extends AbstractDataSourceConfiguration {
	
	IDataSet dataset;
	
	public DataSetDataSourceConfiguration(String modelName, IDataSet dataset) {
		super(modelName);
		this.dataset = dataset;

	}

	public IDataSet getDataset() {
		return dataset;
	}

	@Override
	public IModelProperties loadModelI18NProperties() {
		Properties props = this.getProperties();
		SimpleModelProperties toReturn = new SimpleModelProperties(props);
		return toReturn;
	}

	private Properties getProperties() {
		Properties props = new Properties();
		String entityName = dataset.getTableNameForReading();
		props.setProperty(entityName + "//" + entityName + ".label", dataset.getName());
		
		IMetaData datasetMetadata = dataset.getMetadata();
		
		for (int i = 0; i < datasetMetadata.getFieldCount(); i++) {
			IFieldMetaData fieldMetadata = datasetMetadata.getFieldMeta(i);
			String fieldUniqueName = entityName + "/" + fieldMetadata.getName();
			String alias = fieldMetadata.getAlias();
			if (alias == null || alias.equals("")) {
				alias = fieldMetadata.getName();
			}
			props.setProperty(fieldUniqueName + ".label", alias);
		}
		
		return props;
	}

	@Override
	public IModelProperties loadModelI18NProperties(Locale locale) {
		return this.loadModelI18NProperties();
	}

}



