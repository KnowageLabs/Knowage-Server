package it.eng.spagobi.tools.dataset.common.datareader;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.CommandResult;

public class MongoDataReader extends AbstractDataReader {

	private static transient Logger logger = Logger.getLogger(MongoDataReader.class);
	/**
	 * True if the statement is an aggregation
	 */
	private boolean aggregatedQuery;

	public MongoDataReader() {
	}

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

		// this map, maps the name of the mongo's document field with the index
		// of the field in the metadata..
		// we need to use this map because there is no constraint in the fields
		// order. Furthermore the fields are sparse so a field
		// can exist in a document and not in the other documents
		Map<String, Integer> fields2ColumnsMap = new HashMap<String, Integer>();

		logger.debug("Processing the result");
		try {

			for (int i = start; i < end; i++) {

				IRecord record = new Record(dataStore);

				// prepare the fields list..
				// suppose the query returns 2 documents [{a:1,b:2},{a:1,c:3}]
				// the dataset should be something like:
				// | a | b | c |
				// | 1 | 2 | _ |
				// | 1 | _ | 3 |
				// To create the second line we should prepare a list of fields
				// with the same length of the result set. In this way we can
				// fill the indexes where we have a fields and leave the others
				// empty

				IField emptyField = new Field("");
				List<IField> fieldsCollection = new ArrayList<IField>();
				for (int k = 0; k < fields2ColumnsMap.size(); k++) {
					fieldsCollection.add(emptyField);
				}

				JSONObject document = resultArray.getJSONObject(i);

				String[] keys = JSONObject.getNames(document);

				logger.debug("Parse the document " + i);
				logger.debug("It has " + keys.length + " fields");

				for (int j = 0; j < keys.length; j++) {
					String fieldName = keys[j];
					String fieldValue = document.get(fieldName).toString();
					IField field = new Field(fieldValue);

					Integer fieldIndex = fields2ColumnsMap.get(fieldName);

					if (fieldIndex == null) {

						fieldIndex = fields2ColumnsMap.size();
						logger.debug("New field. Name: " + fieldValue + " Position: " + fieldIndex);
						fields2ColumnsMap.put(fieldName, fieldIndex);

						FieldMetadata fieldMeta = new FieldMetadata();
						fieldMeta.setName(fieldName);
						setFieldType(field, fieldMeta, fieldValue);

						dataStoreMeta.addFiedMeta(fieldMeta);

						// add the new field at the end of the fields array
						fieldsCollection.add(field);
					} else {
						IFieldMetaData fieldMeta = dataStoreMeta.getFieldMeta(fieldIndex);

						// check for each value if the type of the field is
						// String or number..
						// if one field of the collection is not number the type
						// is String
						setFieldType(field, fieldMeta, fieldValue);

						// insert the field in the correct position
						fieldsCollection.set(fieldIndex, field);
					}

				}
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

	private void setFieldType(IField field, IFieldMetaData fieldMeta, String fieldValue) {
		// if one field of the collection is not number the type
		// is String
		if (fieldMeta.getType() == null || !fieldMeta.getType().equals(String.class)) {
			try {
				Object value = new Double(fieldValue);
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