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
import it.eng.qbe.statement.IStatement;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.qbe.services.core.AbstractQbeEngineAction;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.service.JSONAcknowledge;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * This action  validate queries stored in current catalogues.
 * It actually executes the query .
 */
public class ValidateCatalogueAction extends AbstractQbeEngineAction {
	
	public static final String SERVICE_NAME = "VALIDATE_CATALOGUE_ACTION";
	public String getActionName(){return SERVICE_NAME;}
	
	// INPUT PARAMETERS
	// no input
	
	public static transient Logger logger = Logger.getLogger(ValidateCatalogueAction.class);
	
	public void service(SourceBean request, SourceBean response) {
		Query query;
		IStatement statement;
		boolean validationResult = false;
		String jpaQueryStr;
		String sqlQueryStr;
		
		logger.debug("IN");
		
		try {
			super.service(request, response);
						
			Set queries = getEngineInstance().getQueryCatalogue().getAllQueries(false);
			logger.debug("Query catalogue contains [" + queries.size() + "] first-class query");
			
			Iterator it = queries.iterator();
			while(it.hasNext()) {
				query = (Query)it.next();
				logger.debug("Validating query [" + query.getName() +"] ...");
				
				statement = getEngineInstance().getDataSource().createStatement( query );
				statement.setParameters( getEnv() );
				
				jpaQueryStr = statement.getQueryString();
				//sqlQueryStr = statement.getSqlQueryString();
				logger.debug("Validating query (HQL/JPQL): [" +  jpaQueryStr+ "]");
				//logger.debug("Validating query (SQL): [" + sqlQueryStr + "]");
				
				try {
					//statement.execute(0, 1, 1, true);
					logger.debug("Query [" + query.getName() + "] validated sucesfully");
				} catch (Throwable t) {
					logger.debug("Query [" + query.getName() + "] is not valid");
					throw new SpagoBIEngineServiceException(getActionName(), "Query [" + query.getName() + "] is not valid", t);
				}
			}
			
			try {
				writeBackToClient( new JSONAcknowledge() );
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
