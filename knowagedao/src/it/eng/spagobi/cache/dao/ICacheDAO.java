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

package it.eng.spagobi.cache.dao;

import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.utilities.cache.CacheItem;

import java.util.List;

/**
 * @author Alessandro Portosa (alessandro.portosa@eng.it)
 */

public interface ICacheDAO extends ISpagoBIDao {

	// ========================================================================================
	// READ operations (cRud)
	// ========================================================================================

	public List<CacheItem> loadAllCacheItems();

	public CacheItem loadCacheItemByTableName(String tableName);

	public CacheItem loadCacheItemBySignature(String signature);

	// ========================================================================================
	// CREATE operations (Crud)
	// ========================================================================================

	public String insertCacheItem(CacheItem cacheItem);

	public void updateCacheItem(CacheItem cacheItem);

	// ========================================================================================
	// DELETE operations (cruD)
	// ========================================================================================

	public void deleteCacheItemByTableName(String tableName);

	public void deleteCacheItemBySignature(String signature);

	public boolean deleteAllCacheItem();
}
