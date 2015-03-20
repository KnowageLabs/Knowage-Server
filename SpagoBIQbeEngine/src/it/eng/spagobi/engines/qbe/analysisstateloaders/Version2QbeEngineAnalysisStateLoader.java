/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.analysisstateloaders;

import it.eng.qbe.query.serializer.json.QuerySerializationConstants;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class Version2QbeEngineAnalysisStateLoader extends AbstractQbeEngineAnalysisStateLoader{

	public final static String FROM_VERSION = "2";
    public final static String TO_VERSION = "3";
    
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(Version2QbeEngineAnalysisStateLoader.class);
    	
    public Version2QbeEngineAnalysisStateLoader() {
    	super();
    }
    
    public Version2QbeEngineAnalysisStateLoader(IQbeEngineAnalysisStateLoader loader) {
    	super(loader);
    }
    
	public JSONObject convert(JSONObject data) {
		JSONObject resultJSON;
		JSONObject catalogueJSON;
		JSONArray queriesJSON;
		JSONObject queryJSON;
		
		
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
		JSONArray filtersJSON;
		JSONArray subqueriesJSON;
		JSONObject fieldJSON;
		JSONObject filterJSON;
		String queryId = null;
		
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
				
				fieldJSON.put(QuerySerializationConstants.FIELD_INCLUDE, true);
				
				logger.debug( "Field [" + (j+1) + "] converted succesfully to [" + fieldJSON.toString() + "]");
			}
			
			// convert filters
			filtersJSON = queryJSON.getJSONArray( "filters" );
			logger.debug( "Query [" + queryId + "] have [" + filtersJSON.length() + "] filters to convert");
			for(int j = 0; j < filtersJSON.length(); j++) {
				logger.debug( "Converting filter [" + (j+1) + "] ...");
				filterJSON = filtersJSON.getJSONObject(j);
				
				filterJSON.put(QuerySerializationConstants.FILTER_ID, filterJSON.get("fname"));
				filterJSON.put(QuerySerializationConstants.FILTER_DESCRIPTION, filterJSON.get("fname"));
				filterJSON.put(QuerySerializationConstants.FILTER_PROMPTABLE, filterJSON.get("isfree"));
				
				filterJSON.put(QuerySerializationConstants.FILTER_LO_VALUE, filterJSON.get("id"));
				filterJSON.put(QuerySerializationConstants.FILTER_LO_DESCRIPTION, filterJSON.get("entity") + " : " + filterJSON.get("field"));
				filterJSON.put(QuerySerializationConstants.FILTER_LO_TYPE, "Field Content");
				filterJSON.put(QuerySerializationConstants.FILTER_LO_DEFAULT_VALUE, "");
				filterJSON.put(QuerySerializationConstants.FILTER_LO_LAST_VALUE, "");
				
	
				filterJSON.put(QuerySerializationConstants.FILTER_RO_VALUE, filterJSON.get("operand"));
				filterJSON.put(QuerySerializationConstants.FILTER_RO_DESCRIPTION, filterJSON.get("odesc"));
				filterJSON.put(QuerySerializationConstants.FILTER_RO_TYPE, filterJSON.get("otype"));
				filterJSON.put(QuerySerializationConstants.FILTER_RO_DEFAULT_VALUE, filterJSON.get("defaultvalue"));
				filterJSON.put(QuerySerializationConstants.FILTER_RO_LAST_VALUE, filterJSON.get("lastvalue"));
				
				filterJSON.put(QuerySerializationConstants.FILTER_BOOLEAN_CONNETOR, filterJSON.get("boperator"));
				
				logger.debug( "Filter [" + (j+1) + "] converted succesfully to [" + filterJSON.toString() + "]");
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
