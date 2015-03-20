/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.services.core;

import it.eng.qbe.statement.IStatement;
import it.eng.qbe.statement.QbeDatasetFactory;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * This action is responsible validate current query.
 * It actually executes the query .
 */
public class ValidateQueryAction extends AbstractQbeEngineAction {
	
	// INPUT PARAMETERS
	public static final String QUERY = "query";
	
	public static transient Logger logger = Logger.getLogger(ValidateQueryAction.class);
	
	public void service(SourceBean request, SourceBean response) {
		
		logger.debug("IN");
		
		try {
			super.service(request, response);
			
			boolean validationResult = false;
			IStatement statement = getEngineInstance().getStatment();	
			statement.setParameters( getEnv() );
			String jpaQueryStr = statement.getQueryString();
		//	String sqlQuery = statement.getSqlQueryString();
			logger.debug("Validating query (HQL/JPQL): [" +  jpaQueryStr+ "]");
		//	logger.debug("Validating query (SQL): [" + sqlQuery + "]");
			try {
				IDataSet dataSet = QbeDatasetFactory.createDataSet(statement);
				
				Map userAttributes = new HashMap();
				UserProfile profile = (UserProfile)this.getEnv().get(EngineConstants.ENV_USER_PROFILE);
				Iterator it = profile.getUserAttributeNames().iterator();
				while(it.hasNext()) {
					String attributeName = (String)it.next();
					Object attributeValue = profile.getUserAttribute(attributeName);
					userAttributes.put(attributeName, attributeValue);
				}
				dataSet.addBinding("attributes", userAttributes);
				dataSet.addBinding("parameters", this.getEnv());
				dataSet.loadData(0, 1, 1);
				
				logger.info("Query execution did not throw any exception. Validation successful.");
				validationResult = true;
			} catch (Throwable t) {
				logger.info("Query execution thrown an exception. Validation failed.");
				logger.debug(t);
				validationResult = false;
			}
			JSONObject result = new JSONObject();
			result.put("validationResult", validationResult);
			writeBackToClient( new JSONSuccess(result) );
		} catch(Throwable t) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException(getActionName(), getEngineInstance(), t);
		} finally {
			logger.debug("OUT");
		}
		
	}
}
