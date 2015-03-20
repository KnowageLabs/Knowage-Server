/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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



