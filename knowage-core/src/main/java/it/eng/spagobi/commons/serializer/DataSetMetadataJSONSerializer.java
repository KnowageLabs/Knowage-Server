/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2020 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.commons.serializer;

import java.util.List;
import java.util.Locale;

import org.json.ICommonObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;

/**
 * @author Marco Libanori
 */
public class DataSetMetadataJSONSerializer implements Serializer {

	public Object metadataSerializerChooser(Object meta) throws SerializationException {
		return serialize(meta);
	}

	public Object serialize(Object meta) throws SerializationException {
		return serialize(meta, Locale.getDefault());
	}

	@Override
	public Object serialize(Object meta, Locale locale) throws SerializationException {
		try {
			String _meta = (String) meta;

			Object ret = serializeToJson(_meta);

			return ret;
		} catch (Exception e) {
			throw new SerializationException("Cannot serialize meta: " + String.valueOf(meta), e);
		}
	}

	public ICommonObject serializeToJson(String meta) throws SourceBeanException, JSONException {
		ICommonObject ret = null;

		if (meta != null && !meta.equals("")) {
			SourceBean source = SourceBean.fromXMLString(meta);
			if (source != null) {
				if (source.getName().equals("COLUMNLIST")) {
					ret = serializeMetada(meta);
				} else if (source.getName().equals("META")) {
					ret = serializeGenericMetadata(meta);
				}
			}
		}
		return ret;
	}

	public JSONArray serializeMetada(String meta) throws JSONException, SourceBeanException {
		JSONArray metaListJSON = new JSONArray();

		if (meta != null && !meta.equals("")) {
			SourceBean source = SourceBean.fromXMLString(meta);
			if (source != null) {
				if (source.getName().equals("COLUMNLIST")) {
					List<SourceBean> rows = source.getAttributeAsList("COLUMN");
					for (int i = 0; i < rows.size(); i++) {
						SourceBean row = rows.get(i);
						String name = (String) row.getAttribute("name");
						String type = (String) row.getAttribute("TYPE");
						String fieldType = (String) row.getAttribute("fieldType");
						JSONObject jsonMeta = new JSONObject();
						jsonMeta.put("name", name);
						jsonMeta.put("type", type);
						jsonMeta.put("fieldType", fieldType);
						metaListJSON.put(jsonMeta);
					}
				} else if (source.getName().equals("METADATALIST")) {
					List<SourceBean> rows = source.getAttributeAsList("ROWS.ROW");
					for (int i = 0; i < rows.size(); i++) {
						SourceBean row = rows.get(i);
						String name = (String) row.getAttribute("NAME");
						String type = (String) row.getAttribute("TYPE");
						JSONObject jsonMeta = new JSONObject();
						jsonMeta.put("name", name);
						jsonMeta.put("type", type);
						metaListJSON.put(jsonMeta);
					}
				}
			}
		}
		return metaListJSON;
	}

	// Serialize the new generalized version of Metadata
	public JSONObject serializeGenericMetadata(String meta) throws JSONException, SourceBeanException {
		JSONObject metadataJSONObject = new JSONObject();

		if (meta != null && !meta.equals("")) {
			SourceBean source = SourceBean.fromXMLString(meta);

			if (source != null) {
				if (source.getName().equals("META")) {
					// Dataset Metadata --------------

					SourceBean dataset = (SourceBean) source.getAttribute("DATASET");
					JSONArray datasetJSONArray = new JSONArray();
					if (dataset != null) {
						List<SourceBean> propertiesDataset = dataset.getAttributeAsList("PROPERTY");
						for (int j = 0; j < propertiesDataset.size(); j++) {
							SourceBean property = propertiesDataset.get(j);
							String propertyName = (String) property.getAttribute("name");
							String propertyValue = (String) property.getAttribute("value");
							JSONObject propertiesJSONObject = new JSONObject();
							propertiesJSONObject.put("pname", propertyName);
							propertiesJSONObject.put("pvalue", propertyValue);
							datasetJSONArray.put(propertiesJSONObject);
						}
					}

					metadataJSONObject.put("dataset", datasetJSONArray);

					// Columns Metadata -------------
					SourceBean columns = (SourceBean) source.getAttribute("COLUMNLIST");
					JSONArray columnsJSONArray = new JSONArray();

					if (columns != null) {
						List<SourceBean> rows = columns.getAttributeAsList("COLUMN");
						for (int i = 0; i < rows.size(); i++) {
							SourceBean row = rows.get(i);
							String columnName = (String) row.getAttribute("name");
							String type = (String) row.getAttribute("TYPE");

							JSONObject typeJSONObject = new JSONObject();
							typeJSONObject.put("column", columnName);
							typeJSONObject.put("pname", "Type");
							typeJSONObject.put("pvalue", type);
							columnsJSONArray.put(typeJSONObject);

							String fieldType = (String) row.getAttribute("fieldType");
							JSONObject fieldTypeJSONObject = new JSONObject();
							fieldTypeJSONObject.put("column", columnName);
							fieldTypeJSONObject.put("pname", "fieldType");
							fieldTypeJSONObject.put("pvalue", fieldType);
							columnsJSONArray.put(fieldTypeJSONObject);

							String fieldAlias = (String) row.getAttribute("alias");
							JSONObject fieldAliasJSONObject = new JSONObject();
							fieldAliasJSONObject.put("column", columnName);
							fieldAliasJSONObject.put("pname", "fieldAlias");
							fieldAliasJSONObject.put("pvalue", fieldAlias);
							columnsJSONArray.put(fieldAliasJSONObject);

							List<SourceBean> properties = row.getAttributeAsList("PROPERTY");
							for (int j = 0; j < properties.size(); j++) {
								SourceBean property = properties.get(j);
								String propertyName = (String) property.getAttribute("name");
								String propertyValue = (String) property.getAttribute("value");
								JSONObject propertiesJSONObject = new JSONObject();
								propertiesJSONObject.put("column", columnName);
								propertiesJSONObject.put("pname", propertyName);
								propertiesJSONObject.put("pvalue", propertyValue);

								columnsJSONArray.put(propertiesJSONObject);
							}

						}
						metadataJSONObject.put("columns", columnsJSONArray);

					}
				}

			}

		}
		return metadataJSONObject;
	}

}
