/**
 *
 */
package it.eng.knowage.document.export.cockpit.converter;

import java.io.IOException;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.knowage.document.export.cockpit.IConverter;
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

		for (Map.Entry<String, String> param : toMap(datasetParams).entrySet()) {

			if (ParametersUtilities.isParameter(param.getValue()) && documentParameters.containsKey(param.getKey())) {
				try {
					datasetParams.put(param.getKey(), documentParameters.get(param.getKey()));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

		return datasetParams;
	}

	/**
	 * @param datasetParams
	 * @return
	 */
	private Map<String, String> toMap(JSONObject datasetParams) {

		try {
			return new ObjectMapper().readValue(datasetParams.toString(), new TypeReference<Map<String, String>>() {
			});
		} catch (IOException e1) {

		}
		return null;
	}

}
