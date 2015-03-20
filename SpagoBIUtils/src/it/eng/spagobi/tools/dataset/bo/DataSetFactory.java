/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.bo;

import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.datasource.bo.DataSourceFactory;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 * 
 */
public class DataSetFactory {

	private static transient Logger logger = Logger.getLogger(DataSetFactory.class);

	public static IDataSet getDataSet(SpagoBiDataSet dataSetConfig) {
		IDataSet dataSet = null;

		if (dataSetConfig == null) {
			throw new IllegalArgumentException("dataset-config parameter cannot be null");
		}

		InputStream source = DataSetFactory.class.getResourceAsStream("/datasetTypes.properties");
		Properties p = new Properties();
		try {
			p.load(source);
		} catch (IOException e) {
			throw new SpagoBIRuntimeException("Cannot load configuration from datasetTypes.properties file", e);
		}
		String dsType = dataSetConfig.getType();
		String className = p.getProperty(dsType);

		logger.debug("Dataset type: " + dsType);
		if (className.equals(JDBCDataSet.class.getName())) {
			try {
				IDataSource ds = DataSourceFactory.getDataSource(dataSetConfig.getDataSource());
				if ((ds.getHibDialectName()).toLowerCase().contains("hive")) {
					className = JDBCHiveDataSet.class.getName();
				} else if ((ds.getHibDialectClass()).toLowerCase().contains("mongo")) {
					className = MongoDataSet.class.getName();
				}

			} catch (Exception e) {
				throw new RuntimeException("Missing right exstension", e);
			}
		}

		logger.debug("Dataset class: " + className);
		if (className == null) {
			throw new SpagoBIRuntimeException("No dataset class found for dataset type [" + dsType + "]");
		}
		Constructor c = null;
		Object object = null;
		if (className.endsWith("JDBCDataSet")) {
			String dialect = dataSetConfig.getDataSource().getHibDialectName();
			if (dialect.contains("hbase")) {
				className = JDBCHBaseDataSet.class.getName();
			} else if (dialect.contains("hive")) {
				className = JDBCHiveDataSet.class.getName();
			} else if (dialect.contains("orient")) {
				className = JDBCOrientDbDataSet.class.getName();
			}

		}
		try {
			c = Class.forName(className).getConstructor(SpagoBiDataSet.class);
			object = c.newInstance(dataSetConfig);
			dataSet = (IDataSet) object;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while instantiating dataset type [" + dsType
					+ "], class [" + className + "]", e);
		}

		// if custom data set type try instantiate the referred class
		IDataSet customDataset = dataSet;
		if (CustomDataSet.DS_TYPE.equals((dataSetConfig.getType()))
				&& customDataset instanceof CustomDataSet) {
			try {
				dataSet = ((CustomDataSet) customDataset).instantiate();
			} catch (Exception e) {
				logger.error("Cannot instantiate class " + ((CustomDataSet) customDataset).getJavaClassName() + ": go on with CustomDatasetClass");
			}
		}

		// if ( ScriptDataSet.DS_TYPE.equals( dataSetConfig.getType() ) ) {
		// dataSet = new ScriptDataSet( dataSetConfig );
		// } else if ( JDBCDataSet.DS_TYPE.equals( dataSetConfig.getType() ) ) {
		// dataSet = new JDBCDataSet( dataSetConfig );
		// } else if ( JavaClassDataSet.DS_TYPE.equals( dataSetConfig.getType()
		// ) ) {
		// dataSet = new JavaClassDataSet( dataSetConfig );
		// } else if ( WebServiceDataSet.DS_TYPE.equals( dataSetConfig.getType()
		// ) ) {
		// dataSet = new WebServiceDataSet( dataSetConfig );
		// } else if ( FileDataSet.DS_TYPE.equals( dataSetConfig.getType() ) ) {
		// dataSet = new FileDataSet( dataSetConfig );
		// } else {
		// logger.error("Invalid dataset type [" + dataSetConfig.getType() +
		// "]");
		// throw new
		// IllegalArgumentException("dataset type in dataset-config cannot be equal to ["
		// + dataSetConfig.getType() + "]");
		// }

		return dataSet;
	}
}
