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
package it.eng.spagobi.engines.qbe.bo;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class wrap the FormViewer state (a JSONObject) and provide parsing methods.
 * 
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class FormViewerState {

	public static transient Logger logger = Logger.getLogger(FormViewerState.class);
	
	private JSONObject xorFilters = null;
	private JSONObject onOffFilters = null;
	private JSONObject staticOpenFilters = null;
	private JSONObject dynamicFilters = null;
	
	public FormViewerState(JSONObject state) {
		JSONObject staticClosedFilters = state.optJSONObject("staticClosedFilters");
		if (staticClosedFilters != null) {
			xorFilters = staticClosedFilters.optJSONObject("xorFilters");
			onOffFilters = staticClosedFilters.optJSONObject("onOffFilters");
		}
		staticOpenFilters = state.optJSONObject("staticOpenFilters");
		dynamicFilters = state.optJSONObject("dynamicFilters");
	}
	
	public String getXORFilterSelectedOption(String xorFilterId) {
		logger.debug("IN: xorFilterId = " + xorFilterId);
		String toReturn = null;
		if (xorFilters != null ) {
			toReturn = xorFilters.optString(xorFilterId);
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}
	
	public boolean isOnOffFilterActive(String ofOffFilterGroupId, String onOffFilterId) {
		logger.debug("IN: ofOffFilterGroupId = " + ofOffFilterGroupId + "; onOffFilterId = " + onOffFilterId);
		boolean toReturn = false;
		if (onOffFilters != null ) {
			JSONObject onOffFilterGroup = onOffFilters.optJSONObject(ofOffFilterGroupId);
			if (onOffFilterGroup != null) {
				String onOff = onOffFilterGroup.optString(onOffFilterId);
				toReturn = "on".equalsIgnoreCase(onOff);
			}
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}
	
	public List<String> getOpenFilterValues(String openFilterId) {
		logger.debug("IN: openFilterId = " + openFilterId);
		List<String> toReturn = new ArrayList<String>();
		if (staticOpenFilters != null ) {
			JSONArray values = staticOpenFilters.optJSONArray(openFilterId);
			if (values != null && values.length() > 0) {
				for (int i = 0; i < values.length(); i++)
					if (values.optString(i) != null) {
						toReturn.add(values.optString(i));
					};
			}
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}
	
	public String getDynamicFilterField(String dynamicFilterId) {
		logger.debug("IN: dynamicFilterId = " + dynamicFilterId);
		String toReturn = null;
		if (dynamicFilters != null ) {
			JSONObject dynamicFilter = dynamicFilters.optJSONObject(dynamicFilterId);
			if (dynamicFilter != null) {
				toReturn = dynamicFilter.optString("field");
			}
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}
	
	public List<String> getDynamicFilterFromToValues(String dynamicFilterId) {
		logger.debug("IN: dynamicFilterId = " + dynamicFilterId);
		List<String> toReturn = new ArrayList<String>();
		if (dynamicFilters != null ) {
			JSONObject dynamicFilter = dynamicFilters.optJSONObject(dynamicFilterId);
			if (dynamicFilter != null && dynamicFilter.optString("fromvalue") != null && dynamicFilter.optString("tovalue") != null) {
				toReturn.add(dynamicFilter.optString("fromvalue"));
				toReturn.add(dynamicFilter.optString("tovalue"));
			}
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}
	
	public String getDynamicFilterValue(String dynamicFilterId) {
		logger.debug("IN: dynamicFilterId = " + dynamicFilterId);
		String toReturn = null;
		if (dynamicFilters != null ) {
			JSONObject dynamicFilter = dynamicFilters.optJSONObject(dynamicFilterId);
			if (dynamicFilter != null && dynamicFilter.optString("value") != null) {
				toReturn = dynamicFilter.optString("value");
			}
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}
}
