/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.services.dataset;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.datasource.dataset.DataSetDataSource;
import it.eng.qbe.query.ISelectField;
import it.eng.qbe.query.Query;
import it.eng.qbe.statement.IStatement;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.engines.qbe.services.core.AbstractQbeEngineAction;
import it.eng.spagobi.engines.qbe.services.core.ExecuteQueryAction;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.jdbc.util.BasicFormatterImpl;
import org.json.JSONObject;

/**
 * The Class GetSQLQueryAction.
 * 
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class GetSQLQueryAction extends AbstractQbeEngineAction {

	// INPUT PARAMETERS

	// OUTPUT PARAMETERS

	// SESSION PARAMETRES

	// AVAILABLE PUBLISHERS

	/** Logger component. */
	private static transient Logger logger = Logger.getLogger(GetSQLQueryAction.class);

	public static final String ENGINE_NAME = "SpagoBIQbeEngine";
	public static final String REPLACE_PARAMETERS_WITH_QUESTION = "replaceParametersWithQuestion";

	@Override
	public void service(SourceBean request, SourceBean response) {

		Query query = null;
		IStatement statement = null;

		logger.debug("IN");

		try {
			super.service(request, response);

			boolean replaceParametersWithQuestion = getAttributeAsBoolean("replaceParametersWithQuestion");

			// retrieving query specified by id on request
			query = getEngineInstance().getActiveQuery();
			if (query == null) {
				query = getEngineInstance().getQueryCatalogue().getFirstQuery();
				getEngineInstance().setActiveQuery(query);
			}
			Assert.assertNotNull(query, "Query not found!!");

			if (replaceParametersWithQuestion) {
				// promptable filters values may come with request (read-only
				// user modality)
				ExecuteQueryAction.updatePromptableFiltersValue(query, this, true);
			} else {
				// promptable filters values may come with request (read-only
				// user modality)
				ExecuteQueryAction.updatePromptableFiltersValue(query, this);
			}

			statement = getEngineInstance().getStatment();
			statement.setParameters(getEnv());

			JSONObject toReturn = new JSONObject();

			IDataSource dataSource = statement.getDataSource();
			if (dataSource instanceof DataSetDataSource) {
				String sqlQuery = statement.getSqlQueryString();
				String sqlQueryFormatted = formatQueryString(sqlQuery);

				logger.debug("Executable query (SQL): [" + sqlQuery + "]");

				toReturn.put("sql", sqlQuery);
				toReturn.put("sqlFormatted", sqlQueryFormatted);
			} else {
				String jpaQueryStr = statement.getQueryString();
				String sqlQuery = statement.getSqlQueryString();
				String jpaQueryStrFormatted = formatQueryString(addAliasInJqpl(query, jpaQueryStr));
				String sqlQueryFormatted = formatQueryString(addAliasInSql(query, sqlQuery));

				logger.debug("Executable query (HQL/JPQL): [" + jpaQueryStr + "]");
				logger.debug("Executable query (SQL): [" + sqlQuery + "]");

				toReturn.put("sql", sqlQuery);
				toReturn.put("jpqlFormatted", jpaQueryStrFormatted);
				toReturn.put("sqlFormatted", sqlQueryFormatted);
			}

			try {
				writeBackToClient(new JSONSuccess(toReturn));
			} catch (IOException e) {
				String message = "Impossible to write back the responce to the client";
				throw new SpagoBIEngineServiceException(getActionName(), message, e);
			}

		} catch (Throwable t) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException(getActionName(), getEngineInstance(), t);
		} finally {
			logger.debug("OUT");
		}

	}

	/**
	 * Get the query string of the passed Query. The returned query is formatted
	 * 
	 * @param dataSource
	 * @param query
	 * @return
	 */
	public String formatQueryString(String queryString) {
		String formattedQuery;
		BasicFormatterImpl fromatter;

		if (queryString == null || queryString.equals("")) {
			logger.error("Impossible to get the query string because the query is null");
			return "";
		}

		fromatter = new BasicFormatterImpl();
		formattedQuery = fromatter.format(queryString);
		return StringUtilities.fromStringToHTML(formattedQuery);
	}

	public String addAliasInSql(Query query, String queryString) {
		List<ISelectField> selectFields = query.getSelectFields(true);
		if (selectFields != null) {
			for (int i = 0; i < selectFields.size(); i++) {
				int startColAlias = queryString.indexOf("as col_");
				int endColAlias = queryString.indexOf(",", startColAlias);
				int fromPosition = queryString.indexOf(" from ");
				if (endColAlias == -1 || fromPosition < endColAlias) {
					endColAlias = fromPosition;
				}
				String fieldAlias = selectFields.get(i).getAlias();
				if (fieldAlias == null) {
					fieldAlias = selectFields.get(i).getName();
					fieldAlias = fieldAlias.replace(" ", "_");
					fieldAlias = "`" + fieldAlias + "`";
				}
				queryString = queryString.substring(0, startColAlias) + " as " + fieldAlias + queryString.substring(endColAlias);
			}
		}
		return queryString;
	}

	public String addAliasInJqpl(Query query, String queryString) {
		List<ISelectField> selectFields = query.getSelectFields(true);
		if (selectFields != null) {
			String[] splitted = queryString.split(",");
			for (int i = 0; i < selectFields.size(); i++) {

				String fieldAlias = selectFields.get(i).getAlias();
				if (fieldAlias == null) {
					fieldAlias = selectFields.get(i).getName();
					fieldAlias = fieldAlias.replace(" ", "_");
					fieldAlias = "`" + fieldAlias + "`";
				}

				if (selectFields.size() - 1 == i) {
					int fromPosition = queryString.indexOf(" FROM ");
					queryString = queryString.substring(0, fromPosition) + "as " + fieldAlias + queryString.substring(fromPosition);

				} else {
					String queryPeace = splitted[i];
					int startColAlias = queryString.indexOf(queryPeace + ",");

					queryString = queryString.substring(0, startColAlias + queryPeace.length()) + "as " + fieldAlias
							+ queryString.substring(startColAlias + queryPeace.length());
				}
			}
		}
		return queryString;
	}

}
