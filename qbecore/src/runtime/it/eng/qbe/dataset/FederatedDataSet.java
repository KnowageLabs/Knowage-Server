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

import it.eng.qbe.datasource.DriverManager;
import it.eng.qbe.datasource.configuration.CompositeDataSourceConfiguration;
import it.eng.qbe.datasource.configuration.DataSetDataSourceConfiguration;
import it.eng.qbe.datasource.dataset.DataSetDataSource;
import it.eng.qbe.datasource.dataset.DataSetDriver;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.bo.DataSetFactory;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.federation.FederationDefinition;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * dataset derived from the federation of differnt datasets
 * 
 * @author ghedin
 */
public class FederatedDataSet extends QbeDataSet {

	public static String DS_TYPE = "SbiFederatedDataSet";
	public static final String QBE_DATASET_CACHE_MAP = "datasetCacheMap";

	private static transient Logger logger = Logger.getLogger(FederatedDataSet.class);

	private FederationDefinition federation;
	private String userId = "";

	public FederatedDataSet(SpagoBiDataSet dataSetConfig) {
		super(dataSetConfig);

		federation = new FederationDefinition();
		setDependentDataSets((SpagoBiDataSet[]) dataSetConfig.getDependentDataSets());

		federation.setLabel(dataSetConfig.getFederationlabel());
		federation.setFederation_id(dataSetConfig.getFederationId());
		federation.setRelationships(dataSetConfig.getFederationRelations());
		
		federation.setDegenerated(dataSetConfig.isDegenerated());

		// load the map dataset->cached table name
		JSONObject jsonConf = ObjectUtils.toJSONObject(dataSetConfig.getConfiguration());
		try {
			this.setDataset2CacheTableName((jsonConf.opt(FederatedDataSet.QBE_DATASET_CACHE_MAP) != null) ? (JSONObject) jsonConf
					.get(FederatedDataSet.QBE_DATASET_CACHE_MAP) : new JSONObject());
		} catch (JSONException e) {
			logger.error("Error loading the map dataset->cached dataset table name", e);
			throw new SpagoBIEngineRuntimeException("Error loading the map dataset->cached dataset table name", e);
		}
	}

	public FederatedDataSet(FederationDefinition federation, String userId) {
		this.userId = userId;
		this.federation = federation;
	}

	@Override
	public SpagoBiDataSet toSpagoBiDataSet() {
		SpagoBiDataSet sbd = super.toSpagoBiDataSet();

		int i = 0;

		Set<IDataSet> dependentDataSets = federation.getSourceDatasets();
		SpagoBiDataSet[] dependantDatasets = new SpagoBiDataSet[dependentDataSets.size()];

		for (IDataSet dataset : dependentDataSets) {
			dependantDatasets[i] = dataset.toSpagoBiDataSet();
			i++;
		}

		sbd.setDependentDataSets(dependantDatasets);
		sbd.setFederationlabel(federation.getLabel());
		sbd.setFederationRelations(federation.getRelationships());
		sbd.setFederationId(federation.getFederation_id());
		sbd.setDegenerated(federation.isDegenerated());
		
		sbd.setType(DS_TYPE);

		return sbd;
	}

	public void setDependentDataSets(Set<IDataSet> sourceDatasets) {
		federation.setSourceDatasets(sourceDatasets);
	}

	public void setDependentDataSets(List<IDataSet> sourceDatasets) {
		Set<IDataSet> sourceDatasetsSet = new HashSet<IDataSet>();
		for (Iterator iterator = sourceDatasets.iterator(); iterator.hasNext();) {
			IDataSet iDataSet = (IDataSet) iterator.next();
			sourceDatasetsSet.add(iDataSet);
		}
		federation.setSourceDatasets(sourceDatasetsSet);
	}

	public void setDependentDataSets(SpagoBiDataSet[] sourceDatasets) {
		Set<IDataSet> sourceDatasetsSet = new HashSet<IDataSet>();
		for (int i = 0; i < sourceDatasets.length; i++) {
			IDataSet iDataSet = DataSetFactory.getDataSet(sourceDatasets[i], getUserIn());
			sourceDatasetsSet.add(iDataSet);
		}
		federation.setSourceDatasets(sourceDatasetsSet);
	}

	@Override
	public it.eng.qbe.datasource.IDataSource getQbeDataSource() {

		Map<String, Object> dataSourceProperties = new HashMap<String, Object>();

		dataSourceProperties.put("datasource", dataSource);
		dataSourceProperties.put("dblinkMap", new HashMap());

		if (this.getSourceDataset() != null) {
			List<IDataSet> dataSets = new ArrayList<IDataSet>();
			dataSets.add(this.getSourceDataset());
			dataSourceProperties.put(EngineConstants.ENV_DATASETS, dataSets);
		}

		JSONObject relations = federation.getRelationshipsAsJSONObject();
		dataSourceProperties.put(EngineConstants.ENV_RELATIONS, relations);

		dataSourceProperties.put(EngineConstants.ENV_USER_ID, getUserId());

		return getDataSourceFromDataSet(dataSourceProperties, useCache);
	}

	@Override
	public String getDsType() {
		return DS_TYPE;
	}

	@Override
	public it.eng.qbe.datasource.IDataSource getDataSourceFromDataSet(Map<String, Object> dataSourceProperties, boolean useCache) {

		it.eng.qbe.datasource.IDataSource dataSource;
		List<String> datasetNames = new ArrayList<String>();

		CompositeDataSourceConfiguration compositeConfiguration = new CompositeDataSourceConfiguration(DataSetDataSource.EMPTY_MODEL_NAME);
		Iterator<String> it = dataSourceProperties.keySet().iterator();

		while (it.hasNext()) {
			String propertyName = it.next();
			compositeConfiguration.loadDataSourceProperties().put(propertyName, dataSourceProperties.get(propertyName));
		}

		// refresh the datasets on cache
		for (Iterator iterator = federation.getSourceDatasets().iterator(); iterator.hasNext();) {
			IDataSet dataSets = (IDataSet) iterator.next();
			datasetNames.add(dataSets.getLabel());
		}

		String userId = (String) dataSourceProperties.get(EngineConstants.ENV_USER_ID);
		
		JSONObject datasetLabels = null;
		try {
			datasetLabels = FederationUtils.createDatasetsOnCache(federation.getDataSetRelationKeysMap(), userId);
		} catch (JSONException e1) {
			logger.error("Error caching the datasets",e1);
			throw new SpagoBIRuntimeException("Error caching the datasets", e1);
		}
		setDataset2CacheTableName(datasetLabels);

		// create the jdbc datasets linked to the tables on cache
		for (Iterator iterator = federation.getSourceDatasets().iterator(); iterator.hasNext();) {
			IDataSet dataSets = (IDataSet) iterator.next();
			IDataSet cachedDataSet = null;
			try {
				cachedDataSet = FederationUtils.createDatasetOnCache(getDataset2CacheTableName().getString(dataSets.getLabel()), dataSets,
						getDataSourceForReading());
			} catch (JSONException e) {
				logger.error("Error getting the name of the cached table linked to the dataset " + dataSets.getLabel(), e);
				throw new SpagoBIRuntimeException("Error getting the name of the cached table linked to the dataset " + dataSets.getLabel(), e);
			}

			DataSetDataSourceConfiguration c = new DataSetDataSourceConfiguration((cachedDataSet).getLabel(), cachedDataSet);
			compositeConfiguration.addSubConfiguration(c);
			// compositeConfiguration.loadDataSourceProperties().put(DataSetDataSource.DATA_SOURCE_TYPE, DS_TYPE);
		}

		dataSource = DriverManager.getDataSource(DataSetDriver.DRIVER_ID, compositeConfiguration, useCache);

		return dataSource;
	}

	@Override
	public FederationDefinition getDatasetFederation() {
		return federation;
	}

	public void setDataset2CacheTableName(JSONObject dataset2CacheTableName) {
		this.dataset2CacheTableName = dataset2CacheTableName;
	}

	public JSONObject getDataset2CacheTableName() {
		return this.dataset2CacheTableName;
	}

	public String getUserId() {
		String userIdFromParam = (String) getParamsMap().get(SpagoBIConstants.USER_ID);
		if (userIdFromParam != null && userIdFromParam.length() > 0) {
			userId = userIdFromParam;
		} else {
			userId = (String) getParamsMap().get("DOCUMENT_USER");
		}

		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public FederationDefinition getFederation() {
		return federation;
	}

	public void setFederation(FederationDefinition federation) {
		this.federation = federation;
	}

}
