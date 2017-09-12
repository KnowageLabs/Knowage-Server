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
