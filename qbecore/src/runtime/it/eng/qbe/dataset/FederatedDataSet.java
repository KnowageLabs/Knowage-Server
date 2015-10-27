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
import org.json.JSONArray;
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

	FederationDefinition federation;

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

	public FederatedDataSet(FederationDefinition federation){
		this.federation = federation;
	}

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


	public it.eng.qbe.datasource.IDataSource getQbeDataSource() {

		Map<String, Object> dataSourceProperties = new HashMap<String, Object>();


		dataSourceProperties.put("datasource", dataSource);
		dataSourceProperties.put("dblinkMap", new HashMap());

		if (this.getSourceDataset() != null) {
			List<IDataSet> dataSets = new ArrayList<IDataSet>();
			dataSets.add(this.getSourceDataset());
			dataSourceProperties.put(EngineConstants.ENV_DATASETS, dataSets);
		}
		
		if (this.getSourceDataset() != null) {
			List<IDataSet> dataSets = new ArrayList<IDataSet>();
			dataSets.add(this.getSourceDataset());
			dataSourceProperties.put(EngineConstants.ENV_DATASETS, dataSets);
		}
		
		JSONObject relations = federation.getRelationshipsAsJSONObject();
		dataSourceProperties.put(EngineConstants.ENV_RELATIONS, relations);
		

		return getDataSourceFromDataSet(dataSourceProperties, useCache);
	}


	public String getDsType() {
		return DS_TYPE;
	}

	public it.eng.qbe.datasource.IDataSource getDataSourceFromDataSet(Map<String, Object> dataSourceProperties, boolean useCache) {

		it.eng.qbe.datasource.IDataSource dataSource;
		List<String> datasetNames = new ArrayList<String>();

		CompositeDataSourceConfiguration compositeConfiguration = new CompositeDataSourceConfiguration(DataSetDataSource.EMPTY_MODEL_NAME);
		Iterator<String> it = dataSourceProperties.keySet().iterator();
		while(it.hasNext()) {
			String propertyName = it.next();
			compositeConfiguration.loadDataSourceProperties().put(propertyName, dataSourceProperties.get(propertyName));
		}

		//adjust the name of the relations
//		try {
//			FederationUtils.adjustRelationName(new JSONArray(federation.getRelationships()), getDataset2CacheTableName());
//		} catch (JSONException e) {
//			logger.error("Error adjusting the table names in the relation for federation",e);
//			throw new SpagoBIRuntimeException("Error adjusting the table names in the relation for federation",e);
//		}
		
		
		//refresh the datasets on cache
		for (Iterator iterator = federation.getSourceDatasets().iterator(); iterator.hasNext();) {
			IDataSet dataSets = (IDataSet) iterator.next();
			datasetNames.add(dataSets.getLabel());
		}
		JSONObject datasetLabels = FederationUtils.createDatasetsOnCache(datasetNames);
		setDataset2CacheTableName(datasetLabels);
		
		
		//create the jdbc datasets linked to the tables on cache
		for (Iterator iterator = federation.getSourceDatasets().iterator(); iterator.hasNext();) {
			IDataSet dataSets = (IDataSet) iterator.next();
			IDataSet cachedDataSet = null;
			try {
				cachedDataSet = FederationUtils.createDatasetOnCache(getDataset2CacheTableName().getString(dataSets.getLabel()), dataSets, getDataSourceForWriting());
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

	public FederationDefinition getDatasetFederation() {
		return federation;
	}
	public void setDataset2CacheTableName(JSONObject dataset2CacheTableName) {
		this.dataset2CacheTableName = dataset2CacheTableName;
	}   
	public JSONObject getDataset2CacheTableName() {
		return this.dataset2CacheTableName;
	}



}
