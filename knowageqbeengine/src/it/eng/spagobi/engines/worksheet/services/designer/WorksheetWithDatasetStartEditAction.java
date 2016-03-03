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
package it.eng.spagobi.engines.worksheet.services.designer;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.worksheet.services.initializers.WorksheetEngineStartAction;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;

import org.apache.log4j.Logger;

/**
 * @authors Davide Zerbetto (davide.zerbetto@eng.it), Alberto Ghedin (alberto.ghedin@eng.it)
 */
public class WorksheetWithDatasetStartEditAction extends WorksheetEngineStartAction{ //WorksheetEngineStartAction {	

	private static final long serialVersionUID = 6272194014941617286L;

	// INPUT PARAMETERS
	public static final String DATASET_LABEL = "dataset_label";
	public static final String ENGINE_DATASOURCE_LABEL = "ENGINE_DATASOURCE_LABEL";
	
	/** Logger component. */
	private static transient Logger logger = Logger.getLogger(WorksheetWithDatasetStartEditAction.class);

	private IDataSet dataSet;

//	public void service(SourceBean serviceRequest, SourceBean serviceResponse) {
//		logger.debug("IN");
//		super.service(serviceRequest, serviceResponse);
//
//		// publisher for the qbe edit
//		String publisherName = "WORKSHEET_START_EDIT_ACTION_DATASET_PUBLISHER";
//
//		try {
//			serviceResponse.setAttribute(DynamicPublisher.PUBLISHER_NAME,
//					publisherName);
//		} catch (SourceBeanException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//
//		logger.debug("OUT");
//	}

//	public Map getEnv() {
//		Map env = new HashMap();
//
//		copyRequestParametersIntoEnv(env, getSpagoBIRequestContainer());
//		//env.put(EngineConstants.ENV_DATASOURCE, getDataSource());
//		// document id can be null (when using QbE for dataset definition)
//		//		   if (getDocumentId() != null) {
//		//			   env.put(EngineConstants.ENV_DOCUMENT_ID, getDocumentId());
//		//		   }
//		env.put(EngineConstants.ENV_USER_PROFILE, getUserProfile());
//		// env.put(EngineConstants.ENV_CONTENT_SERVICE_PROXY, getContentServiceProxy());
//		env.put(EngineConstants.ENV_AUDIT_SERVICE_PROXY, getAuditServiceProxy() );
//		env.put(EngineConstants.ENV_DATASET_PROXY, getDataSetServiceProxy()); 
//		env.put(EngineConstants.ENV_LOCALE, getLocale()); 
//
//		return env;
//	}

	@Override
    protected boolean goToWorksheetPreentation() {
		return true;
	}
	
	@Override
	public IDataSet getDataSet() {
		if (dataSet == null) {
			// dataset information is coming with the request
			String datasetLabel = this.getAttributeAsString( DATASET_LABEL );
			logger.debug("Parameter [" + DATASET_LABEL + "]  is equal to [" + datasetLabel + "]");
			Assert.assertNotNull(datasetLabel, "Dataset not specified");
			dataSet = getDataSetServiceProxy().getDataSetByLabel(datasetLabel);
		}
		return dataSet;
	}

	@Override
	public IDataSource getDataSource() {
		IDataSet dataset = this.getDataSet();
		IDataSource datasetDatasource = dataset.getDataSource();
		if (datasetDatasource == null) {
			logger.debug("Dataset has no datasouce. Getting the engine's datasource...");
			String datasourceLabel = this.getAttributeAsString( ENGINE_DATASOURCE_LABEL );
			logger.debug("Parameter [" + ENGINE_DATASOURCE_LABEL + "]  is equal to [" + datasourceLabel + "]");
			if (datasourceLabel != null) {
				datasetDatasource = getDataSourceServiceProxy().getDataSourceByLabel(datasourceLabel);
			}
		}
		return datasetDatasource;
	}

	@Override
	public SourceBean getTemplateAsSourceBean() {
		// we must start with an empty template
		return null;
	}

	@Override
	public String getDocumentId() {
		// there is no document at the time
		return null;
	}

}