/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.analysisstateloaders;

import it.eng.qbe.query.catalogue.QueryCatalogue;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class Version0QbeEngineAnalysisStateLoader extends AbstractQbeEngineAnalysisStateLoader{

	public final static String FROM_VERSION = "0";
    public final static String TO_VERSION = "1";
    
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(Version0QbeEngineAnalysisStateLoader.class);
	
    public Version0QbeEngineAnalysisStateLoader() {
    	super();
    }
    
    public Version0QbeEngineAnalysisStateLoader(IQbeEngineAnalysisStateLoader loader) {
    	super(loader);
    }
    
	public JSONObject convert(JSONObject data) {
		JSONObject resultJSON;
		JSONObject catalogueJSON;
		JSONArray queriesJSON;
		JSONObject queryJSON;
		JSONArray filtersJSON;
		JSONObject filterJSON;
		// just to create well formed id for loaded queries
		QueryCatalogue catalogue;
		
		logger.debug("IN");
		
		resultJSON = new JSONObject();
		catalogueJSON = new JSONObject();
		queriesJSON = new JSONArray();
		try {
			logger.debug( "Converting from encoding version [" + FROM_VERSION + "] to encoding version [" + TO_VERSION + "] ..." );
			
			queryJSON = data;
			// fix query encoding ...
			catalogue = new QueryCatalogue();
			String queryId = catalogue.getNextValidId();
			queryJSON.put( "id",  queryId);
			queryJSON.put( "name", "query_" + queryId );
			queryJSON.put("description", "query_" + queryId );
			queryJSON.put( "distinct", false );
			queryJSON.put( "subqueries", new JSONArray() );
			
			filtersJSON = queryJSON.getJSONArray("filters");
			for(int i = 0; i < filtersJSON.length(); i++) {
				filterJSON = filtersJSON.getJSONObject(i);
				filterJSON.put("isfree", false);
				filterJSON.put("defaultvalue", "");
				filterJSON.put("lastvalue", "");
			}
						
			queriesJSON.put(queryJSON);
			catalogueJSON.put("queries", queriesJSON);
			resultJSON.put("catalogue", catalogueJSON);
			logger.debug( "Conversion from encoding version [" + FROM_VERSION + "] to encoding version [" + TO_VERSION + "] terminated succesfully" );
		}catch(Throwable t) {
			throw new SpagoBIEngineRuntimeException("Impossible to load from rowData [" + data + "]", t);
		} finally {
			logger.debug("OUT");
		}
		
		return resultJSON;
	}

}
