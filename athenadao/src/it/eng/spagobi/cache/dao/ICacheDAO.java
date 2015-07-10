/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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

	public List<CacheItem> loadCacheJoinedItemsReferringTo(String signature);

	public boolean hasCacheItemReferenceToCacheJoinedItem(String signature, String joinedSignature);

	// ========================================================================================
	// CREATE operations (Crud)
	// ========================================================================================

	public String insertCacheItem(CacheItem cacheItem);

	public void updateCacheItem(CacheItem cacheItem);

	public Integer insertCacheJoinedItem(CacheItem cacheItem, CacheItem joinedCacheitem);

	// ========================================================================================
	// DELETE operations (cruD)
	// ========================================================================================

	public void deleteCacheItemByTableName(String tableName);

	public void deleteCacheItemBySignature(String signature);

	public boolean deleteAllCacheItem();

	public boolean deleteAllCacheJoinedItem();

}
