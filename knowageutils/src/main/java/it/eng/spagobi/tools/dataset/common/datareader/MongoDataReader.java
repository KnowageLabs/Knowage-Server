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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.CommandResult;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class MongoDataReader extends AbstractDataReader {

	private static transient Logger logger = Logger.getLogger(MongoDataReader.class);
	/**
	 * True if the statement is an aggregation
	 */
	private boolean aggregatedQuery;

	public MongoDataReader() {
	}

	@Override
	public IDataStore read(Object data) throws EMFUserError, EMFInternalError {
		DataStore dataStore = null;
		MetaData dataStoreMeta;
		int start;
		int end;

		logger.debug("IN");
		CommandResult result = (CommandResult) data;

		// Check if there is some error in the execution of the query
		String error = result.getString("errmsg");
		if (error != null) {
			throw new SpagoBIRuntimeException(error);
		}

		JSONArray resultArray = getResultAsJSONArray(result);

		// init the data store
		dataStore = new DataStore();
		dataStoreMeta = new MetaData();
		dataStore.setMetaData(dataStoreMeta);

		// preparing the bound of the result
		end = resultArray.length();
		start = 0;

		if (isOffsetSupported() && getOffset() >= 0) {
			start = getOffset();
		}

		if (isFetchSizeSupported() && getFetchSize() >= 0) {
			end = start + getFetchSize();
			if (isMaxResultsSupported() && end > maxResults) {
				end = maxResults;
			}
			if (end > resultArray.length()) {
				end = resultArray.length();
			}
		}
		logger.debug("the bounds of the result are: [" + start + ", " + end + "]");

		logger.debug("Processing the result");
		try {

			Set<String> allFieldsNames = new LinkedHashSet<String>();
			for (int i = start; i < end; i++) {
				JSONObject document = resultArray.getJSONObject(i);
				allFieldsNames.addAll(Arrays.asList(JSONObject.getNames(document)));
			}

			allFieldsNames.forEach((String name) -> {
				dataStoreMeta.addFiedMeta(new FieldMetadata(name, String.class));
			});

			for (int i = start; i < end; i++) {

				IRecord record = new Record(dataStore);

				List<IField> fieldsCollection = new ArrayList<IField>();

				JSONObject document = resultArray.getJSONObject(i);

				allFieldsNames.forEach(fieldName -> {
					Object value = document.opt(fieldName);
					if (value != null) {
						value = value.toString();
					}
					IField field = new Field(value);
					setFieldType(field, dataStoreMeta.getFieldMeta(dataStoreMeta.getFieldIndex(fieldName)));
					fieldsCollection.add(field);
				});

				record.setFields(fieldsCollection);
				dataStore.appendRecord(record);
			}

		} catch (Exception e) {
			logger.error("Error preparing the datastore", e);
			throw new SpagoBIRuntimeException("Error preparing the datastore", e);
		}
		// set the result number
		dataStore.getMetaData().setProperty("resultNumber", new Integer(resultArray.length()));
		dataStore.getMetaData().setProperty("isSparse", new Boolean(true));

		return dataStore;

	}

	/**
	 * Parse the result in a JSONArray
	 *
	 * @param result
	 * @return
	 */
	public JSONArray getResultAsJSONArray(CommandResult result) {
		logger.debug("IN");
		logger.debug("Parsing the CommandResult into a JSONArray");
		JSONArray resultArray = null;
		String obj = result.getString("retval");

		// DBObject dbObject = (DBObject) JSON.parse(obj);
		// dbObject.get("result");
		try {

			if (isAggregatedQuery()) {
				logger.debug("It is an aggregation");

				JSONObject resultObject = new JSONObject(obj);

				obj = resultObject.optString("result");

				if (obj == null || obj.equals("")) {
					obj = resultObject.optString("_firstBatch");
				}

				if (obj == null || obj.equals("")) {
					obj = resultObject.optString("_batch");
				}

				try {
					logger.debug("The result of the aggregation is an array");
					resultArray = new JSONArray(obj);
				} catch (Exception e) {
					logger.debug("The result of the aggregation is an object");
					resultArray = new JSONArray();
					resultObject = new JSONObject(obj);
					resultArray.put(resultObject);
				}

			} else {
				try {
					logger.debug("List of document query");
					resultArray = new JSONArray(obj);
				} catch (Exception e) {
					logger.debug("One document query");
					resultArray = new JSONArray();
					JSONObject resultObject = new JSONObject(obj);
					resultArray.put(resultObject);
				}
			}

		} catch (JSONException e) {
			logger.error("Error getting the result from the CommandResult " + result, e);
			throw new SpagoBIRuntimeException("Error getting the result from the CommandResult " + result, e);
		}
		logger.debug("OUT");
		return resultArray;
	}

	private void setFieldType(IField field, IFieldMetaData fieldMeta) {
		if (field.getValue() != null && (fieldMeta.getType() == null || !fieldMeta.getType().equals(String.class))) {
			try {
				Object value = new Double(field.getValue().toString());
				fieldMeta.setType(Double.class);
				logger.debug("Double type");
				field.setValue(value);
			} catch (Exception e) {
				fieldMeta.setType(String.class);
				logger.debug("String type");
			}
		}
	}

	@Override
	public boolean isOffsetSupported() {
		return true;
	}

	@Override
	public boolean isFetchSizeSupported() {
		return true;
	}

	public boolean isAggregatedQuery() {
		return aggregatedQuery;
	}

	public void setAggregatedQuery(boolean aggregatedQuery) {
		this.aggregatedQuery = aggregatedQuery;
	}

}