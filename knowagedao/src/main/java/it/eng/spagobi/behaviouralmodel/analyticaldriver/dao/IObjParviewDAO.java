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
package it.eng.spagobi.behaviouralmodel.analyticaldriver.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParview;
import it.eng.spagobi.commons.dao.ISpagoBIDao;

/**
 * Defines the interfaces for all methods needed to insert, modify and deleting a ObjParview.
 *
 * @author Gavardi
 *
 */
public interface IObjParviewDAO extends ISpagoBIDao {

	/**
	 * Loads the list of ObjParview associated to the input <code>objParId</code> and <code>paruseId</code>. All these information, achived by a query to the DB,
	 * are stored into a List of <code>ObjParview</code> object, which is returned.
	 *
	 * @param objParId  The id for the BI object parameter to load
	 * @param parvireId The parameterUse-id for the Parameter to load
	 *
	 * @return A List of <code>ObjParuse</code> object containing all loaded information
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	List<ObjParview> loadObjParview(Integer objParId, Integer parviewId) throws EMFUserError;

	/**
	 * Implements the query to modify a ObjParview. All information needed is stored into the input <code>ObjParview</code> object.
	 *
	 * @param aObjParview The ObjParview containing all modify information
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	void modifyObjParview(ObjParview aObjParview) throws HibernateException;

	/**
	 * Implements the query to insert a ObjParview. All information needed is stored into the input <code>ObjParview</code> object.
	 *
	 * @param aObjParview The ObjParview containing all insert information
	 * @return
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	Integer insertObjParview(ObjParview aObjParview) throws HibernateException;

	/**
	 * Implements the query to erase a ObjParview. All information needed is stored into the input <code>ObjParview</code> object.
	 *
	 * @param aObjParview The object containing all delete information
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	void eraseObjParview(ObjParview aObjParview) throws HibernateException;

	/**
	 * Implements the query to erase a ObjParview preserving the session. All information needed is stored into the input <code>ObjParview</code> object.
	 *
	 * @param aObjParview
	 * @param aSession
	 * @throws EMFUserError
	 */

	void eraseObjParview(ObjParview aObjParview, Session aSession);

	/**
	 * Implements the query to erase a ObjParview preserving the session. All information needed is stored into the input <code>ObjParview</code> object. If does
	 * not exist don'd do anything
	 *
	 * @param aObjParview
	 * @param aSession
	 * @throws EMFUserError
	 */

	void eraseObjParviewIfExists(ObjParview aObjParview, Session aSession) throws HibernateException;

	/**
	 * Returns the list of all ObjParview objects associated to a <code>BIObjectParameter</code>, known its <code>objParId</code>.
	 *
	 * @param objParId The input BIObjectParameter id code
	 *
	 * @return The list of all ObjParview objects associated
	 *
	 * @throws EMFUserError If any exception occurred
	 */
	List<ObjParview> loadObjParviews(Integer objParId) throws HibernateException;

	/**
	 * Returns the list of labels of BIObjectParameter objects that have a correlation relationship with the BIObjectParameter at input, given its id.
	 *
	 * @param objParFatherId The id of the BIObjectParameter
	 *
	 * @return the list of BIObjectParameter objects that have a correlation relationship with the BIObjectParameter at input
	 *
	 * @throws EMFUserError the EMF user error
	 */
	List<String> getDependencies(Integer objParFatherId) throws EMFUserError;

	/**
	 * Returns the list of dependencies (ObjParview objects list) for the Parameterview object identified by the id passes at input.
	 *
	 * @param viewId The Integer representing the view id
	 *
	 * @return The list of ObjParview objects
	 *
	 * @throws EMFUserError the EMF user error
	 */
	List<ObjParview> getAllDependenciesForParameterview(Integer viewId) throws EMFUserError;

	/**
	 * Returns the list of all ObjParview objects associated to a <code>BIObjectParameter</code> by a father relationship, known its <code>objParId</code>.
	 *
	 * @param objParId The input BIObjectParameter id code
	 *
	 * @return The list of all ObjParview objects associated
	 *
	 * @throws EMFUserError If any exception occurred
	 */
	List<ObjParview> loadObjParviewsFather(Integer objParId) throws HibernateException;
}