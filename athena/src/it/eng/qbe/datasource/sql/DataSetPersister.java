package it.eng.qbe.datasource.sql;

import it.eng.spagobi.api.v2.DataSetResource;

import java.util.List;


import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Override the DataSetPersister of the qbe. This is because if the dataset is tested in the dataset management interface ther isn't the need to call a service, but directly the api 
 */
public class DataSetPersister {

	/**
	 * Override the perdidter of 
	 * @param datasetLabels
	 * @return
	 * @throws Exception
	 */
	public JSONObject cacheDataSets(List<String> datasetLabels) throws Exception {

		JSONArray datasetLabelsArray = new JSONArray();
		
		for(int i=0; i<datasetLabels.size(); i++){
			datasetLabelsArray.put(datasetLabels.get(i));
		}
		
		
		DataSetResource ds = new DataSetResource();
		String respString = ds.persistDataSets(datasetLabelsArray);
		
		
		JSONObject ja = new JSONObject(respString);
		
		return ja;
	}

}
