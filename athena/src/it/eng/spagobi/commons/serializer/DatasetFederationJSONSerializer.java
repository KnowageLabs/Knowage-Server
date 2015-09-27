/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.serializer;

import it.eng.spagobi.api.CacheResource;
import it.eng.spagobi.tools.dataset.federation.DatasetFederation;

import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

public class DatasetFederationJSONSerializer implements Serializer {

	public static final String ID = "id";
	public static final String LABEL = "label";
	public static final String NAME = "name";
	public static final String DATA_SOURCE_LABEL = "data_source_label";
	public static final String DESCRIPTION = "description";
	public static final String RELATIONSHIPS = "relationships";
	public static final String TYPE = "type";
	public static final String SOURCE_DATASETS = "sourceDataset";
	public static final String CACHE_DATA_SOURCE = "cache_data_source";

	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject result = null;

		if (!(o instanceof DatasetFederation)) {
			throw new SerializationException("FederatedDatasetJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}

		try {
			DatasetFederation fd = (DatasetFederation) o;
			result = new JSONObject();
			result.put(ID, fd.getFederation_id());
			result.put(LABEL, fd.getLabel());
			result.put(NAME, fd.getName());
			result.put(DESCRIPTION, fd.getDescription());
			result.put(RELATIONSHIPS, fd.getRelationships());
			result.put(TYPE, "FEDERATED_DATASET");
			if(fd.getSourceDatasets()!=null){
				result.put(SOURCE_DATASETS, (JSONArray) SerializerFactory.getSerializer("application/json").serialize(fd.getSourceDatasets(), Locale.ENGLISH));
			} 

			
			
			String cacheDataSource = new CacheResource().getCacheDataSource();
			if (cacheDataSource != null) {
				result.put(CACHE_DATA_SOURCE, cacheDataSource);
			}

		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {

		}

		return result;
	}

}
