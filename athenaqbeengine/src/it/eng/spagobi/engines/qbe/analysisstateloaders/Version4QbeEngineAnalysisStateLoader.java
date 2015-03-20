/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
