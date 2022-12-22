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
package it.eng.qbe.dataset;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.qbe.datasource.DriverManager;
import it.eng.qbe.datasource.configuration.CompositeDataSourceConfiguration;
import it.eng.qbe.datasource.configuration.DataSetDataSourceConfiguration;
import it.eng.qbe.datasource.configuration.FileDataSourceConfiguration;
import it.eng.qbe.datasource.dataset.DataSetDataSource;
import it.eng.qbe.datasource.dataset.DataSetDriver;
import it.eng.qbe.datasource.jpa.JPADataSource;
import it.eng.qbe.model.accessmodality.IModelAccessModality;
import it.eng.qbe.query.Query;
import it.eng.qbe.query.TimeAggregationHandler;
import it.eng.qbe.query.catalogue.QueryCatalogue;
import it.eng.qbe.statement.AbstractQbeDataSet;
import it.eng.qbe.statement.IStatement;
import it.eng.qbe.statement.QbeDatasetFactory;
import it.eng.qbe.utility.CustomFunctionsSingleton;
import it.eng.qbe.utility.CustomizedFunctionsReader;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.bo.ConfigurableDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStoreFilter;
import it.eng.spagobi.tools.dataset.common.iterator.DataIterator;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.exceptions.DataSetNotLoadedYetException;
import it.eng.spagobi.tools.dataset.persist.IDataSetTableDescriptor;
import it.eng.spagobi.tools.dataset.utils.DatasetMetadataParser;
import it.eng.spagobi.tools.dataset.utils.datamart.IQbeDataSetDatamartRetriever;
import it.eng.spagobi.tools.datasource.bo.DataSourceFactory;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it), Alberto Ghedin (alberto.ghedin@eng.it)
 */
public class QbeDataSet extends ConfigurableDataSet {

	public static String DS_TYPE = "SbiQbeDataSet";

	private static Logger logger = Logger.getLogger(QbeDataSet.class);

	public static final String QBE_DATA_SOURCE = "qbeDataSource";

	public static final String QBE_DATAMARTS = "qbeDatamarts";
	public static final String QBE_JSON_QUERY = "qbeJSONQuery";
	public static final String QBE_SQL_QUERY = "qbeSQLQuery";

	protected IDataSet ds = null;
	protected String jsonQuery = null;
	protected String datamarts = null;
	protected Map attributes = null;
	protected Map params = null;
	protected JSONObject dataset2CacheTableName = null;

	protected boolean useCache = false;

	protected IDataSource dataSource = null;

	private IDataSet sourceDataset = null;

	public QbeDataSet() {
	}

	public QbeDataSet(SpagoBiDataSet dataSetConfig) {

		super(dataSetConfig);
		try {
			JSONObject jsonConf = ObjectUtils.toJSONObject(dataSetConfig.getConfiguration());
			this.setDatamarts((jsonConf.opt(QBE_DATAMARTS) != null) ? jsonConf.get(QBE_DATAMARTS).toString() : "");
			this.setJsonQuery((jsonConf.opt(QBE_JSON_QUERY) != null) ? jsonConf.get(QBE_JSON_QUERY).toString() : "");

		} catch (Exception e) {
			logger.error("Error while defining dataset configuration.  Error: " + e.getMessage());
		}

		setDatasourceInternal(dataSetConfig);

	}

	protected void setDatasourceInternal(SpagoBiDataSet dataSetConfig) {
		IDataSource dataSource = DataSourceFactory.getDataSource(dataSetConfig.getDataSource());
		this.setDataSource(dataSource);
	}

	public QbeDataSet(AbstractQbeDataSet ds) {
		this.ds = ds;
	}

	private void init() {
		if (ds == null) {
			UserProfile profile = getUserProfile();
			if (profile != null) {
				JSONObject jsonObj = new CustomizedFunctionsReader().getJSONCustomFunctionsVariable(profile);
				CustomFunctionsSingleton.getInstance().setCustomizedFunctionsJSON(jsonObj);
			}

			it.eng.qbe.datasource.IDataSource qbeDataSource = getQbeDataSource();
			QueryCatalogue catalogue = getCatalogue(jsonQuery, qbeDataSource);
			Query query = catalogue.getFirstQuery();
			setQuery(query);
			Query filteredQuery = filterQueryWithProfileAttributes(qbeDataSource, query);
			initDs(qbeDataSource, filteredQuery);
		}
	}

	public void initDs(it.eng.qbe.datasource.IDataSource qbeDataSource, Query query) {
		ds = QbeDatasetFactory.createDataSet(qbeDataSource.createStatement(query));
		ds.setUserProfile(getUserProfile());
		ds.setUserProfileAttributes(attributes);
		ds.setParamsMap(params);
		ds.setTransformerId(transformerId);
		ds.setTransformerCd(transformerCd);
		ds.setPivotColumnName(pivotColumnName);
		ds.setPivotColumnValue(pivotColumnValue);
		ds.setPivotRowName(pivotRowName);
		ds.setNumRows(numRows);
		ds.addDataStoreTransformers(dataStoreTransformers);
		ds.setPersisted(persisted);
		ds.setPersistTableName(persistTableName);
		ds.setDataSourceForReading(this.getDataSourceForReading());
		ds.setDataSourceForWriting(this.getDataSourceForWriting());
		ds.setDrivers(this.getDrivers());
	}

	public Query filterQueryWithProfileAttributes(it.eng.qbe.datasource.IDataSource qbeDataSource, Query query) {
		IModelAccessModality accessModality = qbeDataSource.getModelAccessModality();
		Query filteredQuery = accessModality.getFilteredStatement(query, qbeDataSource, attributes);
		return filteredQuery;
	}

	@Override
	public void loadData(int offset, int fetchSize, int maxResults) {
		init();
		Query qbeQuery = (Query) query;
		new TimeAggregationHandler(getQbeDataSource()).handleTimeFilters(qbeQuery);
		Map<String, Map<String, String>> inlineFilteredSelectFields = qbeQuery.getInlineFilteredSelectFields();
		if (inlineFilteredSelectFields != null && inlineFilteredSelectFields.size() > 0) {
			initDs(getQbeDataSource(), qbeQuery);
			ds.loadData(0, 0, -1);
		} else {
			ds.loadData(offset, fetchSize, maxResults);
		}
	}

	@Override
	public void loadData() {
		init();
		Query qbeQuery = (Query) query;
		Map<String, Map<String, String>> inlineFilteredSelectFields = qbeQuery.getInlineFilteredSelectFields();
		if (inlineFilteredSelectFields != null && inlineFilteredSelectFields.size() > 0) {
			ds.loadData(0, 0, -1);
		} else {
			ds.loadData();
		}
	}

	@Override
	public void setUserProfileAttributes(Map attributes) {
		this.attributes = attributes;
	}

	@Override
	public void setParamsMap(Map params) {
		if (this.params == null || this.params.isEmpty()) {
			this.params = params;
			return;
		}

		// keeping previous datamart retriever, if input map doesn't contain any
		IQbeDataSetDatamartRetriever previousRetriever = (IQbeDataSetDatamartRetriever) this.params.get(SpagoBIConstants.DATAMART_RETRIEVER);
		IQbeDataSetDatamartRetriever newRetriever = params == null ? null : (IQbeDataSetDatamartRetriever) params.get(SpagoBIConstants.DATAMART_RETRIEVER);

		this.params = params;
		if (this.params == null) {
			this.params = new HashMap();
		}

		if (previousRetriever != null && newRetriever == null) {
			this.params.put(SpagoBIConstants.DATAMART_RETRIEVER, previousRetriever);
		}
	}

	@Override
	public Map getParamsMap() {
		return this.params;
	}

	@Override
	public IDataStore getDataStore() {
		if (ds == null) {
			throw new DataSetNotLoadedYetException();
		}

		IDataStore dataStore = ds.getDataStore();
		Query qbeQuery = (Query) query;
		Map<String, Map<String, String>> inlineFilteredSelectFields = qbeQuery.getInlineFilteredSelectFields();
		if (inlineFilteredSelectFields != null && inlineFilteredSelectFields.size() > 0) {
			dataStore = new TimeAggregationHandler(getQbeDataSource()).handleTimeAggregations(qbeQuery, dataStore);
		}

		return dataStore;
	}

	public String getJsonQuery() {
		return this.jsonQuery;
	}

	public void setJsonQuery(String jsonQuery) {
		this.jsonQuery = jsonQuery;
	}

	public String getDatamarts() {
		return this.datamarts;
	}

	public void setDatamarts(String datamarts) {
		this.datamarts = datamarts;
	}

	@Override
	public void setDataSource(IDataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public IDataSource getDataSource() {
		return this.dataSource;
	}

	@Override
	public SpagoBiDataSet toSpagoBiDataSet() {
		SpagoBiDataSet sbd;

		sbd = super.toSpagoBiDataSet();

		sbd.setType(DS_TYPE);
		if (getDataSource() != null) {
			sbd.setDataSource(getDataSource().toSpagoBiDataSource());
		}

		return sbd;
	}

	@Override
	public void setPersisted(boolean persisted) {
		super.setPersisted(persisted);
		if (ds != null) {
			ds.setPersisted(persisted);
		}
	}

	public it.eng.qbe.datasource.IDataSource getQbeDataSource() {

		Map<String, Object> dataSourceProperties = new HashMap<String, Object>();

		String modelName = getDatamarts();
		List<String> modelNames = new ArrayList<String>();
		modelNames.add(modelName);
		dataSourceProperties.put("datasource", dataSource);
		dataSourceProperties.put("dblinkMap", new HashMap());

		if (this.getSourceDataset() != null) {
			List<IDataSet> dataSets = new ArrayList<IDataSet>();
			dataSets.add(this.getSourceDataset());
			dataSourceProperties.put(EngineConstants.ENV_DATASETS, dataSets);
		}

		if (dataSourceProperties.get(EngineConstants.ENV_DATASETS) != null) {
			return getDataSourceFromDataSet(dataSourceProperties, useCache);
		} else {
			return getORMDataSource(modelNames, dataSourceProperties, useCache);
		}

	}

	public it.eng.qbe.datasource.IDataSource getDataSourceFromDataSet(Map<String, Object> dataSourceProperties, boolean useCache) {

		it.eng.qbe.datasource.IDataSource dataSource;
		List<IDataSet> dataSets = (List<IDataSet>) dataSourceProperties.get(EngineConstants.ENV_DATASETS);
		dataSourceProperties.remove(EngineConstants.ENV_DATASETS);

		CompositeDataSourceConfiguration compositeConfiguration = new CompositeDataSourceConfiguration(DataSetDataSource.EMPTY_MODEL_NAME);
		Iterator<String> it = dataSourceProperties.keySet().iterator();
		while (it.hasNext()) {
			String propertyName = it.next();
			compositeConfiguration.loadDataSourceProperties().put(propertyName, dataSourceProperties.get(propertyName));
		}

		for (int i = 0; i < dataSets.size(); i++) {
			DataSetDataSourceConfiguration c = new DataSetDataSourceConfiguration((dataSets.get(i)).getLabel(), dataSets.get(i));
			compositeConfiguration.addSubConfiguration(c);
		}

		dataSource = DriverManager.getDataSource(DataSetDriver.DRIVER_ID, compositeConfiguration, useCache);

		return dataSource;
	}

	private it.eng.qbe.datasource.IDataSource getORMDataSource(List<String> dataMartNames, Map<String, Object> dataSourceProperties, boolean useCache) {

		File modelJarFile = null;
		List<File> modelJarFiles = new ArrayList<File>();
		CompositeDataSourceConfiguration compositeConfiguration = new CompositeDataSourceConfiguration();
		compositeConfiguration.loadDataSourceProperties().putAll(dataSourceProperties);

		IQbeDataSetDatamartRetriever retriever = this.getDatamartRetriever();
		if (retriever == null) {
			throw new SpagoBIRuntimeException("Missing datamart retriever, cannot proceed.");
		}
		modelJarFile = retriever.retrieveDatamartFile(dataMartNames.get(0));
		modelJarFiles.add(modelJarFile);
		compositeConfiguration.addSubConfiguration(new FileDataSourceConfiguration(dataMartNames.get(0), modelJarFile));

		logger.debug("OUT: Finish to load the data source for the model names " + dataMartNames + "..");
		return DriverManager.getDataSource(getDriverName(modelJarFile), compositeConfiguration, this.useCache);
	}

	private IQbeDataSetDatamartRetriever getDatamartRetriever() {
		if (this.params == null || this.params.isEmpty()) {
			return null;
		}
		IQbeDataSetDatamartRetriever retriever = (IQbeDataSetDatamartRetriever) this.params.get(SpagoBIConstants.DATAMART_RETRIEVER);
		return retriever;
	}

	/**
	 * Get the driver name (hibernate or jpa). It checks if the passed jar file contains the persistence.xml in the META-INF folder
	 *
	 * @param jarFile
	 *            a jar file with the model definition
	 * @return jpa if the persistence provder is JPA o hibernate otherwise
	 */
	private static String getDriverName(File jarFile) {
		logger.debug("IN: Check the driver name. Looking if " + jarFile + " is a jpa jar file..");
		JarInputStream zis;
		JarEntry zipEntry;
		String dialectName = null;
		boolean isJpa = false;

		try {
			FileInputStream fis = new FileInputStream(jarFile);
			zis = new JarInputStream(fis);
			while ((zipEntry = zis.getNextJarEntry()) != null) {
				logger.debug("Zip Entry is [" + zipEntry.getName() + "]");
				if (zipEntry.getName().equals("META-INF/persistence.xml")) {
					isJpa = true;
					break;
				}
				zis.closeEntry();
			}
			zis.close();
			if (isJpa) {
				dialectName = "jpa";
			} else {
				dialectName = "hibernate";
			}
		} catch (Throwable t) {
			logger.error("Impossible to read jar file [" + jarFile + "]", t);
			throw new SpagoBIRuntimeException("Impossible to read jar file [" + jarFile + "]", t);
		}

		logger.debug("OUT: " + jarFile + " has the dialect: " + dialectName);
		return dialectName;
	}

	public QueryCatalogue getCatalogue(String json, it.eng.qbe.datasource.IDataSource dataSource) {
		QueryCatalogue catalogue;
		JSONObject catalogueJSON;
		JSONArray queriesJSON;
		JSONObject queryJSON;
		Query query;

		catalogue = new QueryCatalogue();
		try {
			catalogueJSON = new JSONObject(json).getJSONObject("catalogue");
			queriesJSON = catalogueJSON.getJSONArray("queries");

			for (int i = 0; i < queriesJSON.length(); i++) {
				queryJSON = queriesJSON.getJSONObject(i);
				query = it.eng.qbe.query.serializer.SerializerFactory.getDeserializer("application/json").deserializeQuery(queryJSON, dataSource);
				catalogue.addQuery(query);
			}
		} catch (Throwable e) {
			throw new SpagoBIEngineRuntimeException("Impossible to deserialize catalogue", e);
		}

		return catalogue;
	}

	@Override
	public IDataSetTableDescriptor persist(String tableName, IDataSource dataSource) {
		init();
		return ((AbstractQbeDataSet) ds).persist(tableName, dataSource);
	}

	@Override
	public IDataStore getDomainValues(String fieldName, Integer start, Integer limit, IDataStoreFilter filter) {
		init();
		return ((AbstractQbeDataSet) ds).getDomainValues(fieldName, start, limit, filter);
	}

	@Override
	public String getSignature() {
		init();
		return ((AbstractQbeDataSet) ds).getSignature();
	}

	public boolean isUseCache() {
		return useCache;
	}

	public void setUseCache(boolean useCache) {
		this.useCache = useCache;
	}

	@Override
	public IMetaData getMetadata() {
		IMetaData metadata = null;
		try {
			DatasetMetadataParser dsp = new DatasetMetadataParser();
			metadata = dsp.xmlToMetadata(getDsMetadata());
		} catch (Exception e) {
			logger.error("Error loading the metadata", e);
			throw new SpagoBIEngineRuntimeException("Error loading the metadata", e);
		}
		return metadata;
	}

	@Override
	public void setMetadata(IMetaData metadata) {
		String metadataStr = null;
		DatasetMetadataParser dsp = new DatasetMetadataParser();
		metadataStr = dsp.metadataToXML(metadata);
		this.setDsMetadata(metadataStr);
	}

	public IDataSet getSourceDataset() {
		return sourceDataset;
	}

	public void setSourceDataset(IDataSet sourceDataset) {
		this.sourceDataset = sourceDataset;
	}

	@Override
	public String getDsType() {
		return DS_TYPE;
	}

	public it.eng.qbe.datasource.IDataSource getQbeDataSourceFromStmt() {
		init();
		return ((AbstractQbeDataSet) ds).getStatement().getDataSource();
	}

	@Override
	public DataIterator iterator() {
		init();
		return ds.iterator();
	}

	@Override
	public boolean isIterable() {
		// only underlying JPQLDataSet is iterable
		return getQbeDataSource() instanceof JPADataSource;
	}

	public IStatement getStatement() {
		init();
		return ((AbstractQbeDataSet) ds).getStatement();
	}

	public String getColumn(String columnName) {
		String result = columnName;

		for (int i = 0; i < getMetadata().getFieldCount(); i++) {
			IFieldMetaData fieldMeta = getMetadata().getFieldMeta(i);
			if (fieldMeta.getName().equals(columnName)) {
				result = fieldMeta.getAlias();
				break;
			}
		}

		return result;
	}

	@Override
	public Map<String, Object> getDrivers() {
		return super.getDrivers();
	}

	@Override
	public void setDrivers(Map<String, Object> drivers) {
		super.setDrivers(drivers);
	}
}
