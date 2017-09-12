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
package it.eng.spagobi.engines.qbe.analysisstateloaders;

import it.eng.qbe.query.ISelectField;
import it.eng.qbe.query.serializer.json.QuerySerializationConstants;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class Version4QbeEngineAnalysisStateLoader extends AbstractQbeEngineAnalysisStateLoader{

	public final static String FROM_VERSION = "4";
    public final static String TO_VERSION = "5";
    
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(Version4QbeEngineAnalysisStateLoader.class);
    	
    public Version4QbeEngineAnalysisStateLoader() {
    	super();
    }
    
    public Version4QbeEngineAnalysisStateLoader(IQbeEngineAnalysisStateLoader loader) {
    	super(loader);
    }
    
	public JSONObject convert(JSONObject data) {
		JSONObject resultJSON;
		JSONObject catalogueJSON;
		JSONArray queriesJSON;
		
		logger.debug( "IN" );
		try {
			Assert.assertNotNull(data, "Data to convert cannot be null");
			
			logger.debug( "Converting from encoding version [" + FROM_VERSION + "] to encoding version [" + TO_VERSION + "] ..." );
			logger.debug( "Data to convert [" + data.toString() + "]");
			
			catalogueJSON = data.getJSONObject("catalogue");
			// fix query encoding ...
			queriesJSON = catalogueJSON.getJSONArray("queries");
			logger.debug( "In the stored catalogue there are  [" + queriesJSON.length() + "] to convert");
			for(int i = 0; i < queriesJSON.length(); i++) {
				convertQuery(queriesJSON.getJSONObject(i));			
			}
			
			resultJSON = new JSONObject();
			resultJSON.put("catalogue", catalogueJSON);
			
			logger.debug( "Converted data [" + resultJSON.toString() + "]");
			logger.debug( "Conversion from encoding version [" + FROM_VERSION + "] to encoding version [" + TO_VERSION + "] terminated succesfully" );
		}catch(Throwable t) {
			throw new SpagoBIEngineRuntimeException("Impossible to load from rowData [" + data + "]", t);
		} finally {
			logger.debug( "OUT" );
		}
			
		
		return resultJSON;
	}
	
	private void convertQuery(JSONObject queryJSON) {
		JSONArray fieldsJSON;
		JSONObject fieldJSON;
		String queryId = null;
		JSONArray subqueriesJSON;
		
		
		logger.debug("IN");
		
		try {
			Assert.assertNotNull(queryJSON, "Query to be converted cannot be null");
			queryId = queryJSON.getString("id");
			logger.debug( "Converting query [" + queryId + "] ...");
			logger.debug( "Query content to be converted [" + queryJSON.toString() + "]");
			
			// convert fields
			fieldsJSON = queryJSON.getJSONArray( "fields" );			
			logger.debug( "Query [" + queryId + "] have [" + fieldsJSON.length() + "] fields to convert");
			for(int j = 0; j < fieldsJSON.length(); j++) {
				logger.debug( "Converting field [" + (j+1) + "] ...");
				fieldJSON = fieldsJSON.getJSONObject(j);
				
				fieldJSON.put(QuerySerializationConstants.FIELD_TYPE, ISelectField.SIMPLE_FIELD);
				
				logger.debug( "Field [" + (j+1) + "] converted succesfully to [" + fieldJSON.toString() + "]");
			}
			
			// convert subqueries
			subqueriesJSON = queryJSON.getJSONArray( "subqueries" );
			logger.debug( "Query [" + queryId + "] have [" + subqueriesJSON.length() + "] subqueries to convert");
			for(int j = 0; j < subqueriesJSON.length(); j++) {
				logger.debug( "Converting subquery [" + (j+1)+ "] of query [" + queryId + "] ...");
				convertQuery( subqueriesJSON.getJSONObject(j) );
			}
			
			logger.debug( "Query [" + queryId + "] converted succesfully");
		}catch(Throwable t) {
			throw new SpagoBIEngineRuntimeException("Impossible convert query [" + queryId + "]", t);
		} finally {
			logger.debug( "OUT" );
		}
	}

}
