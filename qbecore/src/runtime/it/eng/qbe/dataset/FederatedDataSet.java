/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
 * @author ghedin
 *
 */
public class FederatedDataSet extends QbeDataSet {

	public static String DS_TYPE = "SbiFederatedDataSet";
	public static final String QBE_DATASET_CACHE_MAP = "datasetCacheMap";

	private static transient Logger logger = Logger.getLogger(FederatedDataSet.class);

	private FederationDefinition federation;
	private  String userId = "";

	public FederatedDataSet(SpagoBiDataSet dataSetConfig) {
		super(dataSetConfig);

		federation = new FederationDefinition();
		setDependentDataSets((SpagoBiDataSet[])dataSetConfig.getDependentDataSets());

		federation.setLabel(dataSetConfig.getFederationlabel());
		federation.setFederation_id(dataSetConfig.getFederationId());
		federation.setRelationships(dataSetConfig.getFederationRelations());

		//load the map dataset->cached table name
		JSONObject jsonConf  = ObjectUtils.toJSONObject(dataSetConfig.getConfiguration());
		try {
			this.setDataset2CacheTableName((jsonConf.opt(FederatedDataSet.QBE_DATASET_CACHE_MAP)!=null)?(JSONObject)jsonConf.get(FederatedDataSet.QBE_DATASET_CACHE_MAP): new JSONObject());
		} catch (JSONException e) {
			logger.error("Error loading the map dataset->cached dataset table name",e);
			throw new SpagoBIEngineRuntimeException("Error loading the map dataset->cached dataset table name",e);
		}
	}

	public FederatedDataSet(FederationDefinition federation, String userId){
		this.userId = userId;
		this.federation = federation;
	}

	@Override
	public SpagoBiDataSet toSpagoBiDataSet() {
		SpagoBiDataSet sbd = super.toSpagoBiDataSet();

		int i=0;

		Set<IDataSet> dependentDataSets= federation.getSourceDatasets();
		SpagoBiDataSet[] dependantDatasets = new SpagoBiDataSet[dependentDataSets.size()];

		for (IDataSet dataset : dependentDataSets) {
			dependantDatasets[i] = dataset.toSpagoBiDataSet();
			i++;
		}

		sbd.setDependentDataSets(dependantDatasets);
		sbd.setFederationlabel(federation.getLabel());
		sbd.setFederationRelations(federation.getRelationships());
		sbd.setFederationId(federation.getFederation_id());

		sbd.setType(DS_TYPE);

		return sbd;
	}


	public void setDependentDataSets(Set<IDataSet> sourceDatasets){
		federation.setSourceDatasets(sourceDatasets);
	}


	public void setDependentDataSets(List<IDataSet> sourceDatasets){
		Set<IDataSet> sourceDatasetsSet = new HashSet<IDataSet>();
		for (Iterator iterator = sourceDatasets.iterator(); iterator.hasNext();) {
			IDataSet iDataSet = (IDataSet) iterator.next();
			sourceDatasetsSet.add(iDataSet);
		}
		federation.setSourceDatasets(sourceDatasetsSet);
	}


	public void setDependentDataSets(SpagoBiDataSet[] sourceDatasets){
		Set<IDataSet> sourceDatasetsSet = new HashSet<IDataSet>();
		for (int i=0; i<sourceDatasets.length;i++) {
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

		while(it.hasNext()) {
			String propertyName = it.next();
			compositeConfiguration.loadDataSourceProperties().put(propertyName, dataSourceProperties.get(propertyName));
		}

		//refresh the datasets on cache
		for (Iterator iterator = federation.getSourceDatasets().iterator(); iterator.hasNext();) {
			IDataSet dataSets = (IDataSet) iterator.next();
			datasetNames.add(dataSets.getLabel());
		}

		String userId = (String) dataSourceProperties.get(EngineConstants.ENV_USER_ID);
		JSONObject datasetLabels = FederationUtils.createDatasetsOnCache(datasetNames, userId);
		setDataset2CacheTableName(datasetLabels);


		//create the jdbc datasets linked to the tables on cache
		for (Iterator iterator = federation.getSourceDatasets().iterator(); iterator.hasNext();) {
			IDataSet dataSets = (IDataSet) iterator.next();
			IDataSet cachedDataSet = null;
			try {
				cachedDataSet = FederationUtils.createDatasetOnCache(getDataset2CacheTableName().getString(dataSets.getLabel()), dataSets, getDataSourceForReading());
			} catch (JSONException e) {
				logger.error("Error getting the name of the cached table linked to the dataset "+dataSets.getLabel(),e);
				throw new SpagoBIRuntimeException("Error getting the name of the cached table linked to the dataset "+dataSets.getLabel(),e);
			}

			DataSetDataSourceConfiguration c = new DataSetDataSourceConfiguration((cachedDataSet).getLabel(), cachedDataSet);
			compositeConfiguration.addSubConfiguration(c);
			//compositeConfiguration.loadDataSourceProperties().put(DataSetDataSource.DATA_SOURCE_TYPE, DS_TYPE);
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
		String userIdFromParam = (String)getParamsMap().get(SpagoBIConstants.USER_ID);
		if(userIdFromParam!=null && userIdFromParam.length()>0){
			userId = userIdFromParam;
		}else{
			userId = (String)getParamsMap().get("DOCUMENT_USER");
		}


		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}



}
