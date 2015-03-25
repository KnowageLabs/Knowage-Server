/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.tools.dataset.validation;

import it.eng.spagobi.rest.validation.IFieldsValidator;
import it.eng.spagobi.utilities.json.JSONUtils;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * @author Monica Franceschini (monica.franceschini@eng.it)
 * 
 */

public class DatasetFieldsValidator implements IFieldsValidator {
	
	private static transient Logger logger = Logger.getLogger(DatasetFieldsValidator.class);

	public JSONArray validateFields(MultivaluedMap<String, String> parameters) {
		JSONArray validationErrors = new JSONArray();

		//Dataset Validation ---------------------------------------------
		String datasetMetadata = parameters.get("meta").get(0);
		
		if (datasetMetadata != null)	{
			datasetMetadata = URLDecoder.decode(datasetMetadata);
			try {
				if ((!datasetMetadata.equals("")) && (!datasetMetadata.equals("[]"))) {
					JSONObject metadataObject = JSONUtils.toJSONObject(datasetMetadata);
					JSONArray columnsMetadataArray = metadataObject.getJSONArray("columns");

					for (int j = 0; j < columnsMetadataArray.length(); j++) {
						JSONObject columnJsonObject = columnsMetadataArray.getJSONObject(j);
						String columnName = columnJsonObject.getString("column");
						if(columnJsonObject.getString("pname").equalsIgnoreCase("type")){
							if(columnName == null){
								validationErrors.put(new JSONObject("{message: 'Validation error: Column name cannot be null'}"));
							} /* else if(Pattern.compile("\\s").matcher(columnName).find()){							
								validationErrors.put(new JSONObject("{message: 'Validation error: Space character not allowed for column name "+ columnName + "'}"));
							}else if(Pattern.compile("[ÀÈÌÒÙàèìòùÁÉÍÓÚÝáéíóúýÂÊÎÔÛâêîôûÃÑÕãñõÄËÏÖÜŸäëïöüŸ¡¿çÇŒœßØøÅåÆæÞþÐðÐð'.,&#@:?!()$\\/]").matcher(columnName).find()){
								validationErrors.put(new JSONObject("{message: 'Validation error: Special characters not allowed for column name "+ columnName + "'}"));
							} */
						} 
						String propertyName = columnJsonObject.getString("pname");
						if(propertyName == null){
							validationErrors.put(new JSONObject("{message: 'Validation error: Property name cannot be null'}"));
						} /* else if( Pattern.compile("\\s").matcher(propertyName).find()){							
							validationErrors.put(new JSONObject("{message: 'Validation error: Space character not allowed for Property name "+ propertyName + "'}"));
						}else if( Pattern.compile("[ÀÈÌÒÙàèìòùÁÉÍÓÚÝáéíóúýÂÊÎÔÛâêîôûÃÑÕãñõÄËÏÖÜŸäëïöüŸ¡¿çÇŒœßØøÅåÆæÞþÐðÐð'.,&#@:?!()$\\/]").matcher(propertyName).find()){
							validationErrors.put(new JSONObject("{message: 'Validation error: Special characters not allowed for Property name "+ propertyName + "'}"));
						} */
						String propertyValue = columnJsonObject.getString("pvalue");

					}

				}

			} catch (JsonMappingException e1) {
				logger.error(e1.getMessage());
			} catch (JsonParseException e1) {
				logger.error(e1.getMessage());
			} catch (JSONException e1) {
				logger.error(e1.getMessage());
			} catch (IOException e1) {
				logger.error(e1.getMessage());
			}
			
		}
		return validationErrors;
	}

}
