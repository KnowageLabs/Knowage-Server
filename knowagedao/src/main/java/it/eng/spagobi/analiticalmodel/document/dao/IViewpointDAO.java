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
package it.eng.spagobi.analiticalmodel.document.dao;

import java.util.List;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.Viewpoint;
import it.eng.spagobi.commons.dao.ISpagoBIDao;

/**
 * Defines the interfaces for all methods needed to insert, modify and deleting a viewpoint.
 *
 * @author Giachino
 */
public interface IViewpointDAO extends ISpagoBIDao {

	/**
	 * Loads all detail information for all viewpoints. For each of them, detail information is stored into a <code>ViewPoint</code> object. After that, all value
	 * constraints are stored into a <code>List</code>, which is returned.
	 *
	 * @return A list containing all viewpoints objects
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	List<Viewpoint> loadAllViewpoints() throws EMFUserError;

	/**
	 * Loads all detail information for all viewpoints of a object specified. For each of them, detail information is stored into a <code>ViewPoint</code> object.
	 * After that, all value constraints are stored into a <code>List</code>, which is returned.
	 *
	 * @param objId the obj id
	 *
	 * @return A list containing all viewpoints objects
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	List<Viewpoint> loadAllViewpointsByObjID(Integer objId) throws EMFUserError;

	/**
	 * Loads all detail information for a viewpoint identified by its <code>id</code>. All these information are stored into a <code>Viewpoint</code> object, which
	 * is returned.
	 *
	 * @param id The id for the viewpoint to load
	 *
	 * @return A <code>Viewpoint</code> object containing all loaded information
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	Viewpoint loadViewpointByID(Integer id) throws EMFUserError;

	/**
	 * Loads all detail information for a viewpoint identified by its <code>name</code> and relevant document identifier. All these information are stored into a
	 * <code>Viewpoint</code> object, which is returned.
	 *
	 * @param name The name for the viewpoint to load
	 * @param name The id of the document
	 *
	 * @return A <code>Viewpoint</code> object containing all loaded information
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	Viewpoint loadViewpointByNameAndBIObjectId(String name, Integer biobjectId) throws EMFUserError;

	/**
	 * Implements the query to erase a viewpoint. All information needed is stored into the input <code>Viewpoint</code> object.
	 *
	 * @param ID The identifier of viewpoint
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	void eraseViewpoint(Integer id) throws EMFUserError;

	/**
	 * Implements the query to insert a viewpoint. All information needed is stored into the input <code>Viewpoint</code> object.
	 *
	 * @param viewpoint the viewpoint
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	void insertViewpoint(Viewpoint viewpoint) throws EMFUserError;

	/**
	 * Loads all detail information for all viewpoints of a object specified accessibile to the user profile at input. For each of them, detail information is
	 * stored into a <code>ViewPoint</code> object. After that, all value constraints are stored into a <code>List</code>, which is returned.
	 *
	 * @return A list containing all viewpoints objects
	 * @throws EMFUserError If an Exception occurred
	 */
	List<Viewpoint> loadAccessibleViewpointsByObjId(Integer objId, IEngUserProfile userProfile) throws EMFUserError;

}