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
package it.eng.spagobi.utilities;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.eng.spagobi.container.IContainer;

/**
 * @author Gioia
 *
 */
public class ParametersDecoder extends BaseParametersDecoder {

	private static final Logger LOGGER = LogManager.getLogger(ParametersDecoder.class);

	/**
	 * Creates MultiValue String
	 *
	 * @param paramaterValue the parameter value
	 *
	 * @return String, if is multi values
	 */
	public String decodeParameter(Object paramaterValue) {
		if (paramaterValue == null)
			return null;
		else {
			String paramaterValueStr = paramaterValue.toString();
			StringBuilder toReturn = new StringBuilder("");

			if (this.isMultiValues(paramaterValueStr)) {
				List<String> values = this.decode(paramaterValueStr);
				for (int i = 0; i < values.size(); i++) {
					toReturn.append(i > 0 ? ";" : "");
					toReturn.append(values.get(i));
				}
			} else {
				toReturn = new StringBuilder(paramaterValueStr);
			}
			return toReturn.toString();
		}
	}

	/**
	 * Get the original values (without adding the quotes)
	 *
	 * @param value the value
	 *
	 * @return the list
	 */
	public List getOriginalValues(String value) {
		LOGGER.debug("IN: value = {}", value);
		List values = null;

		if (value == null)
			return null;

		if (isMultiValues(value)) {
			values = new ArrayList();
			String separator = getSeparator(value);
			String innerBlock = getInnerBlock(value);
			String[] chunks = innerBlock.split(separator);
			for (int i = 0; i < chunks.length; i++) {
				String singleValue = chunks[i];
				values.add(singleValue);
			}
		} else {
			values = new ArrayList();
			values.add(value);
		}

		LOGGER.debug("OUT: list of values = {}", (values == null ? null : values.toString()));
		return values;
	}

	/**
	 * Gets all decoded parameters defined on http request. Multivalue parameters are converted into List The returned HashMap will contain the request parameters'
	 * names as key; for each parameter, the value will be a String if it has a single value, it will be a List if it is multi value (each element of the List being
	 * a String).
	 *
	 * @param servletRequest The http request
	 * @return an HashMap containing all decoded parameters defined on http request. Multivalue parameters are converted into List
	 * @deprecated To all users, the method's return type will be changed to {@link Map}
	 */
	@Deprecated
	public static HashMap getDecodedRequestParameters(HttpServletRequest servletRequest) {
		LOGGER.debug("IN");
		HashMap requestParameters = new HashMap();
		ParametersDecoder decoder = new ParametersDecoder();
		Enumeration enumer = servletRequest.getParameterNames();
		while (enumer.hasMoreElements()) {
			String key = (String) enumer.nextElement();
			Object value = null;
			String valueStr = servletRequest.getParameter(key);
			LOGGER.debug("Found request parameter with key = [{}] and value = [{}]", key, valueStr);
			try {
				if (decoder.isMultiValues(valueStr)) {
					value = decoder.getOriginalValues(valueStr).toArray();
				} else {
					value = valueStr;
				}
			} catch (Exception e) {
				LOGGER.warn("Error while decoding parameter with key = [{}] and value = [{}]. It will be not decoded",
						key, valueStr);
				value = valueStr;
			}
			requestParameters.put(key, value);
		}
		LOGGER.debug("OUT");
		return requestParameters;
	}

	/**
	 * @deprecated To all users, the method's return type will be changed to {@link Map}
	 */
	@Deprecated
	public static HashMap getDecodedRequestParameters(IContainer requestContainer) {
		LOGGER.debug("IN");
		HashMap requestParameters = new HashMap();
		ParametersDecoder decoder = new ParametersDecoder();
		Iterator it = requestContainer.getKeys().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			Object value = null;
			String valueStr = requestContainer.getString(key);
			LOGGER.debug("Found request parameter with key = [{}] and value = [{}]", key, valueStr);
			try {
				if (decoder.isMultiValues(valueStr)) {
					value = decoder.getOriginalValues(valueStr).toArray();
				} else {
					value = valueStr;
				}
			} catch (Exception e) {
				LOGGER.error("Error while decoding parameter with key = [{}] and value = [{}]. It will be not decoded",
						key, valueStr);
				value = valueStr;
			}
			requestParameters.put(key, value);
		}
		LOGGER.debug("OUT");
		return requestParameters;
	}
}
