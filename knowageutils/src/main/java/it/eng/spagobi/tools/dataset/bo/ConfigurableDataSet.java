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
package it.eng.spagobi.tools.dataset.bo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.dataset.common.behaviour.QuerableBehaviour;
import it.eng.spagobi.tools.dataset.common.dataproxy.IDataProxy;
import it.eng.spagobi.tools.dataset.common.datareader.FileDatasetCsvDataReader;
import it.eng.spagobi.tools.dataset.common.datareader.IDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.UnreachableCodeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public abstract class ConfigurableDataSet extends AbstractDataSet {

	IDataReader dataReader;
	IDataProxy dataProxy;
	IDataStore dataStore;

	protected boolean abortOnOverflow;
	protected Map bindings;
	private boolean calculateResultNumberOnLoad = true;

	Map<String, Object> userProfileParameters;

	private static transient Logger logger = Logger.getLogger(ConfigurableDataSet.class);

	public ConfigurableDataSet() {
		super();
		userProfileParameters = new HashMap<String, Object>();
	}

	public ConfigurableDataSet(SpagoBiDataSet dataSetConfig) {
		super(dataSetConfig);
		userProfileParameters = new HashMap<String, Object>();
	}

	/**
	 * utility method used to clean different parameters values that should be null
	 *
	 * @param params parameters map
	 * @return cleaned params map
	 */
	private Map cleanNullParametersValues(Map params) {
		if (params == null) {
			return null;
		}
		Iterator keys = params.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			Object val = params.get(key);
			if (val instanceof String) {
				if (val != null && (val.equals("") || val.equals("''"))) {
					params.put(key, null);
				}
			}
		}

		return params;
	}

	@Override
	public void loadData(int offset, int fetchSize, int maxResults) {

		if (this.isPersisted() || this.isFlatDataset() || this.isPreparedDataSet()) {
			JDBCDataSet dataset = new JDBCDataSet();
			dataset.setDataSource(getDataSourceForReading());
			dataset.setQuery("select * from " + getTableNameForReading());
			dataset.loadData(offset, fetchSize, maxResults);
			dataStore = dataset.getDataStore();
		} else {

			Map parameters = cleanNullParametersValues(getParamsMap());

			dataProxy.setParameters(parameters);

			dataProxy.setProfile(getUserProfileAttributes());
			dataProxy.setResPath(resPath);

			// check if the proxy is able to manage results pagination
			if (dataProxy.isOffsetSupported()) {
				dataProxy.setOffset(offset);
			} else if (dataReader.isOffsetSupported()) {
				dataReader.setOffset(offset);
			} else {

			}

			if (dataProxy.isFetchSizeSupported()) {
				dataProxy.setFetchSize(fetchSize);
			} else if (dataReader.isFetchSizeSupported()) {
				dataReader.setFetchSize(fetchSize);
			} else {

			}

			// check if the proxy is able to manage results limit
			if (dataProxy.isMaxResultsSupported()) {
				dataProxy.setMaxResults(maxResults);
			} else if (dataReader.isMaxResultsSupported()) {
				dataReader.setMaxResults(maxResults);
			} else {

			}

			if (hasBehaviour(QuerableBehaviour.class.getName())) {
				QuerableBehaviour querableBehaviour = (QuerableBehaviour) getBehaviour(QuerableBehaviour.class.getName());
				String stm = querableBehaviour.getStatement();
				dataProxy.setStatement(stm);
			}

			dataProxy.setCalculateResultNumberOnLoad(this.isCalculateResultNumberOnLoadEnabled());

			try {
				// in file dataset metadata can be set by users
				if (dataReader instanceof FileDatasetCsvDataReader)
					((FileDatasetCsvDataReader) dataReader).setMetaData(this.getMetadata());
			} catch (Exception e) {
				// Yes, it's mute
				logger.debug("Cannot set user metadata", e);
			}

			dataStore = dataProxy.load(dataReader);

			if (hasDataStoreTransformer()) {
				getDataStoreTransformer().transform(dataStore);
			}

		}
	}

	@Override
	public IDataStore getDataStore() {
		return this.dataStore;
	}

	public IDataReader getDataReader() {
		return dataReader;
	}

	public void setDataReader(IDataReader dataReader) {
		this.dataReader = dataReader;
	}

	public IDataProxy getDataProxy() {
		return dataProxy;
	}

	public void setDataProxy(IDataProxy dataProxy) {
		this.dataProxy = dataProxy;
	}

	@Override
	public Map<String, Object> getUserProfileAttributes() {
		return userProfileParameters;
	}

	@Override
	public void setUserProfileAttributes(Map<String, Object> parameters) {
		this.userProfileParameters = parameters;
	}

	@Override
	public void setAbortOnOverflow(boolean abortOnOverflow) {
		this.abortOnOverflow = abortOnOverflow;
	}

	@Override
	public void addBinding(String bindingName, Object bindingValue) {
		bindings.put(bindingName, bindingValue);
	}

	@Override
	public IDataStore test() {
		logger.debug("IN");
		loadData();
		logger.debug("OUT");
		return getDataStore();
	}

	@Override
	public IDataStore test(int offset, int fetchSize, int maxResults) {
		logger.debug("IN");
		loadData(offset, fetchSize, maxResults);
		logger.debug("OUT");
		return getDataStore();

	}

	@Override
	public IDataStore decode(IDataStore datastore) {
		return datastore;
	}

	@Override
	public boolean isCalculateResultNumberOnLoadEnabled() {
		return calculateResultNumberOnLoad;
	}

	@Override
	public void setCalculateResultNumberOnLoad(boolean enabled) {
		calculateResultNumberOnLoad = enabled;
	}

	@Override
	public String getSignature() {
		Map paramsMap = getParamsMap();
		if (paramsMap == null) {
			paramsMap = new HashMap();
		}

		Tenant tenant = TenantManager.getTenant();
		if (tenant == null) {
			throw new SpagoBIRuntimeException("Tenant is not set");
		}

		StringBuilder sb = new StringBuilder();
		sb.append(getConfiguration());
		sb.append("_");
		sb.append(paramsMap);
		sb.append("_");
		if (hasBehaviour(QuerableBehaviour.class.getName())) {
			QuerableBehaviour querableBehaviour = (QuerableBehaviour) getBehaviour(QuerableBehaviour.class.getName());
			sb.append(querableBehaviour.getStatement());
			sb.append("_");
		}
		if (getDataSource() != null && getDataSource().checkIsJndi() && getDataSource().checkIsMultiSchema()) {
			sb.append(getDataSource().getJNDIRunTime(getUserProfile()));
			sb.append("_");
		}
		sb.append(tenant.getName());
		return sb.toString();
	}

	@Override
	public void setDataSource(IDataSource dataSource) {
		throw new UnreachableCodeException("setDataSource method not implemented in class " + this.getClass().getName() + "!!!!");
	}

	@Override
	public IDataSource getDataSource() {
		throw new UnreachableCodeException("getDataSource method not implemented in class " + this.getClass().getName() + "!!!!");
	}

}
