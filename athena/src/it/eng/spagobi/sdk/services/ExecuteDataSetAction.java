/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.sdk.services;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.commons.utilities.SpagoBIServiceExceptionHandler;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.services.dataset.service.DataSetSupplier;
import it.eng.spagobi.tools.dataset.bo.DataSetFactory;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class ExecuteDataSetAction extends AbstractSpagoBIAction {

	public static final String SERVICE_NAME = "EXECUTE_DATASET";

	// request parameters
	public static String DATASET_LABEL = "label";
	public static String CALLBACK = "callback";

	// logger component
	private static Logger logger = Logger.getLogger(ExecuteDataSetAction.class);

	@Override
	public void doService() {

		String dataSetLabel;
		String callback;
		DataSetSupplier dataSetSupplier;
		SpagoBiDataSet dataSetConfig;
		IDataSet dataSet;
		IDataStore dataStore;
		JSONObject dataSetJSON;

		logger.debug("IN");

		try {

			dataSetLabel = getAttributeAsString(DATASET_LABEL);
			logger.debug("Parameter [" + DATASET_LABEL + "] is equals to [" + dataSetLabel + "]");
			Assert.assertTrue(!StringUtilities.isEmpty(dataSetLabel), "Parameter [" + DATASET_LABEL + "] cannot be null or empty");

			callback = getAttributeAsString(CALLBACK);
			logger.debug("Parameter [" + CALLBACK + "] is equals to [" + callback + "]");

			dataSetConfig = null;
			try {
				dataSetSupplier = new DataSetSupplier();
				dataSetConfig = dataSetSupplier.getDataSetByLabel(dataSetLabel);
			} catch (Throwable t) {
				throw new SpagoBIServiceException("Impossible to find a dataset whose label is [" + dataSetLabel + "]", t);
			}
			Assert.assertNotNull(dataSetConfig, "Impossible to find a dataset whose label is [" + dataSetLabel + "]");

			// START -> section added to manage the QBE datamart retriever in DataSetFactory

			UserProfile userProfile = (UserProfile) this.getUserProfile();

			Assert.assertNotNull(userProfile, "Impossible to find the user profile");

			String userId = (String) userProfile.getUserId();

			Assert.assertNotNull(userId, "Impossible to find the userId in user profile [" + userProfile + "]");

			HttpServletRequest httpRequest = getHttpRequest();

			Assert.assertNotNull(httpRequest, "Impossible to find a valid HTTP Servlet Request");

			HttpSession session = httpRequest.getSession();

			// END

			// added userId and session parameters to manage correctly Qbe dataset configuration in DataSetFactory
			dataSet = DataSetFactory.getDataSet(dataSetConfig, userId, session);
			// checkQbeDataset(dataSet);
			dataSet.loadData();
			dataStore = dataSet.getDataStore();
			Assert.assertNotNull(dataStore, "The dataStore returned by loadData method of the class [" + dataSet.getClass().getName() + "] cannot be null");

			dataSetJSON = null;
			try {
				dataSetJSON = (JSONObject) SerializerFactory.getSerializer("application/json").serialize(dataStore, null);
			} catch (SerializationException e) {
				throw new SpagoBIServiceException("Impossible to serialize datastore", e);
			}

			try {
				writeBackToClient(new JSONSuccess(dataSetJSON, callback));
			} catch (IOException e) {
				throw new SpagoBIServiceException("Impossible to write back the responce to the client", e);
			}
		} catch (Throwable t) {
			throw SpagoBIServiceExceptionHandler.getInstance().getWrappedException(SERVICE_NAME, t);
		} finally {
			logger.debug("OUT");
		}
	}

	// private void checkQbeDataset(IDataSet dataSet) {
	// if (dataSet instanceof QbeDataSet) {
	// SpagoBICoreDatamartRetriever retriever = new SpagoBICoreDatamartRetriever();
	// Map parameters = dataSet.getParamsMap();
	// if (parameters == null) {
	// parameters = new HashMap();
	// dataSet.setParamsMap(parameters);
	// }
	// dataSet.getParamsMap().put(SpagoBIConstants.DATAMART_RETRIEVER, retriever);
	// }
	// }

}
