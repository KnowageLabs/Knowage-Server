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
package it.eng.spagobi.engines.qbe.services.core.catalogue;

import it.eng.qbe.query.Query;
import it.eng.qbe.query.catalogue.QueryCatalogue;
import it.eng.qbe.query.serializer.SerializerFactory;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.qbe.QbeEngineInstance;
import it.eng.spagobi.engines.qbe.services.core.AbstractQbeEngineAction;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * This action is responsible to retrieve the first query contained into the catalog.
 * It is used for Read-only users (a Read-only user is able only to execute the first query of the catalog)
 * 
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 * 
 * @deprecated use GetQueryAction?SEARCH_TYPE=byType&SEARCH_FILTER=first
 */
public class GetFirstQueryAction extends AbstractQbeEngineAction {
	
	public static final String SERVICE_NAME = "GET_FIRST_QUERY_ACTION";
	public String getActionName(){return SERVICE_NAME;}

	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(GetFirstQueryAction.class);
    
	
	public void service(SourceBean request, SourceBean response) {
		
		logger.debug("IN");
		
		try {
			super.service(request, response);	
			// retrieve first query from catalog
			QbeEngineInstance engineInstance = getEngineInstance();
			QueryCatalogue queryCatalogue = engineInstance.getQueryCatalogue();
			Query query = queryCatalogue.getFirstQuery();
			// serialize query
			JSONObject queryJSON = (JSONObject)SerializerFactory.getSerializer("application/json").serialize(query, getEngineInstance().getDataSource(), getLocale());
			
			try {
				writeBackToClient( new JSONSuccess(queryJSON) );
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
