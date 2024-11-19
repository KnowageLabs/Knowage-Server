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
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse;
import it.eng.spagobi.commons.dao.ISpagoBIDao;

/**
 * Defines the interfaces for all methods needed to insert, modify and deleting a ObjParuse.
 *
 * @author Zerbetto
 *
 */
public interface IObjParuseDAO extends ISpagoBIDao {

	/**
	 * Loads the list of ObjParuse associated to the input <code>objParId</code> and <code>paruseId</code>. All these information, achived by a query to the DB, are
	 * stored into a List of <code>ObjParuse</code> object, which is returned.
	 *
	 * @param objParId The id for the BI object parameter to load
	 * @param paruseId The parameterUse-id for the Parameter to load
	 *
	 * @return A List of <code>ObjParuse</code> object containing all loaded information
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	List<ObjParuse> loadObjParuse(Integer objParId, Integer paruseId) throws EMFUserError;

	/**
	 * Implements the query to modify a ObjParuse. All information needed is stored into the input <code>ObjParuse</code> object.
	 *
	 * @param aObjParuse The ObjParuse containing all modify information
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	void modifyObjParuse(ObjParuse aObjParuse) throws HibernateException, IllegalArgumentException;

	/**
	 * Implements the query to insert a ObjParuse. All information needed is stored into the input <code>ObjParuse</code> object.
	 *
	 * @param aObjParuse The ObjParuse containing all insert information
	 * @return
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	Integer insertObjParuse(ObjParuse aObjParuse) throws HibernateException, IllegalArgumentException;

	/**
	 * Implements the query to erase a ObjParuse. All information needed is stored into the input <code>ObjParuse</code> object.
	 *
	 * @param aObjParuse The object containing all delete information
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	void eraseObjParuse(ObjParuse aObjParuse) throws HibernateException;

	/**
	 * Returns the list of all ObjParuse objects associated to a <code>BIObjectParameter</code>, known its <code>objParId</code>.
	 *
	 * @param objParId The input BIObjectParameter id code
	 *
	 * @return The list of all ObjParuse objects associated
	 *
	 * @throws EMFUserError If any exception occurred
	 */
	List<ObjParuse> loadObjParuses(Integer objParId) throws HibernateException;

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
	 * Returns the labels list of the documents containing dependencies for the parameter use identified by the id at input.
	 *
	 * @param useId The Integer representing the use id
	 *
	 * @return The list of BIObject objects labels
	 *
	 * @throws EMFUserError the EMF user error
	 */
	List getDocumentLabelsListWithAssociatedDependencies(Integer useId) throws EMFUserError;

	/**
	 * Returns the list of dependencies (ObjParuse objects list) for the ParameterUse object identified by the id passes at input.
	 *
	 * @param useId The Integer representing the use id
	 *
	 * @return The list of ObjParuse objects
	 *
	 * @throws EMFUserError the EMF user error
	 */
	List<ObjParuse> getAllDependenciesForParameterUse(Integer useId) throws EMFUserError;

	/**
	 * Implements the query to erase a ObjParuse. If the object does not exist does not do anything All information needed is stored into the input
	 * <code>ObjParuse</code> object.
	 *
	 * @param aObjParuse The object containing all delete information
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	void eraseObjParuseIfExists(ObjParuse aObjParuse, Session aSession) throws EMFUserError;

	/**
	 * Returns the list of all ObjParuse objects associated to a <code>BIObjectParameter</code> via a father relationship, known its <code>objParId</code>.
	 *
	 * @param objParId The input BIObjectParameter id code
	 *
	 * @return The list of all ObjParuse objects associated
	 *
	 * @throws EMFUserError If any exception occurred
	 */
	List<ObjParuse> loadObjParusesFather(Integer objParId) throws EMFUserError;

}