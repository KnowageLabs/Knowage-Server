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

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.qbe.query.serializer.json.QuerySerializationConstants;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class Version3QbeEngineAnalysisStateLoader extends AbstractQbeEngineAnalysisStateLoader{

	/** Logger component. */
	private static final Logger LOGGER = Logger.getLogger(Version3QbeEngineAnalysisStateLoader.class);

	public static final String FROM_VERSION = "3";
    public static final String TO_VERSION = "4";

    public Version3QbeEngineAnalysisStateLoader() {
    	super();
    }

    public Version3QbeEngineAnalysisStateLoader(IQbeEngineAnalysisStateLoader loader) {
    	super(loader);
    }

	@Override
	public JSONObject convert(JSONObject data) {
		JSONObject resultJSON;
		JSONObject catalogueJSON;
		JSONArray queriesJSON;

		LOGGER.debug( "IN" );
		try {
			Assert.assertNotNull(data, "Data to convert cannot be null");

			LOGGER.debug( "Converting from encoding version [" + FROM_VERSION + "] to encoding version [" + TO_VERSION + "] ..." );
			LOGGER.debug( "Data to convert [" + data.toString() + "]");

			catalogueJSON = data.getJSONObject("catalogue");
			// fix query encoding ...
			queriesJSON = catalogueJSON.getJSONArray("queries");
			LOGGER.debug( "In the stored catalogue there are  [" + queriesJSON.length() + "] to convert");
			for(int i = 0; i < queriesJSON.length(); i++) {
				convertQuery(queriesJSON.getJSONObject(i));
			}

			resultJSON = new JSONObject();
			resultJSON.put("catalogue", catalogueJSON);

			LOGGER.debug( "Converted data [" + resultJSON.toString() + "]");
			LOGGER.debug( "Conversion from encoding version [" + FROM_VERSION + "] to encoding version [" + TO_VERSION + "] terminated succesfully" );
		}catch(Throwable t) {
			throw new SpagoBIEngineRuntimeException("Impossible to load from rowData [" + data + "]", t);
		} finally {
			LOGGER.debug( "OUT" );
		}


		return resultJSON;
	}

	private void convertQuery(JSONObject queryJSON) {
		JSONArray havingsJSON;
		String queryId = null;
		JSONArray subqueriesJSON;

		LOGGER.debug("IN");

		try {
			Assert.assertNotNull(queryJSON, "Query to be converted cannot be null");
			queryId = queryJSON.getString("id");
			LOGGER.debug( "Converting query [" + queryId + "] ...");
			LOGGER.debug( "Query content to be converted [" + queryJSON.toString() + "]");

			// adding having clause filters (empty)
			havingsJSON = new JSONArray();
			queryJSON.put(QuerySerializationConstants.HAVINGS, havingsJSON);
			LOGGER.debug( "Empty having clause filters array added");

			// convert subqueries
			subqueriesJSON = queryJSON.getJSONArray( "subqueries" );
			LOGGER.debug( "Query [" + queryId + "] have [" + subqueriesJSON.length() + "] subqueries to convert");
			for(int j = 0; j < subqueriesJSON.length(); j++) {
				LOGGER.debug( "Converting subquery [" + (j+1)+ "] of query [" + queryId + "] ...");
				convertQuery( subqueriesJSON.getJSONObject(j) );
			}

			LOGGER.debug( "Query [" + queryId + "] converted succesfully");
		}catch(Throwable t) {
			throw new SpagoBIEngineRuntimeException("Impossible convert query [" + queryId + "]", t);
		} finally {
			LOGGER.debug( "OUT" );
		}
	}

}
