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
package it.eng.spagobi.engines.qbe.services.initializers;

import it.eng.qbe.dataset.QbeDataSet;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.engines.qbe.template.QbeXMLTemplateParser;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.persist.IDataSetTableDescriptor;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class FormEngineFromDatasetStartAction extends FormEngineStartAction {	
	
	// INPUT PARAMETERS
	private final static String IS_NEW_DOCUMENT = "IS_NEW_DOCUMENT";
	
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(FormEngineFromDatasetStartAction.class);
    
    private IDataSet dataSet;
 
	@Override
	public IDataSet getDataSet() {
		logger.debug("IN");
		if (dataSet == null) {
			// dataset information is coming with the request
			String datasetLabel = this.getAttributeAsString( QbeEngineFromFederationStartAction.DATASET_LABEL );
			logger.debug("Parameter [" + QbeEngineFromFederationStartAction.DATASET_LABEL + "]  is equal to [" + datasetLabel + "]");
			Assert.assertNotNull(datasetLabel, "Dataset not specified");
			dataSet = getDataSetServiceProxy().getDataSetByLabel(datasetLabel);  
			Assert.assertNotNull(dataSet, "Dataset with label [" + datasetLabel + "] not found");
		}
		logger.debug("OUT");
		return dataSet;
	}
	
	@Override
	public Map addDatasetsToEnv() {
		Map env = super.getEnv();
		env.put(EngineConstants.ENV_LOCALE, getLocale());
		String datasetLabel = this.getAttributeAsString( QbeEngineFromFederationStartAction.DATASET_LABEL );
		logger.debug("Parameter [" + QbeEngineFromFederationStartAction.DATASET_LABEL + "] is equals to [" + datasetLabel + "]");
		Assert.assertNotNull(datasetLabel, "Missing dataset label");
		
		env.put(EngineConstants.ENV_DATASET_LABEL, datasetLabel);

		IDataSet dataset = this.getDataSet();
		
		// substitute default engine's datasource with dataset one
		IDataSource dataSource = dataset.getDataSource();
		if (dataSource == null) {
			logger.debug("Dataset has no datasource.");
		} else {
			env.put(EngineConstants.ENV_DATASOURCE, dataSource);
		}
		
		IDataSetTableDescriptor descriptor = this.persistDataset(dataset, env);
		if (dataset instanceof QbeDataSet) {
			adjustMetadataForQbeDataset((QbeDataSet) dataset, descriptor);
		}

		List<IDataSet> dataSets = new ArrayList<IDataSet>();
		dataSets.add(dataset);
		env.put(EngineConstants.ENV_DATASETS, dataSets);
		return env;
	}
	
	/**
	 * This method solves the following issue: SQLDataSet defines the SQL
	 * statement directly considering the names' of the wrapped dataset fields,
	 * but, in case of QbeDataSet, the fields' names are
	 * "it.eng.spagobi......Entity.fieldName" and not the name of the
	 * persistence table!!! We modify the dataset's metadata in order to fix
	 * this.
	 * 
	 * @param dataset
	 *            The persisted Qbe dataset
	 * @param descriptor
	 *            The persistence table descriptor
	 */
//	TODO move this logic inside the SQLDataSet: when building the
//	SQL statement, the SQLDataSet should get the columns' names
//	from the IDataSetTableDescriptor. Replace
//	IDataSet.getPersistTableName with
//	IDataSet.getPersistTableDescriptor in order to permit the
//	IDataSetTableDescriptor to go with its dataset.
//	TODO merge with it.eng.spagobi.engines.worksheet.services.initializers.WorksheetEngineStartAction.adjustMetadataForQbeDataset
	private void adjustMetadataForQbeDataset(QbeDataSet dataset,
			IDataSetTableDescriptor descriptor) {
		IMetaData metadata = dataset.getMetadata();
		int columns = metadata.getFieldCount();
		for (int i = 0; i < columns; i++) {
			IFieldMetaData fieldMetadata = metadata.getFieldMeta(i);
			String newName = descriptor.getColumnName(fieldMetadata
					.getName());
			fieldMetadata.setName(newName);
			fieldMetadata.setProperty("uniqueName", newName);
		}
		dataset.setMetadata(metadata);
	}
	
	@Override
	public String getTemplateAsString() {
		String template = null;
		boolean isNewDocument = this.getAttributeAsBoolean( IS_NEW_DOCUMENT );
		logger.debug("Parameter [" + IS_NEW_DOCUMENT + "] is equals to [" + isNewDocument + "]");
		if (isNewDocument) {
			SourceBean sourceBean;
			try {
				sourceBean = new SourceBean( QbeXMLTemplateParser.TAG_ROOT_SMART_FILTER );
			} catch (SourceBeanException e) {
				throw new SpagoBIEngineRuntimeException("Error while initializing a new template", e);
			}
			template = sourceBean.toXML();
		} else {
			template = super.getTemplateAsString();
		}
		return template;
	}
	
}
