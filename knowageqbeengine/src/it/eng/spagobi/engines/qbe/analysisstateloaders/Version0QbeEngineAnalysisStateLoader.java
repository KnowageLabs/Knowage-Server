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
