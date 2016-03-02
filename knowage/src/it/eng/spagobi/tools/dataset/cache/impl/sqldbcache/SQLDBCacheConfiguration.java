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
package it.eng.spagobi.tools.dataset.cache.impl.sqldbcache;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import it.eng.spagobi.tools.dataset.cache.SimpleCacheConfiguration;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class SQLDBCacheConfiguration extends SimpleCacheConfiguration {
	
	private List<Properties> objectsTypeDimension = new ArrayList<Properties>();
	
	
	public static final String CACHE_TABLE_NAME_PREFIX = "CACHE_NAME_PREFIX_CONFIG";
	public static final String CACHE_DATABASE_SCHEMA = "CACHE_DATABASE_SCHEMA";
	
	/**
	 * @return the schema
	 */
	public String getSchema() {
		return (String)getProperty(CACHE_DATABASE_SCHEMA);
	}
	/**
	 * @param schema the schema to set
	 */
	public void setSchema(String schema) {
		setProperty(CACHE_DATABASE_SCHEMA, schema);
	}
	
	/**
	 * @return the tableNamePrefixConfig
	 */
	public String getTableNamePrefix() {
		return (String)getProperty(CACHE_TABLE_NAME_PREFIX);
	}
	/**
	 * @param tableNamePrefix the tableNamePrefixConfig to set
	 */
	public void setTableNamePrefix(String tableNamePrefix) {
		setProperty(CACHE_TABLE_NAME_PREFIX, tableNamePrefix);
	}
	
	public List<Properties> getObjectsTypeDimension() {
		return objectsTypeDimension;
	}
	public void setObjectsTypeDimension(List<Properties> objectsTypeDimension) {
		this.objectsTypeDimension = objectsTypeDimension;
	}
}
