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
package it.eng.spagobi.engines.qbe.services.core;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.query.Query;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.engines.qbe.QbeEngineInstance;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.utilities.engines.AbstractEngineAction;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

// TODO: Auto-generated Javadoc
/**
 * The Class AbstractQbeEngineAction.
 *
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public abstract class AbstractQbeEngineAction extends AbstractEngineAction {

	/** Logger component. */
	private static transient Logger logger = Logger.getLogger(AbstractQbeEngineAction.class);

	private static final String PARAM_VALUE_NAME = "value";
	public static final String DEFAULT_VALUE_PARAM = "defaultValue";
	public static final String MULTI_PARAM = "multiValue";

	@Override
	public QbeEngineInstance getEngineInstance() {
		return (QbeEngineInstance) getAttributeFromSession(EngineConstants.ENGINE_INSTANCE);
	}

	public IDataSource getDataSource() {
		QbeEngineInstance qbeEngineInstance = null;
		qbeEngineInstance = getEngineInstance();
		if (qbeEngineInstance == null) {
			return null;
		}
		return qbeEngineInstance.getDataSource();
	}

	public void setDataSource(IDataSource dataSource) {
		QbeEngineInstance qbeEngineInstance = null;
		qbeEngineInstance = getEngineInstance();
		if (qbeEngineInstance == null) {
			return;
		}
		qbeEngineInstance.setDataSource(dataSource);
	}

	public Query getQuery() {
		QbeEngineInstance qbeEngineInstance = null;
		qbeEngineInstance = getEngineInstance();
		if (qbeEngineInstance == null) {
			return null;
		}
		return qbeEngineInstance.getActiveQuery();
	}

	public void addParameters(JSONArray parsListJSON) {
		try {
			if (parsListJSON != null) {

				for (int i = 0; i < parsListJSON.length(); i++) {
					JSONObject obj = (JSONObject) parsListJSON.get(i);
					String name = obj.getString("name");
					String type = null;
					if (obj.has("type")) {
						type = obj.getString("type");
					}

					// check if has value, if has not a valid value then use default
					// value
					boolean hasVal = obj.has(PARAM_VALUE_NAME) && !obj.getString(PARAM_VALUE_NAME).isEmpty();
					String tempVal = "";
					if (hasVal) {
						tempVal = obj.getString(PARAM_VALUE_NAME);
					} else {
						boolean hasDefaultValue = obj.has(DEFAULT_VALUE_PARAM);
						if (hasDefaultValue) {
							tempVal = obj.getString(DEFAULT_VALUE_PARAM);
							logger.debug("Value of param not present, use default value: " + tempVal);
						}
					}

					/**
					 * This block of code:
					 *
					 * boolean multivalue = false;
					 * if (tempVal != null && tempVal.contains(",")) {
					 * 	multivalue = true;
					 * }
					 *
					 * Was replaced by the following because the user has the ability
					 * to say if the value is multivalue or not, we don't need to do
					 * any logic.
					 */
					boolean multivalue = obj.optBoolean(MULTI_PARAM);

					String value = "";
					if (multivalue) {
						value = getMultiValue(tempVal, type);
					} else {
						value = getSingleValue(tempVal, type);
					}

					logger.debug("name: " + name + " / value: " + value);

					getEnv().put(name + SpagoBIConstants.PARAMETER_TYPE, type);
					getEnv().put(name, value);
				}
			}

		} catch (Throwable t) {
			if (t instanceof SpagoBIServiceException) {
				throw (SpagoBIServiceException) t;
			}
			throw new SpagoBIRuntimeException("An unexpected error occured while deserializing dataset parameters", t);
		}
	}

	public String getMultiValue(String value, String type) {
		String toReturn = "";

		String[] tempArrayValues = value.split(",");
		for (int j = 0; j < tempArrayValues.length; j++) {
			String tempValue = tempArrayValues[j];
			if (j == 0) {
				toReturn = getSingleValue(tempValue, type);
			} else {
				toReturn = toReturn + ", " + getSingleValue(tempValue, type);
			}
		}

		return toReturn;
	}

	public String getSingleValue(String value, String type) {
		String toReturn = "";
		value = value.trim();
		if (type.equalsIgnoreCase(DataSetUtilities.STRING_TYPE)) {

			if ((!(value.startsWith("'") && value.endsWith("'")))) {
				toReturn = "'" + value + "'";
			} else {
				toReturn = value;
			}

		} else if (type.equalsIgnoreCase(DataSetUtilities.NUMBER_TYPE)) {

			if (value.startsWith("'") && value.endsWith("'") && value.length() >= 2) {
				toReturn = value.substring(1, value.length() - 1);
			} else {
				toReturn = value;
			}
			if (toReturn == null || toReturn.length() == 0) {
				toReturn = "";
			}
		}

		return toReturn;
	}

}
