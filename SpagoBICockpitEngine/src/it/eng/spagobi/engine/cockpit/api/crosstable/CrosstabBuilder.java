/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engine.cockpit.api.crosstable;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.tools.dataset.persist.IDataSetTableDescriptor;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class CrosstabBuilder {

	static private Logger logger = Logger.getLogger(CrosstabBuilder.class);

	// INPUT PARAMETERS
	private static final NodeComparator ASC = new NodeComparator(1);
	private static final NodeComparator DESC = new NodeComparator(-1);
	private final Locale locale;
	private final String crosstabDefinition;
	private final JSONArray jsonDataSource;
	private final JSONObject jsonMetaData;

	public CrosstabBuilder(Locale locale, String crosstabDefinition, JSONArray jsonDataSource, JSONObject metadata) {
		this.locale = locale;
		this.crosstabDefinition = crosstabDefinition;
		this.jsonDataSource = jsonDataSource;
		this.jsonMetaData = metadata;
	}

	public String getSortedCrosstab(Map<Integer, NodeComparator> columnsSortKeysMap, Map<Integer, NodeComparator> rowsSortKeysMap, Integer myGlobalId)
			throws JSONException {
		logger.debug("IN");

		return createCrossTable(crosstabDefinition, jsonDataSource, columnsSortKeysMap, rowsSortKeysMap, myGlobalId);

	}

	private String createCrossTable(String jsonData, JSONArray jsonDataSource, Map<Integer, NodeComparator> columnsSortKeysMap, Map<Integer, NodeComparator> rowsSortKeysMap,
			Integer myGlobalId) {

		CrossTab crossTab;
		CrosstabDefinition crosstabDefinition = null;

		Monitor totalTimeMonitor = null;
		Monitor errorHitsMonitor = null;

		logger.debug("IN");

		String htmlCode = "";

		try {

			totalTimeMonitor = MonitorFactory.start("WorksheetEngine.loadCrosstabAction.totalTime");
			// jsonData =
			// "{\"config\":{\"measureson\":\"columns\",\"type\":\"pivot\",\"maxcellnumber\":2000},\"rows\":[{\"id\":\"Comune\",\"alias\":\"Comune\",\"iconCls\":\"attribute\",\"nature\":\"attribute\",\"values\":\"[]\"}],\"columns\":[{\"id\":\"Maschi Totale\",\"alias\":\"Maschi Totale\",\"iconCls\":\"attribute\",\"nature\":\"attribute\",\"values\":\"[]\"}],\"measures\":[{\"id\":\"Femmine corsi a tempo pieno\",\"alias\":\"Femmine corsi a tempo pieno\",\"iconCls\":\"measure\",\"nature\":\"measure\",\"funct\":\"SUM\"}]}";
			JSONObject crosstabDefinitionJSON = new JSONObject(jsonData);

			logger.debug("Parameter [" + crosstabDefinitionJSON + "] is equals to [" + crosstabDefinitionJSON.toString() + "]");

			// deserialize crosstab definition
			CrosstabJSONDeserializer crosstabJSONDeserializer = (CrosstabJSONDeserializer) CrosstabDeserializerFactory.getInstance().getDeserializer(
					"application/json");

			crosstabDefinition = crosstabJSONDeserializer.deserialize(crosstabDefinitionJSON);

			// // serialize crosstab
			// if (crosstabDefinition.isPivotTable()) {
			// // load the crosstab for a crosstab widget (with headers, sum,
			// // ...)
			// if (crosstabDefinition.isStatic()) {
			// crossTab = new CrossTab(jsonDataSource, crosstabDefinition, null,
			// columnsSortKeysMap, rowsSortKeysMap, myGlobalId);
			// } else {
			// crossTab = new CrossTab(jsonDataSource, crosstabDefinition, null,
			// columnsSortKeysMap, rowsSortKeysMap, myGlobalId);
			// }
			// } else {
			// // load the crosstab data structure for all other widgets
			// crossTab = new CrossTab(valuesDataStore, crosstabDefinition,
			// null, columnsSortKeysMap, rowsSortKeysMap, myGlobalId);
			// }

			crossTab = new CrossTab(jsonDataSource, this.jsonMetaData, crosstabDefinition, null, columnsSortKeysMap, rowsSortKeysMap, myGlobalId);

			htmlCode = crossTab.getHTMLCrossTab(locale);//

		} catch (Exception e) {
			errorHitsMonitor = MonitorFactory.start("WorksheetEngine.errorHits");
			errorHitsMonitor.stop();
			throw new SpagoBIRuntimeException("An unexpecte error occured while genereting cross tab html", e);
		} finally {
			if (totalTimeMonitor != null) {
				totalTimeMonitor.stop();
			}
			logger.debug("OUT");
		}

		return htmlCode;
	}

	protected void adjustMetadata(DataStore dataStore, IDataSet dataset, IDataSetTableDescriptor descriptor, JSONArray fieldOptions) {

		IMetaData dataStoreMetadata = dataStore.getMetaData();
		IMetaData dataSetMetadata = dataset.getMetadata();
		MetaData newdataStoreMetadata = new MetaData();
		int fieldCount = dataStoreMetadata.getFieldCount();
		for (int i = 0; i < fieldCount; i++) {
			IFieldMetaData dataStoreFieldMetadata = dataStoreMetadata.getFieldMeta(i);
			String columnName = dataStoreFieldMetadata.getName();
			logger.debug("Column name : " + columnName);
			String fieldName = descriptor.getFieldName(columnName);
			logger.debug("Field name : " + fieldName);
			int index = dataSetMetadata.getFieldIndex(fieldName);
			logger.debug("Field index : " + index);
			IFieldMetaData dataSetFieldMetadata = dataSetMetadata.getFieldMeta(index);
			logger.debug("Field metadata : " + dataSetFieldMetadata);
			FieldMetadata newFieldMetadata = new FieldMetadata();
			String decimalPrecision = (String) dataSetFieldMetadata.getProperty(IFieldMetaData.DECIMALPRECISION);
			if (decimalPrecision != null) {
				newFieldMetadata.setProperty(IFieldMetaData.DECIMALPRECISION, decimalPrecision);
			}
			if (fieldOptions != null) {
				addMeasuresScaleFactor(fieldOptions, dataSetFieldMetadata.getName(), newFieldMetadata);
			}
			newFieldMetadata.setAlias(dataSetFieldMetadata.getAlias());
			newFieldMetadata.setFieldType(dataSetFieldMetadata.getFieldType());
			newFieldMetadata.setName(dataSetFieldMetadata.getName());
			newFieldMetadata.setType(dataStoreFieldMetadata.getType());
			newdataStoreMetadata.addFiedMeta(newFieldMetadata);
		}
		newdataStoreMetadata.setProperties(dataStoreMetadata.getProperties());
		dataStore.setMetaData(newdataStoreMetadata);
	}

	public static final String WORKSHEETS_ADDITIONAL_DATA_FIELDS_OPTIONS_OPTIONS = "options";
	public static final String WORKSHEETS_ADDITIONAL_DATA_FIELDS_OPTIONS_SCALE_FACTOR = "measureScaleFactor";

	private void addMeasuresScaleFactor(JSONArray fieldOptions, String fieldId, FieldMetadata newFieldMetadata) {
		if (fieldOptions != null) {
			for (int i = 0; i < fieldOptions.length(); i++) {
				try {
					JSONObject afield = fieldOptions.getJSONObject(i);
					JSONObject aFieldOptions = afield.getJSONObject(WORKSHEETS_ADDITIONAL_DATA_FIELDS_OPTIONS_OPTIONS);
					String afieldId = afield.getString("id");
					String scaleFactor = aFieldOptions.optString(WORKSHEETS_ADDITIONAL_DATA_FIELDS_OPTIONS_SCALE_FACTOR);
					if (afieldId.equals(fieldId) && scaleFactor != null) {
						newFieldMetadata.setProperty(WORKSHEETS_ADDITIONAL_DATA_FIELDS_OPTIONS_SCALE_FACTOR, scaleFactor);
						return;
					}
				} catch (Exception e) {
					throw new RuntimeException("An unpredicted error occurred while adding measures scale factor", e);
				}
			}
		}
	}

}
