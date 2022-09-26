/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2022 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.knowage.parameter;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;

abstract class AbstractParameterManager implements IParameterManager {

	private static final Logger logger = LogManager.getLogger(AbstractParameterManager.class);

	@Override
	public final String fromFeToBe(String type, String value, String defaultValue, boolean multiValue) throws JSONException {
		String retValue = "";

		// check if has value, if has not a valid value then use default
		// value
		boolean hasVal = isNotEmpty(value);
		String tempVal = "";
		if (hasVal) {
			tempVal = value;
		} else {
			boolean hasDefaultValue = isNotEmpty(defaultValue);
			if (hasDefaultValue) {
				tempVal = defaultValue;
				logger.debug("Value of param not present, use default value: " + tempVal);
			}
		}

		if (multiValue) {

			// WORKAROUND : the preview and the save dataset service have different format
			List<Object> listValue = new ArrayList<>();

			if (isNotEmpty(tempVal) && tempVal.startsWith("[") && tempVal.endsWith("]")) {
				JSONArray arrayValue = new JSONArray(tempVal);

				for (int j = 0; j < arrayValue.length(); j++) {
					listValue.add(arrayValue.get(j));
				}
			} else {
				// TODO : Delete this branch when the format between preview and save dataset will be the same
				listValue = asList(tempVal.split(","));
			}

			retValue = getMultiValue(listValue, type);
		} else {
			retValue = getSingleValue(tempVal, type);
		}

		return retValue;
	}

	@Override
	public final Object fromBeToFe(String type, String defaultValue, boolean multiValue) throws JSONException {
		Object retValue = "";

		// check if has value, if has not a valid value then use default
		// value
		String tempVal = StringUtils.defaultString(defaultValue, "");

		if (multiValue) {

			// WORKAROUND : the preview and the save dataset service have different format
			if (isNotEmpty(tempVal) && tempVal.startsWith("[") && tempVal.endsWith("]")) {
				retValue = new JSONArray(tempVal);
			} else {
				JSONArray altRetValue = new JSONArray();

				// TODO : Delete this branch when the format between preview and save dataset will be the same
				for(String curr : asList(tempVal.split(","))) {
					altRetValue.put(getSingleValueForFE(curr, type));
				}

				retValue = altRetValue;
			}

		} else {
			retValue = getSingleValueForFE(tempVal, type);
		}

		return retValue;
	}

	protected abstract String getSingleValue(String tempVal, String type);

	protected abstract String getMultiValue(List<Object> listValue, String type);

	protected Object getSingleValueForFE(String value, String type) {
		if (type.equalsIgnoreCase(DataSetUtilities.STRING_TYPE)
				|| type.equalsIgnoreCase(DataSetUtilities.GENERIC_TYPE)
				|| type.equalsIgnoreCase(DataSetUtilities.RAW_TYPE)) {

			return value;

		} else if (type.equalsIgnoreCase(DataSetUtilities.NUMBER_TYPE)) {

			String temp = "";
			if (value.startsWith("'") && value.endsWith("'") && value.length() >= 2) {
				temp = value.substring(1, value.length() - 1);
			} else {
				temp = value;
			}
			if (temp == null || temp.length() == 0) {
				return null;
			} else {
				return new BigDecimal(temp);
			}
		} else {
			throw new IllegalArgumentException("Type " + type + " unknown!");
		}

	}
}
