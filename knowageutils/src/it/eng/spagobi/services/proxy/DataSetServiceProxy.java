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
package it.eng.spagobi.services.proxy;

import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.services.dataset.stub.DataSetServiceServiceLocator;
import it.eng.spagobi.services.security.exceptions.SecurityException;
import it.eng.spagobi.tools.dataset.bo.DataSetFactory;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.utils.datamart.DefaultEngineDatamartRetriever;
import it.eng.spagobi.tools.dataset.utils.datamart.IQbeDataSetDatamartRetriever;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;

/**
 *
 * Proxy of Data Set Service
 *
 * @author Angelo Bernabei angelo.bernabei@eng.it
 *
 */
public final class DataSetServiceProxy extends AbstractServiceProxy {

	static private final String SERVICE_NAME = "DataSet Service";

	static private Logger logger = Logger.getLogger(DataSetServiceProxy.class);

	private MetamodelServiceProxy metamodelServiceProxy;

	public DataSetServiceProxy(String user, HttpSession session) {
		super(user, session);
		if (user == null)
			logger.error("User ID IS NULL....");
		if (session == null)
			logger.error("HttpSession IS NULL....");
	}

	private DataSetServiceProxy() {
		super();
	}

	public DataSetServiceProxy(String user, String secureAttributes, String serviceUrlStr, String spagoBiServerURL, String token, String pass) {
		super(user, secureAttributes, serviceUrlStr, spagoBiServerURL, token, pass);
		if (user == null)
			logger.error("User ID IS NULL....");
		if (pass == null)
			logger.error("pass IS NULL....");
		if (secureAttributes == null)
			logger.error("secureAttributes IS NULL....");
		if (serviceUrlStr == null)
			logger.error("serviceUrlStr NULL....");
		if (spagoBiServerURL == null)
			logger.error("spagoBiServerURL IS NULL....");
		if (token == null)
			logger.error("token IS NULL....");
	}

	public DataSetServiceProxy(String user, String secureAttributes, String serviceUrlStr, String spagoBiServerURL, String token, String pass,
			MetamodelServiceProxy metamodelServiceProxy) {
		this(user, secureAttributes, serviceUrlStr, spagoBiServerURL, token, pass);
		this.metamodelServiceProxy = metamodelServiceProxy;

	}

	public DataSetServiceProxy(String user, String secureAttributes, String serviceUrlStr, String spagoBiServerURL, String token, String pass,
			MetamodelServiceProxy metamodelServiceProxy, HttpSession session) {
		this(user, secureAttributes, serviceUrlStr, spagoBiServerURL, token, pass, metamodelServiceProxy);
		this.session = session;
	}

	private it.eng.spagobi.services.dataset.stub.DataSetService lookUp() throws SecurityException {
		try {
			DataSetServiceServiceLocator locator = new DataSetServiceServiceLocator();
			it.eng.spagobi.services.dataset.stub.DataSetService service = null;
			if (serviceUrl != null) {
				service = locator.getDataSetService(serviceUrl);
			} else {
				service = locator.getDataSetService();
			}
			return service;
		} catch (ServiceException e) {
			logger.error("Impossible to locate [" + SERVICE_NAME + "] at [" + serviceUrl + "]");
			throw new SecurityException("Impossible to locate [" + SERVICE_NAME + "] at [" + serviceUrl + "]", e);
		}
	}

	public IDataSet getDataSetByLabel(String label) {
		IDataSet dataSet = null;
		SpagoBiDataSet dataSetConfig = null;

		logger.debug("IN.dataset.label=" + label);

		if (label == null || label.length() == 0) {
			logger.error("dataset label is NULL");
			return null;
		}
		try {
			dataSetConfig = lookUp().getDataSetByLabel(readTicket(), userId, label);
			if (dataSetConfig != null) {
				// added userId and session parameters to manage correctly Qbe dataset configuration in DataSetFactory
				dataSet = DataSetFactory.getDataSet(dataSetConfig, userId, this.session);
				addMetaModelProxy(dataSet);
			}
		} catch (Exception e) {
			logger.error("Error during Service LookUp", e);
		} finally {
			logger.debug("OUT");
		}
		return dataSet;
	}

	public IDataSet saveDataSet(IDataSet dataset) {
		IDataSet toReturn = null;
		SpagoBiDataSet dataSetConfig = null;

		logger.debug("IN.dataset=" + dataset);

		if (dataset == null) {
			logger.error("Input dataset is NULL");
			return null;
		}
		try {
			dataSetConfig = dataset.toSpagoBiDataSet();
			SpagoBiDataSet returnedConfig = lookUp().saveDataSet(readTicket(), userId, dataSetConfig);
			if (returnedConfig != null) {
				// added userId and session parameters to manage correctly Qbe dataset configuration in DataSetFactory
				toReturn = DataSetFactory.getDataSet(returnedConfig, userId, this.session);
				addMetaModelProxy(toReturn);
			}
		} catch (Exception e) {
			logger.error("Error during Service LookUp", e);
		} finally {
			logger.debug("OUT");
		}
		return toReturn;
	}

	private void addMetaModelProxy(IDataSet dataSet) {
		// in case of qbe dataset, it need a MetamodelServiceProxy
		MetamodelServiceProxy proxy;
		if (this.session != null && this.metamodelServiceProxy == null) {
			proxy = new MetamodelServiceProxy(userId, this.session);
		} else {
			proxy = this.metamodelServiceProxy;
		}

		IQbeDataSetDatamartRetriever retriever = new DefaultEngineDatamartRetriever(proxy);
		Map parameters = dataSet.getParamsMap();
		if (parameters == null) {
			parameters = new HashMap();
			dataSet.setParamsMap(parameters);
		}
		dataSet.getParamsMap().put(SpagoBIConstants.DATAMART_RETRIEVER, retriever);
	}

	public IDataSet getDataSet(String documentId) {
		IDataSet dataSet = null;
		SpagoBiDataSet dataSetConfig = null;

		logger.debug("IN.documentId=" + documentId);

		if (documentId == null || documentId.length() == 0) {
			logger.error("documentId is NULL");
			return null;
		}
		try {
			dataSetConfig = lookUp().getDataSet(readTicket(), userId, documentId);
			if (dataSetConfig != null) {
				// added userId and session parameters to manage correctly Qbe dataset configuration in DataSetFactory
				dataSet = DataSetFactory.getDataSet(dataSetConfig, userId, this.session);
				addMetaModelProxy(dataSet);
			}
		} catch (Exception e) {
			logger.error("Error during Service LookUp", e);
		} finally {
			logger.debug("OUT");
		}
		return dataSet;
	}

}
