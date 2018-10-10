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
package it.eng.spagobi.engines.qbe;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.query.Query;
import it.eng.qbe.query.catalogue.QueryCatalogue;
import it.eng.qbe.statement.AbstractQbeDataSet;
import it.eng.qbe.statement.IStatement;
import it.eng.qbe.statement.QbeDatasetFactory;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.engines.qbe.datasource.QbeDataSourceManager;
import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.utilities.engines.AbstractEngineInstance;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.IEngineAnalysisState;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class QbeEngineInstance extends AbstractEngineInstance {

	Locale locale;
	IDataSource dataSource;
	QueryCatalogue queryCatalogue;
	String activeQueryId;
	IDataSet dataSet;

	// executable version of the query. cached here for performance reasons (i.e. avoid query re-compilation
	// over result-set paging)
	IStatement statement;

	boolean isTechnicalUser;
	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(QbeEngineInstance.class);

	protected QbeEngineInstance(Map env) throws QbeEngineException {
		super(env);

		logger.debug("IN");

		this.locale = this.getLocale();

		queryCatalogue = new QueryCatalogue();
		queryCatalogue.addQuery(new Query());

		it.eng.spagobi.tools.datasource.bo.IDataSource dataSrc = (it.eng.spagobi.tools.datasource.bo.IDataSource) env.get(EngineConstants.ENV_DATASOURCE);
		logger.debug("Datasource is " + dataSrc);

		Map<String, Object> dataSourceProperties = new HashMap<String, Object>();


		dataSourceProperties.put("metadataServiceProxy", env.get(EngineConstants.ENV_METAMODEL_PROXY));
		dataSourceProperties.put(EngineConstants.ENV_DATASETS, env.get(EngineConstants.ENV_DATASETS));

		// relation for federated datasets
		dataSourceProperties.put(EngineConstants.ENV_RELATIONS, env.get(EngineConstants.ENV_RELATIONS));

		dataSourceProperties.put("datasource", dataSrc);
		dataSourceProperties.put(EngineConstants.MODEL_NAME, env.get(EngineConstants.MODEL_NAME));
		if( env.get(EngineConstants.MODEL_NAME)==null){
			dataSourceProperties.put(EngineConstants.MODEL_NAME, env.get("DATAMART_NAME"));
		}

		dataSource = QbeDataSourceManager.getInstance().getDataSource(dataSourceProperties, QbeEngineConfig.getInstance().isDataSourceCacheEnabled());

		validate();

		logger.debug("OUT");
	}



	@Override
	public void validate() throws QbeEngineException {
		return;
	}

	public IDataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(IDataSource dataSource) {
		this.dataSource = dataSource;
	}

	public QueryCatalogue getQueryCatalogue() {
		return queryCatalogue;
	}

	public QueryCatalogue getQueryCatalogue(Locale locale) {
		return queryCatalogue;
	}

	public void setQueryCatalogue(QueryCatalogue queryCatalogue) {
		this.queryCatalogue = queryCatalogue;
	}

	private String getActiveQueryId() {
		return activeQueryId;
	}

	private void setActiveQueryId(String activeQueryId) {
		this.activeQueryId = activeQueryId;
	}

	public Query getActiveQuery() {
		return getQueryCatalogue().getQuery(getActiveQueryId());
	}

	public void setActiveQuery(Query query) {
		setActiveQueryId(query.getId());
		this.statement = getDataSource().createStatement(query);
	}

	public void setActiveQuery(String queryId) {
		Query query;

		query = getQueryCatalogue().getQuery(queryId);
		if (query != null) {
			setActiveQueryId(query.getId());
			this.statement = getDataSource().createStatement(query);
		}
	}

	public void resetActiveQuery() {
		setActiveQueryId(null);
		setStatment(null);
	}

	public IStatement getStatment() {
		return statement;
	}

	public void setStatment(IStatement statment) {
		this.statement = statment;
	}


	/**
	 * Builds a IDataSet starting from the active query.
	 *
	 * @return the data set representation of the active query
	 */
	public IDataSet getActiveQueryAsDataSet() {
		logger.debug("Getting the dataset from the query ");
		try {

			if (this.dataSet == null) {

				dataSet = QbeDatasetFactory.createDataSet(statement);
				boolean isMaxResultsLimitBlocking = QbeEngineConfig.getInstance().isMaxResultLimitBlocking();
				dataSet.setAbortOnOverflow(isMaxResultsLimitBlocking);

			} else {

				((AbstractQbeDataSet) dataSet).setStatement(statement);

			}

			Map userAttributes = new HashMap();
			UserProfile userProfile = (UserProfile) this.getEnv().get(EngineConstants.ENV_USER_PROFILE);
			userAttributes.putAll(userProfile.getUserAttributes());
			userAttributes.put(SsoServiceInterface.USER_ID, userProfile.getUserId().toString());

			dataSet.addBinding("attributes", userAttributes);
			dataSet.addBinding("parameters", this.getEnv());
			dataSet.setUserProfileAttributes(userAttributes);

			dataSet.setParamsMap(this.getEnv());

		} catch (Exception e) {
			logger.debug("Error getting the data set from the query");
			throw new SpagoBIRuntimeException("Error getting the data set from the query", e);
		}
		logger.debug("Dataset correctly taken from the query ");
		return dataSet;
	}

	public boolean isTechnicalUser() {
		return isTechnicalUser;
	}

	public void setTechnicalUser(boolean isTechnicalUser) {
		this.isTechnicalUser = isTechnicalUser;
	}

	public Locale getLocale() {
		return (Locale) this.getEnv().get(EngineConstants.ENV_LOCALE);
	}


	@Override
	public IEngineAnalysisState getAnalysisState() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void setAnalysisState(IEngineAnalysisState analysisState) {
		// TODO Auto-generated method stub

	}

}
