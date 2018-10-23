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

import java.util.List;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.metasql.query.item.Filter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Projection;
import it.eng.spagobi.tools.dataset.metasql.query.item.Sorting;
import it.eng.spagobi.utilities.database.DataBaseException;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public interface ICache {

	/**
	 * Facility method. It is equivalent to contains(dataSet.getSignature) call.
	 *
	 * @param dataSet
	 *            the dataSet that generate the resultSet
	 *
	 * @return true the dataset is cached, false elsewhere
	 */
	boolean contains(IDataSet dataSet);

	boolean contains(List<IDataSet> dataSets);

	List<IDataSet> getNotContained(List<IDataSet> dataSets);

	/**
	 * Facility method. It is equivalent to getMetadata().containsCacheItem(resultSetSignature) call.
	 *
	 * @param dataSet
	 *            the signature of the dataset that generate the resultset
	 *
	 * @return true the dataset is cached, false elsewhere
	 */
	boolean contains(String resultsetSignature);

	/**
	 * Facility method. It is equivalent to get(dataSet.getSignature) call.
	 *
	 * @param dataSet
	 *            the dataSet that generate the resultSet
	 *
	 * @return the resultSet if cached, null elsewhere
	 */
	IDataStore get(IDataSet dataSet);

	/**
	 * @param resultsetSignature
	 *            the signature of the resultSet
	 *
	 * @return the resultSet if cached, null elsewhere
	 */
	IDataStore get(String resultsetSignature);

	IDataStore get(String signature, boolean isHash);

	/**
	 * @param dataSet
	 *            the dataSet that generate the resultSet
	 * @param projections
	 *            (fields to select) on the resultSet
	 * @param filter
	 *            filter used on the resultSet
	 * @param groups
	 *            grouping criteria for the resultSet
	 * @param sortings
	 *            sorting criteria for the resultSet
	 * @return the resultSet if cached, null elsewhere
	 */

	IDataStore get(UserProfile userProfile, IDataSet dataSet, List<Projection> projections, Filter filter, List<Projection> groups, List<Sorting> sortings,
			List<Projection> summaryRowProjections, int offset, int fetchSize, int maxRowCount) throws DataBaseException;


	public void refresh(IDataSet dataSet);

	/**
	 * Facility method. It is equivalent to delete(dataSet.getSignature) call.
	 *
	 * @param dataSet
	 *            the dataSet
	 *
	 * @return true if resultSet is deleted from cache, false if resultSet wasn't cached
	 */
	boolean delete(IDataSet dataSet);

	/**
	 * Delete the specified resultSet
	 *
	 * @param resultsetSignature
	 *            the unique resultSet signature
	 *
	 * @return true if resultSet is deleted from cache, false if resultSet wasn't cached
	 */
	boolean delete(String resultsetSignature);

	/**
	 * Delete objects in the cache till the cleaning quota is reached
	 */
	void deleteToQuota();

	/**
	 * Delete all objects inside the cache
	 */
	void deleteAll();

	/**
	 * Insert a resultSet inside the cache using the resultsetSignature as an identifier
	 *
	 * @param signature
	 *            the unique resultSet signature
	 * @param dataset
	 *            the dataSet from which derives the resultSet
	 * @param dataStore
	 *            the resultSet to cache
	 * @throws DataBaseException
	 */
	@Deprecated
	long put(IDataSet dataSet, IDataStore dataStore) throws DataBaseException;

	/**
	 * Insert a resultSet inside the cache using the resultsetSignature as an identifier
	 *
	 * @param signature
	 *            the unique resultSet signature
	 * @param dataset
	 *            the dataSet from which derives the resultSet
	 * @param dataStore
	 *            the resultSet to cache
	 * @param forceUpdate
	 *            if true this method force the update of the dataset
	 * @throws DataBaseException
	 */
	long put(IDataSet dataSet, IDataStore dataStore, boolean forceUpdate) throws DataBaseException;

	/**
	 * Insert a resultSet inside the cache
	 *
	 * @param dataset
	 *            the dataSet from which derives the resultSet
	 */
	void put(IDataSet dataSet);

	/**
	 * Update the item referenced as hashedSignature with the dataStore
	 *
	 * @param hashedSignature
	 *            the hashed unique dataSet signature
	 * @param dataStore
	 *            the resultSet to cache
	 */
	void update(String hashedSignature, IDataStore dataStore);

	/**
	 * @return the metadata description object of the cache
	 */
	ICacheMetadata getMetadata();

	UserProfile getUserProfile();

	void setUserProfile(UserProfile userProfile);

}
