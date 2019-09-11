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
package it.eng.knowage.document.export.cockpit.converter;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.document.export.cockpit.IConverter;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.utilities.parameters.ParametersUtilities;

/**
 * @author Dragan Pirkovic
 *
 */
public class ParametersConverter implements IConverter<JSONObject, JSONObject> {

	private final Map<String, String> documentParameters;

	/**
	 *
	 */
	public ParametersConverter(Map<String, String> documentParameters) {
		this.documentParameters = documentParameters;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.export.cockpit.IConverter#convert(java.lang.Object)
	 */
	@Override
	public JSONObject convert(JSONObject datasetParams) {

		for (Map.Entry<String, String> param : DataSetUtilities.getParametersMap(datasetParams).entrySet()) {

			if (ParametersUtilities.isParameter(param.getValue()) && getDocumentParam(param) != null) {
				try {

					datasetParams.put(param.getKey(), getDocumentParam(param));
				} catch (JSONException e) {

				}
			}

		}

		return datasetParams;
	}

	/**
	 * @param param
	 * @return
	 */
	private String getDocumentParam(Map.Entry<String, String> param) {
		return documentParameters.get(ParametersUtilities.getParameterName(param.getValue()));
	}

}
