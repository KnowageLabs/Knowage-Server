/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 **/
package it.eng.spagobi.tools.dataset.cache;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.FilterCriteria;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.GroupCriteria;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.ProjectionCriteria;
import it.eng.spagobi.tools.dataset.common.association.AssociationGroup;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;

import java.util.List;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public interface ICache {

	/**
	 * Enable disable the cache
	 *
	 * @param enable
	 *            true to enable the cache, false to disable it.
	 */
	void enable(boolean enable);

	/**
	 * @return true if the cache is enabled false otherwise.
	 */
	boolean isEnabled();

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

	/**
	 * Facility method. It is equivalent to get(dataSet.getSignature, groups, filters, projections) call.
	 *
	 * @param resultsetSignature
	 *            the unique resultSet signature
	 * @param groups
	 *            grouping criteria for the resultSet
	 * @param filters
	 *            filters used on the resultSet
	 * @param projections
	 *            (fields to select) on the resultSet
	 * @return the resultSet if cached, null elsewhere
	 */
	IDataStore get(IDataSet dataSet, List<GroupCriteria> groups, List<FilterCriteria> filters, List<ProjectionCriteria> projections);

	// =====================================================================================
	// LOAD METHODS
	// =====================================================================================
	IDataStore load(IDataSet dataSet, boolean wait);

	List<IDataStore> load(List<IDataSet> dataSets, boolean wait);

	// =====================================================================================
	// REFRESH METHODS
	// =====================================================================================
	/**
	 * refresh the dataset and save the result dataStore in cache
	 *
	 * @param dataSet
	 * @param wait
	 *            true to wait until the dataStore is persisted in cache. false to return as soon as the dataStore is ready and persisting it in cache
	 *            asynchronously
	 *
	 * @return the updated dataStore
	 */
	IDataStore refresh(IDataSet dataSet, boolean wait);

	/**
	 * if the dataset is not contained in the cache, refresh the dataset and save the result dataStore in cache
	 *
	 * @param dataSet
	 * @param wait
	 *            true to wait until the dataStore is persisted in cache. false to return as soon as the dataStore is ready and persisting it in cache
	 *            asynchronously
	 *
	 * @return the updated dataStore
	 */
	void refreshIfNotContained(IDataSet dataSet, boolean wait);

	IDataStore refresh(List<IDataSet> dataSets, boolean wait);

	/**
	 * refresh the store ignoring cache content
	 */
	IDataStore refresh(JoinedDataSet dataSet, AssociationGroup associationGroup);

	/**
	 * Facility method. It is equivalent to delete(dataSet.getSignature) call.
	 *
	 * @param resultsetSignature
	 *            the unique resultSet signature
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
	 */
	void put(IDataSet dataSet, IDataStore dataStore);

	/**
	 * @return the metadata description object of the cache
	 */
	ICacheMetadata getMetadata();

	/**
	 * Register the listener on the specified event
	 *
	 * @param event
	 *            the event type
	 * @param listener
	 *            the listener type
	 */
	void addListener(ICacheEvent event, ICacheListener listener);

	/**
	 * Schedule the execution of an activity
	 *
	 * @param activity
	 *            the type of activity
	 * @param trigger
	 *            the condition
	 */
	void scheduleActivity(ICacheActivity activity, ICacheTrigger trigger);

	/**
	 * delete cache of joined datasets referring to dataset passed as parameter
	 *
	 * @param signature
	 */

	public void deleteDatasetAndJoined(String signature);

}
