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
package it.eng.spagobi.tools.dataset.cache;

import java.math.BigDecimal;

import it.eng.spagobi.tools.datasource.bo.IDataSource;

/**
 * The cache configuration.
 *
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public interface ICacheConfiguration {

	IDataSource getCacheDataSource();

	void setCacheDataSource(IDataSource dataSource);

	// =========================================================================================
	// FACILITY METHODS
	// =========================================================================================
	BigDecimal getCacheSpaceAvailable();

	void setCacheSpaceAvailable(BigDecimal cacheSpaceAvailable);

	Integer getCachePercentageToClean();

	void setCachePercentageToClean(Integer cachePercentageToClean);

	Integer getCacheDsLastAccessTtl();

	void setCacheDsLastAccessTtl(Integer cacheDsLastAccessTtl);

	String getCacheSchedulingFullClean();

	void setCacheSchedulingFullClean(String cacheSchedulingFullClean);

	Integer getCachePercentageToStore();

	void setCachePercentageToStore(Integer cachePercentageToStore);

	Object getProperty(String propertyName);

	Object setProperty(String propertyName, Object propertyValue);

	String getCacheRefresh();

	/**
	 * @param cacheSchedulingFullClean the cacheSchedulingFullClean to set
	 */
	void setCacheRefresh(String refresh);
}
