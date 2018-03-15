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

import org.apache.log4j.Logger;

import it.eng.spagobi.tools.dataset.cache.query.DatabaseDialect;
import it.eng.spagobi.tools.datasource.bo.IDataSource;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public abstract class AbstractDataBase implements IDataBase {

	IDataSource dataSource;
	protected DatabaseDialect databaseDialect;
	public static final String STANDARD_ALIAS_DELIMITER = "\"";

	private static transient Logger logger = Logger.getLogger(AbstractDataBase.class);

	protected AbstractDataBase() {

	}

	public AbstractDataBase(IDataSource dataSource) {
		this.dataSource = dataSource;
		this.databaseDialect = DatabaseDialect.get(dataSource.getHibDialectClass());
	}

	@Override
	public String getName() {
		return databaseDialect.getName();
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
	public boolean isCacheSupported() {
		return this instanceof CacheDataBase;
	}

	@Override
	public boolean isMetaSupported() {
		return this instanceof MetaDataBase;
	}

	@Override
	public IDataSource getDataSource() {
		return dataSource;
	}

	@Override
	public int compareTo(IDataBase o) {
		return getName().compareToIgnoreCase(o.getName());
	}
}
