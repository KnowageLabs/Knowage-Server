/**
 *
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
