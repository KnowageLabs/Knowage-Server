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
package it.eng.knowage.engine.api.export.oldcockpit.crosstable;

import it.eng.qbe.serializer.IDeserializer;
import it.eng.qbe.serializer.SerializationException;
import it.eng.qbe.serializer.SerializationManager;
import it.eng.spagobi.utilities.assertion.Assert;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CrosstabJSONDeserializer implements IDeserializer {
	public static Logger logger = Logger.getLogger(CrosstabJSONDeserializer.class);

	@Override
	public CrosstabDefinition deserialize(Object o) throws SerializationException {
		CrosstabDefinition crosstabDefinition;
		JSONObject crosstabDefinitionJSON = null;

		logger.debug("IN");

		try {
			Assert.assertNotNull(o, "Object to be deserialized cannot be null");

			if (o instanceof String) {
				logger.debug("Deserializing string [" + o + "]");
				try {
					crosstabDefinitionJSON = new JSONObject((String) o);
				} catch (Exception e) {
					logger.debug("Object to be deserialized must be string encoding a JSON object");
					throw new SerializationException("An error occurred while deserializing query: " + o, e);
				}
			} else if (o instanceof JSONObject) {
				crosstabDefinitionJSON = (JSONObject) o;
			} else {
				Assert.assertUnreachable("Object to be deserialized must be of type string or of type JSONObject, not of type [" + o.getClass().getName() + "]");
			}

			crosstabDefinition = new CrosstabDefinition();

			try {
				deserializeRows(crosstabDefinitionJSON, crosstabDefinition);
				deserializeColumns(crosstabDefinitionJSON, crosstabDefinition);
				deserializeMeasures(crosstabDefinitionJSON, crosstabDefinition);

				// config (measures on rows/columns, totals/subototals on
				// rows/columns) remains a JSONObject
				JSONObject config = crosstabDefinitionJSON.optJSONObject(CrosstabSerializationConstants.CONFIG);
				crosstabDefinition.setConfig(config);

				String maxCellsString = config.optString("maxcellnumber");

				if (maxCellsString != null && !maxCellsString.isEmpty()) {
					try {
						crosstabDefinition.setCellLimit(Integer.parseInt(maxCellsString));
					} catch (Exception e) {
						logger.error("The cell limit of the crosstab definition is not a number : " + maxCellsString + ". We consier it 0");
					}

				}

				JSONArray calculatedFields = crosstabDefinitionJSON.optJSONArray(CrosstabSerializationConstants.CALCULATED_FIELDS);
				crosstabDefinition.setCalculatedFields(calculatedFields);

				JSONObject additionalData = crosstabDefinitionJSON.optJSONObject(CrosstabSerializationConstants.ADDITIONAL_DATA);
				crosstabDefinition.setAdditionalData(additionalData);

			} catch (Exception e) {
				throw new SerializationException("An error occurred while deserializing query: " + crosstabDefinitionJSON, e);
			}

		} finally {
			logger.debug("OUT");
		}

		return crosstabDefinition;
	}

	private void deserializeRows(JSONObject crosstabDefinitionJSON, CrosstabDefinition crosstabDefinition) throws Exception {
		List<CrosstabDefinition.Row> rows = new ArrayList<>();
		JSONArray rowsJSON = crosstabDefinitionJSON.optJSONArray(CrosstabSerializationConstants.ROWS);

		AttributeJSONDeserializer attributeJSONDeserializer = (AttributeJSONDeserializer) AttributeDeserializerFactory.getInstance().getDeserializer(
				"application/json");

		if (rowsJSON != null) {
			for (int i = 0; i < rowsJSON.length(); i++) {
				JSONObject obj = (JSONObject) rowsJSON.get(i);
				Attribute attribute = attributeJSONDeserializer.deserialize(obj);
				rows.add(crosstabDefinition.new Row(attribute));
			}
		}
		crosstabDefinition.setRows(rows);
	}

	private void deserializeMeasures(JSONObject crosstabDefinitionJSON, CrosstabDefinition crosstabDefinition) throws Exception {
		List<Measure> measures = new ArrayList<>();
		JSONArray measuresJSON = crosstabDefinitionJSON.optJSONArray(CrosstabSerializationConstants.MEASURES);

		MeasureJSONDeserializer measureJSONDeserializer = (MeasureJSONDeserializer) MeasureDeserializerFactory.getInstance()
				.getDeserializer("application/json");

		if (measuresJSON != null) {
			for (int i = 0; i < measuresJSON.length(); i++) {
				JSONObject obj = (JSONObject) measuresJSON.get(i);
				Measure measure = measureJSONDeserializer.deserialize(obj);
				measures.add(measure);
			}
		}
		crosstabDefinition.setMeasures(measures);
	}

	private void deserializeColumns(JSONObject crosstabDefinitionJSON, CrosstabDefinition crosstabDefinition) throws Exception {
		List<CrosstabDefinition.Column> columns = new ArrayList<>();
		JSONArray columnsJSON = crosstabDefinitionJSON.optJSONArray(CrosstabSerializationConstants.COLUMNS);

		if (columnsJSON != null) {
			for (int i = 0; i < columnsJSON.length(); i++) {
				JSONObject obj = (JSONObject) columnsJSON.get(i);
				Attribute attribute = (Attribute) SerializationManager.deserialize(obj, "application/json", Attribute.class);
				columns.add(crosstabDefinition.new Column(attribute));
			}
		}
		crosstabDefinition.setColumns(columns);
	}
}
