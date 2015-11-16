/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
