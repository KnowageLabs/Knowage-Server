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

package it.eng.knowage.engine.cockpit.api.crosstable;

import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.tools.dataset.persist.IDataSetTableDescriptor;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class CrosstabBuilder {

	static private Logger logger = Logger.getLogger(CrosstabBuilder.class);

	// INPUT PARAMETERS
	private final Locale locale;
	private final String crosstabDefinition;
	private final JSONArray jsonDataSource;
	private final JSONObject jsonMetaData;
	private final JSONObject variables;

	public CrosstabBuilder(Locale locale, JSONObject crosstabDefinition, JSONArray jsonDataSource, JSONObject metadata, JSONObject variables) {
		this.locale = locale;
		this.crosstabDefinition = crosstabDefinition.toString();
		this.jsonDataSource = jsonDataSource;
		this.jsonMetaData = metadata;
		this.variables = variables;
	}

	public String getSortedCrosstab(Map<Integer, NodeComparator> columnsSortKeysMap, Map<Integer, NodeComparator> rowsSortKeysMap,
			Map<Integer, NodeComparator> measuresSortKeysMap, Integer myGlobalId) throws JSONException {
		logger.debug("IN");

		return createCrossTable(crosstabDefinition, jsonDataSource, columnsSortKeysMap, rowsSortKeysMap, measuresSortKeysMap, myGlobalId, variables);

	}

	public CrossTab getSortedCrosstabObj(Map<Integer, NodeComparator> columnsSortKeysMap, Map<Integer, NodeComparator> rowsSortKeysMap,
			Map<Integer, NodeComparator> measuresSortKeysMap, Integer myGlobalId) throws JSONException {
		logger.debug("IN");

		return createCrossTableObj(crosstabDefinition, jsonDataSource, columnsSortKeysMap, rowsSortKeysMap, measuresSortKeysMap, myGlobalId);

	}

	private String createCrossTable(String jsonData, JSONArray jsonDataSource, Map<Integer, NodeComparator> columnsSortKeysMap,
			Map<Integer, NodeComparator> rowsSortKeysMap, Map<Integer, NodeComparator> measuresSortKeysMap, Integer myGlobalId, JSONObject variables) {

		CrossTab crossTab;
		CrosstabDefinition crosstabDefinition = null;

		Monitor totalTimeMonitor = null;
		Monitor deserializeTimeMonitor = null;
		Monitor crossTabDefTimeMonitor = null;
		Monitor htmlTimeMonitor = null;
		Monitor errorHitsMonitor = null;

		logger.debug("IN");

		String htmlCode = "";

		try {

			totalTimeMonitor = MonitorFactory.start("CockpitEngine.loadCrosstabAction.totalTime");
			// jsonData =
			// "{\"config\":{\"measureson\":\"columns\",\"type\":\"pivot\",\"maxcellnumber\":2000},\"rows\":[{\"id\":\"Comune\",\"alias\":\"Comune\",\"iconCls\":\"attribute\",\"nature\":\"attribute\",\"values\":\"[]\"}],\"columns\":[{\"id\":\"Maschi
			// Totale\",\"alias\":\"Maschi Totale\",\"iconCls\":\"attribute\",\"nature\":\"attribute\",\"values\":\"[]\"}],\"measures\":[{\"id\":\"Femmine corsi
			// a tempo pieno\",\"alias\":\"Femmine corsi a tempo pieno\",\"iconCls\":\"measure\",\"nature\":\"measure\",\"funct\":\"SUM\"}]}";
			JSONObject crosstabDefinitionJSON = new JSONObject(jsonData);

			logger.debug("Parameter [" + crosstabDefinitionJSON + "] is equals to [" + crosstabDefinitionJSON.toString() + "]");

			deserializeTimeMonitor = MonitorFactory.start("CockpitEngine.loadCrosstabAction.deserialize");
			// deserialize crosstab definition
			CrosstabJSONDeserializer crosstabJSONDeserializer = (CrosstabJSONDeserializer) CrosstabDeserializerFactory.getInstance()
					.getDeserializer("application/json");
			crosstabDefinition = crosstabJSONDeserializer.deserialize(crosstabDefinitionJSON);
			deserializeTimeMonitor.stop();

			crossTabDefTimeMonitor = MonitorFactory.start("CockpitEngine.loadCrosstabAction.CrossTab");
			crossTab = new CrossTab(jsonDataSource, this.jsonMetaData, crosstabDefinition, null, columnsSortKeysMap, rowsSortKeysMap, measuresSortKeysMap,
					myGlobalId);
			crossTabDefTimeMonitor.stop();

			htmlTimeMonitor = MonitorFactory.start("CockpitEngine.loadCrosstabAction.getHTMLCrossTab");
			htmlCode = crossTab.getHTMLCrossTab(locale, variables);//
			htmlTimeMonitor.stop();

		} catch (Exception e) {
			errorHitsMonitor = MonitorFactory.start("CockpitEngine.errorHits");
			errorHitsMonitor.stop();
			throw new SpagoBIRuntimeException("An unexpecte error occured while genereting cross tab html", e);
		} finally {
			if (totalTimeMonitor != null) {
				totalTimeMonitor.stop();
			}
			if (deserializeTimeMonitor != null) {
				deserializeTimeMonitor.stop();
			}
			if (crossTabDefTimeMonitor != null) {
				crossTabDefTimeMonitor.stop();
			}
			if (htmlTimeMonitor != null) {
				htmlTimeMonitor.stop();
			}
			logger.debug("OUT");
		}

		return htmlCode;
	}

	private CrossTab createCrossTableObj(String jsonData, JSONArray jsonDataSource, Map<Integer, NodeComparator> columnsSortKeysMap,
			Map<Integer, NodeComparator> rowsSortKeysMap, Map<Integer, NodeComparator> measuresSortKeysMap, Integer myGlobalId) {

		CrossTab crossTab;
		CrosstabDefinition crosstabDefinition = null;

		Monitor totalTimeMonitor = null;
		Monitor deserializeTimeMonitor = null;
		Monitor crossTabDefTimeMonitor = null;
		Monitor htmlTimeMonitor = null;
		Monitor errorHitsMonitor = null;

		logger.debug("IN");

		try {

			totalTimeMonitor = MonitorFactory.start("CockpitEngine.loadCrosstabAction.totalTime");
			// jsonData =
			// "{\"config\":{\"measureson\":\"columns\",\"type\":\"pivot\",\"maxcellnumber\":2000},\"rows\":[{\"id\":\"Comune\",\"alias\":\"Comune\",\"iconCls\":\"attribute\",\"nature\":\"attribute\",\"values\":\"[]\"}],\"columns\":[{\"id\":\"Maschi
			// Totale\",\"alias\":\"Maschi Totale\",\"iconCls\":\"attribute\",\"nature\":\"attribute\",\"values\":\"[]\"}],\"measures\":[{\"id\":\"Femmine corsi
			// a tempo pieno\",\"alias\":\"Femmine corsi a tempo pieno\",\"iconCls\":\"measure\",\"nature\":\"measure\",\"funct\":\"SUM\"}]}";
			JSONObject crosstabDefinitionJSON = new JSONObject(jsonData);

			logger.debug("Parameter [" + crosstabDefinitionJSON + "] is equals to [" + crosstabDefinitionJSON.toString() + "]");

			deserializeTimeMonitor = MonitorFactory.start("CockpitEngine.loadCrosstabAction.deserialize");
			// deserialize crosstab definition
			CrosstabJSONDeserializer crosstabJSONDeserializer = (CrosstabJSONDeserializer) CrosstabDeserializerFactory.getInstance()
					.getDeserializer("application/json");
			crosstabDefinition = crosstabJSONDeserializer.deserialize(crosstabDefinitionJSON);
			deserializeTimeMonitor.stop();

			crossTabDefTimeMonitor = MonitorFactory.start("CockpitEngine.loadCrosstabAction.CrossTab");
			crossTab = new CrossTab(jsonDataSource, this.jsonMetaData, crosstabDefinition, null, columnsSortKeysMap, rowsSortKeysMap, measuresSortKeysMap,
					myGlobalId);
			crossTabDefTimeMonitor.stop();

		} catch (Exception e) {
			errorHitsMonitor = MonitorFactory.start("CockpitEngine.errorHits");
			errorHitsMonitor.stop();
			throw new SpagoBIRuntimeException("An unexpecte error occured while genereting cross tab html", e);
		} finally {
			if (totalTimeMonitor != null) {
				totalTimeMonitor.stop();
			}
			if (deserializeTimeMonitor != null) {
				deserializeTimeMonitor.stop();
			}
			if (crossTabDefTimeMonitor != null) {
				crossTabDefTimeMonitor.stop();
			}
			if (htmlTimeMonitor != null) {
				htmlTimeMonitor.stop();
			}
			logger.debug("OUT");
		}

		return crossTab;
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

	public static final String CROSSTAB_ADDITIONAL_DATA_FIELDS_OPTIONS_OPTIONS = "options";
	public static final String CROSSTAB_ADDITIONAL_DATA_FIELDS_OPTIONS_SCALE_FACTOR = "measureScaleFactor";

	private void addMeasuresScaleFactor(JSONArray fieldOptions, String fieldId, FieldMetadata newFieldMetadata) {
		if (fieldOptions != null) {
			for (int i = 0; i < fieldOptions.length(); i++) {
				try {
					JSONObject afield = fieldOptions.getJSONObject(i);
					JSONObject aFieldOptions = afield.getJSONObject(CROSSTAB_ADDITIONAL_DATA_FIELDS_OPTIONS_OPTIONS);
					String afieldId = afield.getString("id");
					String scaleFactor = aFieldOptions.optString(CROSSTAB_ADDITIONAL_DATA_FIELDS_OPTIONS_SCALE_FACTOR);
					if (afieldId.equals(fieldId) && scaleFactor != null) {
						newFieldMetadata.setProperty(CROSSTAB_ADDITIONAL_DATA_FIELDS_OPTIONS_SCALE_FACTOR, scaleFactor);
						return;
					}
				} catch (Exception e) {
					throw new RuntimeException("An unpredicted error occurred while adding measures scale factor", e);
				}
			}
		}
	}

}
