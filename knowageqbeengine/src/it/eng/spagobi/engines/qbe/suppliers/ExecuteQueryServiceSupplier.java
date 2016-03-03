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
package it.eng.spagobi.engines.qbe.suppliers;

import it.eng.qbe.query.Query;
import it.eng.qbe.statement.IStatement;
import it.eng.qbe.statement.QbeDatasetFactory;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.engines.qbe.QbeEngineInstance;
import it.eng.spagobi.engines.qbe.services.core.ExecuteQueryAction;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class ExecuteQueryServiceSupplier {
	
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(ExecuteQueryAction.class);
    public static transient Logger auditlogger = Logger.getLogger("audit.query");
    
    public static void execute(QbeEngineInstance engineInstance) {
    	execute(engineInstance, engineInstance.getStatment());
    }
    
    public static void execute(QbeEngineInstance engineInstance, String queryId) {
    	Query query = engineInstance.getQueryCatalogue().getQuery(queryId);
    	execute(engineInstance, query);
    }
    
    public static void execute(QbeEngineInstance engineInstance, Query query) {
    	IStatement statement = engineInstance.getDataSource().createStatement( query );
    	execute(engineInstance, statement);
    }
    
	public static void execute(QbeEngineInstance engineInstance, IStatement statement) {
		
		Integer limit = null;
		Integer start = null;
		Integer maxSize = null;
		boolean isMaxResultsLimitBlocking = false;
		IDataStore dataStore = null;
		IDataSet dataSet = null;
		
		Integer resultNumber = null;
		
		
		try {
			statement.setParameters( engineInstance.getEnv() );
			
			String jpaQueryStr = statement.getQueryString();
			//String sqlQuery = statement.getSqlQueryString();
			logger.debug("Executable query (HQL/JPQL): [" +  jpaQueryStr+ "]");
			//logger.debug("Executable query (SQL): [" + sqlQuery + "]");
			UserProfile userProfile = (UserProfile)engineInstance.getEnv().get(EngineConstants.ENV_USER_PROFILE);
			auditlogger.info("[" + userProfile.getUserId() + "]:: HQL/JPQL: " + jpaQueryStr);
			//auditlogger.info("[" + userProfile.getUserId() + "]:: SQL: " + sqlQuery);
			
			
			logger.debug("Executing query ...");
			dataSet = QbeDatasetFactory.createDataSet(statement);
			dataSet.setAbortOnOverflow(isMaxResultsLimitBlocking);
				
			Map userAttributes = new HashMap();
			UserProfile profile = (UserProfile)engineInstance.getEnv().get(EngineConstants.ENV_USER_PROFILE);
			Iterator it = profile.getUserAttributeNames().iterator();
			while(it.hasNext()) {
				String attributeName = (String)it.next();
				Object attributeValue = profile.getUserAttribute(attributeName);
				userAttributes.put(attributeName, attributeValue);
			}
			dataSet.addBinding("attributes", userAttributes);
			dataSet.addBinding("parameters", engineInstance.getEnv());
			dataSet.loadData(start, limit, (maxSize == null? -1: maxSize.intValue()));
				
			dataStore = dataSet.getDataStore();
			Assert.assertNotNull(dataStore, "The dataStore returned by loadData method of the class [" + dataSet.getClass().getName()+ "] cannot be null");
			
			logger.debug("Query executed succesfully");
			
			resultNumber = (Integer)dataStore.getMetaData().getProperty("resultNumber");
			Assert.assertNotNull(resultNumber, "property [resultNumber] of the dataStore returned by loadData method of the class [" + dataSet.getClass().getName()+ "] cannot be null");
			logger.debug("Total records: " + resultNumber);			
			
			
			boolean overflow = maxSize != null && resultNumber >= maxSize;
			if (overflow) {
				logger.warn("Query results number [" + resultNumber + "] exceeds max result limit that is [" + maxSize + "]");
		//		auditlogger.info("[" + userProfile.getUserId() + "]:: max result limit [" + maxSize + "] exceeded with SQL: " + sqlQuery);
			}
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException(t);
		}
	}
}
