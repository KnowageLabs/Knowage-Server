/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.service;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.UserProfileUtils;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ExecuteDatasets extends AbstractSpagoBIAction {

	// logger component
	public static Logger logger = Logger.getLogger(ExecuteDatasets.class);

	@Override
	public void doService() {
		logger.debug("IN");
		IDataSetDAO dsDao;
		IEngUserProfile profile = getUserProfile();
		try {
			dsDao = DAOFactory.getDataSetDAO();
			dsDao.setUserProfile(profile);
		} catch (EMFUserError e1) {
			logger.error(e1.getMessage(), e1);
			throw new SpagoBIServiceException(SERVICE_NAME, "Error occurred");
		}
		Locale locale = getLocale();
		String serviceType = this.getAttributeAsString(DataSetConstants.MESSAGE_DET);
		logger.debug("Service type " + serviceType);

		if (serviceType != null && serviceType.equalsIgnoreCase(DataSetConstants.DATASET_EXEC)) {
			try {
				Integer dsId = getAttributeAsInteger(DataSetConstants.ID);
				JSONObject dataSetJSON = getJSONDatasetResult(dsId, profile);
				if (dataSetJSON != null) {
					try {
						writeBackToClient(new JSONSuccess(dataSetJSON));
					} catch (IOException e) {
						throw new SpagoBIServiceException("Impossible to write back the responce to the client", e);
					}
				} else {
					throw new SpagoBIServiceException(SERVICE_NAME, "No data found");
				}
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
				throw new SpagoBIServiceException(SERVICE_NAME, "sbi.ds.testError", e);
			}
		}
		logger.debug("OUT");
	}

	public JSONObject getJSONDatasetResult(Integer dsId, IEngUserProfile profile) {
		logger.debug("IN");
		JSONObject dataSetJSON = null;
		// Integer id = obj.getDataSetId();
		// gets the dataset object informations
		try {
			IDataSet dataset = DAOFactory.getDataSetDAO().loadDataSetById(dsId);
			if (dataset.getParameters() != null) {
				// JSONArray parsJSON = serializeJSONArrayParsList(dataset.getParameters());
				JSONArray parsJSON = getAttributeAsJSONArray(DataSetConstants.PARS);
				HashMap h = new HashMap();
				if (parsJSON != null && parsJSON.length() > 0) {
					h = deserializeParValuesListJSONArray(parsJSON);
				}
				dataSetJSON = getDatasetTestResultList(dataset, h, profile);
			}
		} catch (Exception e) {
			logger.error("Error while executing dataset", e);
			return null;
		}
		logger.debug("OUT");
		return dataSetJSON;
	}

	// private void checkQbeDataset(IDataSet dataSet) {
	// SpagoBICoreDatamartRetriever retriever = new SpagoBICoreDatamartRetriever();
	// Map parameters = dataSet.getParamsMap();
	// if (parameters == null) {
	// parameters = new HashMap();
	// dataSet.setParamsMap(parameters);
	// }
	// dataSet.getParamsMap().put(SpagoBIConstants.DATAMART_RETRIEVER, retriever);
	// }

	private HashMap deserializeParValuesListJSONArray(JSONArray parsListJSON) throws JSONException {
		HashMap h = new HashMap();
		for (int i = 0; i < parsListJSON.length(); i++) {
			JSONObject obj = (JSONObject) parsListJSON.get(i);
			String name = obj.getString("name");
			boolean hasVal = obj.has("value");
			String tempVal = "";
			if (hasVal) {
				tempVal = obj.getString("value");
			}
			String value = "";
			if (tempVal != null && tempVal.contains(",")) {
				String[] tempArrayValues = tempVal.split(",");
				for (int j = 0; j < tempArrayValues.length; j++) {
					if (j == 0) {
						value = "'" + tempArrayValues[j] + "'";
					} else {
						value = value + ",'" + tempArrayValues[j] + "'";
					}
				}
			} else {
				value = "'" + tempVal + "'";
			}
			h.put(name, value);
		}
		return h;
	}

	public JSONObject getDatasetTestResultList(IDataSet dataSet, HashMap parametersFilled, IEngUserProfile profile) throws Exception {
		logger.debug("IN");
		JSONObject dataSetJSON = null;

		Integer start = -1;
		try {
			start = getAttributeAsInteger(DataSetConstants.START);
		} catch (NullPointerException e) {
			logger.info("start option undefined");
		}
		Integer limit = -1;
		try {
			limit = getAttributeAsInteger(DataSetConstants.LIMIT);
		} catch (NullPointerException e) {
			logger.info("limit option undefined");
		}
		dataSet.setUserProfileAttributes(UserProfileUtils.getProfileAttributes(profile));
		dataSet.setParamsMap(parametersFilled);
		// checkQbeDataset(dataSet);
		try {
			if (dataSet.getTransformerId() != null) {
				dataSet.loadData();
			} else {
				dataSet.loadData(start, limit, GeneralUtilities.getDatasetMaxResults());
			}
			IDataStore dataStore = dataSet.getDataStore();
			JSONDataWriter dataSetWriter = new JSONDataWriter();
			dataSetJSON = (JSONObject) dataSetWriter.write(dataStore);
		} catch (Exception e) {
			logger.error("Error while executing dataset for test purpose", e);
			return null;
		}

		logger.debug("OUT");
		return dataSetJSON;
	}

}
