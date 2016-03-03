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
package it.eng.spagobi.tools.dataset.utils;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.services.proxy.DataSetServiceProxy;
import it.eng.spagobi.tools.dataset.bo.DataSetParameterItem;
import it.eng.spagobi.tools.dataset.bo.DataSetParametersList;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * TODO : move it in it.eng.spagobi.tools.dataset and rename to DataSetProfiler (or similar)
 */
public class DataSetUtilities {

	public static final String STRING_TYPE = "string";
	public static final String NUMBER_TYPE = "number";
	public static final String RAW_TYPE = "raw";
	public static final String GENERIC_TYPE = "generic";

	/**
	 * It's possible to disable the default params behavior
	 */
	private static boolean disableDefaultParams;

	private final static Logger logger = Logger.getLogger(DataSetUtilities.class);

	/**
	 * Check if the dataset is executable by the user
	 *
	 * @param dataset
	 * @param owner
	 * @param isAdminUser
	 * @return
	 */
	public static boolean isExecutableByUser(IDataSet dataset, IEngUserProfile profile) {
		if (profile == null) {
			return false;
		}
		boolean isAdminUser = isAdministrator(profile);
		if (isAdminUser) {
			return true;
		}
		String owner = profile.getUserUniqueIdentifier().toString();

		return (dataset.isPublic() || (!owner.equals(null) && owner.equals(dataset.getOwner())));
	}
	
	public static boolean isExecutableByUser(boolean isPublic, String ownerDataSet, IEngUserProfile profile) {
		if (profile == null) {
			return false;
		}
		boolean isAdminUser = isAdministrator(profile);
		if (isAdminUser) {
			return true;
		}
		String owner = profile.getUserUniqueIdentifier().toString();

		return (isPublic|| (!owner.equals(null) && owner.equals(ownerDataSet)));
	}

	public static boolean isAdministrator(IEngUserProfile profile) {
		Assert.assertNotNull(profile, "Object in input is null");
		try {
			if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while getting user's information", e);
		}
	}

	/**
	 * Retrieve the params default values from dataSet
	 *
	 * @param dataSet
	 * @return null if some errors occur (no exceptions thrown)
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, ParamDefaultValue> getParamsDefaultValues(IDataSet dataSet) {
		Helper.checkNotNull(dataSet, "dataSet");

		// Retrieve the default values directly from dataSet
		try {
			String params = dataSet.getParameters();
			if (params == null || params.isEmpty()) {
				return null;
			}

			Map<String, ParamDefaultValue> res = new HashMap<String, ParamDefaultValue>();
			DataSetParametersList dspl = DataSetParametersList.fromXML(params);
			for (DataSetParameterItem item : (List<DataSetParameterItem>) dspl.getItems()) {
				String defaultValue = item.getDefaultValue();
				if (defaultValue == null || defaultValue.isEmpty()) {
					continue;
				}
				String name = item.getName();
				String type = item.getType();
				res.put(name, new ParamDefaultValue(name, type, defaultValue));
			}
			return res;
		} catch (Exception e) {
			logger.warn(
					"Default parameters values can't be retrieved from dataSet. I try from dataSet persistence. Empty defaults values map will be returned.", e);
		}
		return null;
	}

	/**
	 * Return the default values from dataSet. If they are not present then retrieve them from {@link DataSetServiceProxy}.
	 *
	 * @return null if some errors occur (no exceptions thrown)
	 */
	public static Map<String, ParamDefaultValue> getParamsDefaultValuesUseAlsoService(IDataSet dataSet, String user, HttpSession session) {
		Helper.checkNotNull(dataSet, "dataSet");
		Helper.checkNotNull(session, "session");
		Helper.checkNotNullNotTrimNotEmpty(user, "user");

		Map<String, ParamDefaultValue> res = getParamsDefaultValues(dataSet);
		if (res != null) {
			return res;
		}
		// res=null, load dataset from service
		logger.warn("No params default values found on dataSet. I try from service.");
		try {
			String label = dataSet.getLabel();
			if (label == null) {
				logger.warn("Label not found -> no default values from service");
				return null;
			}
			DataSetServiceProxy proxy = new DataSetServiceProxy(user, session);
			IDataSet ds = proxy.getDataSetByLabel(label);
			if (ds == null) {
				logger.warn("Dataset not found -> no default values from service");
				return null;
			}

			res = getParamsDefaultValues(ds);
		} catch (Exception e) {
			logger.warn("Default parameters values can't be retrieved from dataSet service.", e);
		}
		return res;
	}

	/**
	 * Fill he parameters map with the default values if and only if they are not already present in the map.
	 *
	 * @param dataSet
	 *            can't be null
	 * @param parameters
	 *            can be null
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void fillDefaultValues(IDataSet dataSet, Map parameters) {
		if (disableDefaultParams) {
			return;
		}
		Helper.checkNotNull(dataSet, "dataset");
		if (parameters == null) {
			return;
		}

		// parameters != null
		int oldSize = parameters.size();
		Map<String, ParamDefaultValue> defaultValues = getParamsDefaultValues(dataSet);
		if (defaultValues == null) {
			// no changes
			return;
		}

		// default values != null
		for (String key : defaultValues.keySet()) {
			ParamDefaultValue paramDefaultValue = defaultValues.get(key);
			String defaultValue = paramDefaultValue.getDefaultValue();
			if (parameters.containsKey(key)) {
				Object oldValueObject = parameters.get(key);
				Assert.assertTrue(oldValueObject == null || oldValueObject instanceof String, "parameter is not a instance of string");
				String oldValue = (String) oldValueObject;
				if (isEmptyValue(oldValue, paramDefaultValue)) {
					parameters.put(key, getDefaultValueByType(defaultValue, paramDefaultValue));
				}
			} else {
				// parameters dosn't contain the default value
				parameters.put(key, getDefaultValueByType(defaultValue, paramDefaultValue));
			}
		}

		Assert.assertTrue(oldSize <= parameters.size(), "parameters can't be removed");
	}

	/**
	 * Return the default value by the type of param
	 *
	 * @param defaultValue
	 * @param paramDefaultValue
	 * @return
	 */
	private static String getDefaultValueByType(String defaultValue, ParamDefaultValue paramDefaultValue) {
		if (!STRING_TYPE.equals(paramDefaultValue.getType())) {
			return defaultValue;
		}
		return "'" + defaultValue + "'";
	}

	/**
	 * Check if it's a empty value already filled by the type (see ManageDataSets from knowage project)
	 *
	 * @param oldValue
	 * @param paramDefaultValue
	 * @return
	 */
	private static boolean isEmptyValue(String oldValue, ParamDefaultValue paramDefaultValue) {
		if (oldValue == null || oldValue.isEmpty()) {
			return true;
		}
		if (STRING_TYPE.equals(paramDefaultValue.getType())) {
			if (oldValue == "''") {
				return true;
			}
		}
		return false;
	}

	public static boolean isDisableDefaultParams() {
		return disableDefaultParams;
	}

	public static void setDisableDefaultParams(boolean disableDefaultParams) {
		DataSetUtilities.disableDefaultParams = disableDefaultParams;
	}
}
