/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

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
package it.eng.spagobi.dataset.cache.test;

import it.eng.spagobi.dataset.cache.impl.sqldbcache.DataType;
import it.eng.spagobi.json.Xml;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.SQLDBCacheConfiguration;
import it.eng.spagobi.tools.datasource.bo.DataSource;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Francesco Lucchi (francesco.lucchi@eng.it)
 *
 */
public class TestXmlFactory {

	private static JSONObject getJsonObjectFromXml(String xmlFilePath) throws Exception {
		String xmlFileAbsolutePath = new File(xmlFilePath).getAbsolutePath();
		String xmlString = new String(Files.readAllBytes(Paths.get(xmlFileAbsolutePath)), "UTF-8");
		String jsonString = Xml.xml2json(xmlString);
		JSONObject jsonObject = new JSONObject(jsonString);
		return jsonObject;
	}

	public static DataSource createDataSource(String xmlFilePath, boolean isWritingDatasource) throws Exception {
		String dataset;
		boolean isReadOnly;
		boolean isWriteDefault;
		if (isWritingDatasource) {
			dataset = "writingDataset";
			isReadOnly = false;
			isWriteDefault = true;
		} else {
			dataset = "readingDataset";
			isReadOnly = true;
			isWriteDefault = false;
		}

		JSONObject jsonObject = getJsonObjectFromXml(xmlFilePath);
		JSONObject jsonDataset = jsonObject.getJSONObject("test").getJSONObject(dataset);
		String label = jsonDataset.getString("text");
		String url = jsonDataset.getString("url");
		String user = jsonDataset.getString("user");
		String password = jsonDataset.getString("password");
		String driver = jsonDataset.getString("driver");
		String hibDialectClass = jsonDataset.getString("hibDialectClass");
		String hibDialectName = jsonDataset.getString("hibDialectName");

		return TestDataSourceFactory.createDataSource(label, url, user, password, driver, hibDialectClass, hibDialectName, isReadOnly, isWriteDefault);
	}

	public static SQLDBCacheConfiguration createCacheConfiguration(String xmlFilePath, DataSource dataSourceWriting) throws Exception {
		JSONObject jsonObject = getJsonObjectFromXml(xmlFilePath);
		JSONObject jsonCacheConfiguration = jsonObject.getJSONObject("test").getJSONObject("cacheConfiguration");
		String tableNamePrefix = jsonCacheConfiguration.getString("tableNamePrefix");
		BigDecimal size = BigDecimal.valueOf(jsonCacheConfiguration.getLong("spaceAvailable"));
		int percentageToClean = jsonCacheConfiguration.getInt("percentageToClean");
		int percentageToStore = jsonCacheConfiguration.getInt("percentageToStore");
		String schemaName = jsonCacheConfiguration.getString("schemaName");

		SQLDBCacheConfiguration cacheConfiguration = new SQLDBCacheConfiguration();
		cacheConfiguration.setTableNamePrefix(tableNamePrefix);
		cacheConfiguration.setCacheSpaceAvailable(size);
		cacheConfiguration.setCachePercentageToClean(percentageToClean);
		cacheConfiguration.setCachePercentageToStore(percentageToStore);
		cacheConfiguration.setSchema(schemaName);
		cacheConfiguration.setCacheDataSource(dataSourceWriting);
		cacheConfiguration.setObjectsTypeDimension(new DataType().getProps());

		return cacheConfiguration;
	}

	public static String getTableName(String xmlFilePath) throws Exception {
		JSONObject jsonObject = getJsonObjectFromXml(xmlFilePath);
		JSONObject jsonTable = jsonObject.getJSONObject("test").getJSONObject("table");
		String tableName = jsonTable.getString("name");
		return tableName;
	}

	public static Map<String, String> getReadingTypes(String xmlFilePath) throws Exception {
		return getTypes(xmlFilePath, "readingType");
	}

	public static Map<String, String> getWritingTypes(String xmlFilePath) throws Exception {
		return getTypes(xmlFilePath, "writingType");
	}

	private static Map<String, String> getTypes(String xmlFilePath, String type) throws Exception {
		JSONObject jsonObject = getJsonObjectFromXml(xmlFilePath);
		JSONArray fields = jsonObject.getJSONObject("test").getJSONObject("table").getJSONArray("field");

		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < fields.length(); i++) {
			JSONObject field = fields.getJSONObject(i);
			map.put(field.getString("text"), field.getString(type));
		}
		return map;
	}
}
