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
package it.eng.spagobi.utilities.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.json.JSONException;

import it.eng.spagobi.tools.dataset.metasql.query.DatabaseDialect;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.StringUtils;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public abstract class AbstractDataBase implements IDataBase {

	public static final String STANDARD_ALIAS_DELIMITER = "\"";
	public static final String STANDARD_SUBQUERY_ALIAS_DELIMITER = "T";
	private static transient Logger logger = Logger.getLogger(AbstractDataBase.class);
	IDataSource dataSource;

	protected DatabaseDialect databaseDialect;

	protected AbstractDataBase() {

	}

	public AbstractDataBase(IDataSource dataSource) {
		this.dataSource = dataSource;
		this.databaseDialect = DatabaseDialect.get(dataSource.getHibDialectClass());
	}

	@Override
	public int compareTo(IDataBase o) {
		return getName().compareToIgnoreCase(o.getName());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.utilities.database.IDataBase#getAliasDelimiter()
	 */
	@Override
	public String getAliasDelimiter() {
		return STANDARD_ALIAS_DELIMITER;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.utilities.database.IDataBase#getSqlDialect()
	 */
	@Override
	public DatabaseDialect getDatabaseDialect() {
		return databaseDialect;
	}

	@Override
	public IDataSource getDataSource() {
		return dataSource;
	}

	@Override
	public String getName() {
		return databaseDialect.getName();
	}

	@Override
	public Map<String, Map<String, String>> getStructure(final String tableNamePatternLike, final String tableNamePatternNotLike) throws JSONException, SQLException, ClassNotFoundException, NamingException, DataBaseException {
		Map<String, Map<String, String>> tableContent = new LinkedHashMap<>();
		try(Connection conn = dataSource.getConnection()) {
			DatabaseMetaData meta = conn.getMetaData();

			final String[] TYPES = { "TABLE", "VIEW" };
			final String tableNamePattern = "%";

			final MetaDataBase database = DataBaseFactory.getMetaDataBase(dataSource);
			final String catalog = database.getCatalog(conn);
			final String schema = database.getSchema(conn);
			logger.debug("This connection has been configured with the catalog [" + catalog + "] and schema [" + schema + "]");
			try (ResultSet rs = meta.getTables(catalog, schema, tableNamePattern, TYPES)) {
				while (rs.next()) {
					ResultSet tabCol = null;
					String tableName = rs.getString(3);
					if (StringUtils.matchesLikeNotLikeCriteria(tableName, tableNamePatternLike, tableNamePatternNotLike)) {
						String param1 = rs.getString(1);
						String param2 = rs.getString(2);
						try {
							tabCol = meta.getColumns(param1, param2, tableName, "%");
							while (tabCol.next()) {
								String param4 = tabCol.getString(4);


								tableContent.putIfAbsent(tableName, new LinkedHashMap<String, String>());
								tableContent.get(tableName).put(param4, param2);
							}

						} catch (Exception e) {
							logger.error("Impossible to obtain metadata for catalog " + param1 + ", schema " + param2 + ", table/view "
									+ tableName, e);
							logger.error("Continue with the other tables/views");
						} finally {
							if (tabCol != null) {
								tabCol.close();
							}
						}
					}
				}
			}
		}

		return tableContent;
	}

	@Override
	public String getSubQueryAlias() {
		return STANDARD_SUBQUERY_ALIAS_DELIMITER;

	}

	@Override
	public boolean isCacheSupported() {
		return this instanceof CacheDataBase;
	}

	@Override
	public boolean isMetaSupported() {
		return this instanceof MetaDataBase;
	}
}
