/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.services.core.catalogue;

import it.eng.qbe.query.Query;
import it.eng.qbe.query.catalogue.QueryCatalogue;
import it.eng.qbe.query.serializer.SerializerFactory;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.engines.qbe.QbeEngineInstance;
import it.eng.spagobi.engines.qbe.services.core.AbstractQbeEngineAction;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 
 * @author Andrea Gioia (davide.zerbetto@eng.it)
 */
public class GetQueryAction extends AbstractQbeEngineAction {
	
	public static final String SERVICE_NAME = "GET_QUERY_ACTION";
	
	public static final String SEARCH_TYPE = "SEARCH_TYPE";
	public static final String SEARCH_TYPE_FULLDUMP = "fullDump";
	public static final String SEARCH_TYPE_BYID = "byId";
	public static final String SEARCH_TYPE_BYNAME = "byName";
	public static final String SEARCH_TYPE_BYTYPE = "byType";
	public static final String SEARCH_FILTER = "SEARCH_FILTER";
	
	
	public String getActionName(){return SERVICE_NAME;}

	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(GetQueryAction.class);
    
	
	public void service(SourceBean request, SourceBean response) {
		
		String searchType;
		String searchFilter;
		QbeEngineInstance engineInstance;
		QueryCatalogue queryCatalogue;
		Set<Query> queries;
		JSONObject queryJSON;
		JSONObject responseJSON;
		JSONArray resultsJSON;
		
		logger.debug("IN");
		
		try {
			super.service(request, response);	
			
			searchType = getAttributeAsString(SEARCH_TYPE);		
			logger.debug("Parameter [" + SEARCH_TYPE + "] is equals to [" + searchType + "]");
			if(StringUtilities.isEmpty(searchType)) searchType = SEARCH_TYPE_FULLDUMP;
			
			searchFilter = getAttributeAsString(SEARCH_FILTER);	
			logger.debug("Parameter [" + SEARCH_FILTER + "] is equals to [" + searchFilter + "]");
			
			// retrieve first query from catalog
			engineInstance = getEngineInstance();
			queryCatalogue = engineInstance.getQueryCatalogue();
			
			queries = new HashSet();
			if(SEARCH_TYPE_FULLDUMP.equalsIgnoreCase(searchType)) {
				queries.addAll( queryCatalogue.getAllQueries(false) );
			} else if (SEARCH_TYPE_BYID.equalsIgnoreCase(searchType)) {
				queries.add(queryCatalogue.getQuery(searchFilter));
			} else if (SEARCH_TYPE_BYNAME.equalsIgnoreCase(searchType)) {
				queries.addAll(queryCatalogue.getQueryByName(searchFilter));
			} else if (SEARCH_TYPE_BYTYPE.equalsIgnoreCase(searchType)) {
				if(searchFilter.equalsIgnoreCase("first")) {
					queries.add(queryCatalogue.getFirstQuery());
				}
			}
			
			// serialize query
			resultsJSON = new JSONArray();
			Iterator<Query> it = queries.iterator();
			while(it.hasNext()) {
				Query query = it.next();
				queryJSON = (JSONObject)SerializerFactory.getSerializer("application/json").serialize(query, getEngineInstance().getDataSource(), getLocale());
				resultsJSON.put(queryJSON);
			}
			
			responseJSON = new JSONObject();
			responseJSON.put("results", resultsJSON);
			
			
			try {
				writeBackToClient( new JSONSuccess(responseJSON) );
			} catch (IOException e) {
				String message = "Impossible to write back the responce to the client";
				throw new SpagoBIEngineServiceException(getActionName(), message, e);
			}
		} catch(Throwable t) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException(getActionName(), getEngineInstance(), t);
		} finally {
			logger.debug("OUT");
		}		
	}

}
