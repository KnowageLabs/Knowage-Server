/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.dataset;

import it.eng.qbe.datasource.sql.DataSetPersister;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class FederationUtils {

	
	private static transient Logger logger = Logger.getLogger(FederationUtils.class);
	
	/**
	 * Here we get the relationships configuration and we replace the name of the dataset with the name of the cached dataset. This because the qbe is working wit the cached
	 */
	public static void adjustRelationName(JSONObject relations, Map<String, String> mapNameTable){
		logger.debug("Starting adjusting the relationsip definition");
		try {
			JSONArray relationshipArray = relations.getJSONArray("relationships");
			if(relationshipArray!=null){
				for(int y=0; y<relationshipArray.length(); y++){
					JSONObject jo = relationshipArray.getJSONObject(y);
					jo.getJSONObject("sourceTable").put("name", mapNameTable.get(jo.getJSONObject("sourceTable").get("name")));
					jo.getJSONObject("sourceTable").put("className", mapNameTable.get(jo.getJSONObject("sourceTable").get("className")));
					jo.getJSONObject("destinationTable").put("name", mapNameTable.get(jo.getJSONObject("destinationTable").get("name")));
					jo.getJSONObject("destinationTable").put("className", mapNameTable.get(jo.getJSONObject("destinationTable").get("className")));
				}
			}
		} catch (Exception e) {
			logger.error("Error adjusting the relationship definition for the federated dataset",e);
			throw new SpagoBIRuntimeException("Error adjusting the relationship definition for the federated dataset",e);
		}

	}
	
	/**
	 * Here we get the relationships configuration and we replace the name of the dataset with the name of the cached dataset. This because the qbe is working wit the cached
	 */
	public static void adjustRelationName(JSONArray relationshipArray, JSONObject mapNameTable){
		logger.debug("Starting adjusting the relationsip definition");
		try {
			
			if(relationshipArray!=null){
				relationshipArray = relationshipArray.getJSONArray(0);
				for(int y=0; y<relationshipArray.length(); y++){
					JSONObject jo = relationshipArray.getJSONObject(y);
					jo.getJSONObject("sourceTable").put("name", mapNameTable.get((String)jo.getJSONObject("sourceTable").get("name")));
					jo.getJSONObject("sourceTable").put("className", mapNameTable.get((String)jo.getJSONObject("sourceTable").get("className")));
					jo.getJSONObject("destinationTable").put("name", mapNameTable.get((String)jo.getJSONObject("destinationTable").get("name")));
					jo.getJSONObject("destinationTable").put("className", mapNameTable.get((String)jo.getJSONObject("destinationTable").get("className")));
				}
			}
			
		} catch (Exception e) {
			logger.error("Error adjusting the relationship definition for the federated dataset",e);
			throw new SpagoBIRuntimeException("Error adjusting the relationship definition for the federated dataset",e);
		}

	}

	/**
	 * Invokes the service that persits the datasets
	 */
	public static JSONObject createDatasetsOnCache(JSONObject datasetLabelsIndexes, String userId){
		//dave in cache the derived datasets
		logger.debug("Saving the datasets on cache");
		DataSetPersister dsp = new DataSetPersister();
		JSONObject datasetPersistedLabels = null;
		try {
			datasetPersistedLabels = dsp.cacheDataSets(datasetLabelsIndexes, userId );
		} catch (Exception e1) {
			logger.error("Error executing the service that persist the datasets on the cache",e1);
			throw new SpagoBIRuntimeException("Error executing the service that persist the datasets on the cache",e1);
		}
		return datasetPersistedLabels;
	}
	


	/**
	 * Creates a jdbc dataset on the cached table
	 * @param cachedTable
	 * @param dataSet
	 * @param cachedDataSource
	 * @return
	 */
	public static IDataSet createDatasetOnCache(String cachedTable, IDataSet dataSet, IDataSource cachedDataSource){
		logger.debug("Creating the cached dataset for the dataSet "+dataSet.getLabel());
		JDBCDataSet dataset = new JDBCDataSet();
		dataset.setDataSource(cachedDataSource);

		String query = "select * from " + cachedTable;
		logger.debug("The query for the dataset "+dataSet.getLabel()+" is "+query);

		dataset.setLabel(cachedTable);		
		dataset.setName(dataSet.getLabel());//the label because we need it for the joins
		dataset.setDescription(dataSet.getDescription());
		dataset.setQuery(query);
		dataset.setPersisted(true);
		dataset.setDsMetadata(dataSet.getDsMetadata());
		dataset.setPersistTableName(cachedTable);
		dataset.setDataSourceForReading(cachedDataSource);
		dataset.setDataSourceForWriting(cachedDataSource);
		dataset.setDsMetadata(dataSet.getDsMetadata());
		return dataset;
	}
	
	/**
	 * Prefix for federated dataset built automatically
	 * @return
	 */
	public static String getDatasetFederationLabelSuffix(){
		return "_from_ds_";
	}
	
}
