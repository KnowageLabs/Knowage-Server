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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.qbe.datasource.DriverManager;
import it.eng.qbe.datasource.configuration.CompositeDataSourceConfiguration;
import it.eng.qbe.datasource.configuration.DataSetDataSourceConfiguration;
import it.eng.qbe.datasource.configuration.FileDataSourceConfiguration;
import it.eng.qbe.datasource.dataset.DataSetDataSource;
import it.eng.qbe.datasource.dataset.DataSetDriver;
import it.eng.qbe.model.structure.HierarchicalDimensionField;
import it.eng.qbe.model.structure.Hierarchy;
import it.eng.qbe.model.structure.HierarchyLevel;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.query.ExpressionNode;
import it.eng.qbe.query.ISelectField;
import it.eng.qbe.query.Query;
import it.eng.qbe.query.SimpleSelectField;
import it.eng.qbe.query.WhereField;
import it.eng.qbe.query.WhereField.Operand;
import it.eng.qbe.query.catalogue.QueryCatalogue;
import it.eng.qbe.query.serializer.json.QuerySerializationConstants;
import it.eng.qbe.statement.AbstractQbeDataSet;
import it.eng.qbe.statement.IStatement;
import it.eng.qbe.statement.QbeDatasetFactory;
import it.eng.qbe.utility.TemporalRecord;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.bo.ConfigurableDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStoreFilter;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
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

	private static transient Logger logger = Logger.getLogger(QbeDataSet.class);

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
		// this.setDatamarts(dataSetConfig.getDatamarts());
		// this.setJsonQuery(dataSetConfig.getJsonQuery());

		setDatasourceInternal(dataSetConfig);

		// (dataSetConfig.getDataSourcePersist() != null) {
		// IDataSource dataSourcePersist = DataSourceFactory.getDataSource( dataSetConfig.getDataSourcePersist() ) ;
		// this.setDataSourcePersist(dataSourcePersist);
		// }

	}
	
	protected void setDatasourceInternal(SpagoBiDataSet dataSetConfig){
		IDataSource dataSource = DataSourceFactory.getDataSource(dataSetConfig.getDataSource());
		this.setDataSource(dataSource);
	}

	public QbeDataSet(AbstractQbeDataSet ds) {
		this.ds = ds;
	}

	private void init() {
		if (ds == null) {
			it.eng.qbe.datasource.IDataSource qbeDataSource = getQbeDataSource();
			QueryCatalogue catalogue = getCatalogue(jsonQuery, qbeDataSource);
			Query query = catalogue.getFirstQuery();
			setQuery(query);
			initDs(qbeDataSource, query);
		}
	}

	public void initDs(it.eng.qbe.datasource.IDataSource qbeDataSource, Query query) {
		ds = QbeDatasetFactory.createDataSet(qbeDataSource.createStatement(query));
		ds.setUserProfileAttributes(attributes);
		ds.setParamsMap(params);
		ds.setTransformerId(transformerId);
		ds.setTransformerCd(transformerCd);
		ds.setPivotColumnName(pivotColumnName);
		ds.setPivotColumnValue(pivotColumnValue);
		ds.setPivotRowName(pivotRowName);
		ds.setNumRows(numRows);
		ds.setDataStoreTransformer(dataSetTransformer);
		ds.setPersisted(persisted);
		ds.setPersistTableName(persistTableName);
		ds.setDataSourceForReading(this.getDataSourceForReading());
		ds.setDataSourceForWriting(this.getDataSourceForWriting());
	}

	@Override
	public void loadData(int offset, int fetchSize, int maxResults) {
		init();
		Query qbeQuery = (Query) query;
		handleTimeFilters(qbeQuery);
		Map<String, Map<String, String>> inlineFilteredSelectFields = qbeQuery.getInlineFilteredSelectFields();
		if(inlineFilteredSelectFields != null && inlineFilteredSelectFields.size() > 0) {
			initDs(getQbeDataSource(), qbeQuery);
			ds.loadData(0, 0, -1);
		}
		else {
			ds.loadData(offset, fetchSize, maxResults);
		}
	}

	@Override
	public void loadData() {
		init();
		Query qbeQuery = (Query) query;
		Map<String, Map<String, String>> inlineFilteredSelectFields = qbeQuery.getInlineFilteredSelectFields();
		if(inlineFilteredSelectFields != null && inlineFilteredSelectFields.size() > 0) {
			ds.loadData(0, 0, -1);
		}
		else {
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
		IQbeDataSetDatamartRetriever previousRetriever = this.params == null ? null : (IQbeDataSetDatamartRetriever) this.params
				.get(SpagoBIConstants.DATAMART_RETRIEVER);
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
		if(inlineFilteredSelectFields != null && inlineFilteredSelectFields.size() > 0) {
			dataStore = handleTimeAggregations(qbeQuery, dataStore, dataStore);
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

		// if (getDataSourcePersist() != null) {
		// sbd.setDataSourcePersist(getDataSourcePersist().toSpagoBiDataSource());
		// }

		/*
		 * next informations are already loaded in method super.toSpagoBiDataSet() through the table field configuration try{ JSONObject jsonConf = new
		 * JSONObject(); jsonConf.put(QBE_JSON_QUERY, getJsonQuery()); jsonConf.put(QBE_DATAMARTS, getDatamarts()); sbd.setConfiguration(jsonConf.toString());
		 * }catch (Exception e){ logger.error("Error while defining dataset configuration.  Error: " + e.getMessage()); }
		 */
		// sbd.setJsonQuery(getJsonQuery());
		// sbd.setDatamarts(getDatamarts());

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

		// String resourcePath = getResourcePath();
		// modelJarFile = new File(resourcePath+File.separator+"qbe" + File.separator + "datamarts" + File.separator +
		// modelNames.get(0)+File.separator+"datamart.jar");
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

	/**
	 * TODO check this
	 */
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

	
	
	/* FROM EXECUTE QUERY ACTION */
	public final static String TEMPORAL_OPERAND_YTD = "YTD";
	public final static String TEMPORAL_OPERAND_QTD = "QTD";
	public final static String TEMPORAL_OPERAND_MTD = "MTD";
	public final static String TEMPORAL_OPERAND_WTD = "WTD";
	public final static String TEMPORAL_OPERAND_LAST_YEAR = "LAST_YEAR";
	public final static String TEMPORAL_OPERAND_LAST_QUARTER = "LAST_QUARTER";
	public final static String TEMPORAL_OPERAND_LAST_MONTH = "LAST_MONTH";
	public final static String TEMPORAL_OPERAND_LAST_WEEK = "LAST_WEEK";
	public final static String TEMPORAL_OPERAND_PARALLEL_YEAR = "PARALLEL_YEAR";
	private IDataStore handleTimeAggregations(Query query, IDataStore fullDatastore, IDataStore finalDatastore) {
		
		logger.debug("fullDatastore: ");
		sysoDatastore(fullDatastore);
		
		Map<String, Map<String, String>> inlineFilteredSelectFields = query.getInlineFilteredSelectFields();
		if(inlineFilteredSelectFields != null && inlineFilteredSelectFields.size() > 0) {
			
			/*
			 * DATA FOR AGGREGATION
			 * */
			Set<String> aliasesToBeRemovedAfterExecution = query.getAliasesToBeRemovedAfterExecution();
			Map<String, String> hierarchyFullColumnMap = query.getHierarchyFullColumnMap();
			LinkedList<String> allYearsOnDWH = query.getAllYearsOnDWH();
			int relativeYearIndex = query.getRelativeYearIndex();
			Set<String> temporalFieldTypesInQuery = query.getTemporalFieldTypesInQuery();
			Map<String, List<String>> distinctPeriods = query.getDistinctPeriods();
			
			// riorganizzo i periodi per type
			Map<String, List<String>> distinctPeriodsByType = new LinkedHashMap<>();
			for (String type : hierarchyFullColumnMap.keySet()) {
				distinctPeriodsByType.put(type, distinctPeriods.get( hierarchyFullColumnMap.get(type)));	
			}
			
			/*
			 * END DATA FOR AGGREGATION
			 * */
			
			// elimino le groupby aggiuntive per ottenere tutte le righe della query finale
			List<ISelectField> selectFields = query.getSelectFields(false);
			for (ISelectField sfield : selectFields) {
				if (sfield.isSimpleField()) {
					SimpleSelectField ssField = (SimpleSelectField) sfield;
					if(aliasesToBeRemovedAfterExecution != null && aliasesToBeRemovedAfterExecution.contains(ssField.getUniqueName())) {
						ssField.setGroupByField(false);
						ssField.setFunction(AggregationFunctions.COUNT_FUNCTION);
					}
				}
			}
			
			// eseguo la query per avere il numero di righe finale
			// finalDatastore = executeQuery(0, 0);
			

			logger.debug("finalDatastore: ");
			sysoDatastore(finalDatastore);
			
			// aggrego!
			for (Iterator finalIterator = finalDatastore.iterator(); finalIterator.hasNext();) {
				Record finalRecord = (Record) finalIterator.next();

				Map<String, String> rowPeriodValuesByType = new HashMap<>();
				for (int fieldIndex = 0; fieldIndex < finalDatastore.getMetaData().getFieldCount(); fieldIndex++) {
					String fieldName = finalDatastore.getMetaData().getFieldName(fieldIndex);
					if(fieldName != null && temporalFieldTypesInQuery.contains(fieldName)){
						rowPeriodValuesByType.put(fieldName, finalRecord.getFieldAt(fieldIndex).getValue().toString()); 
					}
				}
				
				
				// recupero l'identificativo della riga, rappresentato 
				// come coppie alias/valore
				Map<String, String> currentRecordId = getRecordAggregatedId(finalRecord, finalDatastore, query);
				
				Map<String, String> periodSetToCurrent = setCurrentIfNotPresent(query, hierarchyFullColumnMap, distinctPeriodsByType, currentRecordId);
				
				// Creo una mappa per tipo in cui tutti gli elementi sono numerati es i mesi da 0 a 11, i quarter da 0 a 3...
				Map<String, Integer> rowPeriodsNumbered = new HashMap<>();
				for (String type : rowPeriodValuesByType.keySet()) {
					String currentPeriodValue = rowPeriodValuesByType.get(type);
					
					if(periodSetToCurrent.get(type) != null) {
						currentPeriodValue = periodSetToCurrent.get(type);
					}
					
					List<String> distinctPeriodsForThisType = distinctPeriods.get(type);
					int currentValueIndexForThisType = -1;
					for(int i = 0; distinctPeriodsForThisType != null && i< distinctPeriodsForThisType.size(); i++) {
						String period = distinctPeriodsForThisType.get(i);
						if(period.equals(currentPeriodValue)) {
							currentValueIndexForThisType = i;
							break;
						}
					}
					rowPeriodsNumbered.put(type, currentValueIndexForThisType);	
				}
				
				
				String rowLog = "| ";
				
				// per ogni colonna di ogni riga, se c'è un operatore inline, ne calcolo il valore
				for (int fieldIndex = 0; fieldIndex < finalDatastore.getMetaData().getFieldCount(); fieldIndex++) {
					Map<String, String> firstRecordId = new HashMap<>();
					firstRecordId.putAll(currentRecordId);
					Map<String, String> lastRecordId = new HashMap<>();
					lastRecordId.putAll(currentRecordId);
					
					String fieldAlias = finalDatastore.getMetaData().getFieldAlias(fieldIndex);
					// se la colonna è da calcolare...
					if(fieldAlias != null && inlineFilteredSelectFields.containsKey(fieldAlias)){
						
						Map<String, String> inlineParameters = inlineFilteredSelectFields.get(fieldAlias);
						String temporalOperand = inlineParameters.get("temporalOperand");
						String temporalOperandParameter_str = inlineParameters.get("temporalOperandParameter");
						int temporalOperandParameter = Integer.parseInt(temporalOperandParameter_str);
						
						String periodType = null;
						boolean lastPeriod = false;
						switch (temporalOperand) {
						
						// PERIOD_TO_DATE
						// per i PERIOD_TO_DATE devo recuperare l'id temporale della riga  da cui partire, 
						// quella a cui fermarmi corrisponde con la riga corrente traslata nel periodo di riferimento
						// YTD_1 per la riga corrispondente a Giugno 2016 visualizzer� il dato aggregato da inizio 2015 a tutto Giugno 2015
						case TEMPORAL_OPERAND_YTD:
						{
							// PORTO AL PRIMO RECORD DEL ANNO
							for (String fieldType : temporalFieldTypesInQuery) {
								if(!hierarchyFullColumnMap.get("YEAR").equals(fieldType)) {
									firstRecordId.put(fieldType, distinctPeriods.get(fieldType).get(0));
								}
							}
							int parallelYearIndex = relativeYearIndex - temporalOperandParameter;
							if(parallelYearIndex >= 0 && allYearsOnDWH.size() > parallelYearIndex -1 ) {
								String parallelYear =  allYearsOnDWH.get(parallelYearIndex);
								firstRecordId.put(hierarchyFullColumnMap.get("YEAR"), parallelYear);
								lastRecordId.put(hierarchyFullColumnMap.get("YEAR"), parallelYear);
							}
							else {
								firstRecordId.put(hierarchyFullColumnMap.get("YEAR"), null);
								lastRecordId.put(hierarchyFullColumnMap.get("YEAR"), null);
							}
							break;
						}	
						case TEMPORAL_OPERAND_QTD:
							if (periodType == null) {
								periodType = "QUARTER";
							}
						case TEMPORAL_OPERAND_MTD:
							if (periodType == null) {
								periodType = "MONTH";
							}
						case TEMPORAL_OPERAND_WTD:
							if (periodType == null) {
								periodType = "WEEK";
							}
						case TEMPORAL_OPERAND_LAST_QUARTER:
							if (periodType == null) {
								periodType = "QUARTER";
								lastPeriod = true;
							}

						case TEMPORAL_OPERAND_LAST_MONTH:
							if (periodType == null) {
								periodType = "MONTH";
								lastPeriod = true;
							}

						case TEMPORAL_OPERAND_LAST_WEEK:
							if (periodType == null) {
								periodType = "WEEK";
								lastPeriod = true;
							}
						{
							// PORTO AL PRIMO RECORD DEL PERIODO (nell'anno)
							for (String fieldType : temporalFieldTypesInQuery) {
								if(!hierarchyFullColumnMap.get("YEAR").equals(fieldType) &&
								   !hierarchyFullColumnMap.get(periodType).equals(fieldType)) {
									firstRecordId.put(fieldType, distinctPeriods.get(fieldType).get(0));
								}
							}
							
							Integer rowPeriodNumber = rowPeriodsNumbered.get(hierarchyFullColumnMap.get(periodType));
							rowPeriodNumber = rowPeriodNumber > 0 ? rowPeriodNumber : 0;
							Integer otherPeriodNumber = rowPeriodNumber - temporalOperandParameter;
							
							
							/*
							if(otherPeriodNumber < rowPeriodNumber) {
								otherPeriodNumber = otherPeriodNumber + 1;
							}
							else {
								otherPeriodNumber = otherPeriodNumber - 1;
							}
							*/
							
							List<String> periods = distinctPeriodsByType.get(periodType);
							int periodsCount = periods.size();
							int periodOtherIndex = (otherPeriodNumber % periodsCount);
							
							int yearOffset = 0;
							while (periodOtherIndex < 0) {
								periodOtherIndex += periodsCount;
								yearOffset--;
							}
							while (periodOtherIndex >= periodsCount) {
								periodOtherIndex = periodOtherIndex % periodsCount;
								yearOffset++;
							}
							
							int yearOtherIndex = (int) (relativeYearIndex + yearOffset);
							if(yearOtherIndex < 0) {
								yearOtherIndex = 0;
							}
							if(yearOtherIndex >= allYearsOnDWH.size()) {
								yearOtherIndex = allYearsOnDWH.size() -1;
								periodOtherIndex = periods.size() -1;
							}
							// L'ANNO LO DEVO METTERE SOLO SE PRESENTE TRA I CAMPI DELLA SELECT ???
							firstRecordId.put(hierarchyFullColumnMap.get("YEAR"), allYearsOnDWH.get(yearOtherIndex));
							firstRecordId.put(hierarchyFullColumnMap.get(periodType), periods.get(periodOtherIndex));
							
							if(lastPeriod) {
								// se operatore last, aggrego fino al periodo della riga corrente
								lastRecordId.put(hierarchyFullColumnMap.get(periodType), rowPeriodValuesByType.get(hierarchyFullColumnMap.get(periodType)));
								lastRecordId.put(hierarchyFullColumnMap.get("YEAR"), allYearsOnDWH.get(relativeYearIndex));
							}
							else {
								// se operatore period to date, aggrego fino allo stesso 'tempo' nel periodo di riferimento
								lastRecordId.put(hierarchyFullColumnMap.get(periodType), rowPeriodValuesByType.get(hierarchyFullColumnMap.get(periodType)));
								lastRecordId.put(hierarchyFullColumnMap.get("YEAR"), allYearsOnDWH.get(relativeYearIndex));
							}
							break;
						}
							
							
						// LAST_PERIOD
						// per i LAST_PERIOD devo recuperare l'id temporale della riga da cui partire, 
						// quella a cui fermarmi corrisponde con la riga corrente
						// LM_3 per la riga Giugno 2016 visualizzer� il dato aggregato da Aprile a Giugno 2015
						// LM_4 per la riga Gennaio 2016 visualizzer� il dato aggregato da Ottobre 2015 a Gennaio 2016
						case TEMPORAL_OPERAND_LAST_YEAR:
						{
							// setta gennaio/Q1/W1
							for (String fieldType : temporalFieldTypesInQuery) {
								if(!hierarchyFullColumnMap.get("YEAR").equals(fieldType)) {
									firstRecordId.put(fieldType, distinctPeriods.get(fieldType).get(0));
								}
							}
							
							int parallelYearIndex = relativeYearIndex - temporalOperandParameter;
							if(parallelYearIndex >= 0 && allYearsOnDWH.size() > parallelYearIndex ) {
								String parallelYear =  allYearsOnDWH.get(parallelYearIndex);
								firstRecordId.put(hierarchyFullColumnMap.get("YEAR"), parallelYear);
							}
							else if(parallelYearIndex < 0) {
								firstRecordId.put(hierarchyFullColumnMap.get("YEAR"), allYearsOnDWH.getFirst());
							}
							else {
								firstRecordId.put(hierarchyFullColumnMap.get("YEAR"), allYearsOnDWH.getLast());
							}
							
							if(relativeYearIndex >= 0 && allYearsOnDWH.size() > relativeYearIndex ) {
								lastRecordId.put(hierarchyFullColumnMap.get("YEAR"), allYearsOnDWH.get(relativeYearIndex));
							}
							else if(relativeYearIndex < 0) {
								firstRecordId.put(hierarchyFullColumnMap.get("YEAR"), allYearsOnDWH.getFirst());
							}
							else {
								firstRecordId.put(hierarchyFullColumnMap.get("YEAR"), allYearsOnDWH.getLast());
							}
							
							break;
						}

						// PARALLEL_PERIOD
						case TEMPORAL_OPERAND_PARALLEL_YEAR:
						{
							// i parallel years si calcolano sempre in funzione di quello che trovo nella where
							
							String year = null;
							
							int parallelYearIndex = relativeYearIndex - temporalOperandParameter;
							if(parallelYearIndex >= 0 && allYearsOnDWH.size() > parallelYearIndex ) {
								year =  allYearsOnDWH.get(parallelYearIndex);
							}
							firstRecordId.put(hierarchyFullColumnMap.get("YEAR"), year);
							lastRecordId.put(hierarchyFullColumnMap.get("YEAR"), year);
							break;
						}
						default:
							break;
						}
						
						
						setCurrentIfNotPresent(query, hierarchyFullColumnMap, distinctPeriodsByType, firstRecordId);
						setCurrentIfNotPresent(query, hierarchyFullColumnMap, distinctPeriodsByType, lastRecordId);
						
						int firstRecordIndex = calculateRecordIndex(hierarchyFullColumnMap, distinctPeriodsByType, firstRecordId);
						int lastRecordIndex = calculateRecordIndex(hierarchyFullColumnMap, distinctPeriodsByType, lastRecordId);
						
						boolean swapped = false;
						if(firstRecordIndex > lastRecordIndex) {
							int swap = lastRecordIndex;
							lastRecordIndex = firstRecordIndex;
							firstRecordIndex = swap;
						}
						logger.debug( fieldAlias +" FIRST: "+firstRecordIndex + " -> LAST: " + lastRecordIndex + (swapped?" (Reading the future: swapped first and last!)":""));

						
						/** A QUESTO PUNTO AGGREGO E CALCOLO IL VALORE */
						if(firstRecordId.get(hierarchyFullColumnMap.get("YEAR")) != null) {
							double finalValue = 0D;
							boolean aValueFound = false;
							/** INQUESTO CICLO DEVO UTILIZZARE I CAMPI FIRST E LAST */
							for (Iterator fullIterator = fullDatastore.iterator(); fullIterator.hasNext();) {
								Record record = (Record) fullIterator.next();
								Map<String, String> recordId = getRecordFullId(record, finalDatastore, query);
								
								int recordIndex = calculateRecordIndex(hierarchyFullColumnMap, distinctPeriodsByType, recordId);
								
								
								if(firstRecordIndex <= recordIndex && recordIndex <= lastRecordIndex) {
									logger.debug("recordIndex: " + recordIndex);
									aValueFound = true;
									finalValue += Double.parseDouble(record.getFieldAt(fieldIndex).getValue().toString());
									finalRecord.getFieldAt(fieldIndex).setValue(finalValue);
								}
							}
							if(!aValueFound) {
								finalRecord.getFieldAt(fieldIndex).setValue(0D);
							}
						}
						else {
							finalRecord.getFieldAt(fieldIndex).setValue(0D);
						}
						
						rowLog += " | " + firstRecordId + " >>> " + lastRecordId;
					}
					else {
						rowLog += " | NON AGGREGATO ";
					}
				}
				
				logger.debug(rowLog);
				
			}
			
			return finalDatastore;
			
		}
		else {
			return fullDatastore;
		}
		
		
	}



	public Map<String,String> setCurrentIfNotPresent(Query query, Map<String, String> hierarchyFullColumnMap,
			Map<String, List<String>> distinctPeriodsByType, Map<String, String> currentRecordId) {
		Map<String,String> periodSetToCurrent = new HashMap<>();
		Set<String> periodElements = distinctPeriodsByType.keySet();
		for (String period : periodElements) {
			List<String> periods = distinctPeriodsByType.get(period);
			if(periods != null){
				String periodUniqueIdentifier = hierarchyFullColumnMap.get(period);
				String currentRecordPeriod = currentRecordId.get(periodUniqueIdentifier);
				if(currentRecordPeriod != null) {
					int periodIndex = periods.indexOf(currentRecordPeriod);
					if(periodIndex < 0) {
						currentRecordId.put(periodUniqueIdentifier, query.getCurrentPeriodValuyesByType().get(periodUniqueIdentifier));
						periodSetToCurrent.put(periodUniqueIdentifier, query.getCurrentPeriodValuyesByType().get(periodUniqueIdentifier));
					}
				}
			}
		}
		return periodSetToCurrent;
	}



	public int calculateRecordIndex(Map<String, String> hierarchyFullColumnMap,
			Map<String, List<String>> distinctPeriodsByType, Map<String, String> recordId)
					throws NumberFormatException {
		String recordCode = "";
		Set<String> periodElements = distinctPeriodsByType.keySet();
		for (String period : periodElements) {
			List<String> periods = distinctPeriodsByType.get(period);
			if(periods != null){
				int periodIndex = periods.indexOf(recordId.get(hierarchyFullColumnMap.get(period)));
				recordCode += new DecimalFormat("000").format(periodIndex+1);
			}
		}
		int recordIndex = new Integer(recordCode.indexOf('-') < 0 ? recordCode : "0");
		return recordIndex;
	}



	public void sysoDatastore(IDataStore ds) throws RuntimeException {
		try {
		JSONDataWriter dataSetWriter = new JSONDataWriter();
		JSONObject dataSetJSON = (JSONObject) dataSetWriter.write(ds);
			logger.debug(dataSetJSON.getJSONArray("rows").toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private Map<String, String> getRecordAggregatedId(Record finalRecord, IDataStore finalDatastore, Query query) {
		Set<String> idAliases = query.getTemporalFieldTypesInSelect();
		return getRecordId(finalRecord, finalDatastore, query, idAliases);
	}
	 
	private Map<String, String> getRecordFullId(Record finalRecord, IDataStore finalDatastore, Query query) {
		Set<String> idAliases = query.getTemporalFieldTypesInQuery();
		return getRecordId(finalRecord, finalDatastore, query, idAliases);
	}
	
	private Map<String, String> getRecordId(Record finalRecord, IDataStore finalDatastore, Query query, Set<String> idAliases) {
		Map<String, String> recordId = new LinkedHashMap<>();
		for (int fieldIndex = 0; fieldIndex < finalDatastore.getMetaData().getFieldCount(); fieldIndex++) {
			String fieldName = finalDatastore.getMetaData().getFieldName(fieldIndex);
			if(fieldName != null && idAliases.contains(fieldName)){
				recordId.put(fieldName, (finalRecord.getFieldAt(fieldIndex).getValue() != null ? finalRecord.getFieldAt(fieldIndex).getValue().toString(): "") );
			}
		}
		return recordId;
	}

	/* END FROM EXECUTE QUERY ACTION */


	/* FROM SET CATALOGUE ACTION */

	private void handleTimeFilters(Query query) {
		// DD: retrieve time dimension
		IModelEntity temporalDimension = getTemporalDimension(getQbeDataSource());
		IModelEntity timeDimension = getTimeDimension(getQbeDataSource());

		if (temporalDimension != null) {
			HierarchicalDimensionField hierarchicalDimensionByEntity = temporalDimension.getHierarchicalDimensionByEntity(temporalDimension.getType());
			if (hierarchicalDimensionByEntity == null) {
				throw new SpagoBIRuntimeException("Temporal dimension hierarchy is [null]");
			}
			Hierarchy defaultHierarchy = hierarchicalDimensionByEntity.getDefaultHierarchy();

			List<Integer> whereFieldsIndexesToRemove = new LinkedList<Integer>();
			List<WhereField> whereFieldsToAdd = new LinkedList<WhereField>();
			List<WhereField> whereFields = query.getWhereFields();
			List<String> nodesToAdd = new LinkedList<String>();

			int timeFilterIndex = 0;
			int whereFieldIndex = 0;
			for (WhereField whereField : whereFields) {
				String[] lValues = whereField.getLeftOperand().values;
				String[] rValues = whereField.getRightOperand().values;

				if (lValues != null && lValues.length > 0 && rValues != null && rValues.length > 0) {
					if (QuerySerializationConstants.TEMPORAL.equals(lValues[0])) {

						whereField.setDescription(QuerySerializationConstants.TEMPORAL);

						String temporalLevelColumn = null;
						String temporalLevel = whereField.getLeftOperand().description;
						temporalLevelColumn = defaultHierarchy.getLevelByType(temporalLevel);

						String temporalDimensionId = getTimeId(temporalDimension);

						// DD: retrieve current period
						TemporalRecord currentPeriod = getCurrentPeriod(temporalDimension, temporalDimensionId,
								temporalLevelColumn, null, defaultHierarchy.getAncestors(temporalLevelColumn));
						// DD: retrieve current period index

						if ("Current".equals(rValues[0])) {
							lValues[0] = temporalDimension.getType() + ":" + temporalLevelColumn;
							rValues[0] = currentPeriod.getPeriod().toString();
						} else if (whereField.getOperator().equals("LAST")) {

							// DD: retrieving all periods start time records
							LinkedList<TemporalRecord> allPeriodsStartingDate = loadAllPeriodsStartingDate(
									temporalDimension, temporalDimensionId, temporalLevelColumn,
									defaultHierarchy.getAncestors(temporalLevelColumn));

							int currentPeriodIndex = getCurrentIndex(allPeriodsStartingDate,
									(Integer) currentPeriod.getId());

							whereFieldsIndexesToRemove.add(whereFieldIndex);

							timeFilterIndex++;

							Operand left = new Operand(
									new String[] { temporalDimension.getType() + ":" + temporalDimensionId },
									temporalDimension.getName() + ":" + temporalDimensionId, "Field Content",
									new String[] {""}, new String[] {""}, "");
							Operand maxRight = new Operand(new String[] { currentPeriod.getId().toString() },
									currentPeriod.getId().toString(), "Static Content", new String[] {""}, new String[] {""}, "");

							String maxFilterId = "TimeFilterMax" + timeFilterIndex;
							WhereField maxWhereField = new WhereField(maxFilterId, maxFilterId, false, left,
									"EQUALS OR LESS THAN", maxRight, "AND");
							nodesToAdd.add(maxFilterId);

							int offset = Integer.parseInt(rValues[0]);
							int oldestPeriodIndex = currentPeriodIndex - offset > 0 ? currentPeriodIndex - offset : 0;
							TemporalRecord oldestPeriod = allPeriodsStartingDate.get(oldestPeriodIndex);
							Operand minRight = new Operand(new String[] { oldestPeriod.getId().toString() },
									oldestPeriod.getId().toString(), "Static Content", new String[] {""}, new String[] {""}, "");

							String minFilterId = "TimeFilterMax" + timeFilterIndex;
							WhereField minWhereField = new WhereField(minFilterId, minFilterId, false, left,
									"EQUALS OR GREATER THAN", minRight, "AND");
							nodesToAdd.add(minFilterId);

							whereFieldsToAdd.add(maxWhereField);
							whereFieldsToAdd.add(minWhereField);
						}
					}
				}
				whereFieldIndex++;
			}

			for (Integer index : whereFieldsIndexesToRemove) {
				query.removeWhereField(index);
			}

			for (int index = 0; index < whereFieldsToAdd.size(); index++) {
				WhereField whereFieldToAdd = whereFieldsToAdd.get(index);
				query.addWhereField(whereFieldToAdd.getName(), whereFieldToAdd.getDescription(),
						whereFieldToAdd.isPromptable(), whereFieldToAdd.getLeftOperand(), whereFieldToAdd.getOperator(),
						whereFieldToAdd.getRightOperand(), whereFieldToAdd.getBooleanConnector());

			}

			query.updateWhereClauseStructure();

			handleInLineTemporalFilter(query, temporalDimension, defaultHierarchy);

		}

		if (timeDimension != null) {

			HierarchicalDimensionField hierarchicalDimensionByEntity = timeDimension
					.getHierarchicalDimensionByEntity(timeDimension.getType());
			Hierarchy defaultHierarchy = hierarchicalDimensionByEntity.getDefaultHierarchy();

			int timeFilterIndex = 0;
			int whereFieldIndex = 0;
			List<WhereField> whereFields = query.getWhereFields();
			List<WhereField> whereFieldsToAdd = new LinkedList<WhereField>();
			List<Integer> whereFieldsIndexesToRemove = new LinkedList<Integer>();
			List<String> nodesToAdd = new LinkedList<String>();

			for (WhereField whereField : whereFields) {
				String[] lValues = whereField.getLeftOperand().values;
				String[] rValues = whereField.getRightOperand().values;

				if (lValues != null && lValues.length > 0 && rValues != null && rValues.length > 0) {
					if ("TIME".equals(lValues[0])) {
						String timeLevelColumn = null;
						String timeLevel = whereField.getLeftOperand().description;
						timeLevelColumn = defaultHierarchy.getLevelByType(timeLevel);

						String timeDimensionId = "ID";

						TemporalRecord currentTime = getCurrentTime(timeDimension, timeDimensionId, timeLevelColumn,
								null, defaultHierarchy.getAncestors(timeLevelColumn));

						if ("Current".equals(rValues[0])) {
							lValues[0] = timeDimension.getType() + ":" + timeLevelColumn;
							rValues[0] = currentTime.getPeriod().toString();

						} else if (whereField.getOperator().equals("LAST")) {

							LinkedList<TemporalRecord> allPeriodsStartingDate = loadAllPeriodsStartingDate(
									timeDimension, timeDimensionId, timeLevelColumn,
									defaultHierarchy.getAncestors(timeLevelColumn));

							int currentPeriodIndex = getCurrentIndex(allPeriodsStartingDate,
									(Integer) currentTime.getId());
							whereFieldsIndexesToRemove.add(whereFieldIndex);
							Operand left = new Operand(new String[] { timeDimension.getType() + ":" + timeDimensionId },
									timeDimension.getName() + ":" + timeDimensionId, "Field Content", new String[] {""}, new String[] {""}, "");
							Operand maxRight = new Operand(new String[] { currentTime.getId().toString() },
									currentTime.getId().toString(), "Static Content", new String[] {""}, new String[] {""}, "");

							String maxFilterId = "TimeFilterMax" + timeFilterIndex;
							WhereField maxWhereField = new WhereField(maxFilterId, maxFilterId, false, left,
									"EQUALS OR LESS THAN", maxRight, "AND");
							nodesToAdd.add(maxFilterId);

							int offset = Integer.parseInt(rValues[0]);
							int oldestPeriodIndex = currentPeriodIndex - offset > 0 ? currentPeriodIndex - offset : 0;
							TemporalRecord oldestPeriod = allPeriodsStartingDate.get(oldestPeriodIndex);
							Operand minRight = new Operand(new String[] { oldestPeriod.getId().toString() },
									oldestPeriod.getId().toString(), "Static Content", new String[] {""}, new String[] {""}, "");

							String minFilterId = "TimeFilterMax" + timeFilterIndex;
							WhereField minWhereField = new WhereField(minFilterId, minFilterId, false, left,
									"EQUALS OR GREATER THAN", minRight, "AND");
							nodesToAdd.add(minFilterId);

							whereFieldsToAdd.add(maxWhereField);
							whereFieldsToAdd.add(minWhereField);
						}
					}
				}
			}

			query.updateWhereClauseStructure();

		}
	}

	private int getCurrentIndex(LinkedList<TemporalRecord> allPeriodsStartingDate, int currentPeriodId) {
		int index = 0;
		for (TemporalRecord temporalRecord : allPeriodsStartingDate) {
			int curr = (Integer) temporalRecord.getId();
			if (currentPeriodId <= curr) {
				break;
			}
			index++;
		}
		return index - 1;
	}

	private void handleInLineTemporalFilter(Query query, IModelEntity temporalDimension, Hierarchy hierarchy) {

		List<ISelectField> selectFields = query.getSelectFields(false);
		List<WhereField> whereFields = query.getWhereFields();
		
		// verifing if there's an inline filter
		if (hasInlineFilters(selectFields)) {

			// Retrieving all hierarchy levels
			List<HierarchyLevel> levels = hierarchy.getLevels();

			// Retrieving all hierarchy columns (used to check if those are all included in the group by clause)
			Map<String, String> hierarchyFullColumnMap = new LinkedHashMap<>();
			Map<String, String> hierarchyColumnMap = new LinkedHashMap<>();
			for (HierarchyLevel level : levels) {
				hierarchyColumnMap.put(level.getType(), level.getColumn());
				hierarchyFullColumnMap.put(level.getType(), extractColumnName(temporalDimension, level.getColumn()));
			}
			
			// retrieving all the temporal fields coming from temporal inline filters
			Set<String> inlineFilterFieldTypes = extractInlineFilterFieldTypes(selectFields, hierarchyFullColumnMap);
			
			// retrieving time_id
			String temporalDimensionId = getTimeId(temporalDimension);

			// adding  time_id to query
			addTimeIdToQuery(query, temporalDimension, temporalDimensionId);

			// relative year
			String relativeYear = (new GregorianCalendar().get(Calendar.YEAR)) + "";
			Set<String> yearsInWhere = extractYearsFromWhereFields(whereFields, hierarchyFullColumnMap.get("YEAR"));
			if (yearsInWhere.size() > 0) {
				relativeYear = yearsInWhere.iterator().next();
			}

			// retrieving all year on DWH
			LinkedList<TemporalRecord> allYearsOnDWH = loadAllPeriodsStartingDate(temporalDimension,
					temporalDimensionId, hierarchyColumnMap.get("YEAR"));
			LinkedList<String> allYearsOnDWHString = new LinkedList<>();
			for (TemporalRecord temporalRecord : allYearsOnDWH) {
				allYearsOnDWHString.add(temporalRecord.getPeriod().toString());
			}

			// retrieving relative year index on allYearsOnDWH list
			int relativeYearIndex = -1;
			for (int i = 0; i < allYearsOnDWHString.size(); i++) {
				if (allYearsOnDWHString.get(i).equals(relativeYear)) {
					relativeYearIndex = i;
					break;
				}
			}

			Set<String> aliasesToBeRemovedAfterExecution = addMissingGroupByToTheQuery(query, selectFields,
					inlineFilterFieldTypes, hierarchyFullColumnMap);

			addSumFunctionToAllMeasureInSelect(selectFields);

			Map<String, String> currentPeriodValuyesByType = addMissingCurrentPeriodWhereClauses(query,
					temporalDimension, selectFields, whereFields, inlineFilterFieldTypes, temporalDimensionId,
					hierarchyFullColumnMap, hierarchyColumnMap, relativeYear);

			// retrieving all temporal fields used in query
			Set<String> temporalFieldTypesInSelect = getTemporalFieldsInSelect(query.getSelectFields(false), hierarchyFullColumnMap);

			// defining wich fields will be calculated after query execution
			Map<String, Map<String, String>> inlineFilteredSelectFields = updateInlineFilteredSelectFieldsAliases(
					selectFields);

			// adding yera as select field if not present
			if (!temporalFieldTypesInSelect.contains(hierarchyFullColumnMap.get("YEAR"))) {
				addYearToQuery(query, temporalDimension, hierarchyFullColumnMap);
				aliasesToBeRemovedAfterExecution.add(hierarchyFullColumnMap.get("YEAR"));
			}

			// preparing data for query post-processing
			query.setInlineFilteredSelectFields(inlineFilteredSelectFields);
			query.setAliasesToBeRemovedAfterExecution(aliasesToBeRemovedAfterExecution);
			query.setTemporalFieldTypesInSelect(temporalFieldTypesInSelect);
			query.setHierarchyFullColumnMap(hierarchyFullColumnMap);
			query.setRelativeYearIndex(relativeYearIndex);
			query.setAllYearsOnDWH(allYearsOnDWHString);

			Map<String, List<String>> distinctPeriods = new LinkedHashMap<>();
			for (String temporalFieldColumn : temporalFieldTypesInSelect) {
				distinctPeriods.put(temporalFieldColumn,
						loadDistinctPeriods(temporalDimension, temporalDimensionId, temporalFieldColumn));
			}
			for (String temporalFieldColumn : aliasesToBeRemovedAfterExecution) {
				distinctPeriods.put(temporalFieldColumn,
						loadDistinctPeriods(temporalDimension, temporalDimensionId, temporalFieldColumn));
			}
			query.setDistinctPeriods(distinctPeriods);
			query.setCurrentPeriodValuyesByType(currentPeriodValuyesByType);

			// relativeYear will be added later on where clause only if needed
			removeRelativeYearFromWhereFields(whereFields, yearsInWhere, hierarchyFullColumnMap.get("YEAR"), relativeYear);
			
			// when period to date will result in a single record, temporal where clauses are used to calculate the relative period, not for filtering 
			if(containsPeriodToDate(selectFields) && !isMultiLineResult(selectFields, hierarchyFullColumnMap)) {
				removeCurrentPeriodFromWhereFields(whereFields, hierarchyFullColumnMap);
			}
			
			addYearsFilterForPerformances(query, selectFields, whereFields, hierarchyFullColumnMap, relativeYear,
					yearsInWhere, allYearsOnDWHString, relativeYearIndex, distinctPeriods, currentPeriodValuyesByType);
		}
	}

	private boolean removeCurrentPeriodFromWhereFields(List<WhereField> whereFields,
			Map<String, String> hierarchyFullColumnMap) {

		List<WhereField> whereFieldsToBeRemoved = new ArrayList<>();
		
		LOOP_4:
		for (String levelType : hierarchyFullColumnMap.keySet()) {
			if(!"YEAR".equals(levelType)) {
				String levelColumn = hierarchyFullColumnMap.get(levelType);
				
				for (WhereField wField : whereFields) {
					if (wField.getLeftOperand().values != null && wField.getLeftOperand().values.length > 0
							&& levelColumn.equals(wField.getLeftOperand().values[0]) && ("EQUALS TO".equals(wField.getOperator()) || ("IN".equals(wField.getOperator())))
							&& wField.getRightOperand().values != null && wField.getRightOperand().values.length > 0) {
						
						whereFieldsToBeRemoved.add(wField);
						break;
					}
				}
			}
		}
		
		if(whereFieldsToBeRemoved.size() > 0) {
			whereFields.removeAll(whereFieldsToBeRemoved);
		}
		return whereFieldsToBeRemoved.size() > 0;
	}

	private void removeRelativeYearFromWhereFields(List<WhereField> whereFields, Set<String> yearsInWhere, String yearColumn, String relativeYear) {
		for (WhereField wField : whereFields) {
			if (wField.getLeftOperand().values != null && wField.getLeftOperand().values.length > 0
					&& yearColumn.equals(wField.getLeftOperand().values[0]) && ("EQUALS TO".equals(wField.getOperator()) || ("IN".equals(wField.getOperator())))
					&& wField.getRightOperand().values != null && wField.getRightOperand().values.length > 0
					&& relativeYear.equals(wField.getRightOperand().values[0] + "")) {
				
				whereFields.remove(wField);
				break;
			}
		}
		
		yearsInWhere.remove(relativeYear);
	}

	private String getTimeId(IModelEntity temporalDimension) {

		List<IModelField> fields = temporalDimension.getAllFields();
		for (IModelField f : fields) {
			if (f.getName().equalsIgnoreCase("time_id")) {
				return f.getName();
			}
		}

		throw new SpagoBIRuntimeException("Impossible to find time_id on Temporal Dimension");
	}

	private String getDateField(IModelEntity temporalDimension) {

		List<IModelField> fields = temporalDimension.getAllFields();
		for (IModelField f : fields) {
			if (f.getName().equalsIgnoreCase("the_date") || f.getName().equalsIgnoreCase("time_date")) {
				return f.getName();
			}
		}

		throw new SpagoBIRuntimeException("Impossible to find a date field on Temporal Dimension");
	}

	private void addYearsFilterForPerformances(Query query, List<ISelectField> selectFields,
			List<WhereField> whereFields, Map<String, String> hierarchyFullColumnMap, String relativeYear,
			Set<String> yearsInWhere, LinkedList<String> allYearsOnDWHString, int relativeYearIndex,
			Map<String, List<String>> distinctPeriods, Map<String, String> currentPeriodValuyesByType) {
		
		if(!existsNotInlineTemporlFilteredColumn(selectFields)) {
		
			Set<String> yearsToBeAddedToWhereClause = extractYearsToBeAddedToWhereClause(selectFields, relativeYear,
					yearsInWhere, allYearsOnDWHString, relativeYearIndex, hierarchyFullColumnMap, distinctPeriods,
					currentPeriodValuyesByType);
			if (yearsToBeAddedToWhereClause.size() > 0) {
				boolean yersAdded = false;
				if (whereFields.size() > 0) {
					for (WhereField wField : whereFields) {
						if (wField.getLeftOperand().values != null && wField.getLeftOperand().values.length > 0
								&& hierarchyFullColumnMap.get("YEAR").equals(wField.getLeftOperand().values[0])
								&& ("EQUALS TO".equals(wField.getOperator()) || ("IN".equals(wField.getOperator()))) &&  wField.getRightOperand().values != null
								&&  wField.getRightOperand().values.length > 0) {
	
							for (String value :  wField.getRightOperand().values) {
								yearsToBeAddedToWhereClause.add(value);
							}
							
							Operand right = new Operand(
									yearsToBeAddedToWhereClause.toArray(new String[yearsToBeAddedToWhereClause.size()]),
									"YEAR", "Static Content", new String[] {""}, new String[] {""}, "");
	
							wField.setRightOperand(right);
							wField.setOperator("IN");
							yersAdded = true;
							break;
						}
					}
				}
				
				if(!yersAdded) {
					Operand left = new Operand(new String[] { hierarchyFullColumnMap.get("YEAR") },
							hierarchyFullColumnMap.get("YEAR"), "Field Content", new String[] {""}, new String[] {""}, "");
	
					Operand right = new Operand(
							yearsToBeAddedToWhereClause.toArray(new String[yearsToBeAddedToWhereClause.size()]), "YEAR",
							"Static Content", new String[] {""}, new String[] {""}, "");
					query.addWhereField("ParallelYear", "ParallelYear", false, left, "IN", right, "AND");
				}
	
			}
		}
		query.updateWhereClauseStructure();
	}

	private boolean existsNotInlineTemporlFilteredColumn(List<ISelectField> selectFields) {
		for (ISelectField sfield : selectFields) {
			if (sfield.isSimpleField()) {
				SimpleSelectField ssField = (SimpleSelectField) sfield;
				String temporalOperand = ssField.getTemporalOperand();
		
				if(StringUtilities.isEmpty(temporalOperand) && 
						ssField.getFunction() != null && !"NONE".equals(ssField.getFunction().getName()) &&
						!ssField.isGroupByField()) {
					return true;
				}
			}
		}
		return false;
	}

	private Map<String, Map<String, String>> updateInlineFilteredSelectFieldsAliases(List<ISelectField> selectFields) {
		Map<String, Map<String, String>> inlineFilteredSelectFields = new HashMap<>();
		for (ISelectField sfield : selectFields) {
			if (sfield.isSimpleField()) {
				SimpleSelectField ssField = (SimpleSelectField) sfield;
				String temporalOperand = ssField.getTemporalOperand();
				if (temporalOperand != null && !"".equals(temporalOperand)) {
					String temporalOperandParameter = ssField.getTemporalOperandParameter();
					if (temporalOperandParameter == null)
						temporalOperandParameter = "0";
					String newAlias = ssField.getAlias() + "_" + temporalOperand + "_" + temporalOperandParameter;
					ssField.setAlias(newAlias);

					Map<String, String> parameters = new HashMap<>();
					parameters.put("temporalOperand", temporalOperand);
					parameters.put("temporalOperandParameter", temporalOperandParameter);

					inlineFilteredSelectFields.put(newAlias, parameters);

				}
			}
		}
		return inlineFilteredSelectFields;
	}

	private Map<String, String> addMissingCurrentPeriodWhereClauses(Query query, IModelEntity temporalDimension,
			List<ISelectField> selectFields, List<WhereField> whereFields, Set<String> inlineFilterFieldTypes,
			String temporalDimensionId, Map<String, String> hierarchyFullColumnMap,
			Map<String, String> hierarchyColumnMap, String relativeYear) {

		Map<String, String> currentPeriodValuesByType = new HashMap<>();

		Set<String> temporalFieldTypesInSelect = new HashSet<>();
		Set<String> temporalFieldTypesInWhere = new HashSet<>();

		boolean hasPeriodToDate = false;
		
		Map<String, int[]> lastOperatorRanges = new HashMap<>();
		for (ISelectField sfield : selectFields) {
			if (sfield.isSimpleField()) {
				SimpleSelectField ssField = (SimpleSelectField) sfield;
				String temporalOperand = ssField.getTemporalOperand();
				int temporalOperandParameter = -1*Integer.parseInt(ssField.getTemporalOperandParameter() != null ? ssField.getTemporalOperandParameter() : "0");
				if (temporalOperand != null) {
					switch (temporalOperand) {
					
					case TEMPORAL_OPERAND_YTD:
						hasPeriodToDate=true;
						break;
					case TEMPORAL_OPERAND_QTD:
						hasPeriodToDate=true;
					case TEMPORAL_OPERAND_LAST_QUARTER:
						int[] opRangeQuarter = {0,0};
						if(lastOperatorRanges.containsKey("QUARTER")) {
							opRangeQuarter = lastOperatorRanges.get("QUARTER");
						}
						if(temporalOperandParameter <= 0 && temporalOperandParameter < opRangeQuarter[0]) {
							opRangeQuarter[0] = temporalOperandParameter;
						}
						if(temporalOperandParameter >= 0 && temporalOperandParameter > opRangeQuarter[1]) {
							opRangeQuarter[1] = temporalOperandParameter;
						}
						lastOperatorRanges.put("QUARTER", opRangeQuarter);
						break;
					case TEMPORAL_OPERAND_MTD:
						hasPeriodToDate=true;
					case TEMPORAL_OPERAND_LAST_MONTH:
						int[] opRangeMonth = {0,0};
						if(lastOperatorRanges.containsKey("MONTH")) {
							opRangeMonth = lastOperatorRanges.get("MONTH");
						}
						if(temporalOperandParameter <= 0 && temporalOperandParameter < opRangeMonth[0]) {
							opRangeMonth[0] = temporalOperandParameter;
						}
						if(temporalOperandParameter >= 0 && temporalOperandParameter > opRangeMonth[1]) {
							opRangeMonth[1] = temporalOperandParameter;
						}
						lastOperatorRanges.put("MONTH", opRangeMonth);
						break;
					default:
					}
				}
			}
		}
		
		LOOP_1: for (String levelType : inlineFilterFieldTypes) {
			String levelColumn = hierarchyFullColumnMap.get(levelType);
			if (!temporalFieldTypesInSelect.contains(levelType)) {

				// seraching on selct fields
				for (ISelectField sfield : selectFields) {
					if (sfield.isSimpleField()) {
						SimpleSelectField ssField = (SimpleSelectField) sfield;
						if (levelColumn.equals(ssField.getUniqueName())) {
							temporalFieldTypesInSelect.add(levelType);

							TemporalRecord currentPeriod = getCurrentPeriod(temporalDimension, temporalDimensionId,
									hierarchyColumnMap.get(levelType), new Date());
							String currentPeriodValue = "K_UNDEFINED";
							if ((currentPeriod != null)) {
								currentPeriodValue = currentPeriod.getPeriod() + "";
							}
							currentPeriodValuesByType.put(levelColumn, currentPeriodValue);

							continue LOOP_1;
						}
					}
				}
			}

			if (!temporalFieldTypesInWhere.contains(levelType)) {
				// serching on filters
				for (WhereField wField : whereFields) {
					if (wField.getLeftOperand().values != null && wField.getLeftOperand().values.length > 0
							&& levelColumn.equals(wField.getLeftOperand().values[0])
							&& "EQUALS TO".equals(wField.getOperator()) || "IN".equals(wField.getOperator())) {
						temporalFieldTypesInWhere.add(levelType);

						currentPeriodValuesByType.put(levelColumn, wField.getRightOperand().values[0]);

						continue LOOP_1;
					}
				}
			}
		}

		Set<String> temporalFieldTypesInSelectOrWhere = new HashSet<>();
		temporalFieldTypesInSelectOrWhere.addAll(temporalFieldTypesInSelect);
		temporalFieldTypesInSelectOrWhere.addAll(temporalFieldTypesInWhere);

		for (String levelType : inlineFilterFieldTypes) {
			if (!temporalFieldTypesInSelectOrWhere.contains(levelType) ) {
				String levelColumn = hierarchyFullColumnMap.get(levelType);
				Operand left = new Operand(new String[] { levelColumn }, levelColumn, "Field Content", new String[] {""}, new String[] {""}, "");

				TemporalRecord currentPeriod = getCurrentPeriod(temporalDimension, temporalDimensionId,
						hierarchyColumnMap.get(levelType), new Date());
				String currentPeriodValue = "K_UNDEFINED";
				if ((currentPeriod != null)) {
					currentPeriodValue = currentPeriod.getPeriod() + "";
				}
				
				String[] currentPeriodValues = new String[] {  currentPeriodValue };
				
				int[] opRange = lastOperatorRanges.get(levelType);
				if(opRange != null) {
				
					LinkedList<LinkedHashMap<String,String>> allMonthOrQuarterPeriods = new LinkedList<>();
					LinkedList<TemporalRecord> allPeriodsInDwh = loadAllPeriodsStartingDate(temporalDimension, temporalDimensionId, hierarchyColumnMap.get(levelType));
					LinkedList<TemporalRecord> allYearsOnDWH = loadAllPeriodsStartingDate(temporalDimension, temporalDimensionId, hierarchyColumnMap.get("YEAR"));
					for (TemporalRecord  yearRecord : allYearsOnDWH) {
						for (TemporalRecord monthOrQuarterRecord : allPeriodsInDwh) {
							LinkedHashMap<String, String> record = new LinkedHashMap<>();
							record.put("YEAR", yearRecord.getPeriod().toString());
							record.put(levelType, monthOrQuarterRecord.getPeriod().toString());
							allMonthOrQuarterPeriods.add(record);
						}
					}
					LinkedHashMap<String, String> currentRecord = new LinkedHashMap<>();
					currentRecord.put("YEAR", relativeYear);
					currentRecord.put(levelType, currentPeriodValue);
					
					int currentPeriodIndex = allMonthOrQuarterPeriods.indexOf(currentRecord);
					
	
					int start = currentPeriodIndex + opRange[0];
					if (start < 0)
						start = 0;
					if (start > allMonthOrQuarterPeriods.size() - 1)
						start = allMonthOrQuarterPeriods.size() - 1;
					int end = currentPeriodIndex + opRange[1];
					if (end < 0)
						end = 0;
					if (end > allMonthOrQuarterPeriods.size()-1)end =  allMonthOrQuarterPeriods.size()-1;
					
					Set<String> currentPeriodValuesSet = new LinkedHashSet<>();
					for (int i = start; i <= end ; i++ ) {
						currentPeriodValuesSet.add(allMonthOrQuarterPeriods.get(i).get(levelType));
					}
					
					currentPeriodValues = currentPeriodValuesSet.toArray(new String[0]);
				}
				
				// Period to date aggregates only with 'daily granularity'
				 if(levelType.equals("YEAR") || !hasPeriodToDate) {
				
					Operand right = new Operand(currentPeriodValues, levelType, "Static Content", new String[] {""}, new String[] {""}, "");
					query.addWhereField("current_" + levelType, "current_" + levelType, false, left, "IN", right,
							"AND");
					query.updateWhereClauseStructure();
				 }
					
				currentPeriodValuesByType.put(levelColumn, currentPeriodValue);
			}
		}
		query.updateWhereClauseStructure();

		return currentPeriodValuesByType;
	}

	private Set<String> getTemporalFieldsInSelect(List<ISelectField> selectFields,
			Map<String, String> hierarchyFullColumnMap) {
		Set<String> temporalFieldsInSelect = new HashSet<>();

		LOOP_3:

		for (String levelType : hierarchyFullColumnMap.keySet()) {
			String levelColumn = hierarchyFullColumnMap.get(levelType);
			// lo cerco nelle select
			for (ISelectField sfield : selectFields) {
				if (sfield.isSimpleField()) {
					SimpleSelectField ssField = (SimpleSelectField) sfield;
					if (levelColumn.equals(ssField.getUniqueName())) {
						temporalFieldsInSelect.add(levelColumn);
						continue LOOP_3;
					}
				}
			}
		}

		return temporalFieldsInSelect;
	}

	private void addTimeIdToQuery(Query query, IModelEntity temporalDimension, String temporalDimensionId) {
		String fieldUniqueName = extractColumnName(temporalDimension, temporalDimensionId);
		String function = "MIN";
		boolean include = true;
		boolean visible = false;
		boolean groupByField = false;
		String orderType = "ASC";
		String pattern = null;
		String temporalOperand = null;
		String temporalOperandParameter = null;
		query.addSelectFiled(fieldUniqueName, function, temporalDimensionId, include, visible, groupByField, orderType,
				pattern, temporalOperand, temporalOperandParameter);
	}

	private void addYearToQuery(Query query, IModelEntity temporalDimension,
			Map<String, String> hierarchyFullColumnMap) {
		String fieldUniqueName = hierarchyFullColumnMap.get("YEAR");
		String function = null;
		boolean include = true;
		boolean visible = false;
		boolean groupByField = true;
		String orderType = "ASC";
		String pattern = null;
		String temporalOperand = null;
		String temporalOperandParameter = null;
		query.addSelectFiled(fieldUniqueName, function, "YEAR", include, visible, groupByField, orderType, pattern,
				temporalOperand, temporalOperandParameter);
	}

	private void addSumFunctionToAllMeasureInSelect(List<ISelectField> selectFields) {
		for (ISelectField sfield : selectFields) {
			if (sfield.isSimpleField()) {
				SimpleSelectField ssField = (SimpleSelectField) sfield;

				if (ssField.getFunction() == null && "MEASURE".equals(ssField.getNature())
						|| ssField.getTemporalOperand() != null && ssField.getTemporalOperand().length() > 0) {
					ssField.setFunction(AggregationFunctions.get(AggregationFunctions.SUM));
				}

			}
		}
	}

	private Set<String> addMissingGroupByToTheQuery(Query query, List<ISelectField> selectFields,
			Set<String> inlineFilterFieldTypes, Map<String, String> hierarchyFullColumnMap) {
		Set<String> aliasesToBeRemovedAfterExecution = new HashSet<>();

		Set<String> temporalFieldAlreadyInSelect = new HashSet<>();
		for (String levelType : hierarchyFullColumnMap.keySet()) {
			String levelColumn = hierarchyFullColumnMap.get(levelType);
			for (ISelectField sfield : selectFields) {
				if (sfield.isSimpleField()) {
					SimpleSelectField ssField = (SimpleSelectField) sfield;
					if (levelColumn.equals(ssField.getUniqueName())) {
						temporalFieldAlreadyInSelect.add(levelColumn);
						// se nei campi select � presente un campo della
						// gerarchia, tale campo parteciper� al raggruppamento
						ssField.setGroupByField(true);
					}
				}
			}
		}

		for (String inlineFilterType : inlineFilterFieldTypes) {
			if (!temporalFieldAlreadyInSelect.contains(hierarchyFullColumnMap.get(inlineFilterType))) {
				String fieldUniqueName = hierarchyFullColumnMap.get(inlineFilterType);
				boolean include = true;
				boolean visible = false;
				boolean groupByField = true;
				String orderType = null;
				String pattern = null;
				String temporalOperand = null;
				String temporalOperandParameter = null;
				query.addSelectFiled(fieldUniqueName, null, inlineFilterType, include, visible, groupByField, orderType,
						pattern, temporalOperand, temporalOperandParameter);

				aliasesToBeRemovedAfterExecution.add(fieldUniqueName);
			}
		}
		return aliasesToBeRemovedAfterExecution;
	}

	private Set<String> extractYearsToBeAddedToWhereClause(List<ISelectField> selectFields, String relativeYear,
			Set<String> yearsInWhere, LinkedList<String> allYearsOnDWHString, int relativeYearIndex,
			Map<String, String> hierarchyFullColumnMap, Map<String, List<String>> distinctPeriods,
			Map<String, String> currentPeriodValuesByType) {
		Set<String> yearsToBeAddedToWhereClause = new HashSet<>();

		Map<String, List<String>> distinctPeriodsByType = new HashMap<>();
		for (String type : hierarchyFullColumnMap.keySet()) {
			distinctPeriodsByType.put(type, distinctPeriods.get(hierarchyFullColumnMap.get(type)));
		}
		Map<String, Integer> currentPeriodsNumbered = new HashMap<>();
		for (String type : currentPeriodValuesByType.keySet()) {
			String currentPeriodValue = currentPeriodValuesByType.get(type);
			List<String> distinctPeriodsForThisType = distinctPeriods.get(type);
			int currentValueIndexForThisType = -1;
			for (int i = 0; i < distinctPeriodsForThisType.size(); i++) {
				String period = distinctPeriodsForThisType.get(i);
				if (period.equals(currentPeriodValue)) {
					currentValueIndexForThisType = i;
					break;
				}
			}
			currentPeriodsNumbered.put(type, currentValueIndexForThisType + 1);
		}

		for (ISelectField sfield : selectFields) {
			if (sfield.isSimpleField()) {
				SimpleSelectField ssField = (SimpleSelectField) sfield;
				String temporalOperand = ssField.getTemporalOperand();
				String temporalOperandParameter = ssField.getTemporalOperandParameter();

				int n = (temporalOperandParameter == null || !temporalOperandParameter.matches("-?[0-9]*")) ? 0
						: Integer.parseInt(temporalOperandParameter);

				if (temporalOperand != null && !"".equals(temporalOperand)) {
					Integer yearOtherIndex = null;
					String periodType = null;
					boolean lastPeriod = false;
					switch (temporalOperand) {

					case TEMPORAL_OPERAND_QTD:
						if (periodType == null) {
							periodType = "QUARTER";
						}
					case TEMPORAL_OPERAND_MTD:
						if (periodType == null) {
							periodType = "MONTH";
						}
					case TEMPORAL_OPERAND_WTD:
						if (periodType == null) {
							periodType = "WEEK";
						}
					case TEMPORAL_OPERAND_LAST_QUARTER:
						if (periodType == null) {
							periodType = "QUARTER";
							lastPeriod = true;
						}

					case TEMPORAL_OPERAND_LAST_MONTH:
						if (periodType == null) {
							periodType = "MONTH";
							lastPeriod = true;
						}

					case TEMPORAL_OPERAND_LAST_WEEK:
						if (periodType == null) {
							periodType = "WEEK";
							lastPeriod = true;
						}

						Integer currentPeriodNumber = currentPeriodsNumbered
								.get(hierarchyFullColumnMap.get(periodType));
						Integer otherPeriodNumber = currentPeriodNumber - n;
						if (otherPeriodNumber < currentPeriodNumber) {
							otherPeriodNumber = otherPeriodNumber + 1;
						} else {
							otherPeriodNumber = otherPeriodNumber - 1;
						}

						List<String> periods = distinctPeriodsByType.get(periodType);
						int periodsCount = periods.size();
						int periodOtherIndex = (otherPeriodNumber % periodsCount);

						int yearOffset = 0;
						while (periodOtherIndex < 0) {
							periodOtherIndex += periodsCount;
							yearOffset--;
						}
						while (periodOtherIndex >= periodsCount) {
							periodOtherIndex = periodOtherIndex % periodsCount;
							yearOffset++;
						}

						yearOtherIndex = (int) (relativeYearIndex + yearOffset);
						if (yearOtherIndex < 0) {
							yearOtherIndex = 0;
						}
						if (yearOtherIndex >= allYearsOnDWHString.size()) {
							yearOtherIndex = allYearsOnDWHString.size() - 1;
							periodOtherIndex = periods.size() - 1;
						}

					case TEMPORAL_OPERAND_LAST_YEAR:
						if (yearOtherIndex == null) {
							yearOtherIndex = relativeYearIndex - n;
						}

						if (lastPeriod) {
							if (yearOtherIndex < relativeYearIndex) {
								yearsToBeAddedToWhereClause
										.addAll(allYearsOnDWHString.subList(yearOtherIndex, relativeYearIndex + 1));
							} else {
								yearsToBeAddedToWhereClause
										.addAll(allYearsOnDWHString.subList(relativeYearIndex, yearOtherIndex + 1));
							}
						} else {
							if (yearOtherIndex >= 0 && allYearsOnDWHString.size() > yearOtherIndex) {
								yearsToBeAddedToWhereClause.add(allYearsOnDWHString.get(yearOtherIndex));
							}
						}
						break;

					case TEMPORAL_OPERAND_YTD:
					case TEMPORAL_OPERAND_PARALLEL_YEAR:
						int parallelYearIndex = relativeYearIndex - n;
						if (parallelYearIndex >= 0 && allYearsOnDWHString.size() > parallelYearIndex) {
							yearsToBeAddedToWhereClause.add(allYearsOnDWHString.get(parallelYearIndex));
						}
						break;

					default:
						break;
					}
				}
			}
		}
		return yearsToBeAddedToWhereClause;
	}

	private Set<String> extractInlineFilterFieldTypes(List<ISelectField> selectFields, Map<String, String> hierarchyFullColumnMap) {
		
		boolean multipleLineResult = isMultiLineResult(selectFields, hierarchyFullColumnMap);
		
		return harvestInlineFilterType(selectFields, !multipleLineResult);
	}

	private boolean isMultiLineResult(List<ISelectField> selectFields, Map<String, String> hierarchyFullColumnMap) {
		boolean multipleLineResult = false;
		for (ISelectField sfield : selectFields) {
			if(sfield instanceof SimpleSelectField) {
				String sfieldUniqueName = ((SimpleSelectField)sfield).getUniqueName();
				if(hierarchyFullColumnMap.values().contains(sfieldUniqueName)) {
					multipleLineResult = true;
					break;
				}
			}
		}
		return multipleLineResult;
	}

	private boolean hasInlineFilters(List<ISelectField> selectFields) {
		return harvestInlineFilterType(selectFields, false).size() > 0;
	}
	
	private Set<String> harvestInlineFilterType(List<ISelectField> selectFields, boolean singleLineResult) {
		Set<String> inlineFilterFieldTypes = new HashSet<>();
		for (ISelectField sfield : selectFields) {
			if (sfield.isSimpleField()) {
				SimpleSelectField ssField = (SimpleSelectField) sfield;
				String temporalOperand = ssField.getTemporalOperand();

				if (temporalOperand != null) {
					switch (temporalOperand) {

					// YEAR
					case TEMPORAL_OPERAND_YTD:
						if(singleLineResult) {
							inlineFilterFieldTypes.add("MONTH");
							inlineFilterFieldTypes.add("DAY");
						}
					case TEMPORAL_OPERAND_LAST_YEAR:
					case TEMPORAL_OPERAND_PARALLEL_YEAR:
						inlineFilterFieldTypes.add("YEAR");
						break;

					// QUARTER
					case TEMPORAL_OPERAND_QTD:
						if(singleLineResult) {
							inlineFilterFieldTypes.add("MONTH");
							inlineFilterFieldTypes.add("DAY");
						}
					case TEMPORAL_OPERAND_LAST_QUARTER:
						inlineFilterFieldTypes.add("QUARTER");
						break;

					// MONTH
					case TEMPORAL_OPERAND_MTD:
						if(singleLineResult) {
							inlineFilterFieldTypes.add("DAY");
						}
					case TEMPORAL_OPERAND_LAST_MONTH:
						inlineFilterFieldTypes.add("MONTH");
						break;

					// WEEK
					case TEMPORAL_OPERAND_WTD:
						if(singleLineResult) {
							inlineFilterFieldTypes.add("DAY");
						}
					case TEMPORAL_OPERAND_LAST_WEEK:
						inlineFilterFieldTypes.add("WEEK");
						break;

					default:
						break;
					}
				}

			}
		}
		return inlineFilterFieldTypes;
	}
	
	private boolean containsPeriodToDate(List<ISelectField> selectFields) {
		for (ISelectField sfield : selectFields) {
			if (sfield.isSimpleField()) {
				SimpleSelectField ssField = (SimpleSelectField) sfield;
				String temporalOperand = ssField.getTemporalOperand();
				if (temporalOperand != null) {
					switch (temporalOperand) {
					case TEMPORAL_OPERAND_YTD:
					case TEMPORAL_OPERAND_QTD:
					case TEMPORAL_OPERAND_MTD:
					case TEMPORAL_OPERAND_WTD:
						return true;

					default:
						break;
					}
				}
			}
		}
		return false;
	}

	private Set<String> extractYearsFromWhereFields(List<WhereField> whereFields, String yearColumn) {
		Set<String> yearsInWhere = new HashSet<>();

		for (WhereField wField : whereFields) {
			if (wField.getLeftOperand().values != null && wField.getLeftOperand().values.length > 0
					&& yearColumn.equals(wField.getLeftOperand().values[0]) && "EQUALS TO".equals(wField.getOperator())
					&& wField.getRightOperand().values != null && wField.getRightOperand().values.length > 0) {
				yearsInWhere.add(wField.getRightOperand().values[0] + "");
			}
		}

		return yearsInWhere;
	}

	private String extractColumnName(IModelEntity temporalDimension, String column) {
		return temporalDimension.getType() + ":" + column;
	}

	private IModelEntity getTemporalDimension(it.eng.qbe.datasource.IDataSource dataSource) {
		IModelEntity temporalDimension = null;
		Iterator<String> it = dataSource.getModelStructure().getModelNames().iterator();
		while (it.hasNext()) {
			String modelName = it.next();
			List<IModelEntity> rootEntities = getQbeDataSource().getModelStructure().getRootEntities(modelName);
			for (IModelEntity bc : rootEntities) {
				if ("temporal_dimension".equals(bc.getProperty("type"))) {
					temporalDimension = bc;
					break;
				}
			}
		}
		return temporalDimension;
	}

	private IModelEntity getTimeDimension(it.eng.qbe.datasource.IDataSource dataSource) {
		IModelEntity timeDimension = null;
		Iterator<String> it = dataSource.getModelStructure().getModelNames().iterator();
		while (it.hasNext()) {
			String modelName = it.next();
			List<IModelEntity> rootEntities = getQbeDataSource().getModelStructure().getRootEntities(modelName);
			for (IModelEntity bc : rootEntities) {
				if ("time_dimension".equals(bc.getProperty("type"))) {
					timeDimension = bc;
					break;
				}
			}
		}
		return timeDimension;
	}

	private TemporalRecord getCurrentPeriod(IModelEntity temporalDimension, String idField, String periodField,
			Date actualTime, String... parentPeriodFields) {
		try {
			// nullsafe
			parentPeriodFields = parentPeriodFields != null ? parentPeriodFields : new String[0];

			actualTime = actualTime != null ? actualTime : new Date();

			Query currentPeriodQuery = new Query();
			currentPeriodQuery.addSelectFiled(temporalDimension.getType() + ":" + idField, null, "ID", true, true,
					false, "ASC", null);
			currentPeriodQuery.addSelectFiled(temporalDimension.getType() + ":" + periodField, null, "LEVEL", true,
					true, false, null, null);
			for (String parentPeriodField : parentPeriodFields) {
				currentPeriodQuery.addSelectFiled(temporalDimension.getType() + ":" + parentPeriodField, null,
						parentPeriodField, true, true, false, null, null);
			}

			String temporalDimensionDateField = getDateField(temporalDimension);

			Operand left = new Operand(new String[] { temporalDimension.getType() + ":" + temporalDimensionDateField },
					temporalDimension.getName() + ":" + temporalDimensionDateField, "Field Content", new String[] {""}, new String[] {""}, "");
			Operand right = new Operand(new String[] { new SimpleDateFormat("dd/MM/yyyy").format(actualTime) },
					new SimpleDateFormat("dd/MM/yyyy").format(actualTime), "Static Content", new String[] {""}, new String[] {""}, "");
			currentPeriodQuery.addWhereField("Filter1", "Filter1", false, left, "EQUALS TO", right, "AND");
			ExpressionNode newFilterNode = new ExpressionNode("NODE_CONST", "$F{" + "Filter1" + "}");
			currentPeriodQuery.setWhereClauseStructure(newFilterNode);

			IDataStore currentPeriodDataStore = executeDatamartQuery(currentPeriodQuery);
			@SuppressWarnings("unchecked")
			Iterator<IRecord> currentPeriodIterator = currentPeriodDataStore.iterator();

			TemporalRecord currentPeriodRecord = null;
			while (currentPeriodIterator.hasNext()) {
				IRecord r = currentPeriodIterator.next();
				currentPeriodRecord = new TemporalRecord(r, parentPeriodFields.length);
				break;
			}
			return currentPeriodRecord;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException(
					"Impossible to retrieve current '" + periodField + "' from the temporal dimesion");
		}
	}

	private TemporalRecord getCurrentTime(IModelEntity timeDimension, String idField, String periodField,
			Date actualTime, String... parentPeriodFields) {

		// nullsafe
		parentPeriodFields = parentPeriodFields != null ? parentPeriodFields : new String[0];

		actualTime = actualTime != null ? actualTime : new Date();

		Query currentTimeQuery = new Query();
		currentTimeQuery.addSelectFiled(timeDimension.getType() + ":" + idField, null, "ID", true, true, false, "ASC",
				null);
		currentTimeQuery.addSelectFiled(timeDimension.getType() + ":" + periodField, null, "LEVEL", true, true, false,
				null, null);
		for (String parentPeriodField : parentPeriodFields) {
			currentTimeQuery.addSelectFiled(timeDimension.getType() + ":" + parentPeriodField, null, parentPeriodField,
					true, true, false, null, null);
		}

		String timeDimensionIdField = "ID";

		Operand left = new Operand(new String[] { timeDimension.getType() + ":" + timeDimensionIdField },
				timeDimension.getName() + ":" + timeDimensionIdField, "Field Content", new String[] {""}, new String[] {""}, "");

		Operand right = new Operand(new String[] { new SimpleDateFormat("HHmm").format(actualTime) },
				new SimpleDateFormat("HHmm").format(actualTime), "Static Content", new String[] {""}, new String[] {""}, "");

		currentTimeQuery.addWhereField("Filter1", "Filter1", false, left, "EQUALS TO", right, "AND");
		ExpressionNode newFilterNode = new ExpressionNode("NODE_CONST", "$F{" + "Filter1" + "}");
		currentTimeQuery.setWhereClauseStructure(newFilterNode);

		IDataStore currentTimeDataStore = executeDatamartQuery(currentTimeQuery);
		@SuppressWarnings("unchecked")
		Iterator<IRecord> currentTimeIterator = currentTimeDataStore.iterator();

		TemporalRecord currentTimeRecord = null;
		while (currentTimeIterator.hasNext()) {
			IRecord r = currentTimeIterator.next();
			currentTimeRecord = new TemporalRecord(r, parentPeriodFields.length);
			break;
		}
		return currentTimeRecord;
	}

	private LinkedList<TemporalRecord> loadAllPeriodsStartingDate(IModelEntity temporalDimension, String idField,
			String periodField, String... parentPeriodFields) {

		// nullsafe
		parentPeriodFields = parentPeriodFields != null ? parentPeriodFields : new String[] {};

		Query periodsStartingDates = new Query();
		periodsStartingDates.addSelectFiled(extractColumnName(temporalDimension, idField), "MIN", "ID", true, true,
				false, "ASC", null);
		periodsStartingDates.addSelectFiled(extractColumnName(temporalDimension, periodField), null, "LEVEL", true,
				true, true, null, null);
		for (String parentPeriodField : parentPeriodFields) {
			periodsStartingDates.addSelectFiled(extractColumnName(temporalDimension, parentPeriodField), null,
					parentPeriodField, true, true, true, null, null);
		}
		IDataStore periodsStartingDatesDataStore = executeDatamartQuery(periodsStartingDates);
		@SuppressWarnings("unchecked")
		Iterator<IRecord> periodsStartingDatesIterator = periodsStartingDatesDataStore.iterator();

		LinkedList<TemporalRecord> periodStartingDates = new LinkedList<TemporalRecord>();
		while (periodsStartingDatesIterator.hasNext()) {
			IRecord r = periodsStartingDatesIterator.next();
			TemporalRecord tr = new TemporalRecord(r, parentPeriodFields.length);
			periodStartingDates.add(tr);
		}
		return periodStartingDates;
	}

	private LinkedList<String> loadDistinctPeriods(IModelEntity temporalDimension, String idField,
			String temporalFieldColumn) {

		Query distinctPeriodsQuery = new Query();
		distinctPeriodsQuery.addSelectFiled(extractColumnName(temporalDimension, idField), "MIN", "ID", true, true,
				false, "ASC", null);
		distinctPeriodsQuery.addSelectFiled(temporalFieldColumn, null, "LEVEL", true, true, true, null, null);
		distinctPeriodsQuery.setDistinctClauseEnabled(true);

		IDataStore distinctPeriodsDatesDataStore = executeDatamartQuery(distinctPeriodsQuery);
		@SuppressWarnings("unchecked")
		Iterator<IRecord> distinctPeriod = distinctPeriodsDatesDataStore.iterator();

		LinkedList<String> distinctPeriods = new LinkedList<String>();
		while (distinctPeriod.hasNext()) {
			IRecord r = distinctPeriod.next();
			TemporalRecord tr = new TemporalRecord(r, 0);
			distinctPeriods.add(tr.getPeriod().toString());
		}
		return distinctPeriods;
	}

	private IDataStore executeDatamartQuery(Query myquery) {

		IStatement statement = getQbeDataSource().createStatement(myquery);
		AbstractQbeDataSet qbeDataSet = (AbstractQbeDataSet)QbeDatasetFactory.createDataSet(statement);
		
		String queryString = qbeDataSet.getStatement().getQueryString();
		logger.debug("QUERY STRING: " + queryString);

		qbeDataSet.loadData();
		IDataStore dataStore = qbeDataSet.getDataStore();

		return dataStore;
	}

	/* END FROM SET CATALOGUE ACTION */

}
