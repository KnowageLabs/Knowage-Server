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
package it.eng.spagobi.engines.qbe.services.formviewer;

import it.eng.qbe.query.SimpleSelectField;
import it.eng.qbe.query.ISelectField;
import it.eng.qbe.query.Query;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.qbe.services.core.AbstractQbeEngineAction;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * The Class ExecuteQueryAction.
 */
public class GetSelectedColumnsAction extends AbstractQbeEngineAction {	
	
	// INPUT PARAMETERS
	public static final String FIELD_TYPE = "type";
	
	public static final String FIELD_TYPE_ALL = "ALL";
	public static final String FIELD_TYPE_GROUPABLE = "GROUPABLE";
	public static final String FIELD_TYPE_AGGREGABLE = "AGGREGABLE";
		
	
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(GetSelectedColumnsAction.class);
   
	public void service(SourceBean request, SourceBean response)  {				
				
		JSONObject results;
		String fieldType;
		Query query;
		List selectFields;
					
		logger.debug("IN");
		
		try {
		
			super.service(request, response);	
			
			
			
			fieldType = getAttributeAsString( FIELD_TYPE );	
			logger.debug("Parameter [" + FIELD_TYPE + "] is equals to [" + fieldType + "]");
			
			if(fieldType == null) {
				logger.warn("Parameter [" + FIELD_TYPE + "] not specified. All select fields will be returned");
				fieldType = FIELD_TYPE_ALL;
			}
			
			query = getEngineInstance().getQueryCatalogue().getFirstQuery();
			Assert.assertNotNull(query, "Impossible to get any query from catalogue");
			
			selectFields = null;
			if(FIELD_TYPE_ALL.equalsIgnoreCase(fieldType)) {
				selectFields = query.getSelectFields(true);
			} else if(FIELD_TYPE_GROUPABLE.equalsIgnoreCase(fieldType)) {
				selectFields = query.getSelectFields(true);
			} else  if(FIELD_TYPE_GROUPABLE.equalsIgnoreCase(fieldType)) {
				Assert.assertUnreachable("Value [" + fieldType + "] for parameter [" + FIELD_TYPE + "] is valid but not yet supported.");
			} else {
				Assert.assertUnreachable("Parameter [" + FIELD_TYPE + "] cannot be equal to [" + fieldType + "]");
			}
			Assert.assertNotNull(selectFields, "The returned list of selected fields cannot be null");
			
			try {
				results = new JSONObject();
				results.put("fields", serialize(selectFields));
			} catch(Throwable t) {
				throw new SpagoBIEngineServiceException(getActionName(), "Impossible to serialize selected fields list", t);
			}
			
			try {
				writeBackToClient( new JSONSuccess(results) );
			} catch (IOException e) {
				throw new SpagoBIEngineServiceException(getActionName(), "Impossible to write back the responce to the client", e);
			}
			
		}  finally {
			logger.debug("OUT");
		}		
		
	}
	
	private JSONArray serialize(List selectedFields) {
		JSONArray selectedFieldsJSON;
		
		selectedFieldsJSON = new JSONArray();
		
		for(int i = 0; i < selectedFields.size(); i++) {
			ISelectField f = (ISelectField)selectedFields.get(i);
			if(f.isSimpleField()) {
				SimpleSelectField dataMartSelectField = (SimpleSelectField)f;
				JSONObject selectFieldJSON = new JSONObject();
				try {
					selectFieldJSON.put("name", dataMartSelectField.getUniqueName());
					selectFieldJSON.put("alias", dataMartSelectField.getAlias());
				} catch (JSONException e) {
					throw new SpagoBIEngineServiceException(getActionName(), "An error occurred while serializing field [" + dataMartSelectField.getUniqueName() + " - " + dataMartSelectField.getAlias() +"]", e);
				}
				selectedFieldsJSON.put(selectFieldJSON);
			}
		}
		
		return selectedFieldsJSON;
	}
	
	
}
