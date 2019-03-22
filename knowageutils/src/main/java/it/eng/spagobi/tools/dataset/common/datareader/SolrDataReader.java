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
package it.eng.spagobi.tools.dataset.common.datareader;

import com.jayway.jsonpath.JsonPath;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SolrDataReader extends JSONPathDataReader {

	static private Logger logger = Logger.getLogger(SolrDataReader.class);
	protected int resultNumber = -1;

	public SolrDataReader(String jsonPathItems) {
		this(jsonPathItems, null);
	}

	public SolrDataReader(String jsonPathItems, List<JSONPathAttribute> jsonPathAttributes) {
		super(jsonPathItems, jsonPathAttributes, false, false);
	}

	@Override
	protected void addFieldMetadata(IMetaData dataStoreMeta, List<Object> parsedData) {
			super.addFieldMetadata(dataStoreMeta, parsedData);
	}

	@Override
	protected void addData(String data, IDataStore dataStore, IMetaData dataStoreMeta, List<Object> parsedData, boolean skipPagination)
			throws ParseException, JSONException {
			super.addData(data, dataStore, dataStoreMeta, parsedData, true);
			logger.debug("Insert [" + dataStore.getRecordsCount() + "] records");
	}

	@Override
	public IDataStore read(Object data) {
		IDataStore ds = super.read(getHighlightedData((String) data).toString());
		Object parsed = JsonPath.read((String) data, "$.response.numFound");
		ds.getMetaData().setProperty("resultNumber", parsed);
		return ds;
	}

	protected JSONObject getHighlightedData(String responseBody) {
		JSONObject jsonObject = null;
		try{
			jsonObject = new JSONObject(responseBody);
			if(jsonObject.has("highlighting")){
				JSONObject highlighting = jsonObject.getJSONObject("highlighting");
				JSONObject jsonResponse = jsonObject.getJSONObject("response");
				JSONArray jsonDocs = jsonResponse.getJSONArray("docs");
				if(jsonDocs.length()>0){
					for (int i = 0; i < jsonDocs.length(); i++) {
						JSONObject jsonDoc = jsonDocs.getJSONObject(i);
						String id = jsonDoc.getString("id");
						JSONObject highlightingDetail = highlighting.getJSONObject(id);
						Iterator<String> keys = highlightingDetail.keys();
						while(keys.hasNext()){
							String field = keys.next();
							JSONArray jsonReplacement = highlightingDetail.getJSONArray(field);
							if(jsonReplacement.length()>0){
								String text = jsonReplacement.getString(0);
								jsonDoc.put(field, text);
							}
						}
					}
				}
				jsonObject.remove("highlighting");
			}
		}catch (JSONException e){
			throw new SpagoBIRuntimeException("Unable to manage highlighting", e);
		}
		return jsonObject;
	}

	public int getResultNumber() {
		return resultNumber;
	}

	public void setResultNumber(int resultNumber) {
		this.resultNumber = resultNumber;
	}

	public List<JSONPathAttribute> getJsonPathAttributes(String type){
		Assert.assertNotEmpty(type, "Type can't be empty");
		List<JSONPathAttribute> attributes = new ArrayList<>();
		List<JSONPathDataReader.JSONPathAttribute> jsonPathAttributes = getJsonPathAttributes();
		for(JSONPathDataReader.JSONPathAttribute attribute : jsonPathAttributes){
			if(type.equals(attribute.getJsonPathType())){
				attributes.add(attribute);
			}
		}
		return attributes;
	}

}
