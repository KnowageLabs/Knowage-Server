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
package it.eng.spagobi.engines.qbe.services.core;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import it.eng.qbe.query.Query;
import it.eng.qbe.query.serializer.SerializerFactory;
import it.eng.qbe.serializer.SerializationException;
import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFAbstractError;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineAnalysisMetadata;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.service.JSONSuccess;

/**
 * This action is responsible to Persist the current working query represented by the object ISingleDataMartWizardObject in session.
 */
public class SaveQueryAction extends AbstractQbeEngineAction {

	// INPUT PARAMETERS
	public static final String QUERY_NAME = "queryName";
	public static final String QUERY_DESCRIPTION = "queryDescription";
	public static final String QUERY_SCOPE = "queryScope";
	public static final String QUERY = "query";

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(SaveQueryAction.class);

	@Override
	public void service(SourceBean request, SourceBean response) {

		String queryName = null;
		String queryDescritpion = null;
		String queryScope = null;
		String jsonEncodedQuery = null;
		EngineAnalysisMetadata analysisMetadata = null;
		String result = null;

		logger.debug("IN");

		try {
			super.service(request, response);

			// validate input that cames from the clien side according to rules spicified in validation.xml
			// todo: move this activity in the parent abstract class and hide input validation from actual
			// service implementation
			validateInput();

			queryName = getAttributeAsString(QUERY_NAME);
			logger.debug(QUERY_NAME + ": " + queryName);
			queryDescritpion = getAttributeAsString(QUERY_DESCRIPTION);
			logger.debug(QUERY_DESCRIPTION + ": " + queryDescritpion);
			queryScope = getAttributeAsString(QUERY_SCOPE);
			logger.debug(QUERY_SCOPE + ": " + queryScope);
			jsonEncodedQuery = getAttributeAsString(QUERY);
			logger.debug(QUERY + ": " + jsonEncodedQuery);

			Assert.assertNotNull(getEngineInstance(), "It's not possible to execute " + this.getActionName()
					+ " service before having properly created an instance of EngineInstance class");
			Assert.assertTrue(!StringUtils.isEmpty(queryName), "Input parameter [" + QUERY_NAME
					+ "] cannot be null or empty in oder to execute " + this.getActionName() + " service");
			Assert.assertTrue(!StringUtils.isEmpty(queryDescritpion), "Input parameter [" + QUERY_DESCRIPTION
					+ "] cannot be null or empty in oder to execute " + this.getActionName() + " service");
			Assert.assertTrue(!StringUtils.isEmpty(queryScope), "Input parameter [" + QUERY_SCOPE
					+ "] cannot be null or empty in oder to execute " + this.getActionName() + " service");
			Assert.assertTrue(!StringUtils.isEmpty(jsonEncodedQuery), "Input parameter [" + QUERY
					+ "] cannot be null or empty in oder to execute " + this.getActionName() + " service");

			analysisMetadata = getEngineInstance().getAnalysisMetadata();
			analysisMetadata.setName(queryName);
			analysisMetadata.setDescription(queryDescritpion);

			if (EngineAnalysisMetadata.PUBLIC_SCOPE.equalsIgnoreCase(queryScope)) {
				analysisMetadata.setScope(EngineAnalysisMetadata.PUBLIC_SCOPE);
			} else if (EngineAnalysisMetadata.PRIVATE_SCOPE.equalsIgnoreCase(queryScope)) {
				analysisMetadata.setScope(EngineAnalysisMetadata.PRIVATE_SCOPE);
			} else {
				Assert.assertUnreachable(
						"Value [" + queryScope + "] is not valid for the input parameter " + QUERY_SCOPE);
			}

			Query query = null;
			try {
				query = SerializerFactory.getDeserializer("application/json").deserializeQuery(jsonEncodedQuery,
						getEngineInstance().getDataSource());
				// query = QueryEncoder.decode(queryRecords, queryFilters, queryFilterExp, getDatamartModel());
			} catch (SerializationException e) {
				String message = "Impossible to decode query string comming from client";
				throw new SpagoBIEngineServiceException(getActionName(), message, e);
			}

			Query queryBkp = getEngineInstance().getActiveQuery();
			query.setId(queryBkp.getId());
			getEngineInstance().setActiveQuery(query);
			result = saveAnalysisState();
			getEngineInstance().setActiveQuery(queryBkp);

			if (!result.trim().toLowerCase().startsWith("ok")) {
				throw new SpagoBIEngineServiceException(getActionName(), result);
			}

			try {
				writeBackToClient(new JSONSuccess(result));
			} catch (IOException e) {
				String message = "Impossible to write back the responce to the client";
				throw new SpagoBIEngineServiceException(getActionName(), message, e);
			}

		} catch (Throwable t) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException(getActionName(),
					getEngineInstance(), t);
		} finally {
			logger.debug("OUT");
		}
	}

	public void validateInput() {
		EMFErrorHandler errorHandler = getErrorHandler();
		if (!errorHandler.isOKBySeverity(EMFErrorSeverity.ERROR)) {
			Collection errors = errorHandler.getErrors();
			Iterator it = errors.iterator();
			while (it.hasNext()) {
				EMFAbstractError error = (EMFAbstractError) it.next();
				if (error.getSeverity().equals(EMFErrorSeverity.ERROR)) {
					throw new SpagoBIEngineServiceException(getActionName(), error.getMessage(), null);
				}
			}
		}
	}
}
