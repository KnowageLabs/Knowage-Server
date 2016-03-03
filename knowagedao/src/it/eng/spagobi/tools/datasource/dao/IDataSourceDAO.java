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
package it.eng.spagobi.tools.datasource.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.tools.datasource.bo.DataSource;
import it.eng.spagobi.tools.datasource.bo.IDataSource;

import java.util.List;

import org.hibernate.Session;

/**
 * Defines the interfaces for all methods needed to insert, modify and deleting
 * an engine.
 */
public interface IDataSourceDAO extends ISpagoBIDao {

	/**
	 * Loads all detail information for a data source identified by its
	 * <code>dsID</code>. All these information, achived by a query to the DB,
	 * are stored into a <code>datasource</code> object, which is returned.
	 *
	 * @param dsID
	 *            The id for the datasource to load
	 *
	 * @return A <code>datasource</code> object containing all loaded
	 *         information
	 *
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public DataSource loadDataSourceByID(Integer dsID) throws EMFUserError;

	/**
	 * Loads all detail information for data source whose label is equal to
	 * <code>label</code>.
	 *
	 * @param label
	 *            The label for the data source to load
	 *
	 * @return An <code>datasource</code> object containing all loaded
	 *         information
	 *
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public IDataSource loadDataSourceByLabel(String label) throws EMFUserError;

	/**
	 * Loads all detail information for all data sources. For each of them,
	 * detail information is stored into a <code>datasource</code> object. After
	 * that, all data sources are stored into a <code>List</code>, which is
	 * returned.
	 *
	 * @return A list containing all datasource objects
	 *
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */

	public List loadAllDataSources() throws EMFUserError;

	/**
	 * Implements the query to modify a data source. All information needed is
	 * stored into the input <code>datasource</code> object.
	 *
	 * @param aDataSource
	 *            The object containing all modify information
	 *
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */

	public void modifyDataSource(IDataSource aDataSource) throws EMFUserError;

	/**
	 * Implements the query to insert a data source. All information needed is
	 * stored into the input <code>datasource</code> object.
	 *
	 * @param aDataSource
	 *            The object containing all insert information
	 * @return the datasource id
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public Integer insertDataSource(IDataSource aDataSource, String organization) throws EMFUserError;

	/**
	 * Implements the query to erase a data source. All information needed is
	 * stored into the input <code>datasource</code> object.
	 *
	 * @param aDataSource
	 *            The object containing all delete information
	 *
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public void eraseDataSource(IDataSource aDataSource) throws EMFUserError;

	/**
	 * Tells if a data source is associated to any BI Object. It is useful
	 * because a data source cannot be deleted if it is used by one or more BI
	 * Objects.
	 *
	 * @param dsId
	 *            The datasource identifier
	 *
	 * @return True if the datasource is used by one or more objects, else false
	 *
	 * @throws EMFUserError
	 *             If any exception occurred
	 */
	public boolean hasBIObjAssociated(String dsId) throws EMFUserError;

	/**
	 * Return the data source thatr is marked wityh write default
	 *
	 * @return
	 * @throws EMFUserError
	 */

	public IDataSource loadDataSourceWriteDefault() throws EMFUserError;

	public IDataSource loadDataSourceWriteDefault(Session session) throws EMFUserError;

	/**
	 * Method called by superadmin to associate a datasource to a tenant
	 *
	 * @param tenantId
	 * @param datasourceId
	 * @throws EMFUserError
	 */
	public void associateToTenant(Integer tenantId, Integer datasourceId) throws EMFUserError;

	/**
	 * Method called by superadmin to load all data sources. For each of them,
	 * detail information is stored into a <code>datasource</code> object. After
	 * that, all data sources are stored into a <code>List</code>, which is
	 * returned.
	 *
	 * @return A list containing all datasource objects
	 *
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */

	public List loadDataSourcesForSuperAdmin() throws EMFUserError;

}