/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
