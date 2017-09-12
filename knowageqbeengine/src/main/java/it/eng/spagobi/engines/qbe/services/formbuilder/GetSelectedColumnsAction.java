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
package it.eng.spagobi.engines.qbe.services.formbuilder;

import it.eng.qbe.query.Query;
import it.eng.qbe.query.serializer.SerializerFactory;
import it.eng.qbe.query.serializer.json.QuerySerializationConstants;
import it.eng.qbe.serializer.SerializationException;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.qbe.services.core.AbstractQbeEngineAction;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class GetSelectedColumnsAction  extends AbstractQbeEngineAction {	

	// INPUT PARAMETERS
	public static final String QUERY_ID = "queryId";



	/** Logger component. */
	private static transient Logger logger = Logger.getLogger(GetSelectedColumnsAction.class);

	public void service(SourceBean request, SourceBean response)  {				

		String queryId;
		Query query;
		JSONObject queryJSON;
		JSONArray fieldsJSON;
		JSONObject resultsJSON;

		logger.debug("IN");

		try {		
			super.service(request, response);	

			queryId = getAttributeAsString( QUERY_ID );
			logger.debug("Parameter [" + QUERY_ID + "] is equals to [" + queryId + "]");

			Assert.assertNotNull(getEngineInstance(), "It's not possible to execute " + this.getActionName() + " service before having properly created an instance of EngineInstance class");

			// get query
			if(queryId != null) {
				logger.debug("Loading query [" + queryId + "] from catalogue");
				query = getEngineInstance().getQueryCatalogue().getQuery(queryId);
				Assert.assertNotNull(query, "Query object with id [" + queryId + "] does not exist in the catalogue");
			} else {
				logger.debug("Loading active query");
				query = getEngineInstance().getActiveQuery();
				logger.warn("Active query not available");
				logger.debug("Loading first query from catalogue");
				query = getEngineInstance().getQueryCatalogue().getFirstQuery();
				Assert.assertNotNull(query, "Query catalogue is empty");
			}
			logger.debug("Query [" + query.getId() + "] succesfully loaded");


			// serialize query
			try {
				queryJSON = (JSONObject)SerializerFactory.getSerializer("application/json").serialize(query, getEngineInstance().getDataSource(), getLocale());
			} catch (SerializationException e) {
				throw new SpagoBIEngineServiceException(getActionName(), "Cannot serialize query [" + query.getId() + "]", e);
			}


			fieldsJSON = queryJSON.getJSONArray(QuerySerializationConstants.FIELDS);			
			resultsJSON = new JSONObject(); 


			// check if mandatory_measure or segment_attribute 
//			boolean mandatory_measure = false;
//			boolean segment_attribute = false;
//
//			for (int i = 0; i < fieldsJSON.length() && (!mandatory_measure || !segment_attribute); i++) {
//				JSONObject jsonObject = (JSONObject)fieldsJSON.get(i);
//				int f = 0;
//				Object natureO = jsonObject.get("iconCls");
//				String nature = natureO != null ? natureO.toString() : null;
//				if(nature.equalsIgnoreCase(QuerySerializationConstants.FIELD_NATURE_SEGMENT_ATTRIBUTE)){
//					segment_attribute = true;
//				}
//				else if(nature.equalsIgnoreCase(QuerySerializationConstants.FIELD_NATURE_MANDATORY_MEASURE)){
//					mandatory_measure = true;
//				}
//			}
//			// add the two informations to each field
//			for (int i = 0; i < fieldsJSON.length(); i++) {
//				JSONObject jsonObject = (JSONObject)fieldsJSON.get(i);
//				jsonObject.put(QuerySerializationConstants.FIELD_NATURE_SEGMENT_ATTRIBUTE, segment_attribute);
//				jsonObject.put(QuerySerializationConstants.FIELD_NATURE_MANDATORY_MEASURE, mandatory_measure);
//			}


			resultsJSON.put("results", fieldsJSON);





			try {
				writeBackToClient( new JSONSuccess( resultsJSON ) );
			} catch (IOException e) {
				throw new SpagoBIEngineServiceException(getActionName(), "Impossible to write back the responce to the client [" + resultsJSON.toString(2)+ "]", e);
			}

		} catch(Throwable t) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException(getActionName(), getEngineInstance(), t);
		} finally {			
			logger.debug("OUT");
		}	
	}
}
